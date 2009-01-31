package org.twdata.lan.server;

import org.apache.log4j.Logger;
import org.twdata.lan.ExportedRepository;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.BrokenBarrierException;
import static java.util.Arrays.asList;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 31/01/2009
 * Time: 2:30:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class GitDaemonServer {

    private final Map<String,Process> processes;


    public GitDaemonServer() {
        this.processes = new HashMap<String,Process>();
    }

    public void start() {}

    public void stop() {
        for (Process process : processes.values())
        {
            process.destroy();
        }
    }

    public void addGitRepository(ExportedRepository repo)
    {
        try {
            Process process = new ProcessBuilder()
                    .command(asList(
                            "git-daemon",
                            "--verbose",
                            "--export-all",
                            "--port="+String.valueOf(repo.getPort()),
                            "--base-path="+repo.getRepository().getPath(),
                            "--base-path-relaxed"
                    ))
                    .start();
            final String name = repo.getRepository().getName();
            PullerThread so =
                    new PullerThread(name+"-STDOUT", process.getInputStream(), System.out);
            so.start();

            PullerThread se =
                    new PullerThread(name+"-STDERR", process.getErrorStream(), System.err);
            se.start();
            processes.put(name, process);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static class PumperThread extends Thread {

        private OutputStream _os;
        private InputStream _is;

        private boolean _closeInput;
        private static final Logger log = Logger.getLogger(PumperThread.class);

        protected PumperThread(
                String name,
                InputStream is,
                OutputStream os,
                boolean closeInput) {
            super(name);
            _is = is;
            _os = os;
            _closeInput = closeInput;
        }

        public void run() {
            try {
                byte[] buf = new byte[1024];
                int read = 0;
                while (!isInterrupted() && (read = _is.read(buf)) != -1) {
                    if (read == 0)
                        continue;
                    _os.write(buf, 0, read);
                    _os.flush();
                }
            } catch (Throwable t) {
                log.debug("ignoring exception from process output", t);
            } finally {
                try {
                    if (_closeInput) {
                        _is.close();
                    } else {
                        _os.close();
                    }
                } catch (IOException ioe) {
                    /* IGNORE */
                }
            }
        }
    }

    private static class PusherThread extends PumperThread {
        PusherThread(String name, InputStream is, OutputStream os) {
            super(name, is, os, false);
        }
    }

    private static class PullerThread extends PumperThread {
        PullerThread(String name, InputStream is, OutputStream os) {
            super(name, is, os, true);
        }
    }
}
