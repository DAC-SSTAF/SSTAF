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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HumanTest {
    private static final String uuidPattern =
            "([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})";


    @Test
    void settingNameAndToStringWork() {
        var person = Human.builder().build();
        person.setName("Leroy Brown");
        String s = person.toString();
        assertTrue(s.contains("Leroy Brown"));
        assertTrue(s.contains("definitionName='UNSPECIFIED'"));
    }

    @Test
    void namingBehaviorWorks() {
        Human h1 = Human.builder().build();

        assertTrue(h1.getName().matches(uuidPattern), "Name was not a UUID");
        assertEquals("UNSPECIFIED", h1.getDefinitionName(), "Definition names did not match");

        var hb = Human.builder();
        hb.definitionName("Bob");
        Human h2 = hb.build();
        assertTrue(h2.getName().matches(uuidPattern));
        assertEquals("Bob", h2.getDefinitionName(), "Definition names did not match");

        hb = Human.builder();
        hb.definitionName("Super-Soldier");
        hb.name("Steve Rogers");
        Human h3 = hb.build();
        assertEquals("Super-Soldier", h3.getDefinitionName());
        assertEquals("Steve Rogers", h3.getName());
    }

    @Test
    void canSpecifyProvidersAndInitialize() {
        Human h1 = Human.builder().build();
        h1.setForce(Force.GRAY);

        FeatureManager featureManager = h1.getFeatureManager();
        featureManager.register(new StringThing());
        featureManager.register(new Bananarama());

        Assertions.assertDoesNotThrow(h1::init);
    }

    @Test
    void equalityAndHash() {
        Human person = Human.builder().build();
        Human anotherPerson = Human.builder().build();

        assertEquals(person, person);
        assertEquals(anotherPerson, anotherPerson);
        assertNotEquals(person, anotherPerson);
        assertNotEquals(person.hashCode(), anotherPerson.hashCode());

        final class Junk {
        }
        Junk notAPerson = new Junk();
        assertNotEquals(person, notAPerson);

        Human.HumanBuilder<?, ?> hb = Human.builder();
        hb.name("Pat");
        Human personNamedPat = hb.build();
        assertNotEquals(person, personNamedPat);
    }

    interface StringProvider extends Feature {

    }

    interface BananaProvider extends Feature {

    }

    static class StringThing extends BaseFeature implements StringProvider {

        public StringThing() {
            super("Senator Vrenak", 3, 1, 4, false, "It's a fake!!!!");
        }


        @Override
        public Class<? extends FeatureConfiguration> getConfigurationClass() {
            return FeatureConfiguration.class;
        }
    }

    static class Bananarama extends BaseFeature implements BananaProvider {

        public Bananarama() {
            super("Bananarama", 80, 81, 82, false, "Pop!");
        }

        @Override
        public Class<? extends FeatureConfiguration> getConfigurationClass() {
            return FeatureConfiguration.class;
        }
    }

}
