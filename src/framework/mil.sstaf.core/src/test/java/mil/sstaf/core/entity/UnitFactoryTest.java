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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mil.sstaf.core.json.JsonLoader;
import mil.sstaf.core.util.SSTAFException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UnitFactoryTest {


    static Path USER_DIR = Path.of(System.getProperty("user.dir"));
    static Path RESOURCE_DIR = Path.of(USER_DIR.toString(), "src/test/resources/UnitFactoryTest");

    private File makeFile(String filename) {
        Path path = Path.of(RESOURCE_DIR.toString(), filename);
        return path.toFile();
    }

    @Test
    void aUnitWithJustSoldiersWorks() {
        String filename = "FireTeam.json";
        Unit unit = Unit.from(makeFile(filename));
        assertNotNull(unit);
        assertEquals(4, unit.getAllMembers().size());
    }

    @Test
    void aUnitHierarchyWorks() {
        String filename = "Squad.json";
        Unit unit = Unit.from(makeFile(filename));
        assertNotNull(unit);
        assertEquals(9, unit.getAllMembers().size());
    }

    @Test
    void canBuildACompany() {
        String filename = "Company.json";
        Unit unit = Unit.from(makeFile(filename));
        assertNotNull(unit);
        assertEquals(90, unit.getAllMembers().size());
    }

    @Test
    void goodJSONWorks() {

        ObjectMapper om = new ObjectMapper();

        ArrayNode soldierList = om.createArrayNode();

        ObjectNode soldier1 = om.createObjectNode();
        soldier1.put("position", "TL");
        soldier1.put("soldier", "../SoldierFactoryTest/TestSoldier1.json");
        soldierList.add(soldier1);

        ObjectNode soldier2 = om.createObjectNode();
        soldier2.put("position", "R");
        soldier2.put("soldier", "../SoldierFactoryTest/TestSoldier1.json");
        soldierList.add(soldier2);

        ObjectNode soldier3 = om.createObjectNode();
        soldier3.put("position", "G");
        soldier3.put("soldier", "../SoldierFactoryTest/TestSoldier1.json");
        soldierList.add(soldier3);

        ObjectNode soldier4 = om.createObjectNode();
        soldier4.put("position", "AR");
        soldier4.put("soldier", "../SoldierFactoryTest/TestSoldier1.json");
        soldierList.add(soldier4);

        ObjectNode fireteam = om.createObjectNode();
        fireteam.put("class", "mil.sstaf.core.entity.Unit");
        fireteam.put("name", "Test FT");
        fireteam.put("type", "FireTeam");

        Path fakePath = Path.of(System.getProperty("user.dir"), "src/test/resources/UnitFactoryTest");
        fireteam.set("soldiers", soldierList);
        fireteam.set("features", om.createArrayNode());
        fireteam.set("configurations", om.createObjectNode());

        Path p  = Path.of(RESOURCE_DIR.toString(),"fake.json");
        Unit unit = Unit.from(fireteam, p);
        assertNotNull(unit);
        assertEquals(4, unit.getAllMembers().size());
    }

    @Test
    void badJSONThrows() {
        ObjectMapper om = new ObjectMapper();
        assertThrows(SSTAFException.class, () -> {
            ObjectNode objectNode = om.createObjectNode();
            Unit unit = Unit.from(objectNode, RESOURCE_DIR
            );
            assertNotNull(unit);
        });
    }

    @Test
    void missingFileThrows() {
        assertThrows(SSTAFException.class, () -> {
                    String filename = "NotThere.json";
                    Unit unit = Unit.from(makeFile(filename));
                    assertNull(unit);
                }
        );
    }

    @Test
    void badFileThrows() {
        assertThrows(SSTAFException.class, () -> {
                    String filename = "BadUnit1.json";
                    Unit unit = Unit.from(makeFile(filename));
                    assertNull(unit);
                }
        );
    }

    @Test
    void soldierWithoutPositionThrows() {
        assertThrows(SSTAFException.class, () -> {
                    String filename = "BadUnit2.json";
                    Unit unit = Unit.from(makeFile(filename));
                    assertNull(unit);
                }
        );
    }

    @Test
    void soldierWithoutDefinitionThrows() {
        assertThrows(SSTAFException.class, () -> {
                    String filename = "BadUnit3.json";
                    Unit unit = Unit.from(makeFile(filename));
                    assertNull(unit);
                }
        );
    }

    @Test
    void unitWithoutLabelThrows() {
        assertThrows(SSTAFException.class, () -> {
                    String filename = "BadUnit4.json";
                    Unit unit = Unit.from(makeFile(filename));
                    assertNull(unit);
                }
        );
    }

    @Test
    void unitWithoutDefinitionThrows() {
        assertThrows(SSTAFException.class, () -> {
                    String filename = "BadUnit5.json";
                    Unit unit = Unit.from(makeFile(filename));
                    assertNull(unit);
                }
        );
    }
}

