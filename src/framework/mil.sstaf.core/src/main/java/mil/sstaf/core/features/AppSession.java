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

import java.io.IOException;

/**
 * Base interface for interacting with an instance of an external helper application.
 */
public interface AppSession extends AutoCloseable{

    /**
     * Sends a message of type {@code P} to the helper and retrieves the response
     * @param cmd the command to send
     * @return the response from the helper
     * @throws IOException if communication fails
     */
    String invoke(String cmd) throws IOException;

}

