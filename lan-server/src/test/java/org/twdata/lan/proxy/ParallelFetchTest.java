package org.twdata.lan.proxy;

import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.twdata.lan.proxy.pauseurl.PauseURLStreamHandlerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import com.sun.net.httpserver.HttpServer;

public class ParallelFetchTest extends TestCase
{
    static
    {
        URL.setURLStreamHandlerFactory(new PauseURLStreamHandlerFactory());
    }


    public void testRetrieve() throws IOException
    {
        String url1 = getClass().getClassLoader().getResource("repo1/").toExternalForm();
        String url2 = getClass().getClassLoader().getResource("repo2/").toExternalForm();
        String url3 = getClass().getClassLoader().getResource("repo3/").toExternalForm();

        File file = new ParallelFetch(Arrays.asList(url1, url2, url3)).fetch("test.txt");
        assertNotNull(file);
    }

    public void testRetrieveWithPause() throws IOException
    {
        String url1 = getClass().getClassLoader().getResource("repo1/").toExternalForm();
        String url2 = getClass().getClassLoader().getResource("repo2/").toExternalForm();
        String url3 = getClass().getClassLoader().getResource("repo3/").toExternalForm();

        File file = new ParallelFetch(Arrays.asList(usePause(url1, 100), usePause(url2, 500), usePause(url3, 0))).fetch("test.txt");
        assertNotNull(file);
        assertEquals("test1", FileUtils.readFileToString(file));

        file = new ParallelFetch(Arrays.asList(usePause(url1, 500), usePause(url2, 50), usePause(url3, 0))).fetch("test.txt");
        assertNotNull(file);
        assertEquals("test2", FileUtils.readFileToString(file));
    }

    public void testHttp()
    {
        //HttpServer server = new HttpServer(){};
    }

    String usePause(String url, int len)
    {
        return url.replace("file:", "pause-"+len+":");
    }
}
