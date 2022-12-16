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

package mil.sstaftest.anthropometry.simple;

import mil.sstaf.core.features.Feature;
import mil.sstaftest.anthropometry.api.AnthroConfiguration;
import mil.sstaftest.anthropometry.api.Anthropometry;
import mil.sstaftest.anthropometry.api.Sex;
import org.junit.jupiter.api.Test;

import static mil.sstaftest.anthropometry.simple.AnthroConfig.*;
import static org.junit.jupiter.api.Assertions.*;

class AnthroConfigTest {
    @Test
    void emptyBuildYieldsDefaults() {
        var b = new AnthroConfig();
        assertEquals(DEFAULT_SEX, b.getSex());
        assertEquals(DEFAULT_AGE, b.getAge());
        assertEquals(DEFAULT_HEIGHT_CM, b.getHeight_cm(), 0.00001);
        assertEquals(DEFAULT_SPAN_CM, b.getSpan_cm(), 0.00001);
        assertEquals(DEFAULT_WEIGHT_KG, b.getWeight_kg(), 0.00001);
    }

    @Test
    void settingSexWorks() {
        AnthroConfig b = new AnthroConfig();
        b.setSex(Sex.FEMALE);
        assertEquals(Sex.FEMALE, b.getSex());
        assertEquals(DEFAULT_AGE, b.getAge());
        assertEquals(DEFAULT_HEIGHT_CM, b.getHeight_cm(), 0.00001);
        assertEquals(DEFAULT_SPAN_CM, b.getSpan_cm(), 0.00001);
        assertEquals(DEFAULT_WEIGHT_KG, b.getWeight_kg(), 0.00001);
    }

    @Test
    void settingAgeWorks() {
        AnthroConfig b = new AnthroConfig();
        b.setAge(33);
        assertEquals(DEFAULT_SEX, b.getSex());
        assertEquals(33, b.getAge());
        assertEquals(DEFAULT_HEIGHT_CM, b.getHeight_cm(), 0.00001);
        assertEquals(DEFAULT_SPAN_CM, b.getSpan_cm(), 0.00001);
        assertEquals(DEFAULT_WEIGHT_KG, b.getWeight_kg(), 0.00001);
    }

    @Test
    void settingHeightWorks() {
        AnthroConfig b = new AnthroConfig();
        b.setHeight_cm(180);
        assertEquals(DEFAULT_SEX, b.getSex());
        assertEquals(DEFAULT_AGE, b.getAge());
        assertEquals(180, b.getHeight_cm(), 0.00001);
        assertEquals(DEFAULT_SPAN_CM, b.getSpan_cm(), 0.00001);
        assertEquals(DEFAULT_WEIGHT_KG, b.getWeight_kg(), 0.00001);
    }

    @Test
    void settingSpanWorks() {
        AnthroConfig b = new AnthroConfig();
        b.setSpan_cm(170);
        assertEquals(DEFAULT_SEX, b.getSex());
        assertEquals(DEFAULT_AGE, b.getAge());
        assertEquals(DEFAULT_HEIGHT_CM, b.getHeight_cm(), 0.00001);
        assertEquals(170, b.getSpan_cm(), 0.00001);
        assertEquals(DEFAULT_WEIGHT_KG, b.getWeight_kg(), 0.00001);
    }

    @Test
    void settingWeightWorks() {
        AnthroConfig b = new AnthroConfig();
        b.setWeight_kg(100);
        assertEquals(DEFAULT_SEX, b.getSex());
        assertEquals(DEFAULT_AGE, b.getAge());
        assertEquals(DEFAULT_HEIGHT_CM, b.getHeight_cm(), 0.00001);
        assertEquals(DEFAULT_SPAN_CM, b.getSpan_cm(), 0.00001);
        assertEquals(100, b.getWeight_kg(), 0.00001);
    }

    @Test
    void settingEverythingWorks() {
        AnthroConfig b = new AnthroConfig();
        b.setSex(Sex.FEMALE);
        b.setWeight_kg(45);
        b.setHeight_cm(160);
        b.setSpan_cm(155);
        b.setAge(18);

        assertEquals(Sex.FEMALE, b.getSex());
        assertEquals(18, b.getAge());
        assertEquals(160, b.getHeight_cm(), 0.00001);
        assertEquals(155, b.getSpan_cm(), 0.00001);
        assertEquals(45, b.getWeight_kg(), 0.00001);

        String ts = b.toString();
        assertTrue(ts.contains("age"));
        assertTrue(ts.contains("sex"));
        assertTrue(ts.contains("weight_kg"));
    }

    @Test
    void equalsWorksAsExpected() {
        var a = new AnthroConfig();
        var b = new AnthroConfig();
        assertEquals(a, b);

        b.setAge(31);
        assertNotEquals(a, b);
        a.setAge(31);
        assertEquals(a, b);

        b.setSex(Sex.FEMALE);
        assertNotEquals(a, b);
        a.setSex(Sex.FEMALE);
        assertEquals(a, b);

        b.setHeight_cm(111);
        assertNotEquals(a, b);
        a.setHeight_cm(111);
        assertEquals(a, b);

        b.setSpan_cm(111);
        assertNotEquals(a, b);
        a.setSpan_cm(111);
        assertEquals(a, b);

        b.setWeight_kg(111);
        assertNotEquals(a, b);
        a.setWeight_kg(111);
        assertEquals(a, b);


        Anthropometry defaultAnthro = new AnthroConfig();
        assertEquals(defaultAnthro, defaultAnthro);

        // a little helper class for our next equality test
        final class AlmostAnAnthro extends AnthroConfig {
            AlmostAnAnthro() {

            }
        }
        AlmostAnAnthro wrongClass = new AlmostAnAnthro();
        assertNotEquals(defaultAnthro, wrongClass);

        Anthropometry anotherDefaultAnthro = new AnthroConfig();
        assertEquals(defaultAnthro, anotherDefaultAnthro);
    }

    @Test
    void configureWorks() {
        AnthroConfiguration ac = AnthroConfiguration.builder()
                .sex(Sex.FEMALE)
                .span(123)
                .weight(450)
                .age(45)
                .height(111).build();

        Anthropometry ap = new AnthroConfig();
        ap.configure(ac);

        assertEquals(Sex.FEMALE, ap.getSex());
        assertEquals(45, ap.getAge());
        assertEquals(111, ap.getHeight_cm());
        assertEquals(123, ap.getSpan_cm());
        assertEquals(450, ap.getWeight_kg());
    }


    @Test
    void featureBehaviorWorksAsExpected() {
        Feature feature = new AnthroConfig();
        assertEquals("Simple Anthropometry", feature.getName());
        assertEquals(0, feature.getMajorVersion());
        assertEquals(1, feature.getMinorVersion());
        assertEquals(0, feature.getPatchVersion());
        assertNotNull(feature.getDescription());
    }
}

