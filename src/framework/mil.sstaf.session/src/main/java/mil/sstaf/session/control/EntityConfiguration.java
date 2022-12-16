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


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.entity.BaseEntity;
import mil.sstaf.core.entity.Entity;
import mil.sstaf.core.entity.Force;

import java.util.List;
import java.util.Map;

@SuperBuilder
@Jacksonized
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public final class EntityConfiguration {
//    public static final String CK_BLUE_FORCE_KEY = "blueForces";
//    public static final String CK_RED_FORCE_KEY = "redForces";
//    public static final String CK_GRAY_FORCE_KEY = "grayForce";

    @Getter
    private final Map<Force, List<BaseEntity>> participants;

}

