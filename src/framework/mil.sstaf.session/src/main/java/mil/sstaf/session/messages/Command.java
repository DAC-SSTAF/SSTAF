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

package mil.sstaf.session.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.entity.EntityHandle;
import mil.sstaf.core.features.HandlerContent;
import mil.sstaf.session.control.Session;

@Jacksonized
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
@EqualsAndHashCode(callSuper = true)
public class Command extends BaseSessionCommand {

    @Getter
    @NonNull
    private final String recipientPath;
    @Getter
    @NonNull
    private final HandlerContent content;
    @Getter
    @Setter
    @Builder.Default
    @JsonIgnore
    private EntityHandle handle = null;

    /**
     * Queries the provided {@code Session} to determine and assign the
     * {@code EntityHandle}.
     *
     * @param session The {@code Session}
     */
    public void setHandleFromSession(Session session) {
        session.getEntityController()
                .getHandleFromPath(recipientPath)
                .ifPresent(this::setHandle);
    }
}

