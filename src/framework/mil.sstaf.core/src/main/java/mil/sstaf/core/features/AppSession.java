package mil.sstaf.core.features;

import java.io.IOException;

/**
 * Base interface for interacting with an instance of an external helper application.
 */
public interface AppSession extends AutoCloseable{

    /**
     * Sends a message of type {@code P} to the helper and retrieves the response
     * @param cmd the command to send
     * @return the response from the helper
     * @throws IOException if communication fails
     */
    String invoke(String cmd) throws IOException;

}
