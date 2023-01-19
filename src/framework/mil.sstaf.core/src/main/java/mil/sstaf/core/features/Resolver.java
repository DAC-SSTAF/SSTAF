package mil.sstaf.core.features;

import mil.sstaf.core.entity.EntityHandle;
import mil.sstaf.core.util.Injector;
import mil.sstaf.core.util.RNGUtilities;
import mil.sstaf.core.configuration.SSTAFConfiguration;
import mil.sstaf.core.util.SSTAFException;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static mil.sstaf.core.features.Loaders.getHelpWithServices;
import static mil.sstaf.core.features.Loaders.load;
import static mil.sstaf.core.util.ReflectionUtils.getAllFields;

/**
 * Recursively resolves {@code Feature} dependencies.
 */
public class Resolver {

    private static final Logger logger = LoggerFactory.getLogger(Resolver.class);
    private final Map<FeatureSpecification, Feature> featureCache;
    private final Map<String, ? extends FeatureConfiguration> configurations;
    private final RandomGenerator generator;
    private final EntityHandle owner;
    private final ModuleLayer moduleLayer;

    /**
     * Constructor
     *
     * @param featureCache   a cache of loaded Features
     * @param configurations the configuration map loaded from the Entity.
     * @param owner          an {@code EntityHandle} to the {@code Entity} that will use this {@code Feature}
     * @param seed           top-level seed for stochastic configurations
     */
    public Resolver(final Map<FeatureSpecification, Feature> featureCache,
                    final Map<String, ? extends FeatureConfiguration> configurations,
                    final EntityHandle owner,
                    final long seed,
                    final ModuleLayer moduleLayer) {
        this.featureCache = featureCache;
        this.configurations = configurations;
        this.owner = owner;
        this.generator = new MersenneTwister(seed);
        this.moduleLayer = moduleLayer;
    }

    /**
     * Easily creates a {@code Resolver} for tests
     *
     * @param configurations The configurations for the loaded {@code Features}
     * @return a new Resolver
     */
    public static Resolver makeTransientResolver(Map<String, ? extends FeatureConfiguration> configurations) {
        EntityHandle eh = EntityHandle.makeDummyHandle();
        ConcurrentMap<FeatureSpecification, Feature> m = new ConcurrentHashMap<>();
        return new Resolver(m, configurations, eh, System.currentTimeMillis(),
                SSTAFConfiguration.getInstance().getRootLayer());
    }

    /**
     * Easily creates a Resolver for tests
     *
     * @return a new Resolver with an empty configuration object.
     */
    public static Resolver makeTransientResolver() {
        return makeTransientResolver(Map.of());
    }


    /**
     * Loads a {@code Feature} and all of its dependencies from a specification
     * <p>
     * If the
     *
     * @param specification the specification for the {@code Feature} or {@code Handler} or {@code Agent}
     * @return the {@code Feature}
     */
    @SuppressWarnings("rawtypes")
    public Feature loadAndResolveDependencies(FeatureSpecification specification) {
        logger.debug("{} - Loading and resolving for {}", owner.getPath(), specification);
        Feature feature = findMatchInCache(specification, featureCache);
        if (feature == null) {
            logger.trace("{} - Feature not in cache, loading {}", owner.getPath(), specification);
            Optional<Feature> optionalFeature = load(Feature.class, specification, owner, moduleLayer);
            if (optionalFeature.isPresent()) {
                feature = optionalFeature.get();
                featureCache.put(FeatureSpecification.from(feature), feature);
                logger.debug("{} - Feature loaded and added to cache. {}", owner.getPath(), featureCache.keySet());
                resolveDependencies(feature);
            } else {
                String errorMessage = getHelpWithServices(specification, Feature.class);
                logger.error(errorMessage);
                throw new SSTAFException("Failed to load " +
                        specification + " as " + Feature.class);
            }
        }
        return feature;
    }

    /**
     * Resolves dependencies for an already-instantiated Feature
     *
     * @param feature the Feature for which dependencies must be loaded.
     */
    public void resolveDependencies(Feature feature) {
        logger.trace("Resolving dependencies for {}",
                feature.getClass().getName());
        var rv = loadRequiredServices(feature, featureCache);
        featureCache.putAll(rv);
        if (logger.isTraceEnabled()) {
            logger.trace("Contents of featureCache:");
            for (Map.Entry<FeatureSpecification, Feature> entry : featureCache.entrySet()) {
                logger.trace("    {} = {}", entry.getKey().toString(), entry.getValue().toString());
            }
        }
        logger.trace("{} - Configuring top-level Feature", feature.getName());
        Optional<? extends FeatureConfiguration> optConfig = getConfiguration(feature.getName());
        Class<? extends FeatureConfiguration> configClass = feature.getConfigurationClass();

        if (optConfig.isPresent()) {
            FeatureConfiguration featureConfiguration = optConfig.get();
            configureFeature(feature, configClass, featureConfiguration);
        } else {
            logger.info("No configuration was provided for {}", feature.getName());
        }
    }

