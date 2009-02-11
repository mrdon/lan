package org.twdata.lan.proxy;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.twdata.lan.proxy.pauseurl.PauseURLStreamHandlerFactory;

public class ArtifactRetrieverTest extends TestCase
{
    public void testRetrieve() throws IOException
    {
        String url1 = getClass().getClassLoader().getResource("repo1/").toExternalForm();
        String url2 = getClass().getClassLoader().getResource("repo2/").toExternalForm();
        String url3 = getClass().getClassLoader().getResource("repo3/").toExternalForm();

        File file = new ParallelFetch(Arrays.asList(url1, url2, url3)).retrieve("test.txt");
        assertNotNull(file);
    }

    public void testRetrieveWithPause() throws IOException
    {
        URL.setURLStreamHandlerFactory(new PauseURLStreamHandlerFactory());

        String url1 = getClass().getClassLoader().getResource("repo1/").toExternalForm();
        String url2 = getClass().getClassLoader().getResource("repo2/").toExternalForm();
        String url3 = getClass().getClassLoader().getResource("repo3/").toExternalForm();

        File file = new ParallelFetch(Arrays.asList(usePause(url1, 100), usePause(url2, 500), usePause(url3, 0))).retrieve("test.txt");
        assertNotNull(file);
        System.out.println("Retrieved "+FileUtils.readFileToString(file));
        assertEquals("test1", FileUtils.readFileToString(file));

        file = new ParallelFetch(Arrays.asList(usePause(url1, 500), usePause(url2, 50), usePause(url3, 0))).retrieve("test.txt");
        assertNotNull(file);
        System.out.println("Retrieved "+FileUtils.readFileToString(file));
        assertEquals("test2", FileUtils.readFileToString(file));
    }

    String usePause(String url, int len)
    {
        return url.replace("file:", "pause-"+len+":");
    }
}
