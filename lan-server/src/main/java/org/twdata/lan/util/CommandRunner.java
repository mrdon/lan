package org.twdata.lan.util;

/*
 * This code is in the public domain and comes with no warranty.
 */

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BrokenBarrierException;


public class CommandRunner {

    private static final Logger log = Logger.getLogger(CommandRunner.class);

    private boolean _waitForExit = true;
    private String _command;
    private String[] _commandWithArgs;
    private String[] _environment;
    private int _timeout = 0;
    private boolean _destroyOnTimeout = true;

    private InputStream _stdin;
    private OutputStream _stdout;
    private OutputStream _stderr;

    private static final int BUF = 4096;

    private int _xit;

    private Throwable _thrownError;

    private CyclicBarrier _barrier;

    public static CommandRunner exec(String cmd, String[] env, int timeout) throws IOException {
        return exec(cmd, null, env, timeout);
    }

    public static CommandRunner exec(String[] cmdWithArgs, String[] env, int timeout) throws IOException {
        return exec(null, cmdWithArgs, env, timeout);
    }

    private static CommandRunner exec(String cmd, String[] cmdWithArgs, String[] env, int timeout) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        CommandRunner cr = new CommandRunner();
        cr.setInputStream(null);
        cr.setStdOutputStream(out);
        cr.setStdErrorStream(err);

        if (cmd != null) {
            cr.setCommand(cmd);
        } else {
            cr.setCommandWithArgs(cmdWithArgs);
        }
        cr.setEnvironment(env);

        if (env != null) {
            cr.setEnvironment(env);
        }
        if (timeout > 0) {
            cr.setTimeout(timeout);
            cr.setDestroyOnTimeout(true);
        }

        log.info("Executing " + (cmd == null ? cmdWithArgs[0] : cmd));
        cr.evaluate();

        if (log.isDebugEnabled() || cr.getExitValue() != 0) {
            String outStr = new String(out.toByteArray());
            String errStr = new String(err.toByteArray());
            StringBuffer sb = new StringBuffer();
            sb.append("\n\tOutput: ").append(outStr);
            sb.append("\n\tError: ").append(errStr);
            String msg = sb.toString();

            switch (cr.getExitValue()) {
                case -1:
                    log.warn("Command timed out" + msg);
                    break;
                case 0:
                    if (log.isDebugEnabled()) log.debug("Command executed successfully" + msg);
                    break;
                default:
                    log.error("Command failed: " + cr.getExitValue() + msg);
            }
        }

        return cr;
    }

    public int getExitValue() {
        return _xit;
    }

    public void setCommand(String s) {
        _command = s;
    }

    public String getCommand() {
        return _command;
    }

    public void setCommandWithArgs(String[] s) {
        _commandWithArgs = s;
    }

    public void setEnvironment(String[] s) {
        _environment = s;
    }

    public void setInputStream(InputStream is) {
        _stdin = is;
    }

    public void setStdOutputStream(OutputStream os) {
        _stdout = os;
    }

    public void setStdErrorStream(OutputStream os) {
        _stderr = os;
    }

    public void evaluate() throws IOException {
        Process proc = null;
        if (_command != null) {
            proc = Runtime.getRuntime().exec(_command, _environment);
        } else if (_commandWithArgs != null) {
            proc = Runtime.getRuntime().exec(_commandWithArgs, _environment);
        } else {
            throw new IOException("No command given");
        }

        _barrier = new CyclicBarrier(3 + ((_stdin != null) ? 1 : 0));

        PullerThread so =
                new PullerThread("STDOUT", proc.getInputStream(), _stdout);
        so.start();

        PullerThread se =
                new PullerThread("STDERR", proc.getErrorStream(), _stderr);
        se.start();

        PusherThread si = null;
        if (_stdin != null) {
            si = new PusherThread("STDIN", _stdin, proc.getOutputStream());
            si.start();
        }

        boolean _timedout = false;
        long end = System.currentTimeMillis() + _timeout * 1000;

        try {
            if (_timeout == 0) {
                _barrier.await();
            } else {
                _barrier.await(_timeout, TimeUnit.SECONDS);
            }
        } catch (TimeoutException ex) {
            _timedout = true;
            if (si != null) {
                si.interrupt();
            }
            so.interrupt();
            se.interrupt();
            if (_destroyOnTimeout) {
                proc.destroy();
            }
        } catch (BrokenBarrierException bbe) {
            /* IGNORE */
        } catch (InterruptedException e) {
            /* IGNORE */
        }

        _xit = -1;

        if (!_timedout) {
            if (_waitForExit) {
                do {
                    try {
                        _xit = proc.exitValue();
                        Thread.sleep(250);
                    } catch (InterruptedException ie) {
                        /* IGNORE */
                    } catch (IllegalThreadStateException iltse) {
                        continue;
                    }
                    break;
                } while (!(_timedout = (System.currentTimeMillis() > end)));
            } else {
                try {
                    _xit = proc.exitValue();
                } catch (IllegalThreadStateException iltse) {
                    _timedout = true;
                }
            }
        }

        if (_timedout) {
            System.out.println("timed out");
            if (_destroyOnTimeout) {
                proc.destroy();
            }
        }
    }

    public Throwable getThrownError() {
        return _thrownError;
    }

    private class PumperThread extends Thread {

        private OutputStream _os;
        private InputStream _is;

        private boolean _closeInput;

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
                byte[] buf = new byte[BUF];
                int read = 0;
                while (!isInterrupted() && (read = _is.read(buf)) != -1) {
                    if (read == 0)
                        continue;
                    _os.write(buf, 0, read);
                    _os.flush();
                }
            } catch (Throwable t) {
                _thrownError = t;
                return;
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
            try {
                _barrier.await();
            } catch (InterruptedException ie) {
                /* IGNORE */
            } catch (BrokenBarrierException bbe) {
                /* IGNORE */
            }
        }
    }

    private class PusherThread extends PumperThread {
        PusherThread(String name, InputStream is, OutputStream os) {
            super(name, is, os, false);
        }
    }

    private class PullerThread extends PumperThread {
        PullerThread(String name, InputStream is, OutputStream os) {
            super(name, is, os, true);
        }
    }

    public int getTimeout() {
        return _timeout;
    }

    public void setTimeout(int timeout) {
        _timeout = timeout;
    }

    public boolean getDestroyOnTimeout() {
        return _destroyOnTimeout;
    }

    public void setDestroyOnTimeout(boolean destroyOnTimeout) {
        _destroyOnTimeout = destroyOnTimeout;
    }

    public boolean getWaitForExit() {
        return _waitForExit;
    }

    public void setWaitForExit(boolean waitForExit) {
        _waitForExit = waitForExit;
    }
}

