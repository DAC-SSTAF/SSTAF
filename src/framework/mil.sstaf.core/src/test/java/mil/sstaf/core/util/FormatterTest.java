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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FormatterTest {
    @Test
    void formatterWorks() {
        long val = 1;
        Assertions.assertEquals("0:0:0:0.001", Formatter.millisToDHMS(val));

        val = 1456;
        Assertions.assertEquals("0:0:0:1.456", Formatter.millisToDHMS(val));

        val = 17456 + 32000 * 60;
        Assertions.assertEquals("0:0:32:17.456", Formatter.millisToDHMS(val));

        val = 17456 + 32000 * 60 + 15 * 3600 * 1000;
        Assertions.assertEquals("0:15:32:17.456", Formatter.millisToDHMS(val));

        val = 17456 + 32000 * 60 + 15 * 3600 * 1000L + 95 * 24 * 3600 * 1000L;
        Assertions.assertEquals("95:15:32:17.456", Formatter.millisToDHMS(val));

    }
}

