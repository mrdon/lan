package org.twdata.lan.proxy;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.*;

public class ParallelFetch
{
    private final List<String> repos;
    private final ThreadPoolExecutor fetchPool;
    private final List<ArtifactRetriever> retrievers;

    public ParallelFetch(List<String> baseRepositories)
    {
        this.repos = new CopyOnWriteArrayList<String>(baseRepositories);
        fetchPool = new ThreadPoolExecutor(10, 20, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
        retrievers = new CopyOnWriteArrayList<ArtifactRetriever>(Arrays.asList(
                new URLArtifactRetriever(), new HttpArtifactRetriever()
        ));
    }

    public void addRepositoryUrl(String url)
    {
        repos.add(url);
    }

    public void removeRepositoryUrl(String url)
    {
        repos.remove(url);
    }

    public File fetch(String path)
    {
        List<FetchTask> tasks = buildTaskList(path);

        CompletionService<InputStream> ecs = new ExecutorCompletionService<InputStream>(fetchPool);
        int n = tasks.size();
        List<Future<InputStream>> futures = new ArrayList<Future<InputStream>>(n);
        InputStream result = null;
        try {
            for (Callable<InputStream> s : tasks)
                futures.add(ecs.submit(s));
            for (int i = 0; i < n; ++i) {
                try {
                    InputStream r = ecs.take().get();
                    if (r != null) {
                        result = r;
                        break;
                    }
                } catch(ExecutionException ignore) {}
                catch (InterruptedException e)
                {
                    // ignore
                }
            }
        }
        finally {
            for (Future<InputStream> f : futures)
                f.cancel(true);
        }

        if (result != null)
        {
            OutputStream out = null;
                try
                {
                    File tmp = File.createTempFile("artifact", ".tmp");
                    out = new FileOutputStream(tmp);
                    IOUtils.copy(result, out);
                    return tmp;
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Cannot copy partifact", e);
                }
            finally
            {
                IOUtils.closeQuietly(result);
                IOUtils.closeQuietly(out);
            }
        }
        return null;
    }

    private List<FetchTask> buildTaskList(String path)
    {
        List<FetchTask> tasks = new ArrayList<FetchTask>();
        for (String repo : repos)
        {
            String url = repo + path;
            ArtifactRetriever retriever = null;
            for (ArtifactRetriever ret : retrievers)
            {
                if (ret.canRetrieve(url))
                {
                    retriever = ret;
                    break;
                }
            }
            if (retriever == null)
            {
                throw new IllegalArgumentException("Cannot find retriever for URL "+url);
            }

            FetchTask task = new FetchTask(retriever, url);
            tasks.add(task);
        }
        return tasks;
    }

    private static class FetchTask implements Callable<InputStream>
    {
        private final ArtifactRetriever ret;
        private final String path;

        public FetchTask(ArtifactRetriever ret, String path)
        {
            this.ret = ret;
            this.path = path;
        }

        public InputStream call()
        {
            Thread thread = Thread.currentThread();
            Object session = ret.tryToRetrieve(path);
            if (session != null)
            {
                if (!thread.isInterrupted())
                {
                    return ret.retrieve(session);
                }
                else
                {
                    ret.abort(session);
                }
            }
            return null;
        }
    }
}