    /**
     * Forces the {@code Feature} and {@code FeatureConfiguration} into agreement.
     *
     * @param feature       the {@code Feature} that is to be configured
     * @param clazz         the {@code Class} of the {@code FeatureConfiguration} object
     * @param configuration the {@code FeatureConfiguraion}
     * @param <T>           The type of the configuration
     */
    private <T extends FeatureConfiguration> void configureFeature(Feature feature,
                                                                   Class<? extends FeatureConfiguration> clazz,
                                                                   FeatureConfiguration configuration) {
        if (clazz.isAssignableFrom(configuration.getClass())) {
            long subSeed = RNGUtilities.generateSubSeed(generator);
            configuration.setSeed(subSeed);
            feature.configure(configuration);
        } else {
            String msg = String.format("The configuration provided for %s was a %s rather than the required %s",
                    feature.getName(), configuration.getClass().getName(),
                    clazz.getName());
            throw new SSTAFException(msg);
        }
    }

    /**
     * Retrieves a configuration for a particular Feature from the configuration map
     *
     * @param featureName the name of the {@code Feature}, used to select the configuration
     * @return the configuration or an empty JSONObject if a matching configuration is not found.
     */
    private Optional<? extends FeatureConfiguration> getConfiguration(final String featureName) {
        logger.trace("Getting configuration for {}", featureName);
        FeatureConfiguration configuration = configurations.get(featureName);
        return Optional.ofNullable(configuration);
    }

    /**
     * Scans all Fields looking for @Requires associated with Feature references
     *
     * @param target the Feature to examine
     * @return a List of Fields that are properly annotated.
     */
    private List<Field> getFieldsWithRequires(final Feature target) {
        logger.debug("{} - Scanning fields for Requires annotations on Feature types", target.getName());
        List<Field> requiresFields = new ArrayList<>();
        for (Field field : getAllFields(target.getClass())) {
            logger.trace("{} - Processing field {}", target.getName(), field.getName());

            if (field.isAnnotationPresent(Requires.class)) {
                logger.trace("{} - Processing Requires on field {}", target.getName(), field.getName());
                Class<?> rawClass = field.getType();

                if (Feature.class.isAssignableFrom(rawClass)) {
                    requiresFields.add(field);

                } else {
                    logger.debug("Field {} is marked @Requires but the type is not a Feature type", field.getName());
                }
            } else {
                logger.trace("{} - Field {} is not annotated", target.getName(), field.getName());
            }
        }
        logger.debug("{} - Found {} @Requires annotations, {}", target.getName(),
                requiresFields.size(), requiresFields);
        return requiresFields;
    }

    /**
     * Checks whether a field has been set
     *
     * @param target the Feature in which to look
     * @param field  the Field to check
     * @return true if it has been set or the Field cannot be accessed.
     */
    private boolean isFieldSet(Feature target, Field field) {
        try {
            boolean accessible = field.canAccess(target);
            if (!accessible) {
                field.setAccessible(true);
            }
            Object contents = field.get(target);
            field.setAccessible(accessible);
            return contents != null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return true;
        }
    }

    private Feature findMatchInCache(FeatureSpecification desired,
                                        Map<FeatureSpecification, Feature> cache) {
        if (logger.isTraceEnabled()) {
            logger.trace("Looking for match to spec {} in cache {}", desired, cache.keySet());
        }
        Feature match = null;
        for (Map.Entry<FeatureSpecification, Feature> entry : cache.entrySet()) {
            FeatureSpecification have = entry.getKey();
            if (desired.isSatisfiedBy(have)) {
                match = entry.getValue();
                break;
            }
        }
        return match;
    }

