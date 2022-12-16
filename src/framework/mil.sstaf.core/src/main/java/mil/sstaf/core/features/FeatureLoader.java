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

package mil.sstaf.core.features;


import mil.sstaf.core.util.SSTAFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Loads SSTAF features.
 *
 * <p>
 * SSTAF {@code Features} are dynamically loaded by the SSTAF core.
 * </p>
 */
final class FeatureLoader<F extends Feature> {

    private static final Logger logger = LoggerFactory.getLogger(FeatureLoader.class);
    private static final Set<Class<?>> registeredClasses = new HashSet<>();
    private final Class<F> type;
    private final List<Class<? extends F>> candidates;
    private final ModuleLayer moduleLayer;
    private final Object owner;

    /**
     * Instantiates a new {@code FeatureLoader}
     *
     * @param type  the type of the Feature
     * @param owner the owning object, used to determine the targeted Module.
     * @param layer the {@link ModuleLayer} into which to load the plugins.
     */
    FeatureLoader(final Class<F> type, Object owner, ModuleLayer layer) {
        this.type = type;
        this.moduleLayer = layer;
        this.owner = owner;
        //
        // Add a 'uses' relationship to the mil.sstaf.core module if necessary
        //
        this.getClass().getModule().addUses(type);

        if (owner != null) {
            owner.getClass().getModule().addUses(type);
        }

        candidates = new ArrayList<>(registeredClasses.size());
        for (Class<?> cls : registeredClasses) {
            if (type.isAssignableFrom(cls)) {
                candidates.add(cls.asSubclass(type));
            }
        }
    }

    /**
     * Registers a class with the SSTAF loading system. This enables classes to be
     * loaded and used without using ServiceLoader. This is useful in testing, but
     * might also be used to eliminate dynamic loading to enable use of GraalVM.
     *
     * @param cls the type to load
     */
    synchronized static void registerClass(Class<?> cls) {
        registeredClasses.add(cls);
    }

    /**
     * Clears the registered classes
     */
    synchronized static void clearClassRegistry() {
        registeredClasses.clear();
    }

    /**
     * Returns an unmodifiable view on the registered classes set
     *
     * @return the registered classes
     */
    synchronized static Set<Class<?>> getRegisteredClasses() {
        return Collections.unmodifiableSet(registeredClasses);
    }

    /**
     * Compares a {@code Feature} against a requirement
     *
     * @param candidate    the {@code Feature} object to be evaluated
     * @param modelName    the required model name
     * @param majorVersion the required major version number
     * @param minorVersion the required minor version number
     * @param exactVersion whether the version comparison requires an exact match or
     *                     the comparison will also accept newer versions.
     * @return whether the candidate meets the requirements
     */
    static <F extends Feature> boolean meetsRequirements(F candidate, final String modelName,
                                                         final int majorVersion,
                                                         final int minorVersion,
                                                         final boolean exactVersion) {
        if (candidate == null) {
            return false;
        }

        String candidateName = candidate.getName();
        int candidateMajorVersion = candidate.getMajorVersion();
        int candidateMinorVersion = candidate.getMinorVersion();

        boolean namesOK = modelName.equals("") ||
                (candidateName != null && candidateName.equals(modelName));
        boolean versionsEquals = candidateMajorVersion == majorVersion && candidateMinorVersion == minorVersion;
        boolean versionSufficient = candidateMajorVersion > majorVersion ||
                (candidateMajorVersion == majorVersion && candidateMinorVersion >= minorVersion);

        if (exactVersion) {
            return namesOK && versionsEquals;
        } else {
            return namesOK && versionSufficient;
        }
    }

    /**
     * Compares the version of the first Feature with the version of the second Feature
     * <p>
     * The behavior is that the value returned is negative if the first argument is "better"
     * and positive if the second is "better." Since better means higher version numbers,
     * regular numeric comparisons must be flipped.
     *
     * @param first  the first Feature
     * @param second the second Feature
     * @return a negative value if the first Feature is newer, a positive value if the second
     * Feature is newer, zero if they are equivalent.
     */
    static <F extends Feature> int compareVersions(final F first, final F second) {
        int major = -(Integer.compare(first.getMajorVersion(), second.getMajorVersion()));
        if (major == 0) {
            int minor = -(Integer.compare(first.getMinorVersion(), second.getMinorVersion()));
            if (minor == 0) {
                return -(Integer.compare(first.getPatchVersion(), second.getPatchVersion()));
            } else {
                return minor;
            }
        } else {
            return major;
        }
    }

