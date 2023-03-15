/**
 * Template implementation of an Agent.
 */
package mil.sstaf.pyagent.impl;

import mil.sstaf.core.entity.Address;
import mil.sstaf.core.features.*;
import mil.sstaf.core.util.SSTAFException;
import mil.sstaf.pyagent.api.PyAgent;
import mil.sstaf.pyagent.messages.CountLettersRequest;
import mil.sstaf.pyagent.messages.CountLettersResult;
import mil.sstaf.pyagent.messages.SetTZero;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PyAgentImpl extends BaseAgent implements PyAgent {
    private static final Logger logger = LoggerFactory.getLogger(PyAgentImpl.class);
    private final AtomicInteger requestCount = new AtomicInteger(0);
    AppAdapter pythonService;

    /**
     * Features require a no-args constructor to be loaded by ServiceLoader.
     */
    public PyAgentImpl() {
        super(/* Feature Name */ "PyAgent",
                /* Major Version */1,
                /* Minor Version */0,
                /* Patch Version */0,
                /* Requires Configuration? */ false,
                /* Description */ "This is an example of a Python-based model");
    }


    @Override
    public void configure(FeatureConfiguration configuration) {
        super.configure(configuration);
        final String resourcePath = getClass().getPackage().getName().replace('.', '/') + "/";

        /*
         * Configure support for the helper application.
         * Every required application, script and data file must be
         * declared to be extracted. Helper applications can't read
         * resources that are packaged in the jar file.
         */
        AppConfiguration config = AppConfiguration.builder()
                //
                // Applications with their own REPLs should be declared
                // as DURABLE. One-and-done applications, even if long-running,
                // should be declared as TRANSIENT
                //
                .mode(AppSupport.Mode.DURABLE)
                .resource(resourcePath + "sstaf_pyagent.py")
                .resourceOwner(this.getClass())
                //
                // Specify the helper application and its arguments
                //
                .processArgs(List.of("python", "sstaf_pyagent.py"))
                .build();
        pythonService = AppSupport.createAdapter(config);
        ResourceManager rm = pythonService.getResourceManager();
        logger.info("Script installation directory = {}", rm.getDirectory());
        for (var entry : rm.getResourceFiles().entrySet()) {
            logger.info(entry.getKey() + " -->" + entry.getValue().getPath());
        }
    }

    /**
     * Tell the framework which messages this Feature accepts
     *
     * @return the list of messages
     */
    @Override
    public List<Class<? extends HandlerContent>> contentHandled() {
        return List.of(CountLettersRequest.class);
    }

    /**
     * Generates common "front matter" for commands
     *
     * @param command the command to execute
     * @return a partially-loaded StringBuilder
     */
    protected StringBuilder generatePrefix(String command) {
        StringBuilder sb = new StringBuilder();
        sb.append(requestCount.getAndIncrement()).append(' ');
        sb.append(command).append(' ');
        return sb;
    }

    /**
     * Example implementation for dispatching a request to the Python script
     * <p>
     * Invoking the script requires:
     * <ol>
     * <li>
     *     Generating the command as a String;
     * </li>
     * <li>
     *     Invoking the script using the AppSession object
     * </li>
     * <li>
     *     Parsing the result String
     * </li>
     * </ol>
     * <p>
     * This method dispatches a list of Strings to the Python app. The
     * application returns the sum of the length of each arg.
     *
     * @param args the Strings to count
     * @return the total number of characters
     */
    @Override
    public int countLetters(List<String> args) {
        int count = -1;
        //
        // Build the command String
        // The general form of commands to the Python script is
        // seqNumber command args...
        //
        StringBuilder sb = generatePrefix("count");

        for (Iterator<String> it = args.iterator(); it.hasNext();) {
            sb.append(it.next());
            sb.append(it.hasNext() ? ' ' : '\n');
        }

        //
        // Invoke and process result
        //
        try {
            AppSession session = pythonService.activate();
            String result = session.invoke(sb.toString());

            String[] parsed = result.split(" ");
            if (parsed.length != 5) {
                throw new SSTAFException("Result string does not contain 5 fields. Got '"
                        + result + "' ");
            }
            //
            // Check request ID
            //
            int expected = requestCount.get() - 1;
            int responseID = Integer.parseInt(parsed[0]);

            if (responseID != expected) {
                throw new SSTAFException("Response ID [" +
                        responseID + "] did not match expected value [" +
                        expected + "]");
            }

            if ("ok".equals(parsed[1])) {
                count = Integer.parseInt(parsed[4]);
            } else if ("error".equals(parsed[1])) {
                throw new SSTAFException("Request failed: got " + result);
            }

        } catch (IOException e) {
            throw new SSTAFException("Script invocation failed", e);
        }
        return count;
    }

    public long setTZero(long tZero) {
        //
        // Invoke and process result
        //
        long tZero_x = 0;
        try {
            String command = generatePrefix("setTZero")
                    .append(tZero).toString();
            logger.warn(command);
            AppSession session = pythonService.activate();
            String result = session.invoke(command);
            logger.warn(result);

            String[] parsed = result.split(" ");
            if (parsed.length != 5) {
                throw new SSTAFException("Result string does not contain 5 fields. Got '"
                        + result + "' ");
            }
            //
            // Check request ID
            //
            int expected = requestCount.get() - 1;
            int responseID = Integer.parseInt(parsed[0]);

            if (responseID != expected) {
                throw new SSTAFException("Response ID [" +
                        responseID + "] did not match expected value [" +
                        expected + "]");
            }

            if ("ok".equals(parsed[1])) {
                tZero_x = Long.parseLong(parsed[4]);
            } else if ("error".equals(parsed[1])) {
                throw new SSTAFException("Request failed: got " + result);
            }

        } catch (IOException e) {
            throw new SSTAFException("Script invocation failed", e);
        }
        return tZero_x;
    }

    private double advanceTheClockInPython(long currentTime_ms) {
        //
        // Invoke and process result
        //
        double capability = 0.0;
        try {
            String command = generatePrefix("advanceClock")
                    .append(currentTime_ms).toString();
            logger.warn(command);
            AppSession session = pythonService.activate();
            String result = session.invoke(command);
            logger.warn(result);
            String[] parsed = result.split(" ");
            if (parsed.length != 5) {
                throw new SSTAFException("Result string does not contain 5 fields. Got '"
                        + result + "' ");
            }
            //
            // Check request ID
            //
            int expected = requestCount.get() - 1;
            int responseID = Integer.parseInt(parsed[0]);

            if (responseID != expected) {
                throw new SSTAFException("Response ID [" +
                        responseID + "] did not match expected value [" +
                        expected + "]");
            }

            if ("ok".equals(parsed[1])) {
                capability = Double.parseDouble(parsed[4]);
            } else if ("error".equals(parsed[1])) {
                throw new SSTAFException("Request failed: got " + result);
            }

        } catch (IOException e) {
            throw new SSTAFException("Script invocation failed", e);
        }
        return capability;
    }

    @Override
    public ProcessingResult tick(long currentTime_ms) {
        double capability = advanceTheClockInPython(currentTime_ms);
        DoubleContent dc = DoubleContent.builder().value(capability).build();
        return ProcessingResult.of(List.of(
                buildNormalResponse(dc,currentTime_ms, Address.CLIENT)
                // Additional messages & events to other entities or the client
        ));
    }

    @Override
    public ProcessingResult process(HandlerContent o, long l, long l1, Address from, long id, Address respondTo) {

        if (o instanceof CountLettersRequest) {
            CountLettersRequest request = (CountLettersRequest) o;
            int count = countLetters(request.getArgs());
            CountLettersResult clr = CountLettersResult.builder().count(count).build();
            return ProcessingResult.of(buildNormalResponse(clr, id, respondTo));
        } else if (o instanceof SetTZero) {
            SetTZero request = (SetTZero) o;
            long res = setTZero(request.getTZero());
            LongContent lc = LongContent.builder().longValue(res).build();
            return ProcessingResult.of(buildNormalResponse(lc, id, respondTo));
        } else {
            throw new SSTAFException("Unrecognized command: " + o.toString());
        }
    }
}
