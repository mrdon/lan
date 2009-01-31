package org.twdata.lan;

import org.twdata.lan.manager.RepositoryManager;
import org.twdata.lan.server.GitDaemonServer;
import org.twdata.lan.server.ZeroconfServer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import com.google.inject.Inject;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 31/01/2009
 * Time: 2:26:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class RepositoryService {
    private final RepositoryManager repositoryManager;
    private final ZeroconfServer zeroconfServer;
    private final GitDaemonServer gitServer;
    private final String host;

    private volatile int nextProcessPort = 9418;

    @Inject
    public RepositoryService(RepositoryManager repositoryManager, ZeroconfServer zeroconfServer, GitDaemonServer gitServer) {
        this.repositoryManager = repositoryManager;
        this.zeroconfServer = zeroconfServer;
        this.gitServer = gitServer;
        try {
        InetAddress addr = InetAddress.getLocalHost();
        this.host = addr.getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

    }

    public void add(String id, Repository repository)
    {
        repositoryManager.add(id, repository);

        ExportedRepository export = new ExportedRepository(
                repository,
                host,
                nextProcessPort++);

        gitServer.addGitRepository(export);
        zeroconfServer.broadcast(export);
    }

    public List<Repository> getAll() {
        return repositoryManager.getAll();
    }
}
