package org.twdata.lan.proxy;

import org.mortbay.jetty.Server;

public class ServerUtils
{
    public static Server startServerServingFile(String path, String content)
    {
        Server server = new Server(300);
        return server;
    }
}
