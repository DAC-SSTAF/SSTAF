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


import mil.sstaf.core.features.BaseFeature;
import mil.sstaf.core.features.Feature;
import mil.sstaf.core.features.FeatureConfiguration;
import org.junit.jupiter.api.Test;

import static mil.sstaf.core.entity.Rank.E7;
import static org.junit.jupiter.api.Assertions.*;

class SoldierTest {

    private final Soldier defaultSoldier = Soldier.builder().build();

    @Test
    void emptyBuildYieldsDefaults() {
        var s = Soldier.builder().build();
        assertEquals(Rank.E1, s.getRank());
    }


    @Test
    void equalityTests() {
        Soldier anotherDefaultSoldier = Soldier.builder().build();

        assertEquals(defaultSoldier, defaultSoldier);
        assertNotEquals(defaultSoldier, anotherDefaultSoldier); // UUIDs are different
        assertNotEquals(defaultSoldier.hashCode(), anotherDefaultSoldier.hashCode()); // ditto

        var sb = Soldier.builder();
        sb.rank(Rank.E3.getCode());
        Soldier officer = sb.build();
        sb.rank(Rank.E6.getCode());
        Soldier oldSoldier = sb.build();

        sb = Soldier.builder();
        sb.name("Joe");
        Soldier giJoe = sb.build();

        // different soldiers are not equal to the default soldier
        assertNotEquals(defaultSoldier, oldSoldier, "Failed to check ages"); // a changed anthro value (age)
        assertNotEquals(defaultSoldier, officer, "Failed to check ranks"); // changes soldier value (rank)
        assertNotEquals(defaultSoldier, giJoe, "Failed to check names"); // a changed human value (name)
    }

    @Test
    void soldierBuildReturnsASoldier() {
        assertEquals(Soldier.class, Soldier.builder().build().getClass());
    }

    @Test
    void namingWorks() {

        var  sb = Soldier.builder();
        sb.name("Bob");
        Soldier s1 = sb.build();

        var ub1 = Unit.builder();
        ub1.name("Unit 1");
        ub1.soldier(Unit.MemberSoldier.of("Commander", s1));
        Unit u1 = ub1.build();

        var ub2 = Unit.builder();
        ub2.name("Unit 2");
        Unit u2 = ub2.build();

        var ub3 = Unit.builder();
        ub3.name("Unit 3");
        Unit u3 = ub3.build();

        var ub4 = Unit.builder();
        ub4.name("Unit 4");
        Unit u4 = ub4.build();

        u1.attach(u2, u1.getName());
        u2.attach(u3, u2.getName());
        u3.attach(u4, u3.getName());

        assertEquals("Commander", s1.getPosition());
        String path = s1.getPath();
        assertEquals("Unit 4:Unit 3:Unit 2:Unit 1:Commander", path);


        var sb1 = Soldier.builder();
        sb1.name("Bob");
        s1 = sb1.build();

        ub1 = Unit.builder();
        ub1.name("Unit 1");
        u1 = ub1.build();

        ub2 = Unit.builder();
        ub2.name("Unit 2");
        u2 = ub2.build();

        ub3 = Unit.builder();
        ub3.name("Unit 3");
        u3 = ub3.build();

        ub4 = Unit.builder();
        ub4.name("Unit 4");
        u4 = ub4.build();

        u1.attach(u2, u1.getName());
        u2.attach(u3, u2.getName());
        u3.attach(u4, u3.getName());

        u1.addSoldier(s1);

        assertEquals(u1, s1.getUnit());
        assertEquals("Unit 4:Unit 3:Unit 2:Unit 1:Bob", s1.getPath());
    }

    @Test
    void canSpecifyProvidersAndInitialize() {
        HumanTest.StringThing ip = new HumanTest.StringThing();
        HumanTest.Bananarama kp = new HumanTest.Bananarama();
        ArcticCircle ap = new ArcticCircle();
        FiveGuys tp = new FiveGuys();
        var sb1 = Soldier.builder();
        Soldier s1 = sb1.build();
        s1.setForce(Force.BLUE);

        FeatureManager featureManager = s1.getFeatureManager();
        featureManager.register(ip);
        featureManager.register(kp);
        featureManager.register(ap);
        featureManager.register(tp);

        s1.init();
        assertTrue(ip.isInitialized());
        assertTrue(kp.isInitialized());
        assertTrue(ap.isInitialized());
        assertTrue(tp.isInitialized());
    }

    @Test
    void otherStuff() {
        var sb = Soldier.builder();
        sb.name("Fred");
        sb.rank(E7.getCode());
        Soldier s = sb.build();
        assertTrue(s.toString().contains("Fred"));
        assertEquals(Rank.E7, s.getRank());
    }

    interface MilkshakeProvider extends Feature {

    }

    interface BurgerProvider extends Feature {

    }


    static class ArcticCircle extends BaseFeature implements MilkshakeProvider {

        public ArcticCircle() {
            super("Arctic Circle", 0, 0, 0, false, "Churchville");
        }

        @Override
        public Class<? extends FeatureConfiguration> getConfigurationClass() {
            return FeatureConfiguration.class;
        }
    }

    static class FiveGuys extends BaseFeature implements BurgerProvider {
        public FiveGuys() {
            super("Five Guys", 0, 0, 5, false, "Rather expensive");
        }
        @Override
        public Class<? extends FeatureConfiguration> getConfigurationClass() {
            return FeatureConfiguration.class;
        }
    }
}

