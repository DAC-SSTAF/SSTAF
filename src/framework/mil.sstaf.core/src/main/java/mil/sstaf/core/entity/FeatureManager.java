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

import mil.sstaf.core.configuration.SSTAFConfiguration;
import mil.sstaf.core.features.*;
import mil.sstaf.core.module.ModuleLayerDefinition;
import mil.sstaf.core.module.ModuleLayerSupport;
import mil.sstaf.core.util.*;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages all Features (Providers, Handlers and Agents) associated with an Entity
 */
public class FeatureManager {

    private static final Logger logger = LoggerFactory.getLogger(FeatureManager.class);
    private final Map<FeatureSpecification, Feature> features = new HashMap<>();
    private final Map<Class<?>, Handler> contentToHandlerMap = new HashMap<>();
    private final Map<String, Handler> nameToHandlerMap = new HashMap<>();
    private final Set<Agent> agents = new HashSet<>();
    //
    // Needed for return address in Agent messages.
    //
    private final EntityHandle owner;
    private final AtomicLong messageCounter = new AtomicLong(0);
    //
    // ModuleLayer support
    //
    ModuleLayer moduleLayer;
    private boolean initialized = false;

    /**
     * Simplified constructor.
     *
     * <p>This constructor does not provide the ability to add modules or define module paths.</p>
     *
     * @param owner          the {@code Entity} to which these features belong.
     * @param features       A list of the features (Handlers or Agents) that the {@code Entity} provides
     * @param configurations the set of configuration objects provided to the Entity, as a JSONObject
     * @param randomSeed     a seed for random number generation.
     */
    public FeatureManager(EntityHandle owner, List<FeatureSpecification> features,
                          Map<String, FeatureConfiguration> configurations, long randomSeed) {
        this(owner, null, features, configurations, randomSeed);
    }

    /**
     * Constructor
     *
     * @param owner          the {@code Entity} to which these features belong.
     * @param moduleLayerDefinition the configuration for a new {@code ModuleLayer}
     * @param features       A list of the features (Handlers or Agents) that the {@code Entity} provides
     * @param configurations the set of configuration objects provided to the Entity, as a JSONObject
     * @param randomSeed     a seed for random number generation.
     */
    public FeatureManager(EntityHandle owner,
                          ModuleLayerDefinition moduleLayerDefinition,
                          List<FeatureSpecification> features,
                          Map<String, FeatureConfiguration> configurations,
                          long randomSeed) {

        //
        // The simple stuff :-)
        //
        this.owner = owner;
        RandomGenerator generator = new MersenneTwister(randomSeed);

        //
        // If modules and paths have been configured, set up a new module layer within this entity.
        // This new layer will encapsulate all the features loaded into this entity.
        // Otherwise, use the class loader associated with the Entity itself.
        //
        moduleLayer = ModuleLayerSupport.makeModuleLayer(SSTAFConfiguration.getInstance().getRootLayer(),
                moduleLayerDefinition, owner.getClass().getClassLoader());

        if (!features.isEmpty()) {
            logger.debug("Resolving features");
            Resolver resolver = new Resolver(this.features, configurations, owner,
                    RNGUtilities.generateSubSeed(generator), moduleLayer);

            features.forEach(spec -> {
                logger.debug("Resolving {}", spec);
                Feature f = resolver.loadAndResolveDependencies(spec);
                logger.debug("Got {} / {}", f.getName(), f.getDescription());
                if (f instanceof Agent) {
                    register((Agent) f);
                } else if (f instanceof Handler) {
                    register((Handler) f);
                } else {
                    register(f);
                }
            });
        }
    }

    /**
     * Empty constructor for testing
     */
    public FeatureManager() {
        this(null, List.of(), Map.of(), 1234567);
    }

    /**
     * Confirms that the object has been initialized properly. If not, a
     * {@code SSTAFException) is thrown
     */
    private void checkInit() {
        if (!initialized) {
            throw new SSTAFException("Handlers has not been properly initialized");
        }
    }

