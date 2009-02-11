package org.twdata.lan.proxy;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;

public class URLArtifactRetriever implements ArtifactRetriever
{
    private final String baseUrl;

    private URLConnection conn;

    public URLArtifactRetriever(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public boolean canRetrieve(String path)
    {
        URL url = null;
        final String urlPath = baseUrl + path;
        try
        {
            url = new URL(urlPath);
            conn = url.openConnection();
            conn.connect();
            return true;
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException("bad url:"+urlPath);
        }
        catch (IOException e)
        {
            //e.printStackTrace();
        }
        return false;
    }

    public InputStream retrieve()
    {
        try
        {
            System.out.println("Retrieved from "+baseUrl);
            return conn.getInputStream();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void abort()
    {
        if (conn != null)
        {
            try
            {
                IOUtils.closeQuietly(conn.getInputStream());
            }
            catch (IOException e)
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}
