package mil.sstaf.core.json;

import com.fasterxml.jackson.databind.JsonNode;

import java.nio.file.Path;

/**
 * Interface for objects that wrap {@code JsonNode}s and help resolve {@code JsonNode}
 * graphs that contain references.
 */
public interface NodeWrapper {

    /**
     * Provides the {@code JsonNode} that is wrapped by this {@code NodeWrapper}
     * @return the {@code JSONNode}
     */
    JsonNode getMyNode();

    /**
     * Returns the whether the {@code NodeWrapper} and its child graph has been resolved.
     * @return True if the graph has been resolved, false otherwise.
     */
    boolean isResolved();

    /**
     * Provides the parent object
     * @return the parent.
     */
    NodeWrapper getParent();

    /**
     * Processes this {@code JsonNode}, creates {@code JSONWrapper}s for each one and resolves
     * any references.
     */
    void resolve();

    /**
     * Scans up the graph to determine the number of times the reference has been loaded. This
     * determines if the graph is cyclic. A count rather that a boolean is used to enable limited
     * cycles to be processed if desired, eventually.
     *
     * @param refName the name of the reference to check
     * @return the number of times the reference is found.
     */
    int refCount(String refName);

    void setDirectory(Path dir);

    /**
     * Returns the {@code Path} for the current working directory.
     * @return
     */
    Path getDirectory();

    /**
     * Replaces the original {@code JsonNode} with the replacement {@code JsonNode}
     * @param original the original {@code JsonNode}
     * @param replacement the {@code JsonNode} with which to replace it.
     */
    void replaceNode(JsonNode original, JsonNode replacement);
}
