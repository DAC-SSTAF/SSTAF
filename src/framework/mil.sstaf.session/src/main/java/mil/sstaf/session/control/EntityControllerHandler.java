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

package mil.sstaf.session.control;

import mil.sstaf.core.entity.Address;
import mil.sstaf.core.features.BaseHandler;
import mil.sstaf.core.features.HandlerContent;
import mil.sstaf.core.features.ProcessingResult;

import java.util.Objects;

public class EntityControllerHandler extends BaseHandler {


    private final EntityController entityController;

    protected EntityControllerHandler(EntityController ec) {
        super("Entity Controller", 1, 0, 1, false, "Provides access to the EntityController via messages");
        entityController = Objects.requireNonNull(ec, "ec");
    }

    @Override
    public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms, Address from, long id, Address respondTo) {
            return ProcessingResult.empty();

    }
}

