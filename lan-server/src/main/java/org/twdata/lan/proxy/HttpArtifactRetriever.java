package org.twdata.lan.proxy;

import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

public class HttpArtifactRetriever implements ArtifactRetriever<GetMethod>
{

    private final HttpClient client;
    private static final Logger log = Logger.getLogger(HttpArtifactRetriever.class);

    public HttpArtifactRetriever()
    {
        client = new HttpClient(new MultiThreadedHttpConnectionManager());
        client.getHttpConnectionManager().
                getParams().setConnectionTimeout(30000);
    }

    public GetMethod tryToRetrieve(String path)
    {
        GetMethod get = new GetMethod(path);
        get.setFollowRedirects(true);

        try
        {
            int status = client.executeMethod(get);
            if (status == 200)
            {
                return get;
            }
            else
            {
                get.releaseConnection();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    public InputStream retrieve(GetMethod get)
    {
        try
        {
            return get.getResponseBodyAsStream();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Problem retrieving url "+get.getPath(), e);
        }
    }

    public void abort(GetMethod get)
    {
        get.releaseConnection();
    }

    public boolean canRetrieve(String urlPath)
    {
        return urlPath.startsWith("http");
    }
}
