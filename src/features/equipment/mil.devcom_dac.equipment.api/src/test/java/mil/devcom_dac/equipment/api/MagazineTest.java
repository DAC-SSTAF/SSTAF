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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MagazineTest {
    @Test
    void canCreateDefault() {
        var magazine = Magazine.builder().build();

        Assertions.assertNotNull(magazine);
        Assertions.assertEquals(30, magazine.getCapacity());
        Assertions.assertEquals(30, magazine.getCurrentLoad());
        Assertions.assertEquals(0.01231, magazine.getPerRoundMass_kg());
        Assertions.assertEquals(0.1207, magazine.getEmptyMass_kg());
        Assertions.assertEquals(0.490, magazine.getMass_kg());
        Assertions.assertEquals("5.56mm STANAG 30rd", magazine.getName());
        Assertions.assertEquals("5.56mm STANAG", magazine.getMagazineType());
    }

    @Test
    void builderWorks() {

        final double EMPTY_MASS = 20.0;
        final double ROUND_MASS = 4.0;
        final int CAPACITY = 4;
        final int LOAD = 3;
        final String NAME = "Fred";
        final String GUN_NAME = "Phaser";

        var builder = Magazine.builder();
        builder.perRoundMass_kg(ROUND_MASS);
        builder.name(NAME);
        builder.emptyMass_kg(EMPTY_MASS);
        builder.magazineType(GUN_NAME);
        builder.capacity(CAPACITY);
        builder.currentLoad(LOAD);
        var magazine = builder.build();

        Assertions.assertEquals(CAPACITY, magazine.getCapacity());
        Assertions.assertEquals(LOAD, magazine.getCurrentLoad());
        Assertions.assertEquals(ROUND_MASS, magazine.getPerRoundMass_kg());
        Assertions.assertEquals(EMPTY_MASS, magazine.getEmptyMass_kg());
        Assertions.assertEquals(EMPTY_MASS + ROUND_MASS * LOAD, magazine.getMass_kg());
        Assertions.assertEquals(NAME, magazine.getName());
        Assertions.assertEquals(GUN_NAME, magazine.getMagazineType());
    }

    @Test
    void singleRoundExpenditureWorks() {

        var magazine = Magazine.builder().build();

        int lastRoundCount = magazine.getCurrentLoad();
        int initialCount = lastRoundCount;
        int totalFired = 0;

        while (magazine.expendRounds(1) > 0) {
            ++totalFired;
            Assertions.assertEquals(lastRoundCount - 1, magazine.getCurrentLoad());
            lastRoundCount = magazine.getCurrentLoad();
        }

        Assertions.assertEquals(magazine.getEmptyMass_kg(), magazine.getMass_kg());
        Assertions.assertEquals(initialCount, totalFired);
    }

    @Test
    void multiRoundExpenditureAndIsEmptyWorks() {

        var magazine = Magazine.builder().build();

        int lastRoundCount = magazine.getCurrentLoad();
        final int initialCapacity = lastRoundCount;
        int totalFired = 0;

        final int NUM_TO_FIRE = 3;
        while (!magazine.isEmpty()) {
            magazine.expendRounds(NUM_TO_FIRE);
            totalFired += NUM_TO_FIRE;

            Assertions.assertEquals(lastRoundCount - NUM_TO_FIRE, magazine.getCurrentLoad());
            lastRoundCount = magazine.getCurrentLoad();
        }
        Assertions.assertEquals(magazine.getEmptyMass_kg(), magazine.getMass_kg());
        Assertions.assertEquals(initialCapacity, totalFired);
    }

    @Test
    void negativeExpenditureThrows() {
        var b = Magazine.builder();
        b.capacity(60);
        var mag1 = b.build();
        var mag2 = Magazine.builder()
                .magazineType(mag1.getMagazineType())
                .currentLoad(mag1.getCurrentLoad())
                .emptyMass_kg(mag1.getMass_kg())
                .build();
        Assertions.assertThrows(IllegalArgumentException.class, () -> mag1.expendRounds(-121));
    }

}

