package mil.sstaf.core.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BaseNodeWrapperTest {

    ObjectMapperFactory omf = path -> null;
    Map<Path, JsonNode> referenceCache = new HashMap<>();

    static class FakeWrapper extends BaseNodeWrapper<JsonNode> {

        protected FakeWrapper(NodeWrapper parent, JsonNode node, ObjectMapperFactory objectMapperFactory, Map<Path, JsonNode> referenceCache) {
            super(parent, node, objectMapperFactory, referenceCache);
        }

        @Override
        public void resolve() {

        }

        @Override
        public void replaceNode(JsonNode original, JsonNode replacement) {

        }

    }

    @Nested
    @DisplayName("Test the 'Happy Path'")
    class HappyTests {

        @Test
        @DisplayName("Confirm that refCount works for non-reference Wrappers")
        public void refCountTest1() {
            FakeWrapper one = new FakeWrapper(null, null, omf, referenceCache);
            FakeWrapper two = new FakeWrapper(one, null, omf, referenceCache);
            FakeWrapper three = new FakeWrapper(two, null, omf, referenceCache);
            FakeWrapper four = new FakeWrapper(three, null, omf, referenceCache);

            assertEquals(0, four.refCount("bob"));
            assertEquals(0, four.refCount("diane"));
            assertEquals(0, four.refCount("luigi"));
        }

        @Test
        @DisplayName("Confirm that isResolved() works as expected.")
        public void isResolvedTest1() {
            FakeWrapper fakeWrapper = new FakeWrapper(null, null, omf, referenceCache);
            assertFalse(fakeWrapper.isResolved());
            fakeWrapper.resolved = true;
            assertTrue(fakeWrapper.isResolved());
            fakeWrapper.resolved = false;
            assertFalse(fakeWrapper.isResolved());
        }

        @Test
        @DisplayName("Confirm that getNode() works as expected")
        public void getNodeTest1() {
            JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true);
            JsonNode textNode = jsonNodeFactory.textNode("This is the text");
            FakeWrapper fakeWrapper = new FakeWrapper(null, textNode, omf, referenceCache);
            assertEquals(textNode, fakeWrapper.getMyNode());
        }

        @Test
        @DisplayName("Confirm that getParent() works as expected")
        public void getParentTest1() {
            FakeWrapper one = new FakeWrapper(null, null, omf, referenceCache);
            FakeWrapper two = new FakeWrapper(one, null, omf, referenceCache);
            FakeWrapper three = new FakeWrapper(two, null, omf, referenceCache);
            FakeWrapper four = new FakeWrapper(three, null, omf, referenceCache);
            assertEquals(three, four.getParent());
            assertEquals(two, three.getParent());
            assertEquals(one, two.getParent());
            assertNull(one.getParent());
        }


    }
}
