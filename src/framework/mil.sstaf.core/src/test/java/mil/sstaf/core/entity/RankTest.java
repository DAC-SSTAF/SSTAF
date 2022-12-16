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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RankTest {

    @Test
    void testRankMatching() {

        for (Rank rank : Rank.values()) {
            assertEquals(rank, Rank.findMatch(rank.name()));
            assertEquals(rank, Rank.findMatch(rank.getCode()));
        }

        assertNull(Rank.findMatch("ADM")); // No Navy!!
        assertNull(Rank.findMatch("A1C")); // No Air Force!!
        assertNull(Rank.findMatch(null)); // No nothing!!
    }
}

