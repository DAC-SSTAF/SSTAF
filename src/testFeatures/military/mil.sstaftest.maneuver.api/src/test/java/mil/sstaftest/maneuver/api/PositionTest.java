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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PositionTest {

    @Test
    void allPositionBehaviorsWork() {

        Position a = Position.of(1, 0);
        Position b = Position.of(0, 1);
        Position c = Position.of(-1, 0);
        Position d = Position.of(0, -1.0);

        double sqrt2 = Math.sqrt(2.000000);

        Assertions.assertNotNull(a);
        Assertions.assertNotNull(b);
        Assertions.assertNotNull(c);
        Assertions.assertNotNull(d);
        Assertions.assertEquals(1, a.x);
        Assertions.assertEquals(1, b.y);
        Assertions.assertEquals(-1, c.x);
        Assertions.assertEquals(-1, d.y);

        Assertions.assertEquals(0, a.distanceTo(a));
        Assertions.assertEquals(0, b.distanceTo(b));
        Assertions.assertEquals(0, c.distanceTo(c));
        Assertions.assertEquals(0, d.distanceTo(d));


        Assertions.assertEquals(sqrt2, a.distanceTo(b));
        Assertions.assertEquals(sqrt2, b.distanceTo(c));
        Assertions.assertEquals(sqrt2, c.distanceTo(d));
        Assertions.assertEquals(sqrt2, d.distanceTo(a));

        Assertions.assertEquals(2.0, a.distanceTo(c));
        Assertions.assertEquals(2.0, c.distanceTo(a));
    }
}

