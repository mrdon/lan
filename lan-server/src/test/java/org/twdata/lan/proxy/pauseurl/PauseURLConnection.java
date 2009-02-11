package org.twdata.lan.proxy.pauseurl;

import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

public class PauseURLConnection extends URLConnection
{
    protected PauseURLConnection(URL url)
    {
        super(url);
    }

    public void connect() throws IOException
    {
        //url.openConnection().connect();
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        String query = getURL().getQuery();
        String[] args = query.split("=");
        try
        {
            Thread.sleep(Integer.parseInt(args[1]));
        }
        catch (InterruptedException e)
        {
            // Ignore
        }
        return url.openStream();
    }
}
