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
import java.util.List;

/**
 * Base inteface for classes that support interaction with external applications.
 * <p>
 * Only one outbound and one inbound parameter is supported in the interface. I is expected that multiple
 * parameters will be marshalled or bundled.
 */
public interface AppAdapter {
    /**
     * Activates the helper application and provides a {@link AppSession} object through which
     * the client can communicate. Uses the currently set arguments.
     *
     * @return a {@code Session}
     * @throws IOException if any of the file or process actions fail
     */
    AppSession activate() throws IOException;

    /**
     * Sets the arguments
     *
     * @param args the arguments to provide to the command.
     */
    void setArgs(List<String> args);

    /**
     * Activates the helper application using the specified arguments and provides a {@link AppSession} object
     * through which the client can communicate.
     *
     * @param args the arguments to provide to the application
     * @return a {@code Session}
     * @throws IOException if any of the file or process actions fail
     */
    AppSession activate(List<String> args) throws IOException;

    /**
     * Retrieves information about extracted resources
     * @return an {@code ExtractedResources} object
     */
    ResourceManager getResourceManager();


}

