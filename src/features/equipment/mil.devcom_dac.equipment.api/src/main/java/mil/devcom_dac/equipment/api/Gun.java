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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@SuperBuilder
@Jacksonized
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
@EqualsAndHashCode(callSuper = true)
public class Gun extends Item {

    @Getter
    private double emptyMass_kg;

    @Getter
    private String magazineType;

    @Getter
    @JsonIgnore
    @Builder.Default
    private Magazine magazine = null;

    private Gun(GunBuilder<?, ?> b) {
        super(b);
        this.emptyMass_kg = b.emptyMass_kg;
        this.magazineType = b.magazineType;
    }

    public double getMass_kg() {
        return emptyMass_kg + (magazine == null ? 0.0 : magazine.getMass_kg());
    }

    public void loadMagazine(final Magazine magazine) {
        this.magazine = magazine;
    }

    public void dropMagazine() {
        this.magazine = null;
    }

    public boolean canShoot() {
        return magazine != null && !(magazine.isEmpty());
    }

    public int shoot(int numToShoot) {
        return magazine.expendRounds(numToShoot);
    }


}

