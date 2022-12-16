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

import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.json.JsonLoader;
import mil.sstaf.core.util.SSTAFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

/**
 * Describes a Feature.
 * <p>
 * Used to specify which Feature to use.
 */
@SuperBuilder
@Jacksonized
public final class FeatureSpecification {
    private static final Logger logger = LoggerFactory.getLogger(FeatureSpecification.class);

    public final Class<? extends Feature> featureClass;
    public final String featureName;
    public final int majorVersion;
    public final int minorVersion;
    public final boolean requireExact;

    private FeatureSpecification(FeatureSpecificationBuilder<?, ?> builder) {
        if (builder.featureName == null) {
            throw new SSTAFException("featureName was null in FeatureSpecification");
        }
        this.featureClass = builder.featureClass;
        this.featureName = builder.featureName;
        this.majorVersion = builder.majorVersion;
        this.minorVersion = builder.minorVersion;
        this.requireExact = builder.requireExact;
    }

    /**
     * Creates a {@code FeatureSpecification} from a requirement.
     *
     * @param requires      the {@code Requires} annotation associated with the target field
     * @param providerClass the type of the target field.
     * @return a new {@code FeatureSpecification} matching the requirement specified by the Requires
     */
    public static FeatureSpecification from(final Requires requires, Class<? extends Feature> providerClass) {
        return builder().featureName(requires.name())
                .majorVersion(requires.majorVersion())
                .minorVersion(requires.minorVersion())
                .featureClass(providerClass)
                .build();
    }

    /**
     * Creates a {@code FeatureSpecification} from an instantiated {@code Feature}
     *
     * @param f the {@code Feature}
     * @return a new {@code FeatureSpecification}
     */
    public static FeatureSpecification from(Feature f) {
        return builder().featureClass(f.getClass())
                .featureName(f.getName())
                .majorVersion(f.getMajorVersion())
                .minorVersion(f.getMinorVersion())
                .build();
    }

    /**
     * Answers whether the supplied specification meets the the specification required by this
     * {@code FeatureSpecification}
     *
     * @param candidate the {@code FeatureSpecification} to test
     * @return true if is satisfies the requirement, false otherwise.
     */
    public boolean isSatisfiedBy(final FeatureSpecification candidate) {
        boolean namesOK;
        boolean classesOK;
        boolean versionsEqual;
        boolean versionSufficient;

        if (logger.isTraceEnabled()) {
            logger.trace("Checking if {} satisfies {}", candidate, this);
        }

        if (candidate == null) {
            return false;
        } else if (this.equals(candidate)) {
            return true;
        } else {
            classesOK = featureClass == null || featureClass.isAssignableFrom(candidate.featureClass);
            namesOK = featureName.isEmpty() || featureName.equals(candidate.featureName);
            versionsEqual = majorVersion == candidate.majorVersion && minorVersion == candidate.minorVersion;
            versionSufficient = candidate.majorVersion > majorVersion ||
                    (candidate.majorVersion == majorVersion && candidate.minorVersion >= minorVersion);
        }

        boolean answer = classesOK && namesOK && (this.requireExact ? versionsEqual : versionSufficient);

        if (logger.isTraceEnabled()) {
            logger.trace("classesOk = {} namesOK = {} versionsEqual = {} versionSufficient = {} requireExact = {} answer = {}",
                    classesOK, namesOK, versionsEqual, versionSufficient, requireExact, answer);
        }
        return answer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return (featureClass == null ? "" : "(class " + featureClass.getSimpleName() + ") ")
                + featureName + " " + majorVersion + "." + minorVersion + "."
                + (requireExact ? " [EXACT]" : "");
    }

    public static FeatureSpecification from(File file) {
        JsonLoader jsonLoader = new JsonLoader();
        return jsonLoader.load(Path.of(file.getPath()),
                FeatureSpecification.class);
    }

    public static FeatureSpecification from(String json, Path sourceDir) {
        JsonLoader jsonLoader = new JsonLoader();
        return jsonLoader.load(json, FeatureSpecification.class, sourceDir);
    }

    public static FeatureSpecification from(JsonNode jsonNode, Path sourceDir) {
        JsonLoader jsonLoader = new JsonLoader();
        return jsonLoader.load(jsonNode, FeatureSpecification.class, sourceDir);
    }
}

