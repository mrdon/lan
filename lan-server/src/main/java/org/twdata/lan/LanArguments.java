package org.twdata.lan;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 30/01/2009
 * Time: 10:32:24 PM
 * To change this template use File | Settings | File Templates.
 */
public interface LanArguments {

    @Option(shortName="p", longName="port", description="the TCP port for the HTTP server", defaultValue="4444")
    int getPort();
}
