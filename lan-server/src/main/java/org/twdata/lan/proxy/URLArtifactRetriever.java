package org.twdata.lan.proxy;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;

public class URLArtifactRetriever implements ArtifactRetriever<URLConnection>
{
    private static final Logger log = Logger.getLogger(URLArtifactRetriever.class);

    public boolean canRetrieve(String urlPath)
    {
        return true;
    }

    public URLConnection tryToRetrieve(String urlPath)
    {
        URL url = null;
        try
        {
            url = new URL(urlPath);
            URLConnection conn = url.openConnection();
            conn.connect();
            return conn;
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException("bad url:"+urlPath);
        }
        catch (IOException e)
        {
            //e.printStackTrace();
        }
        return null;
    }

    public InputStream retrieve(URLConnection conn)
    {
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("Retrieved from "+conn.getURL());
            }
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

    public void abort(URLConnection conn)
    {
        if (conn != null)
        {
            try
            {
                IOUtils.closeQuietly(conn.getInputStream());
            }
            catch (IOException e)
            {
                // ignore as we can't get the input stream from the connection
            }
        }
    }
}
