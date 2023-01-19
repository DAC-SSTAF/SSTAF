package mil.sstaf.core.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mil.sstaf.core.util.SSTAFException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReferenceResolver {

    private final ObjectNode rootNode;
    private final Path sourceFile;
    private final Path baseDir;
    private ObjectMapper objectMapper;

    ReferenceResolver(ObjectNode rootNode, Path sourceFile, ObjectMapper objectMapper) {
        Objects.requireNonNull(rootNode, "rootNode");
        Objects.requireNonNull(sourceFile, "sourceFile");
        if (sourceFile.isAbsolute()) {
            this.sourceFile = sourceFile.normalize();
        } else {
            String userDir = System.getProperty("user.dir");
            Path p = Path.of(userDir, sourceFile.toString()).normalize().toAbsolutePath();
            this.sourceFile = p;
        }
        baseDir = this.sourceFile.getParent();
        this.rootNode = rootNode;
        this.objectMapper = objectMapper;
    }

    public void processTree() {
        Deque<Path> pathDeque = new ArrayDeque<>();
        Map<Path, JsonNode> cache = new ConcurrentHashMap<>();
        pathDeque.push(sourceFile);
        processNode(rootNode, baseDir, pathDeque, cache);
    }

    private void processNode(JsonNode node, Path cwd, Deque<Path> paths, Map<Path, JsonNode> cache) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            processObject(objectNode, cwd, paths, cache);
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            processArray(arrayNode, cwd, paths, cache);
        }
    }

    private boolean addDirectoryInfo(Set<String> keys) {
        return keys.contains("modules") && keys.contains("modulePaths");
    }

    private boolean isReference(JsonNode node) {
        if (node.isTextual()) {
          return node.asText().endsWith(".json");
        }
        return false;
    }

    private Path referenceToPath(JsonNode refNode, Path cwd) {
        Objects.requireNonNull(refNode, "refNode");
        Objects.requireNonNull(cwd, "cwd");
        if (refNode.isTextual()) {
            String ref = refNode.asText();
            Path raw = Path.of(cwd.toString(), ref);
            Path normalized = raw.normalize();
            Path absolute = normalized.toAbsolutePath();
            return absolute;
        } else {
            throw new SSTAFException("node is not text");
        }
    }

    private void processObject(ObjectNode objectNode, Path cwd, Deque<Path> paths, Map<Path, JsonNode> cache) {
        Set<String> keys = new HashSet<>();
        var iter = objectNode.fields();
        Map<String, JsonNode> replacements = new HashMap<>();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> entry = iter.next();
            JsonNode value = entry.getValue();

            if (isReference(value)) {
                Path refPath = referenceToPath(value, cwd);
                checkCircularity(refPath, paths);

                JsonNode replacement = loadReference(refPath);
                cache.put(refPath, replacement);
                replacements.put(entry.getKey(), replacement);

                paths.push(refPath);
                Path refWorkingDir = refPath.getParent();
                processNode(replacement, refWorkingDir, paths, cache);
                paths.pop();
            } else {
                processNode(value, cwd, paths, cache);
            }
            keys.add(entry.getKey());
        }

        // After the original contents have been processed, replace
        // any references with their loaded nodes.
        objectNode.setAll(replacements);

        // 2022.04.14 : RAB : kludge to inject source directory info into
        //                    objects during deserialization. Currently
        if (addDirectoryInfo(keys)) {
            objectNode.put("sourceDir", cwd.toString());
        }
    }

    private void processArray(ArrayNode arrayNode, Path cwd, Deque<Path> paths, Map<Path, JsonNode> cache) {

        Map<Integer, JsonNode> replacements = new HashMap<>();

        int i = 0;
        Iterator<JsonNode> iter = arrayNode.elements();
        while (iter.hasNext()) {
            JsonNode value = iter.next();

            if (isReference(value)) {
                Path refPath = referenceToPath(value, cwd);
                checkCircularity(refPath, paths);
                paths.push(refPath);
                JsonNode replacement = loadReference(refPath);
                cache.put(refPath, replacement);
                replacements.put(i, replacement);
                Path refWorkingDir = refPath.getParent();
                processNode(replacement, refWorkingDir, paths, cache);
                paths.pop();
            } else {
                processNode(value, cwd, paths, cache);
            }

            ++i;
        }

        // After the original contents have been processed, replace
        // any references with their loaded nodes.
        for (var entry : replacements.entrySet()) {
            arrayNode.set(entry.getKey(), entry.getValue());
        }
    }

    private void checkCircularity(Path refPath, Deque<Path> stack) {
        if (stack.contains(refPath)) {
            throw new JsonResolutionException("Circular reference to '" + refPath + "'");
        }
    }

    private JsonNode loadReference(Path reference) {
        try {
            return objectMapper.readTree(reference.toFile());
        } catch (IOException e) {
            throw new JsonResolutionException("Could not load reference '" + reference +"'", e);
        }
    }
}
