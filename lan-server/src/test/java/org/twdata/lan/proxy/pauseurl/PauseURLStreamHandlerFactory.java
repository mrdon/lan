package org.twdata.lan.proxy.pauseurl;

import java.net.URLStreamHandlerFactory;
import java.net.URLStreamHandler;

public class PauseURLStreamHandlerFactory implements URLStreamHandlerFactory
{
    public URLStreamHandler createURLStreamHandler(String s)
    {
        if (s.startsWith("pause-"))
        {
            return new PauseURLStreamHandler();
        }
        return null;
    }
}
