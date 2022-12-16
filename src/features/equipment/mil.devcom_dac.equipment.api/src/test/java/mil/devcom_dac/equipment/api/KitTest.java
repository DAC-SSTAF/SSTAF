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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import mil.sstaf.core.json.JsonLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class KitTest {

    @Test
    void canCreateDefault() {
        Kit kit = Kit.builder().build();
        assertNotNull(kit);
        assertEquals(0.0, kit.getMass());
    }

    @Test
    void builderWorks() {
        Kit kit = Kit.builder().build();

        var mag1 = Magazine.builder().build();
        var mag2 = Magazine.builder().build();
        var gun1 = Gun.builder().build();
        var gun2 = Gun.builder().name("M17").build();
        var pack1 = Pack.builder().build();
        var pack2 = Pack.builder().name("Fanny").build();

        kit.add(mag1);
        kit.add(mag2);
        kit.add(gun1);
        kit.add(gun2);
        kit.add(pack1);
        kit.add(pack2);

        assertEquals(2, kit.getMagazines(mag1.getMagazineType()).size());
        assertEquals(0, kit.getMagazines("M14 20rd").size());
        assertEquals(2, kit.getGuns().size());
        assertEquals(2, kit.getPacks().size());
    }

    @Test
    void addAndDropWork() {
        var kit = Kit.builder().build();
        var gun = Gun.builder().build();
        var mag = Magazine.builder().build();
        var pack = Pack.builder().build();

        kit.add(gun);
        assertEquals(gun.getMass_kg(), kit.getMass());

        kit.add(pack);
        assertEquals(gun.getMass_kg() + pack.getMass_kg(), kit.getMass());

        kit.add(mag);
        assertEquals(gun.getMass_kg() + pack.getMass_kg() + mag.getMass_kg(),
                kit.getMass());

        kit.drop(mag);
        assertEquals(gun.getMass_kg() + pack.getMass_kg(),
                kit.getMass());

        kit.drop(pack);
        assertEquals(gun.getMass_kg(), kit.getMass());

        kit.drop(gun);
        assertEquals(0, kit.getMass());

    }

    @Test
    void nullsCauseThrows() {
        var kit = Kit.builder().build();
        assertThrows(IllegalArgumentException.class,
                () -> kit.add((Gun) null));
        assertThrows(IllegalArgumentException.class,
                () -> kit.add((Magazine) null));
        assertThrows(IllegalArgumentException.class,
                () -> kit.add((Pack) null));
        assertThrows(IllegalArgumentException.class,
                () -> kit.drop((Gun) null));
        assertThrows(IllegalArgumentException.class,
                () -> kit.drop((Magazine) null));
        assertThrows(IllegalArgumentException.class,
                () -> kit.drop((Pack) null));
    }

    @Test
    void reloadLogicWorks() {
        var bldr = Magazine.builder();
        bldr.magazineType("5.56mm STANAG");


        var mag4_1 = bldr.build();
        var mag4_2 = bldr.build();
        var mag4_3 = bldr.build();
        var mag4_4 = bldr.build();

        var bldr14 = Magazine.builder();
        bldr14.magazineType("7.62x51 M14");


        var mag14_1 = bldr14.build();
        var mag14_2 = bldr14.build();
        var mag14_3 = bldr14.build();
        var mag14_4 = bldr14.build();

        var bldr_m4 = Gun.builder();
        bldr_m4.name("M4");
        bldr_m4.magazineType("5.56mm STANAG");
        var m4 = bldr_m4.build();

        var bldr_m14 = Gun.builder();
        bldr_m14.name("M14");
        bldr_m14.magazineType("7.62x51 M14");
        var m14 = bldr_m14.build();

        var bldr_m41 = Gun.builder();
        bldr_m41.name("M41");
        bldr_m41.magazineType("10mm Caseless");
        var m41 = bldr_m41.build();


        var kit = Kit.builder().build();
        kit.add(mag4_1);
        kit.add(mag4_2);
        kit.add(mag4_3);
        kit.add(mag4_4);
        kit.add(mag14_1);
        kit.add(mag14_2);
        kit.add(mag14_3);
        kit.add(mag14_4);

        assertTrue(kit.canReload(m4));
        assertTrue(kit.canReload(m14));
        assertFalse(kit.canReload(m41));

        kit.drop(mag14_1);
        kit.drop(mag14_2);
        kit.drop(mag14_3);
        kit.drop(mag14_4);
        assertFalse(kit.canReload(m14));

        assertTrue(kit.reload(m4));
        assertFalse(kit.reload(m14));
        assertFalse(kit.reload(m41));
    }


    @Test
    void aFullScenarioWorks() {
        var bldr = Magazine.builder();
        bldr.magazineType("5.56mm STANAG");
        var mag1 = bldr.build();
        var mag2 = bldr.build();
        var mag3 = bldr.build();
        var mag4 = bldr.build();
        var mag5 = bldr.build();
        var mag6 = bldr.build();
        var mag7 = bldr.build();

        var gbldr = Gun.builder();
        gbldr.name("M4");
        gbldr.magazineType("5.56mm STANAG");
        var gun = gbldr.build();

        var pb = Pack.builder();
        pb.mass_kg(30);
        var pack = pb.build();
        assertEquals(30.0, pack.getMass_kg());

        var kit = Kit.builder().build();
        kit.add(gun);
        kit.add(pack);
        kit.add(mag1);
        kit.add(mag2);
        kit.add(mag3);
        kit.add(mag4);
        kit.add(mag5);
        kit.add(mag6);
        kit.add(mag7);

        Item[] allItems = {mag1, mag2, mag3, mag4, mag5, mag6, mag7, gun, pack};
        double totalMass = 0.0;

        for (Item item : allItems) {
            totalMass += item.getMass_kg();
        }

        assertEquals(totalMass, kit.getMass());

        //
        // Load the gun.
        //
        double expectedMass = totalMass;
        int magsDumped = 0;
        while (kit.canReload(gun)) {
            kit.reload(gun);
            while (gun.canShoot()) {
                gun.shoot(1);
                expectedMass = expectedMass - mag1.getPerRoundMass_kg();
                assertEquals(expectedMass, kit.getMass(), 0.000001);
            }
            assertEquals(gun.getEmptyMass_kg() + mag1.getEmptyMass_kg(), gun.getMass_kg());
            gun.dropMagazine();
            ++magsDumped;
            expectedMass -= mag1.getEmptyMass_kg();
            assertEquals(gun.getEmptyMass_kg(), gun.getMass_kg());
            assertEquals(expectedMass, kit.getMass(), 0.000001);
        }
        assertEquals(pack.getMass_kg() + gun.getEmptyMass_kg(), kit.getMass(), 0.000001);
        assertEquals(7, magsDumped);
    }

    @Test
    void emptySpecificationProduceEmptyKit() {
        Map<String, Object> map = Collections.emptyMap();
        EquipmentConfiguration equipmentConfiguration =
                EquipmentConfiguration.builder().build();
        Kit kit = equipmentConfiguration.getKit();
        assertNotNull(kit);
        assertEquals(0.0, kit.getMass());
    }


    @Test
    void goodFileWorks() {
        String kitFile = "src/test/resources/TestKit.json";
        Kit kit = new JsonLoader().load(Path.of(kitFile), Kit.class);
        assertEquals(4, kit.getMagazines("5.56mm STANAG").size());
        assertEquals(1, kit.getGuns().size());
        assertEquals(1, kit.getPacks().size());
    }

}

