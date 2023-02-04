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

    /**
     * Returns whether the feature requires explicit configuration
     * @return true if a configuration must be provided.
     */
    boolean featureRequiresConfiguration();
}
