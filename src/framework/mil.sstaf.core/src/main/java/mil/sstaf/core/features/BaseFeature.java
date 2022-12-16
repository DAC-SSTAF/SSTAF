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

import mil.sstaf.core.entity.EntityHandle;
import mil.sstaf.core.util.Injected;
import mil.sstaf.core.util.SSTAFException;
import mil.sstaf.core.util.Validation;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Objects;

/**
 * Base class for implementation of {@code Feature}.
 */
public abstract class BaseFeature implements Feature {

    private static final Logger logger = LoggerFactory.getLogger(BaseFeature.class);

    protected final String featureName;
    protected final int majorVersion;
    protected final int minorVersion;
    protected final int patchVersion;
    protected final String description;
    protected final boolean requiresConfiguration;
    @Injected
    protected EntityHandle ownerHandle = null;
    protected boolean initialized = false;
    protected boolean configured = false;

    /**
     * Constructor for subclasses.
     * <p>
     * Note that concrete implementations must have a no-args constructor to be loadable as services
     *
     * @param featureName           the name of this {@code Feature}
     * @param majorVersion          the major version number
     * @param minorVersion          the minor version number
     * @param patchVersion          the patch version number
     * @param requiresConfiguration indicates whether a configuration must be provided for this Feature to be valid
     * @param description           a more verbose description of the {@code Feature}
     */
    protected BaseFeature(String featureName, int majorVersion, int minorVersion, int patchVersion,
                          boolean requiresConfiguration, String description) {
        this.featureName = Objects.requireNonNull(featureName, "Feature name was null");
        this.description = Objects.requireNonNull(description, "Feature description was null");
        this.majorVersion = Validation.require(majorVersion, val -> val >= 0, "Major version was < 0");
        this.minorVersion = Validation.require(minorVersion, val -> val >= 0, "Minor version was < 0");
        this.patchVersion = Validation.require(patchVersion, val -> val >= 0, "Patch version was < 0");
        this.requiresConfiguration = requiresConfiguration;
        logger.trace("Created {}", this);
    }

    /**
     * Provides a FeatureConfiguration
     * @return the default
     */
    @Override
    public Class<? extends FeatureConfiguration> getConfigurationClass() {
        return FeatureConfiguration.class;
    }

    /**
     * Provides the EntityHandle for the owning Entity
     *
     * @return the owner's EntityHandle
     */
    @Override
    public EntityHandle getOwner() {
        return ownerHandle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return featureName + " "
                + majorVersion + "." + minorVersion + "." + patchVersion
                + " [" + description + "]";
    }

    /**
     * Provides the name of the service.
     *
     * @return the name of the service
     */
    @Override
    public String getName() {
        return featureName;
    }

    /**
     * Provides a description of the service.
     *
     * @return the description
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Provides the major version of the service.
     *
     * @return the major version number
     */
    @Override
    public int getMajorVersion() {
        return majorVersion;
    }

    /**
     * Provides the minor version number of the service
     *
     * @return the minor version number.
     */
    @Override
    public int getMinorVersion() {
        return minorVersion;
    }

    /**
     * Provides the patch version number of the service
     *
     * @return the patch version number
     */
    @Override
    public int getPatchVersion() {
        return patchVersion;
    }

    /**
     * Initializes this {@code Feature}
     *
     * @throws SSTAFException if an error occurs.
     */
    @Override
    public void init() throws SSTAFException {
        if (requiresConfiguration && !configured) {
            String ownerString = ownerHandle == null ? "Unknown" : ownerHandle.getPath();
            throw new IllegalStateException(ownerString + ":" + featureName + " was not configured");
        }
        initialized = true;
        logger.trace("{}/BaseFeature initialized", featureName);
    }

    /**
     * Sets the configuration for this provider.
     * The configuration is applied when {@code init()} is invoked.
     *
     * @param configuration The configuration for the {@code Feature}
     */
    @Override
    public void configure(FeatureConfiguration configuration) {
        configured = true;
    }

    /**
     * Returns whether the Feature has been configured.
     * <p>
     * Note that subclasses must call up through super.configure() to make this true
     *
     * @return true if the Feature has been configured.
     */
    @Override
    public boolean isConfigured() {
        return configured;
    }

    /**
     * Returns whether the Feature has been initialized.
     * <p>
     * Note that subclasses must call up through super.init() to make this true
     *
     * @return true if the Feature has been initialized.
     */
    @Override
    public boolean isInitialized() {
        return initialized;
    }

}

