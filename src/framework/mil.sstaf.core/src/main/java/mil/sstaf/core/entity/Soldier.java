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
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.json.JsonLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

@SuperBuilder
@Jacksonized
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public class Soldier extends Human {

    private static final Logger logger = LoggerFactory.getLogger(Soldier.class);

    @Builder.Default
    private Rank rankEnum = Rank.E1;
    private String rank;
    //
    // Identity
    //
    @Setter
    @Getter
    private String position; // My position within a Unit, used a key in Unit

    @Getter
    @Setter
    private Unit unit; // My unit

    private String path; // Cached path from top unit to me.

    //
    // Soldier-specific services
    //

    protected Soldier(SoldierBuilder<?, ?> builder) {
        super(builder);
        this.rank = builder.rank == null ? "E1" : builder.rank;
        this.rankEnum = Rank.findMatch(this.rank);
    }

    public static Soldier from(File file) {
        JsonLoader jsonLoader = new JsonLoader();
        return (Soldier) jsonLoader.load(Path.of(file.getPath()));
    }

    public static Soldier from(String json, Path sourceFile) {
        JsonLoader jsonLoader = new JsonLoader();
        return (Soldier) jsonLoader.load(json, sourceFile);
    }

    public static Soldier from(JsonNode jsonNode, Path sourceFile) {
        JsonLoader jsonLoader = new JsonLoader();
        return (Soldier) jsonLoader.load(jsonNode, sourceFile);
    }

    public String getPath() {
        if (path == null) {
            if (unit == null) {
                path = Entity.ENTITY_PATH_DELIMITER + super.getName();
            } else {
                path = unit.getPath() + Entity.ENTITY_PATH_DELIMITER + position;
            }
        }
        return path;
    }

    public void setForce(final Force force) {
        super.setForce(force);
        path = null;
    }

    public Rank getRank() {
        return rankEnum;
    }

    public void setUnit(final Unit unit, String position) {
        this.path = null;
        this.unit = unit;
        this.position = position;
    }

    /**
     * Performs any Soldier-level initialization
     */
    public void init() {
        logger.debug("Initializing Soldier {}", getPath());
        super.init();
    }
}

