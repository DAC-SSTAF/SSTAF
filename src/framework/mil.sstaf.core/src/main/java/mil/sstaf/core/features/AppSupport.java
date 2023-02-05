package mil.sstaf.core.features;

import mil.sstaf.core.util.SSTAFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * Infrastructure for starting and interacting with external applications
 */
public class AppSupport {

    private static final Logger logger = LoggerFactory.getLogger(AppSupport.class);

    public static AppAdapter createAdapter(AppConfiguration configuration) {
        try {
            switch (configuration.getMode()) {
                case TRANSIENT:
                    return new TransientAdapter(configuration);
                case DURABLE:
                default:
                    return new DurableAdapter(configuration);
            }
        } catch (IOException ie) {
            throw new SSTAFException(ie);
        }
    }

    /**
     * Determines the working directory for the process.
     * <p>
     * To determine the working directory, the args are scanned to find one that matches the name of
     * an extracted resource. The path of the extracted resource is then used as the working directory.
     *
     * @param args the args provided to start the process, one is assumed to be a file in the working directory
     * @param rm   the {@code ResourceManager} object
     * @return a File that is the working directory
     */
    static File determineWorkDir(List<String> args, ResourceManager rm) {
        for (String arg : args) {
            for (var entry : rm.getResourceFiles().entrySet()) {
                String resourcePath = entry.getValue().getAbsolutePath();
                int idx = resourcePath.indexOf(arg);
                logger.debug("arg = {} resourcePath = {} idx = {}", arg, resourcePath, idx);
                if (idx > 0) {
                    String trimmed = resourcePath.substring(0, idx - 1);
                    logger.debug("trimmed = {}", trimmed);
                    File candidate = new File(trimmed);
                    if (candidate.exists() && candidate.canRead() && candidate.canWrite() && candidate.canExecute()) {
                        logger.debug("Working directory is: {}", candidate);
                        return candidate;
                    }
                }
            }
        }
        return rm.getDirectory().toFile();
    }

    /**
     * Enum for selecting whether applications are durable or transient.
     * Durable application stay open during
     */
    public enum Mode {
        TRANSIENT, DURABLE
    }

    private static String readLine(BufferedReader reader, long timeout_ms ) throws IOException {
        String read;
        long start = System.currentTimeMillis();
        long deltaT;
        do {
            read = reader.readLine();
            if (read == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    logger.debug("Interrupted!");
                }
                deltaT = System.currentTimeMillis() - start;
            } else {
                return read;
            }
        } while (deltaT < timeout_ms);
        throw new SSTAFException("Did not receive a response in "
                + deltaT + " ms");
    }

    static class TransientAdapter implements AppAdapter {
        Logger logger = LoggerFactory.getLogger(AppSupport.TransientAdapter.class);
        List<String> args;
        boolean dirtyArgs;

        ResourceManager resourceManager;
        TAppSession session;

        TransientAdapter(AppConfiguration configuration) throws IOException {
            resourceManager = ResourceManager.getManager(configuration.getResourceOwner());
            dirtyArgs = false;
            this.args = configuration.getProcessArgs();
        }

        @Override
        public AppSession activate() throws IOException {
            if (session != null) {
                session.close();
            }
            session = new TAppSession();
            return session;
        }

        @Override
        public AppSession activate(List<String> args) throws IOException {
            setArgs(args);
            return activate();
        }

        @Override
        public void setArgs(List<String> args) {
            this.args = args;
            dirtyArgs = true;
        }

        @Override
        public ResourceManager getResourceManager() {
            return resourceManager;
        }

        class TAppSession implements AppSession {

            Process process;
            BufferedReader input;
            BufferedWriter output;

            TAppSession() throws IOException {
                File workDir = determineWorkDir(args, resourceManager);
                logger.debug("workDir = {}", workDir);

                ProcessBuilder pb = new ProcessBuilder();
                pb.directory(workDir);
                pb.command(args);
                process = pb.start();
                input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            }

            @Override
            public String invoke(String cmd) throws IOException {
                output.write(cmd);
                if (!cmd.endsWith("\n")) output.write("\n");
                output.flush();
                return readLine(input, 60000);
            }

            @Override
            public void close() {
                process.destroy();
                session = null;
                if (logger.isDebugEnabled()) {
                    logger.debug(process.info().toString());
                }
            }
        }
    }

    static class DurableAdapter implements AppAdapter {
        Logger logger = LoggerFactory.getLogger(AppSupport.DurableAdapter.class);
        List<String> args;
        boolean dirtyArgs;
        ResourceManager resourceManager;
        DAppSession session;

        DurableAdapter(AppConfiguration configuration) throws IOException {
            resourceManager = ResourceManager.getManager(configuration.getResourceOwner());
            this.args = configuration.getProcessArgs();
            dirtyArgs = false;
        }

        @Override
        public AppSession activate() throws IOException {
            if (dirtyArgs && session != null) {
                session.process.destroy();
                session = null;
            }
            if (session == null) {
                session = new DAppSession();
                dirtyArgs = false;
            }
            return session;
        }

        @Override
        public AppSession activate(List<String> args) throws IOException {
            setArgs(args);
            return activate();
        }

        @Override
        public void setArgs(List<String> args) {
            this.args = args;
            dirtyArgs = true;
        }

        @Override
        public ResourceManager getResourceManager() {
            return resourceManager;
        }

        class DAppSession implements AppSession {

            Process process;
            BufferedReader input;
            BufferedWriter output;

            DAppSession() throws IOException {
                File workDir = determineWorkDir(args, resourceManager);
                logger.debug("workDir = {}", workDir);

                ProcessBuilder pb = new ProcessBuilder();
                pb.command(args);
                pb.directory(workDir);
                process = pb.start();
                input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            }

            @Override
            public String invoke(String cmd) throws IOException {
                output.write(cmd);
                if (!cmd.endsWith("\n")) output.write("\n");
                output.flush();
                return readLine(input, 30000);
            }

            @Override
            public void close() {
                if (logger.isDebugEnabled()) {
                    logger.debug(process.info().toString());
                }
            }
        }
    }
}
