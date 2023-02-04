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

package mil.sstaf.analyzer;

import mil.sstaf.analyzer.messages.*;
import mil.sstaf.core.util.SSTAFException;
import mil.sstaf.session.control.Session;
import mil.sstaf.session.messages.BaseSessionCommand;
import mil.sstaf.session.messages.SessionTickResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Main processing loop for the service implementations.
 */
public class Analyzer {

    private static final Logger logger = LoggerFactory.getLogger(Analyzer.class);
    private final Session session;
    private final Supplier<String> inputSupplier;
    private final Function<String, BaseAnalyzerCommand> deserializer;
    private final Function<BaseAnalyzerResult, String> serializer;
    private final Consumer<String> outputConsumer;

    private final AtomicBoolean keepRunning = new AtomicBoolean(true);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private final AtomicLong taskCounter = new AtomicLong(0);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    /**
     * Evaluation loop task.
     */
    private final Callable<Integer> analysisREPL = new Callable<>() {
        @Override
        public Integer call() {
            final int maxNulls = 100;

            //
            // Use anonymous inner classes so that Session already exists when
            // the strategies are instantiated.
            //
            ProcessingStrategy commandListProcessingStrategy = command -> {
                CommandList cl = (CommandList) command;
                for (BaseSessionCommand cmd : cl.getCommands()) {
                    session.submit(cmd);
                }
                SessionTickResult tickResult;
                switch (cl.getMode()) {
                    case SUBMIT_AND_DISPATCH:
                        tickResult = session.tickAgain();
                        break;
                    case TICK:
                        tickResult = session.tick(cl.getTime_ms());
                        break;
                    case SUBMIT_ONLY:
                    default:
                        tickResult = session.getEmptyTickResult();
                        break;
                }
                return mil.sstaf.analyzer.messages.TickResult.builder()
                        .sessionTickResult(tickResult)
                        .build();
            };

            ProcessingStrategy tickProcessingStrategy = command -> {
                Tick tick = (Tick) command;
                SessionTickResult tickResult;
                tickResult = session.tick(tick.getTime_ms());
                return mil.sstaf.analyzer.messages.TickResult.builder()
                        .sessionTickResult(tickResult)
                        .build();
            };

            ProcessingStrategy getEntitiesProcessingStrategy = command -> {
                List<String> entityList = session.getEntityController().getEntityPaths();
                return GetEntitiesResult.builder().entities(entityList).build();
            };

            ProcessingStrategy exitStrategy = command -> {
                keepRunning.set(false);
                return ExitResult.builder().exitTime(System.currentTimeMillis()).build();
            };

            Map<Class<? extends BaseAnalyzerCommand>, ProcessingStrategy> processingStrategyMap =
                    Map.of(Tick.class, tickProcessingStrategy,
                            GetEntities.class, getEntitiesProcessingStrategy,
                            CommandList.class, commandListProcessingStrategy,
                            Exit.class, exitStrategy);

            isRunning.set(true);
            int rv = 0;
            int nulls=0;
            String input = "";
            while (keepRunning.get()) {
                try {
                    logger.debug("Getting command");
                    long startTime = System.currentTimeMillis();
                    input = inputSupplier.get();
                    logger.debug("Got {}", input);
                    if (input != null && input.length() > 0) {
                        nulls = 0;
                        BaseAnalyzerCommand command = deserializer.apply(input);
                        ProcessingStrategy strategy = processingStrategyMap.get(command.getClass());
                        BaseAnalyzerResult result;
                        if (strategy == null) {
                            throw new SSTAFException("Unknown Analyzer Command");
                        } else {
                            result = strategy.process(command);
                        }
                        taskCounter.incrementAndGet();
                        result.setId(command.getId());
                        result.setProcessingTime_ms(System.currentTimeMillis() - startTime);
                        String output = serializer.apply(result);
                        logger.debug("Output is {}", output);
                        outputConsumer.accept(output);
                        logger.debug("Sending {}", output);
                    } else if (input != null) {
                        logger.debug("Received 0-length command");
                    } else {
                        ++ nulls;
                        logger.debug("Received null command - {}/{}",
                                nulls, maxNulls);
                        if (nulls >= 100) {
                            keepRunning.set(false);
                        } else {
                            Thread.sleep(100);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Exception thrown processing {}", input);
                    e.printStackTrace();
                    keepRunning.set(false);
                    rv = -1;
                    break;
                }
            }
            logger.info("Exiting Analyzer loop");
            isRunning.set(false);
            return rv;
        }
    };


    /**
     * Constructor
     *
     * @param session        The SSTAF session
     * @param inputSupplier  A {@code Supplier<String>} that provides a {@code String}
     *                       that can be transformed into messages for the
     *                       {@code Session}.
     * @param deserializer   Converts the input String into a {@code BaseAnalyzerCommand}
     * @param serializer     Converts the result into a String
     * @param outputConsumer A {@code Consumer<String>} that accepts the String
     */
    Analyzer(final Session session,
             final Supplier<String> inputSupplier,
             final Function<String, BaseAnalyzerCommand> deserializer,
             final Function<BaseAnalyzerResult, String> serializer,
             final Consumer<String> outputConsumer) {
        this.session = Objects.requireNonNull(session, "session");
        this.inputSupplier = Objects.requireNonNull(inputSupplier, "inputSupplier");
        this.deserializer = Objects.requireNonNull(deserializer, "deserializer");
        this.serializer = Objects.requireNonNull(serializer, "serializer");
        this.outputConsumer = Objects.requireNonNull(outputConsumer, "outputConsumer");
    }

    /**
     * Constructs an {@code Analyzer} using {@code System.in} and
     * {@code System.out}
     *
     * @param session the {@code Session}
     * @return a newly-constructed Analyzer
     */
    public static Analyzer fromSystemIO(final Session session) {
        var reader = new InputStreamReader(System.in);
        var bufferedReader = new BufferedReader(reader);
        var inputSupplier = new MessageReader(bufferedReader);
        var deserializer = new JsonDeserializer();
        var serializer = new JsonSerializer();
        var outputConsumer = new PrintStreamConsumer(System.out);
        return new Analyzer(session, inputSupplier, deserializer,
                serializer, outputConsumer);
    }

    /**
     * Starts the evaluation loop
     */
    public int start() throws InterruptedException, ExecutionException {
        keepRunning.set(true);
        List<Future<Integer>> futures = executor.invokeAll(List.of(analysisREPL));
        Future<Integer> one = futures.get(0);
        return one.get();
    }

    /**
     * Stops the evaluation loop
     */
    public void stop() {
        keepRunning.set(false);
    }

    /**
     * Answers whether the Analyzer is running.
     *
     * @return true if the main loop is running.
     */
    public boolean isRunning() {
        return isRunning.get();
    }

    /**
     * Provides the number of tasks that have been executed by the
     * {@code Analyzer}.
     *
     * @return the number of tasks.
     */
    public long getTaskCount() {
        return taskCounter.get();
    }

    private interface ProcessingStrategy {
        BaseAnalyzerResult process(BaseAnalyzerCommand command);
    }
}

