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

package mil.sstaf.core.entity;

import java.util.Optional;

/**
 * @author Ron Bowers
 * @version 1.0
 * @since 1.0
 *
 * <p>Single-method interface for objects that map a {@code Path} to
 * an {@code EntityHandle}.</p>
 */
@FunctionalInterface
public interface PathToHandleMapper {
    /**
     * Resolves the {@code String} that an {@code Entity} path to an {@code EntityHandle}.
     *
     * @param path the path to map to an {@code}.
     * @return an {@code Optional} containing the {@code EntityHandle} if it was found
     */
    Optional<EntityHandle> invoke(String path);
}

