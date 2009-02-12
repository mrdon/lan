package org.twdata.lan.proxy;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface ArtifactRetriever<T>
{
    T tryToRetrieve(String path);
    InputStream retrieve(T session);
    void abort(T session);

    boolean canRetrieve(String urlPath);
}
