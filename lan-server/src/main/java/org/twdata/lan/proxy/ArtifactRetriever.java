package org.twdata.lan.proxy;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface ArtifactRetriever
{
    boolean canRetrieve(String path);
    InputStream retrieve();
    void abort();
}
