package mil.sstaf.core.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mil.sstaf.core.util.SSTAFException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;

public class JsonLoader {

    private final ObjectMapper objectMapper;

    public JsonLoader() {
        objectMapper = new ObjectMapper();
    }

    private Class<?> getRootClass(ObjectNode rootNode) {
        if (rootNode.has("class")) {
            String className = rootNode.get("class").asText();
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new SSTAFException("Could not load specified class '" + className + "'", e);
            }
        } else {
            throw new SSTAFException("JSON does not specify a class name\n" + rootNode.toPrettyString());
        }
    }

    private Object loadObject(JsonNode jsonNode, Path sourceFile) throws JsonProcessingException {
        if (jsonNode.isObject()) {
            Class<?> rootClass = getRootClass((ObjectNode) jsonNode);
            return getObjectInner(jsonNode, rootClass, sourceFile);
        } else {
            throw new SSTAFException("JSON did not define an object");
        }
    }

    private <T> T loadObject(JsonNode jsonNode, Class<T> asClass, Path sourceFile) throws JsonProcessingException {
        if (jsonNode.isObject()) {
            return getObjectInner(jsonNode, asClass, sourceFile);
        } else {
            throw new SSTAFException("JSON did not define an object");
        }
    }

    private <T> T getObjectInner(JsonNode jsonNode, Class<T> asClass, Path sourceFile) {
        ObjectNode topNode = (ObjectNode) jsonNode;
        ReferenceResolver referenceResolver = new ReferenceResolver(topNode, sourceFile, objectMapper);
        referenceResolver.processTree();

        try {
            return objectMapper.treeToValue(jsonNode, asClass);
        } catch (Exception e) {
            throw new SSTAFException("Could not read object '" + jsonNode.toPrettyString() + "'", e);
        }
    }

    public Object load(Reader reader, Path sourceDir) {
        // 2022.04.26 : RAB : Note: The 'fakeFile.json' gets stripped later.
        Path pathToFile;
        if (sourceDir.isAbsolute()) {
            Path tmp = sourceDir.normalize();
            pathToFile = Path.of(tmp.toString(), "fakeFile.json");
        } else {
            pathToFile = Path.of(System.getProperty("user.dir"), sourceDir.toString(), "fakeFile.json").normalize().toAbsolutePath();
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(reader);
            return loadObject(jsonNode, pathToFile);
        } catch (IOException e) {
            throw new SSTAFException("Could not load JSON", e);
        }
    }

    public Object load(Path filePath) {
        Path pathToFile;
        if (filePath.isAbsolute()) {
            pathToFile = filePath.normalize();
        } else {
            pathToFile = Path.of(System.getProperty("user.dir"), filePath.toString()).normalize().toAbsolutePath();
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(pathToFile.toFile());
            return loadObject(jsonNode, pathToFile);
        } catch (IOException e) {
            throw new SSTAFException("Could not load JSON", e);
        }
    }

    public Object load(String jsonString, Path sourceFile) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return loadObject(jsonNode, sourceFile);
        } catch (IOException e) {
            throw new SSTAFException("Could not load JSON from '" + jsonString + "'", e);
        }
    }

    public Object load(JsonNode node, Path sourceDir) {
        try {
            return loadObject(node, sourceDir);
        } catch (IOException e) {
            throw new SSTAFException("Could not load JSON", e);
        }
    }

    public <T> T load(Reader reader, Class<T> asClass, Path sourceDir) {
        // 2022.04.26 : RAB : Note: The 'fakeFile.json' gets stripped later.
        Path pathToFile;
        if (sourceDir.isAbsolute()) {
            Path tmp = sourceDir.normalize();
            pathToFile = Path.of(tmp.toString(), "fakeFile.json");
        } else {
            pathToFile = Path.of(System.getProperty("user.dir"), sourceDir.toString(), "fakeFile.json").normalize().toAbsolutePath();
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(reader);
            return loadObject(jsonNode, asClass, pathToFile);
        } catch (IOException e) {
            throw new SSTAFException("Could not load JSON", e);
        }
    }

    public <T> T load(Path filePath, Class<T> asClass) {
        Path pathToFile;
        if (filePath.isAbsolute()) {
            pathToFile = filePath.normalize();
        } else {
            pathToFile = Path.of(System.getProperty("user.dir"), filePath.toString()).normalize().toAbsolutePath();
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(pathToFile.toFile());
            return loadObject(jsonNode, asClass, pathToFile);
        } catch (IOException e) {
            throw new SSTAFException("Could not load JSON from '" + pathToFile + "'", e);
        }
    }

    public <T> T load(String jsonString, Class<T> asClass, Path sourceFile) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return loadObject(jsonNode, asClass, sourceFile);
        } catch (IOException e) {
            throw new SSTAFException("Could not load JSON from '" + jsonString + "'", e);
        }
    }

    public <T> T load(JsonNode node, Class<T> asClass, Path sourceFile) {
        try {
            return loadObject(node, asClass, sourceFile);
        } catch (IOException e) {
            throw new SSTAFException("Could not load JSON", e);
        }
    }
}