    /**
     * Processes a single message from a client.
     *
     * @param message        the message to be processed.
     * @param currentTime_ms the current simulation time
     * @return a {@code ProcessingResult} that encapsulates the result of processing the message.
     */
    public ProcessingResult process(final Message message,
                                    final long currentTime_ms) {
        checkInit();

        final HandlerContent content = message.getContent();

        if (content == null) {
            var b = ErrorResponse.builder();
            b.source(Address.makeAddress(owner, "FeatureManager"));
            b.errorDescription("Message content was null");
            b.destination(message.getRespondTo());
            b.sequenceNumber(messageCounter.getAndIncrement());
            b.messageID(message.getSequenceNumber());
            logger.error("In {}, message content was null", owner.getPath());
            return ProcessingResult.of(b.build());
        }

        final long scheduledTime_ms = (message instanceof EntityEvent) ?
                ((EntityEvent) message).getEventTime_ms() : currentTime_ms;

        //
        // Select handler based on destination name and content class
        //
        Handler handler = getHandler(content.getClass(), message.getDestination().handlerName);

        if (handler == null) {
            if (content instanceof ExceptionCommand) {
                //
                // default handling for error messages.
                //
                Throwable t = ((ExceptionCommand) content).getThrown();
                logger.error("Received throwable from " + message.getSource().toString(), t);
                return ProcessingResult.empty();
            } else {
                var b = ErrorResponse.builder();
                b.source(Address.makeAddress(owner, "FeatureManager"));
                b.destination(message.getRespondTo());
                b.sequenceNumber(messageCounter.getAndIncrement());
                b.messageID(message.getSequenceNumber());
                String msg = "No handler for message of type " + content.getClass().getName();
                b.errorDescription(msg);
                b.content(ExceptionCommand.builder().thrown(new SSTAFException(msg)).build());
                logger.error(
                        "\tIn {}, No handler for message of type {}, received from {}\n" +
                                "\tTypes handled are {}\n" +
                                "\tThe handlers are {}",
                        owner.getPath(),
                        content.getClass(),
                        message.getSource(),
                        contentToHandlerMap.keySet(),
                        nameToHandlerMap.values());
                return ProcessingResult.of(b.build());
            }
        } else {
            logger.trace("Dispatching {} to {}", message, handler.getName());
            return handler.process(content, scheduledTime_ms, currentTime_ms,
                    message.getSource(), message.getSequenceNumber(), message.getRespondTo());
        }
    }

    /**
     * Chooses a Handler to process the message
     *
     * @param contentClass the Class of the message content.
     * @param handlerName  the name of the destination Handler specified in the message.
     * @return a Handler, or null if no suitable Handler was found.
     */
    Handler getHandler(final Class<? extends HandlerContent> contentClass, final String handlerName) {
        Objects.requireNonNull(contentClass, "Content class was null");
        Handler handler;
        if (handlerName == null) {
            logger.trace("In {}, selecting handler using class {}",
                    owner.getPath(), contentClass.getName());
            handler = contentToHandlerMap.get(contentClass);

            if (handler == null) {
                for (Class<?> key : contentToHandlerMap.keySet()) {
                    if (key.getName().equals(contentClass.getName())) {
                        logger.error("Duplicated class! {} key CL = {}, message CL = {} ",
                                contentClass.getName(),
                                key.getClassLoader(),
                                contentClass.getClassLoader());
                    }
                }
            }
        } else {
            logger.trace("In {}, selecting handler using destination name {}",
                    owner.getPath(), handlerName);
            handler = nameToHandlerMap.get(handlerName);
            if (handler != null && !handler.contentHandled().contains(contentClass)) {
                handler = null;
                logger.debug("In {}, handler {} does not support {}",
                        owner.getPath(), handlerName, contentClass.getName());
            }
        }
        logger.debug("In {}, selected {} to handle {}", owner.getPath(),
                handler == null ? "null" : handler.getName(), contentClass.getName());
        logger.debug("In {}, module for message {}, classloader for module {}",
                owner.getPath(),
                contentClass.getModule().getName(),
                contentClass.getModule().getClassLoader());
        return handler;
    }

    /**
     * Generates a formatted String that describes the loaded features
     *
     * @return a String
     */
    String generateConfigurationReport() {
        StringBuilder sb = new StringBuilder();
        for (Feature f : features.values()) {
            sb.append('\'').append(f.getName()).append("' ")
                    .append(f.getMajorVersion()).append('.')
                    .append(f.getMinorVersion()).append('.')
                    .append(f.getPatchVersion()).append(' ');
            if (f instanceof Handler) {
                Handler h = (Handler) f;
                List<Class<? extends HandlerContent>> lc = h.contentHandled();
                if (lc == null) {
                    logger.error("Handler {} returned 'null' from contentHandled(), it should return an empty list!. Fix it!",
                            h.getClass().getCanonicalName());
                } else {
                    sb.append("[");
                    for (Class<?> c : h.contentHandled()) {
                        String sn = c.getSimpleName();
                        sb.append(sn).append(' ');
                    }
                    sb.append(']');
                }
            }
            sb.append("; ");
        }
        return sb.toString();
    }

