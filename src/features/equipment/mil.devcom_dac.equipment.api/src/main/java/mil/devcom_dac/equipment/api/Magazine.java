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

package mil.devcom_dac.equipment.api;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@SuperBuilder
@Jacksonized
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
@EqualsAndHashCode(callSuper = true)
public class Magazine extends Item {

    @Getter
    private String magazineType;
    @Getter
    private double emptyMass_kg;
    @Getter
    private int capacity;

    @Getter
    private double perRoundMass_kg;

    @Getter
    private int currentLoad;

    private Magazine(MagazineBuilder<?,?> b) {
        super(b);
        if (b.magazineType == null) {
            // 2022.04.21 : RAB : Default is to generate a fully loaded 5.56
            this.capacity = b.capacity == 0 ? 30 : b.capacity;
            this.currentLoad = b.currentLoad == 0 ? this.capacity : b.currentLoad;
            this.magazineType = "5.56mm STANAG";
            this.name = "5.56mm STANAG 30rd";
            this.perRoundMass_kg = 0.01231;
            this.emptyMass_kg = 0.1207;
        } else {
            this.magazineType = b.magazineType;
            this.emptyMass_kg = b.emptyMass_kg;
            this.perRoundMass_kg = b.perRoundMass_kg;
            this.capacity = b.capacity;
            if (b.currentLoad >= 0 && b.currentLoad <= capacity) {
                this.currentLoad = b.currentLoad;
            } else {
                this.currentLoad = capacity;
            }
        }
    }

    public double getMass_kg() {
        return emptyMass_kg + currentLoad * perRoundMass_kg;
    }

    /**
     * Removes
     *
     * @param numToExpend the desired number of rounds to expend
     * @return the number of rounds expended
     */
    public int expendRounds(final int numToExpend) {
        if (numToExpend < 0) {
            throw new IllegalArgumentException("numToExpend must be non-negative");
        }
        int shot = Math.min(numToExpend, currentLoad);
        currentLoad -= shot;
        return shot;
    }

    public boolean isEmpty() {
        return currentLoad == 0;
    }
}

