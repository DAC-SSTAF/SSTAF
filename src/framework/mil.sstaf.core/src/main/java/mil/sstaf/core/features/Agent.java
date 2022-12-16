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

package mil.sstaf.core.features;

/**
 * Interface for classes that should be triggered on every tick regardless
 * of whether or not a message was received.
 */
public interface Agent
        extends Handler {
    /**
     * Activate the Agent to perform a function at the specified time.
     *
     * @param currentTime_ms the simulation time.
     * @return a {@code ProcessingResult} containing internal and external {@code Message}s
     */
    ProcessingResult tick(final long currentTime_ms);
}


