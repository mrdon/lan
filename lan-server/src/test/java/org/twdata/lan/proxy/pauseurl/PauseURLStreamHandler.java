package org.twdata.lan.proxy.pauseurl;

import java.net.URLStreamHandler;
import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;

public class PauseURLStreamHandler extends URLStreamHandler
{
    protected URLConnection openConnection(URL url) throws IOException
    {
        String pause = url.getProtocol().substring("pause-".length());
        URL fixed = new URL(url.toExternalForm().replaceFirst(url.getProtocol(), "file") + "?pause="+pause);
        return new PauseURLConnection(fixed);
    }
}
