package mil.sstaf.core.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ObjectNodeWrapper extends BaseNodeWrapper<ObjectNode> {

    protected ObjectNodeWrapper(NodeWrapper parent, ObjectNode node, ObjectMapperFactory objectMapperFactory, Map<Path, JsonNode> referenceCache) {
        super(parent, node, objectMapperFactory, referenceCache);
    }

    protected ObjectNodeWrapper(ObjectNode node, ObjectMapperFactory objectMapperFactory, Map<Path, JsonNode> referenceCache, Path workingDir) {
        super(null, node, objectMapperFactory, referenceCache);
        if (workingDir.isAbsolute()) {
            this.setDirectory(workingDir.normalize());
        } else {
            String userDir = System.getProperty("user.dir");
            Path p = Path.of(userDir, workingDir.toString()).normalize().toAbsolutePath();
            setDirectory(p);
        }
    }

    @Override
    public void resolve() {
        var iter = myNode.fields();
        Set<String> keys = new HashSet<>();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> entry = iter.next();
            JsonNode value = entry.getValue();
            processNode(value);
            keys.add(entry.getKey());
        }
        resolved = true;

        // 2022.04.14 : RAB : kludge to inject source directory info into
        //                    objects during deserialization. Currently
        if (addDirectoryInfo(keys)) {
            myNode.put("sourceDir", getDirectory().toString());
        }
        ;
    }

    /**
     * Determine if the "sourceDir" property should be injected
     *
     * @param keys the keys in the object, used to determine the type
     * @return true if "sourceDir" should be added.
     */
    private boolean addDirectoryInfo(Set<String> keys) {
        return keys.contains("modules") && keys.contains("modulePaths");
    }

    @Override
    public void replaceNode(JsonNode original, JsonNode replacement) {
        var iter = myNode.fields();
        String key = null;
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> entry = iter.next();
            JsonNode value = entry.getValue();
            if (value.equals(original)) {
                key = entry.getKey();
                break;
            }
        }
        if (key != null) {
            myNode.set(key, replacement);
        }
    }
}
