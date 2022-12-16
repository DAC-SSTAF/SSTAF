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

import mil.sstaf.core.features.Agent;
import mil.sstaf.core.features.ProcessingResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Base class for testing implementations of {@link Agent}
 * <p>
 * This class extends {@link BaseHandlerTest} to provide a framework and series of tests to confirm than
 * an implementation of the {@code Agent} interface fulfils all of the contractual obligations necessary
 * for the implementation to be used in SSTAF.
 * </p>
 */
public abstract class BaseAgentTest<T extends Agent> extends BaseHandlerTest<T> {

    @Nested
    @DisplayName("Check requirements for implementations of 'Agent'")
    public class AgentContractTests {

        /**
         * Confirms that the {@code tick()} method works and that it returns a valid {@link ProcessingResult}.
         */
        @Test
        @DisplayName("Confirm that tick() returns a ProcessingResult")
        public void checkMultipleTicks() {
            for (long time = -10000; time < 100000L; time += 1000) {
                tickCheck(time);
            }
        }

        private void tickCheck(long time) {
            T agent = setupFeature();
            ProcessingResult pr = agent.tick(time);
            assertNotNull(pr, agent.getClass().getName()
                    + ".tick(" + time + ") returned a null ProcessingResult");
        }


    }
}

