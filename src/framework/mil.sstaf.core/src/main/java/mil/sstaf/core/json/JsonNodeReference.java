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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JsonNodeReference {


    private final JsonNode rootNode;
    private final Path directory;
    private final JsonNodeReference parent;
    private final String reference;
    private final Map<String, JsonNodeReference> cache;

    public JsonNodeReference(JsonNode node, String ref, JsonNodeReference parent) {
        Objects.requireNonNull(parent);
        this.rootNode = node;
        this.directory = parent.directory;
        this.parent = parent;
        this.reference = ref;
        this.cache = parent.cache;
    }

    public JsonNodeReference(JsonNode node, Path directory, Map<String, JsonNodeReference> cache) {
        this.rootNode = node;
        this.directory = directory;
        this.parent = null;
        this.reference = null;
        this.cache = cache;
    }

    public JsonNode getRootNode() {
        return rootNode;
    }

    public Path getDirectory() {
        return directory;
    }

    public JsonNodeReference getParent() {
        return parent;
    }

    public String getReference() {
        return reference;
    }

    List<JsonNodeReference> identifyReferences() {
        List<JsonNodeReference> nodeContexts = new ArrayList<>();
        var iter = rootNode.fields();
        while (iter.hasNext()) {
            var entry = iter.next();
            JsonNode node = entry.getValue();
            if (node.isTextual()) {
                String s = node.textValue();
                if (s.endsWith(".json")) {
                          nodeContexts.add(new JsonNodeReference(node, s, this));
                }
            }

        }
        return nodeContexts;
    }


}

