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

package mil.sstaftest.maneuver.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HeadingTest {
    @Test
    void constructionWorks() {

        Heading h = Heading.of(0); // North
        assertEquals(0.0, h.heading_degs);
        assertEquals(Math.PI / 2.0, h.heading_rads);

        h = Heading.of(315); // North West;
        assertEquals(315.0, h.heading_degs);
        assertEquals(0.75 * Math.PI, h.heading_rads);

        h = Heading.of(180); // South;
        assertEquals(180.0, h.heading_degs);
        assertEquals(-Math.PI / 2.0, h.heading_rads);

        h = Heading.of(90); // East;
        assertEquals(90.0, h.heading_degs);
        assertEquals(0.0, h.heading_rads);

        assertThrows(IllegalArgumentException.class, () -> Heading.of(-17));
        assertThrows(IllegalArgumentException.class, () -> Heading.of(1701));
    }

}

