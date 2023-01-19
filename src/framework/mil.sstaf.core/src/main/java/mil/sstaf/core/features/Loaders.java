package mil.sstaf.core.features;

import mil.sstaf.core.configuration.SSTAFConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.module.ModuleDescriptor;
import java.util.*;

/**
 * Static utilities for loading {@code Feature} services.
 */
public class Loaders {

    public static final String PRELOAD_PROPERTY = "sstaf.preloadFeatureClasses";
    private static final Logger logger = LoggerFactory.getLogger(Loaders.class);

    /**
     * Creates a new {@code FeatureLoader} parameterized to the required class type.
     *
     * @param type the {@code Class} of the desired service
     * @param <F>  the generic type
     * @return a new FeatureLoader
     */
    private static <F extends Feature> FeatureLoader<F> getFeatureLoader(Class<F> type, Object owner, ModuleLayer moduleLayer) {
        return new FeatureLoader<>(type, owner, moduleLayer);
    }

    /**
     * Creates a new {@code FeatureLoader} parameterized to the required class type.
     *
     * @param type the {@code Class} of the desired service
     * @param <F>  the generic type
     * @return a new FeatureLoader
     */
    private static <F extends Feature> FeatureLoader<F> getFeatureLoader(Class<F> type) {
        return new FeatureLoader<>(type, null, SSTAFConfiguration.getInstance().getRootLayer());
    }

    /**
     * Registers a class with the SSTAF loading system. This enables classes to be
     * loaded and used without using ServiceLoader. This is useful in testing, but
     * might also be used to eliminate dynamic loading to enable use of GraalVM.
     *
     * @param cls the type to load
     */
    public static void registerClass(Class<?> cls) {
        FeatureLoader.registerClass(cls);
    }

    /**
     * Loads a {@code Feature} using the specified {@code ServiceSpecification}
     *
     * @param <T>   the type of the service
     * @param clazz the {@code Class} of the service
     * @param spec  the {@code ServiceSpecification} that specifies which service is required
     * @return an {@code Optional} that will contain the service if a match was found.
     */
    public static <T extends Feature> Optional<T> load(final Class<T> clazz, final FeatureSpecification spec) {
        return getFeatureLoader(clazz).load(spec.featureName, spec.majorVersion, spec.minorVersion, spec.requireExact);
    }

    /**
     * Loads a {@code Feature} using the specified {@code ServiceSpecification} into a specific
     * {@link ModuleLayer}.
     *
     * @param <T>         the type of the service
     * @param clazz       the {@code Class} of the service
     * @param spec        the {@code ServiceSpecification} that specifies which service is required
     * @param moduleLayer the {@code ModuleLayer} into which to load the {@code Feature}.
     * @return an {@code Optional} that will contain the service if a match was found.
     */
    public static <T extends Feature> Optional<T> load(final Class<T> clazz, final FeatureSpecification spec, Object owner, ModuleLayer moduleLayer) {
        return getFeatureLoader(clazz, owner, moduleLayer).load(spec.featureName, spec.majorVersion, spec.minorVersion, spec.requireExact);
    }

    /**
     * Loads a {@code Feature} using the specified {@code ServiceSpecification}
     *
     * @param <T>          the specific type of the {@code Feature}
     * @param clazz        the {@code Class} of the service
     * @param modelName    the name of the service provided by its getName() method
     * @param majorVersion the major version of the service provided by its getMajorVersion() method
     * @param minorVersion the minor version of the service provided by its getMinorVersion method
     * @param requireExact whether the version of the found service must exactly match the specified version
     * @param moduleLayer  the {@link ModuleLayer} to use
     * @return the loaded Object or null if it could not be found.
     */
    public static <T extends Feature> T loadAsRef(final Class<T> clazz, final String modelName,
                                                  final int majorVersion, final int minorVersion,
                                                  boolean requireExact, ModuleLayer moduleLayer) {
        return getFeatureLoader(clazz, null, moduleLayer).loadRef(modelName, majorVersion, minorVersion, requireExact);
    }

    /**
     * Loads a {@code Feature} using the specified parameters, assumes that an exact match is not required.
     * <p>
     * Since an exact match is not required, any service with a matching name and a major and minor version pair
     * greater than the specified version will be used. The behavior when multiple matching services are found
     * on the module path is not defined.
     *
     * @param <T>          the type of the service
     * @param clazz        the {@code Class} of the service
     * @param modelName    the name of the service provided by its getName() method
     * @param majorVersion the major version of the service provided by its getMajorVersion() method
     * @param minorVersion the minor version of the service provided by its getMinorVersion method
     * @return an {@code Optional} that contains the matching Feature.
     */
    public static <T extends Feature> Optional<T> load(final Class<T> clazz, final String modelName,
                                                       final int majorVersion, final int minorVersion) {
        return getFeatureLoader(clazz).load(modelName, majorVersion, minorVersion, false);
    }

