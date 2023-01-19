package mil.sstaf.core.features;

/**
 * Interface for classes that should be triggered on every tick regardless
 * of whether or not a message was received.
 */
public interface Agent
        extends Handler {
    /**
     * Activate the Agent to perform a function at the specified time.
     *
     * @param currentTime_ms the simulation time.
     * @return a {@code ProcessingResult} containing internal and external {@code Message}s
     */
    ProcessingResult tick(final long currentTime_ms);
}

