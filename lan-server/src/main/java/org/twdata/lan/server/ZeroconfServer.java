package org.twdata.lan.server;

import org.twdata.lan.ExportedRepository;
import org.twdata.lan.LanArguments;
import org.mortbay.log.Log;
import org.apache.log4j.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;

import com.google.inject.Inject;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 31/01/2009
 * Time: 2:09:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class ZeroconfServer {
    private JmDNS dns;
    private final int port;
    private final Logger log = Logger.getLogger(ZeroconfServer.class);

    @Inject
    public ZeroconfServer(LanArguments config) {
        this.port = config.getPort();
    }

    public void start() throws IOException {
        this.dns = JmDNS.create();
        ServiceInfo svc = ServiceInfo.create(
                "_maven-http._tcp",
                "local",
                port,
                "Local Maven Repository");
        try {
            dns.registerService(svc);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        log.info("Zeroconf server running");
    }

    public void stop()
    {
        this.dns.close();
        log.info("Zeroconf server stopped");
    }

    public void broadcast(ExportedRepository export) {
        ServiceInfo svc = ServiceInfo.create(
                "_git._tcp",
                export.getRepository().getName(),
                export.getPort(),
                "Exported Git Repository");
        try {
            dns.registerService(svc);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        log.info("Git repository "+export.getRepository().getName()+" broadcasted");
    }
}