    /**
     * Loads a {@code Feature} using the specified parameters, assumes that an exact match is not required.
     * <p>
     * Since an exact match is not required, any service with a matching name and a major and minor version pair
     * greater than the specified version will be used. The behavior when multiple matching services are found
     * on the module path is not defined.
     *
     * @param <T>          the type of the service
     * @param clazz        the {@code Class} of the service
     * @param modelName    the name of the service provided by its getName() method
     * @param majorVersion the major version of the service provided by its getMajorVersion() method
     * @param minorVersion the minor version of the service provided by its getMinorVersion method
     * @param moduleLayer  the {@link ModuleLayer} to use
     * @return an {@code Optional} that contains the matching Feature.
     */
    public static <T extends Feature> Optional<T> load(final Class<T> clazz, final String modelName,
                                                       final int majorVersion, final int minorVersion, ModuleLayer moduleLayer) {
        return getFeatureLoader(clazz, null, moduleLayer).load(modelName, majorVersion, minorVersion, false);
    }

    /**
     * Loads a {@code Feature} using the specified parameters.
     *
     * @param <T>          the type of the service
     * @param clazz        the {@code Class} of the service
     * @param modelName    the name of the service provided by its getName() method
     * @param majorVersion the major version of the service provided by its getMajorVersion() method
     * @param minorVersion the minor version of the service provided by its getMinorVersion method
     * @param exactVersion whether the version of the found service must exactly match the specified version
     * @param moduleLayer  the {@code ModuleLayer} into which to load the {@code Feature}
     * @return an {@code Optional} that will contain the service if a match was found.
     */
    public static <T extends Feature> Optional<T> load(final Class<T> clazz, final String modelName,
                                                       final int majorVersion, final int minorVersion,
                                                       final boolean exactVersion, ModuleLayer moduleLayer) {
        return getFeatureLoader(clazz, null, moduleLayer).load(modelName, majorVersion, minorVersion, exactVersion);
    }

    /**
     * Utility for diagnosing service problems.
     *
     * @param fs          the {@code FeatureSpecification}
     * @param featureType the type of the {@code Feature}
     * @param <T>         the type of the service
     * @return a descriptive {@code String}
     */
    public static <T extends Feature> String getHelpWithServices(FeatureSpecification fs, Class<T> featureType) {

        return "SSTAF failed to load the specified service: '" + fs.toString()
                + " as an implementation of '" +
                featureType.getName() + "'\n" +
                "Check: \n" +
                "1. The jar containing the service is on the module path or the \n" +
                "   module and and its module path are specified in the SSTAFConfiguration\n" +
                "   or the Entity configuration file in which it is referenced.\n" +
                "2. The module-info specifies:\n   'provides " + featureType.getName() +
                " with NameOfTheImplementationClass'\n" +
                "3. The module-info specifies that it is open to mil.sstaf.core\n\n" +
                generateServiceReport(featureType);
    }


    /**
     * Prints all services matching the specified service type.
     *
     * @param serviceType the service to match
     * @param <T>         the service type.
     */
    public static <T extends Feature> String generateServiceReport(Class<T> serviceType) {
        StringBuilder sb = new StringBuilder("Found implementations of '").append(serviceType.getName()).append("'\n");
        ServiceLoader<T> sl = ServiceLoader.load(serviceType);
        for (Feature o : sl) {
            sb.append(String.format("%s %d.%d.%d\n",
                    o.getName(), o.getMajorVersion(),
                    o.getMinorVersion(), o.getPatchVersion()));
            Class<? extends Feature> cl = o.getClass();
            sb.append(String.format("    Implemented by: %s\n", cl.getName()));
            Module module = cl.getModule();
            sb.append(String.format("    Found in Module: %s\n", module.isNamed() ? module.getName() : "UNNAMED"));

            ModuleDescriptor desc = module.getDescriptor();

            sb.append("    Module exports:\n");
            for (ModuleDescriptor.Exports exp : desc.exports()) {
                sb.append("        ").append(exp.toString()).append("\n");
            }

            sb.append("    Module provides:\n");
            for (ModuleDescriptor.Provides provides : desc.provides()) {
                sb.append("        ").append(provides.toString()).append("\n");
            }

            sb.append("    Module requires:\n");
            for (ModuleDescriptor.Requires requires : desc.requires()) {
                sb.append("        ").append(requires.toString()).append("\n");
            }

        }
        return sb.toString();
    }

    /**
     * Instantiates and registers a Class using its name.
     *
     * @param className the name of the Class.
     * @throws ClassNotFoundException if the class is not found in the classpath.
     */
    public static void registerClass(final String className) throws ClassNotFoundException {
        registerClass(Class.forName(className));
    }

    /**
     * Preloads 1 or more classes by name
     *
     * @param classes a varargs array of class names
     * @throws ClassNotFoundException if the class was not found in the classpath
     */
    public static void preloadFeatureClasses(List<String> classes) throws ClassNotFoundException {
        Objects.requireNonNull(classes);
        if (Boolean.getBoolean(PRELOAD_PROPERTY)) {
            logger.debug("Preloading {} feature classes", classes.size());
            for (String s : classes) { // Using a loop rather than a lambda because of the exception
                logger.trace("Preloading {}", s);
                registerClass(s);
            }
        } else {
            logger.debug("Preloading disabled");
        }

    }

    /**
     * Preloads 1 or more classes by name
     *
     * @param classes a varargs array of class names
     * @throws ClassNotFoundException if the class was not found in the classpath
     */
    public static void preloadFeatureClasses(String... classes) throws ClassNotFoundException {
        Objects.requireNonNull(classes);
        preloadFeatureClasses(Arrays.asList(classes));
    }
}
