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
                            Map<Path, JsonNode> referenceCache) {
        super(parent, node, objectMapperFactory, referenceCache);
        this.ref = ref;
        this.pathToRef = makeRegPath(ref);
        this.setDirectory(pathToRef.getParent());
    }

    public Path makeRegPath(String ref) {
        Path p = Path.of(ref);
        if (p.isAbsolute()) {
            return p.normalize();
        } else {
            return Path.of(getDirectory().toString(), ref).normalize().toAbsolutePath();
        }
    }

    @Override
    public void resolve() {

        if (parent.refCount(ref) == 0) {
            Path path = makeRegPath(pathToRef.toString());
            JsonNode replacementNode;
            if (cache.containsKey(path)) {
                replacementNode = cache.get(path);
            } else {
                replacementNode = loadAndResolve(path);
                cache.put(path, replacementNode);
            }
            parent.replaceNode(myNode, replacementNode);
            resolved = true;
        } else {
            throw new JsonResolutionException("Circular reference '" + ref + "'");
        }
    }

    private JsonNode loadAndResolve(Path path) {
        try {
            ObjectMapper objectMapper = objectMapperFactory.create(path);
            JsonNode replacementNode = objectMapper.readTree(path.toFile());
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
