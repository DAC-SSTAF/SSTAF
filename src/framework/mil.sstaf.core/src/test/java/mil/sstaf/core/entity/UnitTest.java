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

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UnitTest {

    @Test
    void canCreateDefaultUnit() {
        Unit unit = Unit.builder().build();
        assertNotNull(unit);
        assertTrue(unit.getName()
                .matches("([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}){1}"));
        unit.setName("It");
        assertEquals("It", unit.getName());
    }

    private Soldier makeFreePeople(final String def, final String name) {
        var d1b = Soldier.builder();
        d1b.definitionName(def);
        d1b.name(name);
        return d1b.build();
    }

    private Soldier makeDwarf(final String name) {
        return makeFreePeople("Dwarf", name);
    }

    private Soldier makeHobbit(final String name) {
        return makeFreePeople("Hobbit", name);
    }

    @Test
    void canBuildAndManipulateUnitHierarchy() {


        var d1 = makeDwarf("Thorin");
        var d2 = makeDwarf("Balin");
        var d3 = makeDwarf("Dwalin");
        var d4 = makeDwarf("Oin");
        var d5 = makeDwarf("Gloin");
        var d6 = makeDwarf("Kili");
        var d7 = makeDwarf("Fili");
        var d8 = makeDwarf("Bifur");
        var d9 = makeDwarf("Bofur");
        var d10 = makeDwarf("Bombur");
        var d11 = makeDwarf("Ori");
        var d12 = makeDwarf("Dori");
        var d13 = makeDwarf("Nori");
        var h1 = makeDwarf("Hobbit");

        var ft1b = Unit.builder();
        ft1b.type(UnitType.FireTeam);
        ft1b.soldier(Unit.MemberSoldier.of("Commander", d10));
        ft1b.soldier(Unit.MemberSoldier.of("Ax 1", d11));
        ft1b.soldier(Unit.MemberSoldier.of("Ax 2", d12));
        ft1b.soldier(Unit.MemberSoldier.of("Miner", d13));
        ft1b.name("Fire Team 1");
        var ft1 = ft1b.build();

        var ft2b = Unit.builder();
        ft2b.type(UnitType.FireTeam);
        ft2b.name("Fire Team 2");
        ft2b.soldier(Unit.MemberSoldier.of("Commander", d6));
        ft2b.soldier(Unit.MemberSoldier.of("Ax 1", d7));
        ft2b.soldier(Unit.MemberSoldier.of("Ax 2", d8));
        ft2b.soldier(Unit.MemberSoldier.of("Miner", d9));
        var ft2 = ft2b.build();

        var ft3b = Unit.builder();
        ft3b.type(UnitType.FireTeam);
        ft3b.name("Fire Team 3");
        ft3b.soldier(Unit.MemberSoldier.of("Commander", d2));
        ft3b.soldier(Unit.MemberSoldier.of("Ax 1", d3));
        ft3b.soldier(Unit.MemberSoldier.of("Ax 2", d4));
        ft3b.soldier(Unit.MemberSoldier.of("Miner", d5));
        var ft3 = ft3b.build();

        var sq1b = Unit.builder();
        sq1b.type(UnitType.Squad);
        sq1b.name("Thorin's Squad");
        sq1b.subUnit(Unit.MemberUnit.of("Fire Team 1", ft1));
        sq1b.soldier(Unit.MemberSoldier.of("G2", h1));
        sq1b.soldier(Unit.MemberSoldier.of("Commander", d1));
        var sq1 = sq1b.build();

        assertEquals(6, sq1.getNumMembers());

        sq1.addUnit(ft2.getName(), ft2);
        sq1.addUnit(ft3.getName(), ft3);

        assertEquals(UnitType.Squad, sq1.getType());
        assertEquals(UnitType.FireTeam, ft1.getType());
        assertEquals(UnitType.FireTeam, ft2.getType());
        assertEquals(UnitType.FireTeam, ft3.getType());


        //
        // Check count
        //
        assertEquals(14, sq1.getNumMembers());
        assertEquals(14, sq1.getAllMembers().size());

        assertNotNull(sq1.getSoldierByName("Kili"));
        assertEquals(d10, sq1.getSoldierByName("Bombur"));
        assertNull(sq1.getSoldierByName("Gandalf"));

        assertNotNull(ft1.getSoldierByName("Ori"));
        ft1.transferSoldierTo("Ori", ft3, "Cook");
        assertNotNull(ft3.getSoldierByName("Ori"));
        assertNull(ft1.getSoldierByName("Ori"));
        assertNotNull(sq1.getSoldierByName("Ori"));

        Unit where = sq1.getSoldierByName("Ori").getUnit();
        assertNotNull(where);
        where.removeSoldierByName("Ori");
        assertNull(sq1.getSoldierByName("Ori"));
        assertEquals(13, sq1.getNumMembers());

        sq1.setCommanderByName(h1.getName());
        assertEquals(h1, sq1.getCommander());

        sq1.setCommanderByName("Ori");
        assertEquals(h1, sq1.getCommander());

        var h2 = makeHobbit("Frodo");
        var h3 = makeHobbit("Sam");
        var h4 = makeHobbit("Merry");
        var h5 = makeHobbit("Pippin");

        var ft4b = Unit.builder();
        ft4b.type(UnitType.FireTeam);
        ft4b.name("Ring Team");
        ft4b.soldier(Unit.MemberSoldier.of("Commander", h2));
        ft4b.soldier(Unit.MemberSoldier.of("Gardener", h3));
        ft4b.soldier(Unit.MemberSoldier.of("Wraith Stabber", h4));
        ft4b.soldier(Unit.MemberSoldier.of("Kid", h5));
        var ft4 = ft4b.build();
        sq1.addUnit(ft4.getName(), ft4);

        assertEquals("Gardener", sq1.getPositionForSoldierByName("Sam"));

        assertEquals(17, sq1.getNumMembers());
        assertEquals(h3, sq1.getSoldierByName("Sam"));

        Soldier found1 = sq1.getSoldierByPosition("Thorin's Squad:Ring Team:Kid");
        assertEquals(h5, found1);

        Soldier notFound = sq1.getSoldierByPosition("Thorin's Squad:Ring Team:Ax 1");
        assertNull(notFound);

        notFound = sq1.getSoldierByPosition("::Ring Team:Ax 1");
        assertNull(notFound);

        Soldier found2 = sq1.getSoldierByPosition("Thorin's Squad:Commander");
        assertEquals(h1, found2);

        Soldier found3 = sq1.getSoldierByPosition("Commander");
        assertEquals(h1, found3);

        notFound = sq1.getSoldierByPosition("Thorin's Squad:Wraiths:Witch King");
        assertNull(notFound);

        Map<String, Unit> all = sq1.getAllUnits();
        assertNotNull(all);
        assertEquals(5, all.size());
        assertTrue(all.containsKey("Thorin's Squad:Ring Team"));

        int countBefore = ft1.getNumMembers();
        ft1.transferSoldierTo("Rosie", ft2, "Bar Maid");
        assertEquals(countBefore, ft1.getNumMembers());

        Soldier h6 = makeHobbit("Fatty");
        assertNull(sq1.getPositionForSoldierByName("Tom Cotton"));

        ft4.addSoldier("Decoy", h6);
        assertEquals(5, ft4.getNumMembers());
        ft4.removeSoldierByName("Frodo");
        assertEquals(4, ft4.getNumMembers());
        ft4.setCommanderByName("Fatty");
        assertEquals(h6, ft4.getCommander());

        h6.setUnit(ft4, null);
        assertThrows(IllegalStateException.class, () -> ft4.removeSoldierByName("Fatty"));

        h6.setUnit(null, "Decoy");
        assertThrows(IllegalStateException.class, () -> ft4.removeSoldierByName("Fatty"));

        ft4.detach();

        assertEquals(13, sq1.getNumMembers());
    }

    @Test
    void checkThatCheckerWork() {
        var h2 = makeHobbit("Frodo");
        var h3 = makeHobbit("Sam");
        var h4 = makeHobbit("Merry");
        var h5 = makeHobbit("Pippin");

        var ft4b = Unit.builder();
        ft4b.type(UnitType.FireTeam);
        ft4b.name("Ring Team");
        ft4b.soldier(Unit.MemberSoldier.of("Commander", h2));
        ft4b.soldier(Unit.MemberSoldier.of("Gardener", h3));
        ft4b.soldier(Unit.MemberSoldier.of("Wraith Stabber", h4));
        ft4b.soldier(Unit.MemberSoldier.of("Kid", h5));
        var ft4 = ft4b.build();

        assertEquals(4, ft4.getNumMembers());

        assertThrows(NullPointerException.class, () -> ft4.removeSoldierByName(null));

        assertThrows(NullPointerException.class, () -> ft4.getSoldierByName(null));

        assertThrows(NullPointerException.class, () -> ft4.getSoldierByPosition(null));

        assertThrows(NullPointerException.class, () -> ft4.setCommanderByName(null));

        assertThrows(NullPointerException.class, () -> ft4.addSoldier(null, h5));

        assertThrows(NullPointerException.class, () -> ft4.addSoldier(null));

        assertThrows(NullPointerException.class, () -> ft4.addSoldier("Boss", null));

        assertThrows(NullPointerException.class, () -> ft4.addUnit(null, null));
    }

    @Test
    void testBuilderBehavior() {

        var builder = Unit.builder();

        assertThrows(NullPointerException.class, () -> {
            Soldier s = Soldier.builder().build();
            builder.soldier(Unit.MemberSoldier.of(null, s));
        });

        assertThrows(NullPointerException.class, () -> builder.soldier(Unit.MemberSoldier.of("Commander", null)));

        assertThrows(NullPointerException.class, () -> {
            Unit u = Unit.builder().build();
            builder.subUnit(Unit.MemberUnit.of(null, u));
        });

        assertDoesNotThrow(() -> {
            Unit u = Unit.builder().build();
            builder.subUnit(Unit.MemberUnit.of("Fred", u));
        });

        assertDoesNotThrow(() -> {
            var b = Unit.builder();
            b.name("a");
            Unit u = b.build();
            builder.subUnit(Unit.MemberUnit.of("Unit", u));
        });

        assertThrows(NullPointerException.class, () -> {
            Unit.builder().build();
            builder.subUnit(Unit.MemberUnit.of("Fire Team", null));
        });
    }
}

