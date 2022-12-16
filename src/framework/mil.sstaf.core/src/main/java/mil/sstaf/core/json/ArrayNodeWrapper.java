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
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.Iterator;
import java.util.Map;

public class ArrayNodeWrapper extends BaseNodeWrapper<ArrayNode> {


    protected ArrayNodeWrapper(NodeWrapper parent, ArrayNode node, ObjectMapperFactory objectMapperFactory, Map<String, JsonNode> referenceCache) {
        super(parent, node, objectMapperFactory, referenceCache);
    }

    @Override
    public void resolve() {
        Iterator<JsonNode> iter = myNode.elements();
        while (iter.hasNext()) {
            JsonNode value = iter.next();
           processNode(value);
        }
    }

    @Override
    public void replaceNode(JsonNode original, JsonNode replacement) {
        int index = -1;
        int i = 0;
        Iterator<JsonNode> iter = myNode.elements();
        while (iter.hasNext()) {
            JsonNode value = iter.next();
            if (value.equals(original)) {
                index = i;
                break;
            }
            ++i;
        }

        if (index < 0) {
            throw new JsonResolutionException("Failed to find original node [" + original.toString()  +
                    "] in list [" + myNode.toString() + "]");
        } else {
            myNode.set(index, replacement);
        }
    }
}