    /**
     * Traverses an {@code Iterable} to find the {@code Feature} that best matches the
     * specification.
     *
     * @param candidates   the {@code Iterable} of Features to evaluate
     * @param modelName    the name of the Feature provided by its getName() method
     * @param majorVersion the major version of the Feature provided by its getMajorVersion() method
     * @param minorVersion the minor version of the Feature provided by its getMinorVersion method
     * @param exactVersion states whether the version of the found service must exactly match the specified version
     * @return the selected Feature or null if none met the requirements.
     */
    static <F extends Feature> F findBestMatch(Iterable<F> candidates, F bestSoFar,
                                               final String modelName,
                                               final int majorVersion, final int minorVersion,
                                               final boolean exactVersion) {

        F selected = bestSoFar;
        for (F candidate : candidates) {
            if (meetsRequirements(candidate, modelName, majorVersion, minorVersion, exactVersion)) {
                if (exactVersion) {
                    return candidate;
                } else if (selected == null) {
                    selected = candidate;
                    continue;
                }
                selected = compareVersions(selected, candidate) <= 0 ? selected : candidate;
            }
        }
        return selected;
    }

    /**
     * Loads a {@code Feature} using the specified parameters.
     * <p>
     * Method executes a series of strategies for loading the desired Feature. The best
     * matching feature, if any, is returned.
     *
     * @param modelName    the name of the service provided by its getName() method
     * @param majorVersion the major version of the service provided by its getMajorVersion() method
     * @param minorVersion the minor version of the service provided by its getMinorVersion method
     * @param exactVersion whether the version of the found service must exactly match the specified version
     * @return a reference to the loaded object
     */
    F loadRef(final String modelName,
              final int majorVersion,
              final int minorVersion,
              final boolean exactVersion) {
        try {
            List<F> instances = generateFeatureInstances();
            F bestYet = findBestMatch(instances, null, modelName, majorVersion,
                    minorVersion, exactVersion);

            ServiceLoader<F> loader = ServiceLoader.load(moduleLayer, type);
            bestYet = findBestMatch(loader, bestYet, modelName, majorVersion,
                    minorVersion, exactVersion);

//            loader = ServiceLoader.load(type);
//            bestYet = findBestMatch(loader, bestYet, modelName, majorVersion,
//                    minorVersion, exactVersion);

            loader = ServiceLoader.load(type, Thread.currentThread().getContextClassLoader());
            bestYet = findBestMatch(loader, bestYet, modelName, majorVersion,
                    minorVersion, exactVersion);

            loader = ServiceLoader.loadInstalled(type);
            bestYet = findBestMatch(loader, bestYet, modelName, majorVersion,
                    minorVersion, exactVersion);

            return bestYet;

        } catch (InstantiationException |
                InvocationTargetException |
                NoSuchMethodException |
                IllegalAccessException |
                ServiceConfigurationError e) {
            String name;
            if (this.owner instanceof Feature) {
                name = ((Feature)owner).getName()+" (" + owner.getClass().getName() + ")";
            } else {
                name = owner.getClass().getName();
            }
            logger.error("In {}, could not load service {}, {}, {}", name, modelName, majorVersion, minorVersion);
            logger.error("{}", e.getMessage());
            e.printStackTrace(System.err);
            throw new SSTAFException(e);
        }
    }

    /**
     * Loads a {@code Feature} using the specified parameters.
     * <p>
     * Method executes a series of strategies for loading the desired Feature. The best
     * matching feature, if any, is returned.
     *
     * @param modelName    the name of the service provided by its getName() method
     * @param majorVersion the major version of the service provided by its getMajorVersion() method
     * @param minorVersion the minor version of the service provided by its getMinorVersion method
     * @param exactVersion whether the version of the found service must exactly match the specified version
     * @return an {@code Optional} that will contains the service.
     */
    Optional<F> load(final String modelName,
                     final int majorVersion,
                     final int minorVersion,
                     final boolean exactVersion) {
        return Optional.ofNullable(loadRef(modelName, majorVersion, minorVersion, exactVersion));
    }

    /**
     * Creates instances of each candidate class so that the properties of the Feature
     * can be evaluated for suitability
     *
     * @return a {@code List} containing Features instantiated for each oif the candidate classes
     * @throws NoSuchMethodException     if a Class does not have a no-arg construction
     * @throws IllegalAccessException    if permissions are wrong
     * @throws InvocationTargetException if reflection goes wrong
     * @throws InstantiationException    if newInstance doesn't work
     */
    List<F> generateFeatureInstances() throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {

        List<F> instances = new ArrayList<>(candidates.size());
        for (Class<? extends F> cls : candidates) {
            Constructor<? extends F> constructor = cls.getConstructor();
            F instance = constructor.newInstance();
            instances.add(instance);
        }
        return instances;
    }

}
