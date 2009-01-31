package org.twdata.lan;

import uk.co.flamingpenguin.jewel.cli.CliFactory;
import org.twdata.lan.server.WebServer;
import org.twdata.lan.server.ZeroconfServer;
import org.twdata.lan.server.GitDaemonServer;
import org.twdata.lan.manager.RepositoryManager;
import org.apache.log4j.BasicConfigurator;
import com.google.inject.Injector;
import com.google.inject.Guice;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {
        BasicConfigurator.configure();
        LanArguments config = CliFactory.parseArguments(LanArguments.class, args);
        final Injector injector = Guice.createInjector(new LanModule(config));

        injector.getInstance(WebServer.class).start();
        injector.getInstance(GitDaemonServer.class).start();
        injector.getInstance(ZeroconfServer.class).start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
        {
            @Override
            public void run() {
                try {
                    injector.getInstance(WebServer.class).stop();
                    injector.getInstance(GitDaemonServer.class).stop();
                    injector.getInstance(ZeroconfServer.class).stop();
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }));
        System.out.println( "LAN up and running" );
    }
}
