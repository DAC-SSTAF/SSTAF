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

package mil.sstaf.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PairValueMapTest {

    @Test
    void reversingOrderOfKeysPreservesEquals() {
        PairValueMap<Integer, Integer> map = new PairValueMap<>();

        PairValueMap.KeyPair<Integer> key1 = new PairValueMap.KeyPair<>(100, 4301);
        PairValueMap.KeyPair<Integer> key2 = new PairValueMap.KeyPair<>(4301, 100);
        PairValueMap.KeyPair<Integer> key3 = new PairValueMap.KeyPair<>(17, 1701);
        PairValueMap.KeyPair<Integer> key4 = new PairValueMap.KeyPair<>(1701, 17);

        assertEquals(key1, key1);
        assertEquals(key2, key2);
        assertEquals(key1, key2);
        assertNotEquals(key1, key3);
        assertNotEquals(key1, key4);
        assertNotEquals(key2, key3);
        assertNotEquals(key2, key4);
        assertEquals(key3, key4);

        assertNotEquals(key1, 15);
    }

    @Test
    void reversingOrderOfKeysPreservesHash() {
        PairValueMap<Integer, Integer> map = new PairValueMap<>();

        PairValueMap.KeyPair<Integer> key1 = new PairValueMap.KeyPair<>(100, 4301);
        PairValueMap.KeyPair<Integer> key2 = new PairValueMap.KeyPair<>(4301, 100);
        PairValueMap.KeyPair<Integer> key3 = new PairValueMap.KeyPair<>(17, 1701);
        PairValueMap.KeyPair<Integer> key4 = new PairValueMap.KeyPair<>(1701, 17);

        assertEquals(key1.hashCode(), key2.hashCode());
        assertEquals(key3.hashCode(), key4.hashCode());
    }

    @Test
    void putsAndGetsWork() {
        PairValueMap<Integer, Integer> map = new PairValueMap<>();

        PairValueMap.KeyPair<Integer> key1 = new PairValueMap.KeyPair<>(100, 4301);
        PairValueMap.KeyPair<Integer> key2 = new PairValueMap.KeyPair<>(4301, 100);
        PairValueMap.KeyPair<Integer> key3 = new PairValueMap.KeyPair<>(17, 1701);
        PairValueMap.KeyPair<Integer> key4 = new PairValueMap.KeyPair<>(1701, 17);

        Integer value1 = 3333;
        Integer value2 = 6666;

        map.put(key1, value1);
        assertNull(map.get(key3));
        assertNull(map.get(key4));

        map.put(key3, value2);

        Integer retrieved1 = map.get(key2);
        Integer retrieved2 = map.get(key4);

        assertNotNull(retrieved1);
        assertNotNull(retrieved2);

        assertEquals(value1, retrieved1);
        assertEquals(value2, retrieved2);

        map.remove(key1);
        assertNull(map.get(key2));
        assertNull(map.get(key1));

    }

}

