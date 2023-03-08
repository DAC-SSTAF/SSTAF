package mil.devcom_sc.ansur.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import mil.sstaf.core.util.SSTAFException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static mil.devcom_sc.ansur.messages.GetValueResponse.getIntegerValue;
import static mil.devcom_sc.ansur.messages.GetValueResponse.getStringValue;
import static org.junit.jupiter.api.Assertions.*;

public class GetValueResponseTest {

    @Nested
    @DisplayName("Test the 'happy path'")
    class HappyTests {
        @Test
        @DisplayName("Confirm that an integer GetValueResponse can be created, serialized and deserialized")
        void roundTripInteger() {
            ObjectMapper mapper = new ObjectMapper();

            ValueKey key = ValueKey.AGE;
            GetValueResponse gvr = GetValueResponse.of(key, 123);
            assertNotNull(gvr);
            assertEquals(key, gvr.getKey());
            assertFalse(GetValueResponse.isString(gvr));
            assertFalse(GetValueResponse.isDouble(gvr));
            assertTrue(GetValueResponse.isInteger(gvr));
            assertEquals(123, getIntegerValue(gvr));

            SSTAFException sstafException = assertThrows(SSTAFException.class,
                    () -> GetValueResponse.getStringValue(gvr));
            assertTrue(sstafException.getMessage().contains("does not contain"));

            sstafException = assertThrows(SSTAFException.class,
                    () -> GetValueResponse.getDoubleValue(gvr));
            assertTrue(sstafException.getMessage().contains("does not contain"));

            assertDoesNotThrow(() -> {
                String json = mapper.writeValueAsString(gvr);
                assertTrue(json.contains("AGE"));
                int value = getIntegerValue(gvr);

                GetValueResponse gvr2 = mapper.readValue(json, GetValueResponse.class);
                assertNotNull(gvr2);
                assertEquals(key, gvr2.getKey());
                assertFalse(GetValueResponse.isString(gvr2));
                assertFalse(GetValueResponse.isDouble(gvr2));
                assertTrue(GetValueResponse.isInteger(gvr2));
                assertEquals(value, getIntegerValue(gvr2));
            });
        }

        @Test
        @DisplayName("Confirm that a String GetValueResponse can be created, serialized and deserialized")
        void roundTripString() {
            ObjectMapper mapper = new ObjectMapper();

            ValueKey key = ValueKey.PRIMARY_MOS;
            GetValueResponse gvr = GetValueResponse.of(key, "11B");
            assertNotNull(gvr);
            assertEquals(key, gvr.getKey());
            assertTrue(GetValueResponse.isString(gvr));
            assertFalse(GetValueResponse.isDouble(gvr));
            assertFalse(GetValueResponse.isInteger(gvr));
            assertEquals("11B", getStringValue(gvr));

            SSTAFException sstafException = assertThrows(SSTAFException.class,
                    () -> GetValueResponse.getIntegerValue(gvr));
            assertTrue(sstafException.getMessage().contains("does not contain"));

            sstafException = assertThrows(SSTAFException.class,
                    () -> GetValueResponse.getDoubleValue(gvr));
            assertTrue(sstafException.getMessage().contains("does not contain"));

            assertDoesNotThrow(() -> {
                String json = mapper.writeValueAsString(gvr);
                assertTrue(json.contains("PRIMARY_MOS"));

                GetValueResponse gvr2 = mapper.readValue(json, GetValueResponse.class);
                assertNotNull(gvr2);
                assertEquals(key, gvr2.getKey());
            });
        }
    }
}
