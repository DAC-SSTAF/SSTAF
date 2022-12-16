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

package mil.sstaftest.util;

import mil.sstaf.core.entity.Address;
import mil.sstaf.core.entity.ErrorResponse;
import mil.sstaf.core.features.Agent;
import mil.sstaf.core.features.Handler;
import mil.sstaf.core.features.HandlerContent;
import mil.sstaf.core.features.ProcessingResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class for testing implementations of {@link Handler}
 * <p>
 * This class extends {@link BaseFeatureTest} to provide a frameowkr and series of tests to confirm than
 * an implementation of the {@code Handler} interface fulfils all of the contractual obligations necessary
 * for the implementation to be used in SSTAF.
 * </p>
 *
 * <p>Like {@code BaseFeatureTest}, using this test class requires some setup</p>
 *
 * <ul>
 *     <li>
 *     The concrete test class must create a valid instance of each supported message content class
 *     and add it to the {@cod sampleMessages} list. This is done in a {@code static} block with
 *     this construct:
 *     <pre><code>
 *     var message1 = new MessageOne()
 *     var message2 = new MessageTwo()
 *     ...
 *     sampleMessages = List.of(message1, message2, ...);
 *     </code></pre>
 *     </li>*
 * </ul>
 *
 * @param <T>
 */
abstract public class BaseHandlerTest<T extends Handler> extends BaseFeatureTest<T> {

    protected static List<? extends HandlerContent> sampleMessages;

    static class NonsenseMessage {

    }

    /**
     * Tests
     */
    @Nested
    @DisplayName("Check requirements for implementations of 'Handler'")
    public class HandlerContractTests {

        /**
         * Confirms that the {@code contentHandled()} method works.
         * <p>
         * Note that if the implementation is an {@code Agent}, it is not required to handle messages. This
         * is because an {@code Agent} will be activated by the {@code tick()} method.
         */
        @Test
        @DisplayName("Confirm that contentHandled() returns non-null and non-empty")
        public void checkContentHandled() {
            T handler = setupFeature();
            List<Class<? extends HandlerContent>> ch = handler.contentHandled();
            String className = handler.getClass().getSimpleName();
            assertNotNull(ch, className + ".contentHandled() returned null, it must return a valid List");
            if (!(handler instanceof Agent)) {
                assertFalse(ch.isEmpty(), className + ".contentHandled() returned an empty list. Handlers must 'handle' at least one message type");
            }
        }

        /**
         * Confirms that the {@code getAddress()} mechanism works.
         */
        @Test
        @DisplayName("Confirm that getAddress() provides the expected Address")
        public void checkAddress() {
            T handler = setupFeature();
            Address a = handler.getAddress();
            String className = handler.getClass().getSimpleName();
            assertNotNull(a, className + ".getAddress() returned null. It should return a valid Address object");
            assertNotNull(a.entityHandle, "The Address returned from " + className + ".getAddress() has a null EntityHandle");
            assertEquals(handler.getName(), a.handlerName,
                    "The Address returned from " + className + ".getAddress() contained the wrong Handler name");
        }

        /**
         * Confirms that the {@code sampleMessage} list is configured. This is a precondition to other tests.
         */
        @Test
        @DisplayName("Confirm that the sample message list is populated")
        public void checkMessageList() {
            T handler = setupFeature();

            if (handler.contentHandled().size() > 0) {
                assertNotNull(sampleMessages, "The sampleMessages list is null, must be a valid List");
                List<Class<? extends HandlerContent>> ch = handler.contentHandled();

                assertEquals(ch.size(), sampleMessages.size(),
                        "The contentHandled() list and sampleMessages list are different sizes");
                List<Class<?>> lsmc = new ArrayList<>();

                for (Object o : sampleMessages) {
                    Class<?> c = o.getClass();
                    lsmc.add(c);
                    assertTrue(ch.contains(c),
                            "SampleMessage class " + c.getName() + " is not in content handled list");
                }

                for (Class<?> o : ch) {
                    assertTrue(lsmc.contains(o),
                            "Handled class " + o.getName() + " is not represented in the sample messages list");
                }
            }
        }

        /**
         * Confirms that each of the sample messages can be processed.
         */
        @Test
        @DisplayName("Confirm that processing the sample messages works")
        public void checkProcess() {
            T handler = setupFeature();
            if (handler.contentHandled().size() > 0) {
                for (HandlerContent o : sampleMessages) {
                    ProcessingResult pr = handler.process(o, 1000, 1000, Address.NOWHERE, 1, Address.NOWHERE);
                    assertNotNull(pr, handler.getClass().getName()
                            + ".process(" + o.getClass().getSimpleName() + ") returned a null ProcessingResult");
                }
            }
        }

        class Garbage extends HandlerContent {

        }

        /**
         * Confirms that if an unsupported message is passed to the {@code Handler}, an {@code ErrorResponse} is
         * generated.
         */
        @Test
        @DisplayName("Confirm that processing an unsupported results in a ProcessingResult with a single error message")
        public void checkUnsupported() {
            T handler = setupFeature();
            ProcessingResult pr = handler.process(new Garbage(), 1000, 1000, Address.NOWHERE, 1, Address.NOWHERE);
            assertNotNull(pr, handler.getClass().getName() + ".process(UnsupportedMessage) returned null ");
            assertNotNull(pr.messages,
                    handler.getClass().getName() + ".process(UnsupportedMessage) returned a ProcessingResult with a null message list");
            assertFalse(pr.messages.isEmpty(),
                    handler.getClass().getName() + ".process(UnsupportedMessage) returned empty ProcessingResult");
            assertTrue(pr.messages.get(0) instanceof ErrorResponse,
                    "Content of ProcessingResult was not an ErrorResponse");
            assertTrue(((ErrorResponse) pr.messages.get(0)).getErrorDescription().contains("not supported"),
                    "ErrorResponse does not appear to be a 'not supported' error");


        }
    }
}


