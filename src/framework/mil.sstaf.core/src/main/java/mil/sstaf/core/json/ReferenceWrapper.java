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
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public class ReferenceWrapper extends BaseNodeWrapper<JsonNode> {

    private final String ref;
    private Path pathToRef;

    public ReferenceWrapper(String ref, NodeWrapper parent, JsonNode node,
                            ObjectMapperFactory objectMapperFactory,
                            Map<String, JsonNode> referenceCache) {
        super(parent, node, objectMapperFactory, referenceCache);
        this.ref = ref;
        this.pathToRef = makeRegPath(ref);
        this.setDirectory(pathToRef.getParent());
    }

    public Path makeRegPath(String ref) {
        if (ref.startsWith("/")) {
            return Path.of(ref).normalize().toAbsolutePath();
        } else {
            return Path.of(getDirectory().toString(), ref).normalize().toAbsolutePath();
        }
    }

    @Override
    public void resolve() {

        if (parent.refCount(ref) == 0) {
            String pathString = pathToRef.toString();
            JsonNode replacementNode;
            if (cache.containsKey(pathString)) {
                replacementNode = cache.get(pathString);
            } else {
                replacementNode = loadAndResolve(pathString);
                cache.put(pathString, replacementNode);
            }
            parent.replaceNode(myNode, replacementNode);
            resolved = true;
        } else {
            throw new JsonResolutionException("Circular reference '" + ref + "'");
        }
    }

    private JsonNode loadAndResolve(String path) {
        try {
            Path p = Path.of(path);
            ObjectMapper objectMapper = objectMapperFactory.create(p);
            JsonNode replacementNode = objectMapper.readTree(p.toFile());
            processNode(replacementNode);
            return replacementNode;
        } catch (IOException ioException) {
            throw new JsonResolutionException("Could not read reference '"
                    + ref
                    + "', which was resolved to '"
                    + path + "'", ioException);
        }
    }

    @Override
    public int refCount(String refName) {
        Objects.requireNonNull(refName);
        return super.refCount(refName) + (refName.equals(ref) ? 1 : 0);
    }

    @Override
    public void replaceNode(JsonNode original, JsonNode replacement) {

    }

}

