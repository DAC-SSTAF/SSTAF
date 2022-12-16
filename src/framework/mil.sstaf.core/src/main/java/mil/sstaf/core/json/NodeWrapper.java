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

