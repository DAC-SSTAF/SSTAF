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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.json.JsonLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

/**
 * Humans have multiple identifies. First, they have their definition name. This is the "definitionName" specified in
 * the JSON configuration. Multiple humans can share the same definition name. Second, there is the simple name. This
 * name identifies each instance of the Human. By default, it is a randomly-generated UUID.
 */
@SuperBuilder
@Jacksonized
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public class Human extends BaseEntity {

    private static final Logger logger = LoggerFactory.getLogger(Human.class);

    @Getter
    private final String definitionName;

    protected Human(HumanBuilder<?, ?> builder) {
        super(builder);
        this.definitionName = builder.definitionName == null
                ? "UNSPECIFIED" : builder.definitionName;
    }

    @Override
    public String toString() {
        return "Human{" +
                ", definitionName='" + definitionName + '\'' +
                ", uuid=" + uuid +
                ", name='" + getName() + '\'' +
                "} " + super.toString();
    }

    /**
     * Performs any Human-level initialization
     */
    public void init() {
        logger.debug("Initializing Human {}", getPath());
        super.init();
    }

    public String getPath() {
        return Entity.ENTITY_PATH_DELIMITER + getName();
    }

    public static Human from(File file) {
        JsonLoader jsonLoader = new JsonLoader();
        return (Human) jsonLoader.load(Path.of(file.getPath()));
    }

    public static Human from(String json, Path sourceDir) {
        JsonLoader jsonLoader = new JsonLoader();
        return (Human) jsonLoader.load(json, sourceDir);
    }

    public static Human from(JsonNode jsonNode, Path sourceDir) {
        JsonLoader jsonLoader = new JsonLoader();
        return (Human) jsonLoader.load(jsonNode, sourceDir);
    }


}