    /**
     * For a Feature, load all of its required services, recurse to fill in the dependencies.
     *
     * @param target the {@code Feature} to inject the services into.
     */
    private Map<FeatureSpecification, Feature>
    loadRequiredServices(Feature target, Map<FeatureSpecification, Feature> parentCache) {
        logger.trace("Loading required services for {}:{}",
                owner.getPath(), target.getName());
        Map<FeatureSpecification, Feature> myCache = new HashMap<>(parentCache);

        for (Field field : getFieldsWithRequires(target)) {
            logger.trace("{}:{} - Processing Requires field {}",
                    owner.getPath(), target.getName(), field.getName());

            Requires requires = field.getAnnotation(Requires.class);
            Class<?> rawClass = field.getType();

            if (isFieldSet(target, field)) {
                logger.trace("{}:{} - Skipping field {} because it is already set",
                        owner.getPath(), target.getName(), field.getName());
            } else {
                var clazz = rawClass.asSubclass(Feature.class);
                FeatureSpecification spec = FeatureSpecification.from(requires, clazz);

                Feature toInject = findMatchInCache(spec, myCache);
                if (toInject != null) {
                    logger.debug("{}:{}/{} Found match in cache, reusing {}",
                            owner.getPath(), target.getName(), field.getName(), spec);
                } else {
                    logger.debug("{}:{}/{} Getting new instantiation of {}",
                            owner.getPath(), target.getName(), field.getName(), spec);
                    final Feature newlyLoaded = Loaders.loadAsRef(clazz, spec.featureName, spec.majorVersion,
                            spec.minorVersion, spec.requireExact, moduleLayer);

                    if (newlyLoaded == null) {
                        String identifier = spec.featureName == null || spec.featureName.length() == 0
                                ? spec.featureClass.getName() : spec.featureName;
                        throw new SSTAFException("In "
                                + owner.getPath()
                                + ", Resolver could not load an implementation for '"
                                + identifier + "' into feature '" + target.getName()
                                + "', field '" + field.getName() + "'");
                    }

                    //
                    // Since this is a new load, recurse to fill it in.
                    //
                    FeatureSpecification fs = FeatureSpecification.from(newlyLoaded);
                    myCache.put(fs, newlyLoaded);
                    logger.trace("{}:{} Registered {} in cache, cache now holds {} entries",
                            owner.getPath(), target.getName(), fs, myCache.size());

                    logger.trace("{}:{} - Recursing now for {}",
                            owner.getPath(), target.getName(), newlyLoaded.getName());
                    var childCache = loadRequiredServices(newlyLoaded, myCache);
                    myCache.putAll(childCache);
                    logger.trace("{}:{} - Back from processing {}, cache now holds {} entries",
                            owner.getPath(), target.getName(), newlyLoaded.getName(), myCache.size());

                    //
                    // Inject the ownerHandle into the feature. This must be done before configuration
                    // or an NPE might occur. Some feature loggers reference owner paths.
                    //
                    logger.trace("{}:{} - Injecting owner handle {}",
                            owner.getPath(), target.getName(), owner.getPath());
                    Injector.inject(newlyLoaded, owner);
                    //
                    // TODO
                    //
                    Class<? extends FeatureConfiguration> fc = newlyLoaded.getConfigurationClass();

                    Optional<? extends FeatureConfiguration> optConfig = getConfiguration(newlyLoaded.getName());

                    optConfig.ifPresentOrElse(configuration -> {
                                logger.trace("{}:{} - Configuring {} with {}",
                                        owner.getPath(), target.getName(), newlyLoaded.getName(),
                                        configuration);
                                configureFeature(newlyLoaded, fc, configuration);
                            },
                            () -> {
                                logger.debug("{}:{} - No configuration provided for {}, using default",
                                        owner.getPath(), target.getName(), newlyLoaded.getName());

                                try {
                                    Constructor<? extends FeatureConfiguration> constructor = fc.getConstructor();
                                    FeatureConfiguration config = constructor.newInstance();
                                    configureFeature(newlyLoaded, fc, config);

                                } catch (NoSuchMethodException |
                                        InvocationTargetException |
                                        InstantiationException |
                                        IllegalAccessException e) {
                                    e.printStackTrace();
                                }


                            }

                    );

                    toInject = newlyLoaded;
                }
                //
                // Inject the service into the field.
                //
                logger.debug("{} - Injecting {} into field {}",
                        target.getName(), toInject.getName(), field.getName());
                Injector.injectField(target, field, toInject);
            }
        }
        return myCache;
    }

    /**
     * Scans the cache of loaded {@code Feature}s and returns those Features that match the
     * provided {@code Class}.
     *
     * @param clazz the Feature Class to match
     * @return a list of found Features
     */
    public <T extends Feature> List<T> getServicesFromCache(Class<? extends T> clazz) {
        List<T> found = new ArrayList<>();
        for (Feature f : featureCache.values()) {
            if (clazz.isAssignableFrom(f.getClass())) {
                found.add(clazz.cast(f));
            }
        }
        return found;
    }
}
