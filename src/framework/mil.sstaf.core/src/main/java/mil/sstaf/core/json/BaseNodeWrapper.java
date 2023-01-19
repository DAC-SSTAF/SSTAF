package mil.sstaf.core.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public abstract class BaseNodeWrapper<T extends JsonNode> implements NodeWrapper {

    protected final NodeWrapper parent;

    protected final ObjectMapperFactory objectMapperFactory;
    protected T myNode;
    protected boolean resolved = false;
    protected Map<Path, JsonNode> cache;
    private Path directory;

    protected BaseNodeWrapper(NodeWrapper parent, T node, ObjectMapperFactory objectMapperFactory, Map<Path, JsonNode> referenceCache) {
        this.parent = parent;
        this.objectMapperFactory = objectMapperFactory;
        this.myNode = node;
        this.cache = referenceCache;
        this.directory = parent == null ? Path.of(System.getProperty("user.dir")) : parent.getDirectory();
    }

    @Override
    public T getMyNode() {
        return myNode;
    }

    @Override
    public boolean isResolved() {
        return resolved;
    }

    @Override
    public NodeWrapper getParent() {
        return parent;
    }

    @Override
    public void setDirectory(Path dir) {
        this.directory = dir;
    }

    @Override
    public Path getDirectory() {
        return directory;
    }

    @Override
    public int refCount(String refName) {
        Objects.requireNonNull(refName);
        return parent == null ? 0 : parent.refCount(refName);
    }

    public void processNode(JsonNode root) {
        NodeWrapper wrapper = null;
        if (root.isArray()) {
            wrapper = new ArrayNodeWrapper(this, (ArrayNode) root, objectMapperFactory, cache);
        } else if (root.isObject()) {
            wrapper = new ObjectNodeWrapper(this, (ObjectNode) root, objectMapperFactory, cache);
        } else {
            if (root.isTextual()) {
                String s = root.textValue();
                if (s.endsWith(".json")) {
                    wrapper = new ReferenceWrapper(s, this, root, objectMapperFactory, cache);
                }
            }
        }
        if (wrapper != null) {
            wrapper.resolve();
        }

    }
}
