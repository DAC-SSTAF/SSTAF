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

package mil.devcom_sc.ansur.api.constraints;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.devcom_sc.ansur.messages.ValueKey;

import java.util.Objects;

/**
 * Interface for classes that perform filtering constraints on
 * {@code CSVRecords}. These constraints can be used to select
 * ANSUR records that match criteria.
 */
@SuperBuilder
@Jacksonized
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
@EqualsAndHashCode
public class Constraint {
    @Getter
    protected String propertyName;

    protected Constraint(ConstraintBuilder<?, ?> builder) {
        Objects.requireNonNull(builder.propertyName, "propertyName");
        ValueKey.matchHeaderName(builder.propertyName);
        this.propertyName = builder.propertyName;
    }
}
