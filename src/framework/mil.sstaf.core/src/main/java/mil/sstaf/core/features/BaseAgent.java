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
 * Base class for {@code Agent}s.
 */
public abstract class BaseAgent
        extends BaseHandler
        implements Agent {

    /**
     * Constructor for subclasses.
     * <p>
     * Note that concrete implementations must have a no-args constructor to be loadable as services
     *
     * @param featureName           the name of this {@code Feature}
     * @param majorVersion          the major version number
     * @param minorVersion          the minor version number
     * @param patchVersion          the patch version number
     * @param requiresConfiguration whether or not the Handler must be provided a configuration to make it valid.
     * @param description           a more verbose description of the {@code Agent}
     */
    protected BaseAgent(String featureName, int majorVersion, int minorVersion, int patchVersion,
                        boolean requiresConfiguration, String description) {
        super(featureName, majorVersion, minorVersion, patchVersion, requiresConfiguration, description);
    }
}

