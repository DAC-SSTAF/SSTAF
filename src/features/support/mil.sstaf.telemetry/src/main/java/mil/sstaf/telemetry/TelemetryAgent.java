/*
 * Copyright (c) 2022
 * United States Government as represented by the U.S. Army DEVCOM Analysis Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mil.sstaf.telemetry;

import mil.sstaf.blackboard.api.Blackboard;
import mil.sstaf.core.entity.Address;
import mil.sstaf.core.entity.Entity;
import mil.sstaf.core.entity.EntityHandle;
import mil.sstaf.core.features.*;
import mil.sstaf.core.state.State;
import mil.sstaf.core.state.StateProperty;
import mil.sstaf.core.util.SSTAFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelemetryAgent extends BaseAgent {
    public static final String FEATURE_NAME = "Telemetry Agent";
    public static final int MAJOR_VERSION = 1;
    public static final int MINOR_VERSION = 0;
    public static final int PATCH_VERSION = 0;

    public static final String CK_STATES_LIST = "statesToRecord";
    public static final String PROP_TELEMETRY_OUTPUT_DIR = "mil.sstaf.telemetry.outputDir";
    private static final Logger logger = LoggerFactory.getLogger(TelemetryAgent.class);
    private final Map<String, StateWriter> writerMap = new HashMap<>();
    private final List<String> stateKeys = new ArrayList<>();

    @Requires
    Blackboard blackboard;

    /**
     * Constructor
     */
    public TelemetryAgent() {
        super(FEATURE_NAME, MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION,
                true, "CSV telemetry writer");
    }

    /**
     * Sets the configuration for this provider.
     * <p>
     * The configuration is applied when {@code init()} is invoked.
     *
     * @param configuration the configuration
     */
    @Override
    public void configure(FeatureConfiguration configuration) {
        super.configure(configuration);
        if (configuration instanceof TelemetryConfiguration) {
            TelemetryConfiguration tc = (TelemetryConfiguration) configuration;
            if (tc.getStateKeys() != null) {
                this.stateKeys.addAll(tc.getStateKeys());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        super.init();
        try {
            File outputDir = makeOutputDir();
            logger.info("Writing telemetry for '" + ownerHandle.getPath() + "' to " + outputDir.getPath());
            stateKeys.forEach(key -> writerMap.put(key, new StateWriter(key, outputDir)));
        } catch (FileNotFoundException e) {
            throw new SSTAFException(e);
        }
    }

    private String filePathFromEntityPath(EntityHandle entityHandle) {
        String[] elements = entityHandle.getPath().split(Entity.ENTITY_PATH_DELIMITER);
        StringBuilder stringBuilder = new StringBuilder();
        for (String element : elements) {
            stringBuilder.append(element).append(File.separator);
        }
        return stringBuilder.toString();
    }

    /**
     *
     */
    private File makeOutputDir() throws FileNotFoundException {
        String logDirPath = System.getProperty(PROP_TELEMETRY_OUTPUT_DIR, "sstafTelemetry");
        String fullPath = logDirPath + File.separator + filePathFromEntityPath(ownerHandle);
        File outputDir = new File(fullPath);
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                throw new FileNotFoundException("Can't make telemetry output directory " + outputDir);
            }
        }
        return outputDir;
    }

    /**
     * Activate the Agent to perform a function at the specified time.
     *
     * @param currentTime_ms the simulation time.
     * @return a {@code ProcessingResult} containing internal and external {@code Message}s
     */
    @Override
    public ProcessingResult tick(long currentTime_ms) {
        if (logger.isDebugEnabled()) {
            logger.debug("{}:{} - Ticking at {}", ownerHandle.getPath(), featureName, currentTime_ms);
        }
        writerMap.values().forEach(writer -> writer.writeValues(currentTime_ms));
        return ProcessingResult.empty();
    }

    /**
     * Provides a {@code List} of all of the message content {@code Class}es to which this
     * {@code Handle} responds.
     * <p>
     * Handlers can respond to multiple message classes. Only one Handler may be associated with
     * a message class.
     *
     * @return a {@code List} of {@code Class} objects.
     */
    @Override
    public List<Class<? extends HandlerContent>> contentHandled() {
        return List.of();
    }

    /**
     * Performs the processing associated with the given argument at the specified time.
     * <p>
     * Both the scheduled time and current simulation time are reported. For an {@code EntityEvent}
     * the scheduled time value will be less than or equal the current simulation time. For an
     * {@code EntityAction} scheduled time will be the same as the current time. Providing both times
     * enables the Handler to adjust for any time difference between when an event was intended to occur
     * and when it was processed.
     *
     * @param arg              the message content
     * @param scheduledTime_ms the time for which an event was scheduled.
     * @param currentTime_ms   the current simulation time
     * @param from             the sources of the {@code Message}
     * @param id               the message sequence numner
     * @param respondTo        to where to send the results.
     * @return a {@code ProcessingResult} that contains the results of the processing
     */
    @Override
    public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms, Address from, long id, Address respondTo) {
        return buildUnsupportedMessageResponse(arg, id, respondTo, new UnsupportedOperationException());
    }

    /**
     * Accessor for tests
     *
     * @return the Blackboard
     */
    Blackboard getBlackboard() {
        return blackboard;
    }

    class StateWriter {
        private final String key;
        private final List<Method> methods = new ArrayList<>();
        private final List<String> headers = new ArrayList<>();
        private PrintStream printStream;
        private boolean configured = false;
        private Class<?> configuredClass;

        private StateWriter(String key, File parentDir) {
            this.key = key;
            String path = parentDir.getPath() + File.separator + key + ".csv";
            File outputFile = new File(path);
            try {
                this.printStream = new PrintStream(outputFile);
            } catch (FileNotFoundException e) {
                this.printStream = System.out;
            }
            if (logger.isTraceEnabled()) {
                logger.trace("{}:{} created writer for key '{}'", ownerHandle.getPath(), featureName, key);
            }
        }

        private void getMethodWithStateProperty(final Class<?> target) {
            if (logger.isDebugEnabled()) {
                logger.debug("{} - Scanning methods for StateProperty annotations in ", target.getName());
            }
            if (Object.class.equals(target)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Ignoring Object.class ");
                }
                return;
            } else {
                getMethodWithStateProperty(target.getSuperclass());
                if (logger.isTraceEnabled()) {
                    logger.trace("{} - returned from superclass ", target.getSuperclass().getName());
                }
            }
            for (Method method : target.getDeclaredMethods()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("{} - Processing field {}", target.getName(), method.getName());
                }
                if (method.isAnnotationPresent(StateProperty.class)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("{} - Registering field {}", target.getName(), method.getName());
                    }
                    methods.add(method);
                    StateProperty sp = method.getAnnotation(StateProperty.class);
                    String headerName = sp.headerLabel();
                    if (headerName.length() == 0) {
                        headerName = method.getName();
                    }
                    headers.add(headerName);
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("{} - Field {} is not annotated", target.getName(), method.getName());
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("{} - Found {} @StateProperty annotations, {}", target.getName(),
                        methods.size(), methods);
            }
        }

        void writeHeader() {
            if (logger.isTraceEnabled()) {
                logger.trace("{}:{} writing header for key '{}'", ownerHandle.getPath(), featureName, key);
            }
            printStream.print("\"Time\",");
            for (String header : headers) {
                String x = '"' + header + '"' + ',';
                printStream.print(x);
            }
            printStream.println();
            printStream.flush();
        }

        void writeValues(long currentTime_ms) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}:{} writing values for key '{}'", ownerHandle.getPath(), featureName, key);
            }
            blackboard.getEntry(key, currentTime_ms, State.class)
                    .ifPresentOrElse(
                            state -> writeValues(currentTime_ms, state),
                            () -> {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("{}:{} No entry for key '{}'", ownerHandle.getPath(),
                                            featureName, key);
                                }
                            });
        }

        void writeValues(long currentTime_ms, State target) {
            if (configured) {
                if (!configuredClass.isAssignableFrom(target.getClass())) {
                    throw new SSTAFException("Record type changed");
                }
            } else {
                getMethodWithStateProperty(target.getClass());
                writeHeader();
                configured = true;
                configuredClass = target.getClass();
            }

            printStream.print(currentTime_ms);
            printStream.print(", ");

            for (Method method : methods) {
                boolean accessible = method.canAccess(target);
                if (!accessible) {
                    method.setAccessible(true);
                }
                try {
                    Class<?> cl = method.getReturnType();
                    if (int.class.equals(cl)) {
                        int val = (Integer) method.invoke(target);
                        printStream.print(val);
                    } else if (double.class.equals(cl)) {
                        double val = (Double) method.invoke(target);
                        printStream.print(val);
                    } else if (boolean.class.equals(cl)) {
                        boolean val = (Boolean) method.invoke(target);
                        printStream.print(val);
                    } else {
                        Object val = method.invoke(target);
                        String output = '"' + val.toString() + '"';
                        printStream.print(output);
                    }
                    printStream.print(", ");
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            printStream.println();
            printStream.flush();
        }
    }
}

