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

package mil.sstaf.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import lombok.experimental.SuperBuilder;
import mil.sstaf.core.features.ExceptionContent;
import mil.sstaf.core.features.FeatureConfiguration;
import mil.sstaf.core.features.FeatureSpecification;
import mil.sstaf.core.features.ProcessingResult;
import mil.sstaf.core.module.ModuleLayerDefinition;
import mil.sstaf.core.util.Injected;
import mil.sstaf.core.util.RNGUtilities;
import mil.sstaf.core.util.SSTAFException;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>The {@code Entity} class is the base class for all simulation participants.</p>
 * <p>{@code Entity} provides the core implementation for the {@code MessageDriven} architecture by providing
 * the queues for receiving and returning messages and a mechanism for dispatching the messages to the appropriate
 * loaded {@code Handler}.</p>
 */
// 2022-04-07 : RAB : Fight the temptation add @Jacksonized. It can't be applied to abstract classes.

@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class BaseEntity implements Entity {

    /**
     * The {@code Logger} for this Entity.
     */
    private static final Logger logger = LoggerFactory.getLogger(BaseEntity.class);
    @Getter
    @Builder.Default
    protected UUID uuid = null;
    /**
     * A unique identification number associated with each {@code Entity}.
     */
    @Getter
    @Builder.Default
    @JsonIgnore
    // 2022-04-12 : RAB : Non-final so that subclasses can replace with custom IDs.
    protected long id = 0;
    /**
     * The random number generator for this {@code Entity}. This generator acts as the source
     * for the seeds used in any loaded {@code Feature}s.
     */
    @Builder.Default
    @JsonIgnore
    protected RandomGenerator randomGenerator = null;
    /**
     * The {@link FeatureManager} that manages the {@code Features} assigned to this {@code Entity}. The
     * {@code FeatureManager} holds the references to the {@code Feature} implementations, dispatches messages
     * to them, gathers responses and invokes the {@code tick()} method on implementations of {@code Agent}
     */
    @Getter(AccessLevel.PACKAGE)
    @JsonIgnore
    protected FeatureManager featureManager;
    //
    // In and Out queues
    //
    @Builder.Default
    @JsonIgnore
    protected Queue<Message> inboundQueue = null;
    @Builder.Default
    @JsonIgnore
    protected Queue<Message> outboundQueue = null;
    @Injected(name = "randomSeed")
    protected long randomSeed;

    //
    // Identification
    //
    @Getter
    @Setter
    protected String name;

    @Getter
    @Builder.Default
    protected EntityHandle handle = null;

    @Getter
    @Setter
    @Builder.Default
    protected Force force = Force.GRAY;

    @Getter
    @Singular
    protected Map<String, FeatureConfiguration> configurations;

    @Getter
    @Singular
    protected List<FeatureSpecification> features;
    @Builder.Default
    @JsonIgnore
    private AtomicLong msgCounter = null;

    @Getter
    @Builder.Default
    @JsonIgnore
    private boolean initialized = false;

    @Getter
    private ModuleLayerDefinition moduleLayerDefinition;

    /**
     * Constructor
     *
     * @param builder the {@code Builder} to use to construct the {@code Entity}
     */
    protected BaseEntity(BaseEntity.BaseEntityBuilder<?, ?> builder) {
        logger.trace("Constructing Entity from builder {}", builder);
        this.id = BlockCounter.userCounter.getID();
        this.configurations = buildConfigurations(builder);
        uuid = UUID.randomUUID();
        this.name = builder.name == null || builder.name.length() == 0 ? this.uuid.toString() : builder.name;

        this.randomSeed = builder.randomSeed == 0 ? id : builder.randomSeed;
        this.randomGenerator = new MersenneTwister();
        this.randomGenerator.setSeed(randomSeed);

        features = builder.features == null ? List.of() : builder.features;

        handle = new EntityHandle(this);
        featureManager = new FeatureManager(handle,
                moduleLayerDefinition,
                features,
                this.configurations,
                RNGUtilities.generateSubSeed(randomGenerator));
        if (logger.isDebugEnabled()) {
            logger.debug("Entity '{}' constructed, features = {}", name,
                    featureManager.generateConfigurationReport());
        }
        inboundQueue = new PriorityQueue<>(new MessageQueueComparator());
        outboundQueue = new ConcurrentLinkedQueue<>();
        msgCounter = new AtomicLong(0);
    }


    protected BaseEntity() {
        this.featureManager = null;
        this.uuid = UUID.randomUUID();
        this.name = uuid.toString();
        this.randomGenerator = null;
        this.id = -666L;
        inboundQueue = new PriorityQueue<>(new MessageQueueComparator());
        outboundQueue = new ConcurrentLinkedQueue<>();
        msgCounter = new AtomicLong(0);
    }

    protected BaseEntity(String name, long id) {
        this.featureManager = null;
        this.name = name;
        this.randomGenerator = null;
        this.id = id;
        this.handle = new EntityHandle(this);
        this.uuid = UUID.randomUUID();
        inboundQueue = new PriorityQueue<>(new MessageQueueComparator());
        outboundQueue = new ConcurrentLinkedQueue<>();
        msgCounter = new AtomicLong(0);
    }

    private Map<String, FeatureConfiguration> buildConfigurations(BaseEntityBuilder<?, ?> builder) {
        Map<String, FeatureConfiguration> configMap;
        if (builder.configurations$key != null) {
            configMap = new TreeMap<>();
            for (int i = 0; i < builder.configurations$key.size(); ++i) {
                configMap.put(builder.configurations$key.get(i), builder.configurations$value.get(i));
            }
        } else {
            configMap = Map.of();
        }
        return configMap;
    }

    /**
     * Injects an item into the features
     *
     * @param object the item to inject.
     */
    @Override
    public void injectInFeatures(Object object) {
        featureManager.injectAll(object);
    }


    /**
     * Checks to determine if the {@code Entity} has been initialized.
     * <p>
     * If the {@code Entity has not been }
     */
    @Override
    public void checkInit() {
        if (!initialized) {
            throw new SSTAFException("Entity " + name + " has not been initialized");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long generateSequenceNumber() {
        return msgCounter.getAndIncrement();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> takeInbound() {
        checkInit();
        return takeFromQueue(inboundQueue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> takeOutbound() {
        checkInit();
        return takeFromQueue(outboundQueue);
    }

    private List<Message> takeFromQueue(Queue<Message> inboundQueue) {
        List<Message> out = new ArrayList<>(inboundQueue.size());
        out.addAll(inboundQueue);
        inboundQueue.clear();
        return out;
    }

    /**
     * Provides the current depth of the inbound message queue.
     *
     * @return the queue depth
     */
    @Override
    public int getInboundQueueDepth() {
        return inboundQueue.size();
    }

    /**
     * Runs all per-tick agents.
     * <p>
     * Agents can produce both outbound messages and internal
     *
     * @param currentTime_ms the current simulation time
     * @return the time of the next event
     */
    @Override
    public long runAgents(final long currentTime_ms) {
        checkInit();
        ProcessingResult pr = featureManager.runAllAgents(currentTime_ms);
        routeProcessingResults(pr);
        return getNextEventTime();
    }

    /**
     * Initializes the {@code Entity}.
     */
    @Override
    public void init() {
        logger.trace("Initializing Entity {}", name);
        if (handle == null) {
            throw new IllegalStateException("EntityHandle has not been initialized");
        }
        randomGenerator.setSeed(randomSeed);
        featureManager.init();
        initialized = true;
    }

    /**
     * Returns the time of the next event in the queue.
     *
     * @return Time of the next event or Double.POSITIVE_INFINITY if there is no event.
     */
    private long getNextEventTime() {
        Message message = inboundQueue.peek();
        if (message instanceof EntityEvent) {
            return ((EntityEvent) message).getEventTime_ms();
        } else {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Polls the queue for the next event whose eventTime is less than or equal to the current simulation time.
     *
     * @param currentTime_ms the current simulation time
     * @return An Optional that contains the Event or is empty if no events are found.
     */
    private Optional<Message> getNextMessage(final long currentTime_ms) {
        Message message = inboundQueue.peek();
        if (message == null) {
            return Optional.empty();
        } else {
            if (message instanceof EntityEvent) {
                if (((EntityEvent) message).getEventTime_ms() > currentTime_ms) {
                    return Optional.empty();
                }
            }
        }
        return Optional.ofNullable(inboundQueue.poll());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void receive(Message message) {
        checkInit();
        inboundQueue.offer(message);
        if (logger.isTraceEnabled()) {
            logger.trace("Entity {} received message: {} queue length: {}",
                    getName(), message, inboundQueue.size());
        }
    }

    /**
     * Answers whether the Entity has a {@code Handler} that can process the content.
     *
     * @param contentClass the {@code Class} to check
     * @return true if it can be handled, false otherwise.
     */
    @Override
    public boolean canHandle(Class<?> contentClass) {
        boolean ok = featureManager.canHandle(contentClass);
        if (!ok) {
            logger.warn("Entity {} can't process messages of type {}, no handler for that type was specified in the input",
                    getName(), contentClass.getName());
        }
        return ok;
    }


    /**
     * Processes all messages in the queue up to the specified time.
     *
     * @param currentTime_ms the current simulation time.
     * @return the time of the next event in the queue.
     */
    @Override
    public long processMessages(final long currentTime_ms) {
        checkInit();
        logger.trace("Entity {}, starting tick at {}", getPath(), currentTime_ms);
        Optional<Message> optMessage;
        while ((optMessage = getNextMessage(currentTime_ms)).isPresent()) {
            Message message = optMessage.get();
            try {
                logger.trace("Entity {}, processing {} at {}", getPath(), message, currentTime_ms);
                ProcessingResult pr = featureManager.process(message, currentTime_ms);
                logger.trace("Entity {}, result was {}", getPath(), pr);
                routeProcessingResults(pr);
            } catch (Exception e) {
                logger.error("Entity {}: {}", name, e);
                e.printStackTrace();
                String errMsg = "Entity " + name + ": Error at time " + currentTime_ms + " ms, processing " + message;
                sendErrorResponse(message.getSequenceNumber(), errMsg, e, message.getRespondTo());
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Entity {}, done processing message", getName());
            }
        }
        return getNextEventTime();
    }


    /**
     * Routes any messages within processing results to either the inbound or outbound queues according to their
     * destination address.
     *
     * @param pr the ProcessingResult
     */
    private void routeProcessingResults(ProcessingResult pr) {
        pr.messages.forEach(m -> {
            if (m.getDestination().equals(Address.NOWHERE)) {
                logger.trace("In {}, dropping message to NOWHERE from {}, contents = {}",
                        getName(), m.getSource().handlerName, m.getContent());
            } else if (m.getDestination().entityHandle.equals(this.handle)) {
                logger.trace("In {}, submitting local message from {} to {}, contents = {}",
                        getName(), m.getSource().handlerName, m.getDestination().handlerName, m.getContent());
                inboundQueue.offer(m);
            } else {
                logger.trace("In {}, submitting message from {} to {}:{}, contents = {}",
                        getName(), m.getSource().handlerName,
                        m.getDestination().entityHandle.getPath(),
                        m.getDestination().handlerName,
                        m.getContent());
                outboundQueue.offer(m);
            }
        });
    }

    /**
     * Builds an ErrorResponse
     *
     * @param id          the sequence number of the {@code Message} that resulted int the error
     * @param message     the error message
     * @param exception   the caught exception
     * @param destination where the message is going
     */
    @Override
    public void sendErrorResponse(final long id, final String message, final Throwable exception,
                                  final Address destination) {
        var b = ErrorResponse.builder()
                .source(Address.makeAddress(this.getHandle(), "NONE"))
                .destination(destination)
                .messageID(id)
                .sequenceNumber(this.generateSequenceNumber())
                .content(ExceptionContent.builder().errorDescription(message).thrown(exception).build());
        Message out = b.build();
        logger.trace("Entity {} sending {}", name, out);
        outboundQueue.offer(out);
    }

    static class Dummy extends BaseEntity {
        @Override
        public String getPath() {
            return "Dummy";
        }

    }
}

