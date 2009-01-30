package org.twdata.lan;

import uk.co.flamingpenguin.jewel.cli.CliFactory;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {
        LanArguments config = CliFactory.parseArguments(LanArguments.class, args);
        final WebServer httpServer = new WebServer(config.getPort());
        httpServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
        {
            @Override
            public void run() {
                try {
                    httpServer.stop();
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }));
        System.out.println( "Hello World!" );
    }
}
