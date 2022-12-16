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

package mil.sstaf.core.features;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FeatureSpecificationTest {

    @Test
    void builderWorks() {
        var builder = FeatureSpecification.builder();
        builder.majorVersion(4)
                .minorVersion(2)
                .featureName("Wawa")
                .requireExact(true);
        FeatureSpecification ps1 = builder.build();

        Assertions.assertEquals("Wawa", ps1.featureName);
        Assertions.assertEquals(4, ps1.majorVersion);
        Assertions.assertEquals(2, ps1.minorVersion);
        Assertions.assertTrue(ps1.requireExact);
    }

}

