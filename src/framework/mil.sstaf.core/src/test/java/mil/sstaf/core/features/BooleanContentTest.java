package mil.sstaf.core.features;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BooleanContentTest {
    @Nested
    @DisplayName("Test the happy path")
    class HappyTests {
        @Test
        @DisplayName("Test BooleanContent")
        public void booleanContentTest() {
            BooleanContent bc1 = BooleanContent.of(false);
            assertNotNull(bc1);
            assertFalse(bc1.getValue());

            BooleanContent bc = BooleanContent.builder().value(Boolean.TRUE).build();
            assertNotNull(bc);
            assertTrue(bc.getValue());

            ObjectMapper mapper = new ObjectMapper();
            assertDoesNotThrow(() -> {
                String json = mapper.writeValueAsString(bc);
                assertTrue(json.contains("mil.sstaf.core.features.BooleanContent"));
                JsonNode jsonNode = mapper.readTree(json);
                assertNotNull(jsonNode);
                BooleanContent msg = mapper.treeToValue(jsonNode, BooleanContent.class);
                assertNotNull(msg);
                assertTrue(msg.getValue());
            });
        }
    }
}