    /**
     * Provides an {@code Handler} associated with the provided content
     *
     * @param content the {@code Message content}
     * @return an Optional that contains the {@code Handler} if one is found
     */
    Optional<Handler> getHandlerForContent(Object content) {
        return Optional.ofNullable(contentToHandlerMap.get(content.getClass()));
    }


    /**
     * Answers whether or not there is a {@code Handler} registered that can
     * process the specified content.
     *
     * @param contentClass the {@code Class} of the content
     * @return true if there is a handler, false otherwise.
     */
    public boolean canHandle(final Class<?> contentClass) {
        return contentToHandlerMap.containsKey(contentClass);
    }

    /**
     * Invokes all Agents at the current time.
     *
     * @param currentTime_ms the current simulation time
     * @return the result of the processing
     */
    public ProcessingResult runAllAgents(final long currentTime_ms) {
        checkInit();
        logger.trace("Entity {} entering runAllAgents", getOwnerName());
        List<ProcessingResult> output = new ArrayList<>();
        agents.forEach(agent -> {
            logger.trace("Entity {} invoking agent {} at {}",
                    getOwnerName(),
                    agent.getClass().getName(), currentTime_ms);
            ProcessingResult pr = agent.tick(currentTime_ms);
            logger.trace("In Entity {}, agent {} returned {}",
                    getOwnerName(),
                    agent.getClass().getName(),
                    pr);
            output.add(pr);
        });
        logger.trace("Entity {} runAllAgents returning {}", getOwnerName(), output);
        return ProcessingResult.merge(output);
    }

    private String getOwnerName() {
        return owner == null ? "NULL" : owner.getName();
    }

    /**
     * Initialized the set of {@code MessageHandlers} and {@code TickAgents}
     * <p>
     * This method injects For each handler and agent,
     */
    public void init() {
        if (owner == null) {
            throw new SSTAFException("Owner has not been set");
        }
        injectAll(owner);
        features.values().forEach(feature -> {
            if (feature instanceof Handler) {
                Handler handler = (Handler) feature;
                handler.contentHandled().forEach(message -> contentToHandlerMap.put(message, handler));
                nameToHandlerMap.putIfAbsent(handler.getName(), handler);
            }
            if (feature instanceof Agent) {
                Agent agent = (Agent) feature;
                agents.add(agent);
            }
            feature.init();
        });
        initialized = true;
    }

    /**
     * Injects the provided {@code Object}s into the loaded {@code Provider}s
     *
     * @param items the items to inject
     */
    public void injectAll(final Object... items) {
        Injector.injectAll(this, items);
        features.values().forEach(provider -> Injector.injectAll(provider, items));
    }

    /**
     * Injects an Object into a named field
     *
     * @param attributeName  the name of the field
     * @param objectToInject the Object
     */
    public void injectNamed(final String attributeName, final Object objectToInject) {
        Injector.inject(this, attributeName, objectToInject);
        features.values().forEach(provider -> Injector.inject(provider, attributeName, objectToInject));
    }

    /**
     * Registers a Handler
     *
     * @param handler the Handler
     * @param <T>     the Handler's type.
     */
    public <T extends Handler> void register(final T handler) {
        Objects.requireNonNull(handler, "Handler cannot be null");
        register((Feature) handler);
        handler.contentHandled().forEach(message -> contentToHandlerMap.put(message, handler));
        nameToHandlerMap.putIfAbsent(handler.getName(), handler);
    }

    /**
     * Registers an Agent
     *
     * @param agent the Agent
     * @param <T>   the Agent's type
     */
    public <T extends Agent> void register(final T agent) {
        Objects.requireNonNull(agent, "Agent cannot be null");
        register((Handler) agent);
        agents.add(agent);
    }

    /**
     * Registers a Feature
     *
     * @param feature the Feature
     * @param <T>     the type of the Feature
     */
    public <T extends Feature> void register(final T feature) {
        Objects.requireNonNull(feature, "Feature must not be null");
        FeatureSpecification ss = FeatureSpecification.builder()
                .featureClass(feature.getClass())
                .featureName(feature.getName())
                .majorVersion(feature.getMajorVersion())
                .minorVersion(feature.getMinorVersion())
                .build();
        features.putIfAbsent(ss, feature);
    }

    public Optional<FeatureSpecification> getSpecificationForHandler(final Class<?> contentClass) {
        Handler handler = contentToHandlerMap.get(contentClass);
        if (handler == null) {
            return Optional.empty();
        } else {
            return Optional.of(FeatureSpecification.from(handler));
        }
    }
}

