package mil.sstaf.core.features;

import mil.sstaf.core.entity.Address;
import mil.sstaf.core.entity.ErrorResponse;
import mil.sstaf.core.entity.Message;
import mil.sstaf.core.entity.MessageResponse;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 * Base class for {@code Handler}s and {@code Agent}s.
 */
public abstract class BaseHandler
        extends BaseFeature
        implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(BaseHandler.class);

    private Address address = null;

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
     * @param description           a more verbose description of the {@code Feature}
     */
    protected BaseHandler(String featureName, int majorVersion, int minorVersion, int patchVersion, boolean requiresConfiguration, String description) {
        super(featureName, majorVersion, minorVersion, patchVersion, requiresConfiguration, description);
    }

    @Override
    public void configure(FeatureConfiguration configuration) {
        super.configure(configuration);
    }

    /**
     * Provides the full Address for the Handler
     *
     * @return the Handler's Address
     */
    @Override
    public Address getAddress() {
        return address;
    }

    /**
     * Provides a descriptive String that fully identifies the Handler
     *
     * @return a descriptive String
     */
    @Override
    public String getInfoString() {
        String path = ownerHandle == null ? "null" : ownerHandle.getPath();
        String entityName = ownerHandle == null ? "null" : ownerHandle.getName();
        long id = ownerHandle == null ? -99999 : ownerHandle.getId();
        String handlerName = getName();
        return path + "[" + entityName + "|" + id + "][" + handlerName + "]";
    }

    /**
     * {@inheritDoc}
     */
    public void init() {
        super.init();
        if (ownerHandle == null) {
            throw new IllegalStateException("Owner handle has not been injected");
        } else {
            address = Address.makeAddress(ownerHandle, featureName);
        }
        logger.trace("{}/BaseHandler initialized", featureName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Class<? extends HandlerContent>> contentHandled() {
        return List.of();
    }

    /**
     * Creates a {@link ProcessingResult} that reports that the message is not supported.
     *
     * @param unsupported the message content class that is not supported by this {@code Handler}
     * @param sourceID    the id number of the original message
     * @param destination to where this message should go
     * @param exception   an exception that captures the stack trace from the handler
     * @return a {@code ProcessingResult}
     */
    protected ProcessingResult buildUnsupportedMessageResponse(final Object unsupported,
                                                               final long sourceID,
                                                               final Address destination,
                                                               final Throwable exception) {
        Objects.requireNonNull(unsupported, "unsupported");
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(exception, "exception");
        String desc = "Message class '" + unsupported.getClass() + "' is not supported.";
        var b = ErrorResponse.builder()
                .source(Address.makeAddress(this.ownerHandle, this.getName()))
                .destination(destination)
                .messageID(sourceID)
                .sequenceNumber(this.ownerHandle.getMessageSequenceNumber())
                .content(ExceptionContent.builder().thrown(exception).build())
                .errorDescription(desc);
        Message out = b.build();
        logger.trace("Entity {} sending {}", ownerHandle.getName(), out);
        return ProcessingResult.of(out);
    }

    /**
     * Builds a Message to report an exception or error.
     *
     * @param message     the error message
     * @param exception   the exception
     * @param sourceID    the ID number of the message that caused the exception.
     * @param destination the {@code Entity} to which to send the message
     * @return a new Message
     */
    protected Message buildErrorResponse(final String message, final Throwable exception, final long sourceID,
                                         final Address destination) {
        Objects.requireNonNull(destination, "Destination address must not be null");
        var b = ErrorResponse.builder()
                .source(Address.makeAddress(this.ownerHandle, this.getName()))
                .destination(destination)
                .messageID(sourceID)
                .sequenceNumber(this.ownerHandle.getMessageSequenceNumber())
                .content(ExceptionContent.builder().thrown(exception).build())
                .errorDescription(message);
        Message out = b.build();
        logger.trace("Entity {} sending {}", ownerHandle.getName(), out);
        return out;
    }

    /**
     * Builds a Message to report the successful result of handling a message.
     *
     * @param response    the contents of the message
     * @param sourceID    the ID number of the message that caused the exception.
     * @param destination the {@code Entity} to which to send the message
     * @return a new Message
     */
    protected Message buildNormalResponse(HandlerContent response, final long sourceID, final Address destination) {
        Objects.requireNonNull(destination, "Destination address must not be null");
        var b = MessageResponse.builder()
                .source(Address.makeAddress(this.ownerHandle, this.getName()))
                .destination(destination)
                .messageID(sourceID)
                .sequenceNumber(this.ownerHandle.getMessageSequenceNumber())
                .content(response);
        Message out = b.build();
        logger.trace("Entity {} sending {}", ownerHandle.getName(), out);
        return out;
    }
}
