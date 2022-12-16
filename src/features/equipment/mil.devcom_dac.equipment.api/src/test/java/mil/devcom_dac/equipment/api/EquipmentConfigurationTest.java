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

import mil.sstaf.core.json.JsonLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EquipmentConfigurationTest {
    @Test
    void canCreateDefault() {
        EquipmentConfiguration ec = EquipmentConfiguration.builder().build();
        Kit kit = ec.getKit();
        assertNotNull(kit);
        assertEquals(0.0, kit.getMass());
    }

    @Test
    void goodFileWorks() {
        String kitFile = "src/test/resources/TestConfig.json";
        EquipmentConfiguration equipmentConfiguration =
                new JsonLoader().load(Path.of(kitFile), EquipmentConfiguration.class);

        assertNotNull(equipmentConfiguration);
        Kit kit = equipmentConfiguration.getKit();
        assertNotNull(kit);
        assertEquals(4, kit.getMagazines("5.56mm STANAG").size());
        assertEquals(1, kit.getGuns().size());
        assertEquals(1, kit.getPacks().size());
    }
}

