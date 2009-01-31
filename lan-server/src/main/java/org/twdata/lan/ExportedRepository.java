package org.twdata.lan;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 31/01/2009
 * Time: 2:38:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExportedRepository {
    private final int port;
    private final String host;
    private final Repository repository;

    public ExportedRepository(Repository repository,  String host, int port) {
        this.host = host;
        this.port = port;
        this.repository = repository;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public Repository getRepository() {
        return repository;
    }
}
