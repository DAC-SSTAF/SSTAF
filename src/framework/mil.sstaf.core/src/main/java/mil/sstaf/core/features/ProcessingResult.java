package mil.sstaf.core.features;

import mil.sstaf.core.entity.EntityEvent;
import mil.sstaf.core.entity.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * The result generated by {@code Handler}s and {@code Agent}s
 */
public class ProcessingResult {
    private static final ProcessingResult EMPTY_RESULT = new ProcessingResult();
    public final long nextEventTime_ms;
    public final List<Message> messages;

    /**
     * Private constructor
     *
     * @param messages a list of {@code Message}s for distribution to other {@code Entity} objects,
     */
    private ProcessingResult(List<Message> messages) {
        this.messages = messages;

        long mt = Long.MAX_VALUE;
        for (Message msg : messages) {
            if (msg instanceof EntityEvent) {
                EntityEvent event = (EntityEvent) msg;
                mt = Long.min(mt, event.getEventTime_ms());
            }
        }
        nextEventTime_ms = mt;
    }

    /**
     * Private constructor for an empty {@code ProcessingResult}
     */
    private ProcessingResult() {
        this.messages = List.of();
        nextEventTime_ms = Long.MAX_VALUE;
    }

    /**
     * Factory method for making a ProcessingResult
     *
     * @param messages a list of {@code Message}s for distribution to this or other {@code Entity} objects.
     * @return a new ProcessingResult
     */
    public static ProcessingResult of(final List<Message> messages) {
        return new ProcessingResult(messages);
    }

    /**
     * Factory method for making a ProcessingResult for a single {@code Message}
     *
     * @param message a single {@code Message} to be distributed to this or another {@code Entity} object.
     * @return a new ProcessingResult
     */
    public static ProcessingResult of(final Message message) {
        return new ProcessingResult(List.of(message));
    }

    /**
     * Merge multiple {@code ProcessingResult}s into a single result.
     *
     * @param results the {@code ProcessingReuslt}s to merge
     * @return a new unified {@code ProcessingResult}
     */
    public static ProcessingResult merge(final List<ProcessingResult> results) {
        List<Message> accumulator = new ArrayList<>(results.size());
        results.forEach(pr -> {
            if (pr != null) {
                accumulator.addAll(pr.messages);
            }
        });
        return ProcessingResult.of(accumulator);
    }

    /**
     * Creates an empty {@code ProcessingResult}
     *
     * @return a new empty {@code ProcessingResult}
     */
    public static ProcessingResult empty() {
        return EMPTY_RESULT;
    }
}