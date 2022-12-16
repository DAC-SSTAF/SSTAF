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

class GunTest {

    @Test
    void canCreateDefault() {
        var gun = Gun.builder().build();
        Assertions.assertNotNull(gun);
        Assertions.assertEquals("Gun", gun.getName());
    }

    @Test
    void builderWorks() {
        final String NAME = "M4";
        final double EMPTY_MASS = 3.5;

        var builder = Gun.builder();
        builder.name(NAME);
        builder.emptyMass_kg(EMPTY_MASS);
        var gun = builder.build();
        Assertions.assertEquals(NAME, gun.getName());
        Assertions.assertEquals(EMPTY_MASS, gun.getEmptyMass_kg());
        Assertions.assertEquals(EMPTY_MASS, gun.getMass_kg());
    }

    @Test
    void magazineAndAmmoManagementWorks() {

        final String NAME = "M4";
        final double EMPTY_MASS = 3.5;

        var builder = Gun.builder();
        builder.name(NAME);
        builder.emptyMass_kg(EMPTY_MASS);
        var gun = builder.build();

        var mb1 = Magazine.builder();
        mb1.name("mag1");
        var mag1 = mb1.build();

        var mb2 = Magazine.builder();
        mb2.name("mag2");
        var mag2 = mb1.build();

        Assertions.assertEquals(NAME, gun.getName());
        Assertions.assertEquals(EMPTY_MASS, gun.getEmptyMass_kg());
        Assertions.assertEquals(EMPTY_MASS, gun.getMass_kg());

        var massMag1 = mag1.getMass_kg();
        var massMag2 = mag2.getMass_kg();

        gun.loadMagazine(mag1);
        Assertions.assertEquals(EMPTY_MASS + massMag1, gun.getMass_kg());
        while (gun.canShoot()) {
            int shot = gun.shoot(1);
            Assertions.assertTrue(shot > 0);
        }
        Assertions.assertEquals(EMPTY_MASS + mag1.getMass_kg(), gun.getMass_kg());
        Assertions.assertEquals(EMPTY_MASS + mag1.getEmptyMass_kg(), gun.getMass_kg());

        gun.loadMagazine(mag2);
        Assertions.assertEquals(EMPTY_MASS + massMag2, gun.getMass_kg());
        while (gun.canShoot()) {
            int shot = gun.shoot(3);
            Assertions.assertTrue(shot > 0);
        }
        Assertions.assertEquals(EMPTY_MASS + mag2.getMass_kg(), gun.getMass_kg());
        Assertions.assertEquals(EMPTY_MASS + mag2.getEmptyMass_kg(), gun.getMass_kg());

        gun.dropMagazine();
        Assertions.assertEquals(EMPTY_MASS, gun.getEmptyMass_kg());
        Assertions.assertEquals(EMPTY_MASS, gun.getMass_kg());
    }
}

