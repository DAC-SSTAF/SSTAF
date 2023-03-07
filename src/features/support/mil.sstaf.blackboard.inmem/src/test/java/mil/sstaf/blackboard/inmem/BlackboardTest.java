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

package mil.sstaf.blackboard.inmem;

import lombok.experimental.SuperBuilder;
import mil.sstaf.blackboard.api.*;
import mil.sstaf.core.entity.*;
import mil.sstaf.core.features.ExceptionContent;
import mil.sstaf.core.features.Handler;
import mil.sstaf.core.features.ProcessingResult;
import mil.sstaf.core.entity.Message;
import mil.sstaf.core.util.Injector;
import mil.sstaftest.util.BaseHandlerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BlackboardTest extends BaseHandlerTest<InMemBlackboard> {

    static {
        preloadedClasses = List.of();

        String value = "I am the answer";
        String key = "daKey";
        AddEntryRequest aer = new AddEntryRequest(key, value, 1, Blackboard.FOREVER);
        GetEntryRequest ger = new GetEntryRequest(key, String.class, 1001000);
        RemoveEntryRequest rer = new RemoveEntryRequest(key);
        sampleMessages = List.of(aer, ger, rer);
    }


    long toMillis(double s) {
        return Math.round(s * 1000);
    }

    @Override
    protected InMemBlackboard buildFeature() {
        return new InMemBlackboard();
    }

    @SuperBuilder
    static class FakeEntity extends BaseEntity {

        @Override
        public String getPath() {
            return "";
        }

    }

    @Nested
    public class BlackboardContractTests {
        @Test
        @DisplayName("An entry with an explicit expiration time works as expected")
        void entryWithExpirationWorksAsExpected() {
            String value = "I am the answer";
            String key = "daKey";

            InMemBlackboard blackboard = new InMemBlackboard();
            blackboard.addEntry(key, value, toMillis(1.0), toMillis(10.0));

            assertEquals(1, blackboard.getEntryMap().size());
            assertTrue(blackboard.getEntryMap().containsKey(key));

            Optional<String> response0 = blackboard.getEntry(key, toMillis(0.5), String.class);
            assertFalse(response0.isPresent(), "Entry is not valid yet");

            Optional<String> response1 = blackboard.getEntry(key, toMillis(7.5), String.class);
            assertTrue(response1.isPresent());
            assertEquals(value, response1.get());

            Optional<String> response2 = blackboard.getEntry(key, 10000, String.class);
            assertTrue(response2.isPresent());
            assertEquals(value, response2.get());

            Optional<String> response3 = blackboard.getEntry(key, 15000, String.class);
            assertFalse(response3.isPresent(), "Entry has expired and should be gone");

            assertEquals(0, blackboard.getEntryMap().size());
            assertFalse(blackboard.getEntryMap().containsKey(key));
        }

        @Test
        void entryWithIndefiniteExpirationWorksAsExpected() {
            String value = "I am the answer";
            String key = "daKey";

            InMemBlackboard blackboard = new InMemBlackboard();
            blackboard.addEntry(key, value, 1000);

            assertEquals(1, blackboard.getEntryMap().size());
            assertTrue(blackboard.getEntryMap().containsKey(key));

            Optional<String> response1 = blackboard.getEntry(key, 7500, String.class);
            assertTrue(response1.isPresent());
            assertEquals(value, response1.get());

            Optional<String> response2 = blackboard.getEntry(key, 10000, String.class);
            assertTrue(response2.isPresent());
            assertEquals(value, response2.get());

            Optional<String> response3 = blackboard.getEntry(key, 15000, String.class);
            assertTrue(response3.isPresent());
            assertEquals(value, response3.get());

            Optional<String> response4 = blackboard.getEntry(key, Blackboard.FOREVER, String.class);
            assertTrue(response4.isPresent());
            assertEquals(value, response4.get());
        }

        @Test
        void updatingTheValuesWorksAsExpected() {
            String value = "I am the answer";
            String value2 = "No I am";
            String key = "daKey";

            InMemBlackboard blackboard = new InMemBlackboard();
            blackboard.addEntry(key, value, toMillis(1.0));

            assertEquals(1, blackboard.getEntryMap().size());
            assertTrue(blackboard.getEntryMap().containsKey(key));

            Optional<String> response1 = blackboard.getEntry(key, 7500, String.class);
            assertTrue(response1.isPresent());
            assertEquals(value, response1.get());

            Optional<String> response2 = blackboard.getEntry(key, 10000, String.class);
            assertTrue(response2.isPresent());
            assertEquals(value, response2.get());

            blackboard.addEntry(key, value2, 23000, 50000);

            Optional<String> response3 = blackboard.getEntry(key, 15000, String.class);
            assertFalse(response3.isPresent(),
                    "Old entry has been replaced, but new entry should not be valid yet");

            Optional<String> response4 = blackboard.getEntry(key, 49900, String.class);
            assertTrue(response4.isPresent());
            assertEquals(value2, response4.get());
        }

        @Test
        void nullKeyWithExpirationThrows() {
            String value = "I am the answer";
            Blackboard blackboard = new InMemBlackboard();
            assertThrows(NullPointerException.class,
                    () -> blackboard.addEntry(null, value, 1000, 10000));
        }

        @Test
        void nullKeyWithoutExpirationThrows() {
            String value = "I am the answer";
            Blackboard blackboard = new InMemBlackboard();
            assertThrows(NullPointerException.class, () -> blackboard.addEntry(null, value, 1000));
        }

        @Test
        void nullValueWithExpirationThrows() {
            String key = "daKey";
            Blackboard blackboard = new InMemBlackboard();
            assertThrows(NullPointerException.class,
                    () -> blackboard.addEntry(key, null, 1000, 10000));
        }

        @Test
        void nullValueWithoutExpirationThrows() {
            String key = "daKey";
            Blackboard blackboard = new InMemBlackboard();
            assertThrows(NullPointerException.class,
                    () -> blackboard.addEntry(key, null, 1000));
        }

        @Test
        void getWithNullKeyThrows1() {
            String value = "I am the answer";
            String key = "daKey";

            Blackboard blackboard = new InMemBlackboard();
            blackboard.addEntry(key, value, 1000);

            assertThrows(NullPointerException.class, () ->
                    blackboard.getEntry(null, 10000));
        }

        @Test
        void getWithNullKeyThrows2() {
            String value = "I am the answer";
            String key = "daKey";

            Blackboard blackboard = new InMemBlackboard();
            blackboard.addEntry(key, value, 1000);

            assertThrows(NullPointerException.class,
                    () -> blackboard.getEntry(null, 10000, String.class));
        }

        @Test
        void getWithNullClassThrows() {
            String value = "I am the answer";
            String key = "daKey";

            Blackboard blackboard = new InMemBlackboard();
            blackboard.addEntry(key, value, 1000);

            assertThrows(NullPointerException.class,
                    () -> blackboard.getEntry(key, 10000, null));
        }

        @Test
        void removeWorks() {
            String value = "I am the answer";
            String key = "daKey";

            InMemBlackboard blackboard = new InMemBlackboard();
            blackboard.addEntry(key, value, 1000);
            assertEquals(1, blackboard.getEntryMap().size());
            blackboard.remove("notDaKey");
            assertEquals(1, blackboard.getEntryMap().size());
            blackboard.remove(key);
            assertEquals(0, blackboard.getEntryMap().size());
        }

        @Test
        void removeWithNullClassDoesNotThrow() {
            String value = "I am the answer";
            String key = "daKey";
            Blackboard blackboard = new InMemBlackboard();
            blackboard.addEntry(key, value, 1000);
            assertDoesNotThrow(() -> blackboard.remove(null));
        }


        @Test
        void handlerIntefaceWorksAsExpected() {

            Handler handler = new InMemBlackboard();
            FakeEntity fakeEntity =  FakeEntity.builder().build();
            EntityHandle entityHandle = fakeEntity.getHandle();
            Injector.inject(handler, entityHandle);

            assertEquals("Blackboard", handler.getName());
            assertEquals(1, handler.getMajorVersion());
            assertEquals(0, handler.getMinorVersion());
            assertEquals(0, handler.getPatchVersion());
            assertNotNull(handler.getDescription());

            String value = "I am the answer";
            String key = "daKey";

            Address address = Address.makeAddress(entityHandle, "bob");

            AddEntryRequest aer = new AddEntryRequest(key, value, 1, Blackboard.FOREVER);
            ProcessingResult pr1 = handler.process(aer, 100000, 100000, address, 1, address);
            Message mr1 = pr1.messages.get(0);
            Object contents = mr1.getContent();
            assertTrue(contents instanceof AddEntryResponse);
            AddEntryResponse response = (AddEntryResponse) contents;
            assertEquals(1, response.size);

            GetEntryRequest ger = new GetEntryRequest(key, String.class, 1001000);
            ProcessingResult pr2 = handler.process(ger, 100000, 100000, address, 1, address);
            Message mr2 = pr2.messages.get(0);
            Object contents2 = mr2.getContent();
            assertTrue(contents2 instanceof GetEntryResponse);
            GetEntryResponse response2 = (GetEntryResponse) contents2;
            assertEquals(value, response2.value);

            RemoveEntryRequest rer = new RemoveEntryRequest(key);
            ProcessingResult pr3 = handler.process(rer, 100000, 100000, address, 1, address);
            Message mr3 = pr3.messages.get(0);
            Object content3 = mr3.getContent();
            assertTrue(content3 instanceof RemoveEntryResponse);
            RemoveEntryResponse response3 = (RemoveEntryResponse) content3;
            assertEquals(0, response3.size);

            ProcessingResult pr4 = handler.process(ExceptionContent.builder().build(), 100000, 100000, address, 1, address);
            Message mr4 = pr4.messages.get(0);
            assertTrue(mr4 instanceof ErrorResponse);
        }
    }
}

