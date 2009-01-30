package org.twdata.lan;

import org.mortbay.jetty.handler.*;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.HandlerContainer;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.Context;

import java.io.File;

import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 30/01/2009
 * Time: 10:16:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class WebServer {

    private final Server server;

    public WebServer(int port) {
        server = new Server(port);

        ContextHandlerCollection contexts = new ContextHandlerCollection();

        configureMavenRepository(contexts);
        configureJersey(contexts);

        server.setHandler(contexts);
    }

    private void configureMavenRepository(HandlerContainer container) {
        File localRepo = determineLocalRepository();
        if (localRepo == null) {
            System.out.println("Cannot find local Maven repository.  Won't start the repository server.");
            return;
        }

         ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setResourceBase(localRepo.getAbsolutePath());

        ContextHandler context = new ContextHandler();
        context.setContextPath("/maven");
        context.setResourceBase(localRepo.getAbsolutePath());
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        context.setHandler(resource_handler);
        container.addHandler(context);
    }

    private void configureJersey(HandlerContainer container) {
        ServletHolder sh = new ServletHolder(ServletContainer.class);

        /*
        * For 0.8 and later the "com.sun.ws.rest" namespace has been renamed to
        * "com.sun.jersey". For 0.7 or early use the commented out code instead
        */
        // sh.setInitParameter("com.sun.ws.rest.config.property.resourceConfigClass", "com.sun.ws.rest.api.core.PackagesResourceConfig");
        // sh.setInitParameter("com.sun.ws.rest.config.property.packages", "jetty");
        //sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        sh.setInitParameter("com.sun.jersey.config.property.packages", "org.twdata.lan.rest");

        Context context = new Context(container, "/", Context.SESSIONS);
        context.addServlet(sh, "/*");

    }

    public void start() throws Exception {
        if (server != null) {
            server.start();
        }
    }

    public void stop() throws Exception {
        if (server != null) server.stop();
    }

    private File determineLocalRepository() {
        File home = new File(System.getProperty("user.home"));
        return new File(home, ".m2" + File.separatorChar + "repository");

    }
}
