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
import mil.sstaf.core.util.SSTAFException;

/**
 * Base interface for all Providers
 */
public interface Feature {

    /**
     * Provides the name of the service.
     *
     * @return the name of the service
     */
    String getName();

    /**
     * Provides a description of the service.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Provides the {@code Class} for the configuration object. This
     * is used to guide deserialization and ensure correct typing.
     * @return the {@code FeatureConfiguration}
     */
    Class<? extends FeatureConfiguration> getConfigurationClass();

    /**
     * Provides the major version of the service.
     *
     * @return the major version number
     */
    int getMajorVersion();

    /**
     * Provides the minor version number of the service
     *
     * @return the minor version number.
     */
    int getMinorVersion();

    /**
     * Provides the patch version number of the service
     *
     * @return the patch version number
     */
    int getPatchVersion();

    /**
     * Provides the EntityHandle for the owning Entity
     *
     * @return the owner's EntityHandle
     */
    EntityHandle getOwner();

    /**
     * Initializes this {@code Provider}
     *
     * @throws SSTAFException if an error occurs.
     */
    void init() throws SSTAFException;

    /**
     * Sets the configuration for this provider.
     * <p>
     * The configuration is applied when {@code init()} is invoked.
     */
    void configure(FeatureConfiguration configuration);

    /**
     * Returns whether the Feature has been configured.
     * <p>
     * Note that subclasses must call up through super.configure() to make this true
     *
     * @return true if the Feature has been configured.
     */
    boolean isConfigured();

    /**
     * Returns whether the Feature has been initialized.
     * <p>
     * Note that subclasses must call up through super.init() to make this true
     *
     * @return true if the Feature has been initialized.
     */
    boolean isInitialized();
}

