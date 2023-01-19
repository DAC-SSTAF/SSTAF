package mil.sstaf.core.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

public class ArrayNodeWrapper extends BaseNodeWrapper<ArrayNode> {


    protected ArrayNodeWrapper(NodeWrapper parent, ArrayNode node, ObjectMapperFactory objectMapperFactory, Map<Path, JsonNode> referenceCache) {
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
