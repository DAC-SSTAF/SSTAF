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

import java.util.HashMap;

public class PairValueMap<K, V> extends HashMap<PairValueMap.KeyPair<K>, V> {

    static class KeyPair<K> {

        K key1;
        K key2;

        KeyPair(K key1, K key2) {
            this.key1 = key1;
            this.key2 = key2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            KeyPair<?> keyPair = (KeyPair<?>) o;

            return (key1.equals(keyPair.key1) && key2.equals(keyPair.key2)
                    || key1.equals(keyPair.key2) && key2.equals(keyPair.key1));

        }

        @Override
        public int hashCode() {
            //
            // I want order independence
            //
            return key1.hashCode() * key2.hashCode();
        }
    }
}

