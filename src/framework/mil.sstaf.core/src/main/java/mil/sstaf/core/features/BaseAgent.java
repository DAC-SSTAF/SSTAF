package mil.sstaf.core.features;

/**
 * Base class for {@code Agent}s.
 */
public abstract class BaseAgent
        extends BaseHandler
        implements Agent {

    /**
     * Constructor for subclasses.
     * <p>
     * Note that concrete implementations must have a no-args constructor to be loadable as services
     *
     * @param featureName           the name of this {@code Feature}
     * @param majorVersion          the major version number
     * @param minorVersion          the minor version number
     * @param patchVersion          the patch version number
     * @param requiresConfiguration whether or not the Handler must be provided a configuration to make it valid.
     * @param description           a more verbose description of the {@code Agent}
     */
    protected BaseAgent(String featureName, int majorVersion, int minorVersion, int patchVersion,
                        boolean requiresConfiguration, String description) {
        super(featureName, majorVersion, minorVersion, patchVersion, requiresConfiguration, description);
    }
}
