package org.twdata.lan.proxy;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelFetch
{
    private final List<String> repos;
    private final ThreadPoolExecutor fetchPool;

    public ParallelFetch(List<String> repos)
    {
        this.repos = repos;
        fetchPool = new ThreadPoolExecutor(10, 20, 5, TimeUnit.MINUTES, new LinkedBlockingQueue());
    }

    public File retrieve(String path)
    {
        List<FetchTask> tasks = new ArrayList<FetchTask>();
        for (String repo : repos)
        {
            FetchTask task = new FetchTask(new URLArtifactRetriever(repo), path);
            tasks.add(task);

        }

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
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            finally
            {
                IOUtils.closeQuietly(result);
                IOUtils.closeQuietly(out);
            }
        }
        return null;
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
            try
            {
                if (ret.canRetrieve(path))
                {
                    if (!thread.isInterrupted())
                    {
                        return ret.retrieve();
                    }
                }
            }
            finally
            {
                ret.abort();
            }
            return null;
        }
    }
}
