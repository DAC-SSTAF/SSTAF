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

package mil.sstaf.core.module;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.nio.file.Path;
import java.util.Set;

/**
 * Defines the configuration of a {@link ModuleLayer}.
 *
 * Multiple module layers can be defined. A root layer can be defined for
 * the entire SSTAF infrastructure by adding a {@code ModuleLayerDefinition
 * to the {@code SSTAFConfiguration}. Additionally, each {@code Entity} can
 * define a {@code ModuleLayer} that will be used to load it's {@code Featues}.
 *
 * 2022.04.26 : RAB : TODO: Need more test coverage for Modules and Class
 *                          loading scenarios.
 */
@Builder
@Jacksonized
public final class ModuleLayerDefinition {
    /**
     * The directory from which this {@code ModuleLayerDefinition} was loaded.
     * Used to resolve relative module paths.
     *
     * 2022.04.26 : RAB :
     * Note: The value for sourceDir is injected by {@code JSONLoader}. It is
     *       hard-wired to recognize {@code ModuleLayerDefinition} objects by
     *       their fields. This is probably the only truly awful hack in
     *       the system.
     */
    @Getter
    private final Path sourceDir;

    /**
     * The modules to include in the new layer.
     */
    @Getter
    private final Set<String> modules;
    /**
     * The paths to search for the modules.
     */
    @Getter
    private final Set<Path> modulePaths;

}

