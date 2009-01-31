package org.twdata.lan;

import com.google.inject.AbstractModule;
import org.twdata.lan.server.WebServer;
import org.twdata.lan.server.ZeroconfServer;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 31/01/2009
 * Time: 3:38:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class LanModule extends AbstractModule {
    private final LanArguments config;

    public LanModule(LanArguments config) {
        this.config = config;
    }

    @Override
    protected void configure() {
        bind(LanArguments.class).toInstance(config);
    }
}
