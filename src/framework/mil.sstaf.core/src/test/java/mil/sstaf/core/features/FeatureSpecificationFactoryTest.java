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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mil.sstaf.core.util.SSTAFException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FeatureSpecificationFactoryTest {


    static Path USER_DIR = Path.of(System.getProperty("user.dir"));
    static Path RESOURCE_DIR = Path.of(USER_DIR.toString(), "src/test/resources");

    @Test
    void readingFromGoodFileWorks() {
        String file = "src/test/resources/TestProviderSpec.json";
        FeatureSpecification ps = FeatureSpecification.from(new File(file));
        assertEquals("Mary Poppins", ps.featureName);
        assertEquals(3, ps.majorVersion);
        assertEquals(1, ps.minorVersion);
        assertTrue(ps.requireExact);
    }

    @Test
    void readingFromBadFileThrows() {
        assertThrows(SSTAFException.class, () -> {
            String file = "src/test/resources/NotATestProviderSpec.json";
            FeatureSpecification.from(new File(file));
        });
    }

    @Test
    void badArgumentThrows() {
        assertThrows(SSTAFException.class, () -> {
            ObjectMapper om = new ObjectMapper();
            FeatureSpecification.from(om.createArrayNode(), RESOURCE_DIR);
        });
    }

    public static final String NAME_KEY = "featureName";
    public static final String MAJOR_VERSION_KEY = "majorVersion";
    public static final String MINOR_VERSION_KEY = "minorVersion";
    public static final String REQUIRE_EXACT_KEY = "requireExact";

    @Test
    void badJSONThrows1() {
        ObjectMapper om = new ObjectMapper();
        assertThrows(SSTAFException.class, () -> {
            ObjectNode testArticle = om.createObjectNode();
            testArticle.put(MAJOR_VERSION_KEY, 3);
            testArticle.put(MINOR_VERSION_KEY, 1);
            testArticle.put(REQUIRE_EXACT_KEY, true);

            FeatureSpecification.from(testArticle, RESOURCE_DIR);
        });
    }

    @Test
    void badJSONThrows2() {
        ObjectMapper om = new ObjectMapper();
        assertThrows(SSTAFException.class, () -> {
            ObjectNode testArticle = om.createObjectNode();
            testArticle.put(NAME_KEY, "MyProvider");
            testArticle.put(MAJOR_VERSION_KEY, "Banana");
            testArticle.put(MINOR_VERSION_KEY, 1);
            testArticle.put(REQUIRE_EXACT_KEY, true);
            FeatureSpecification.from(testArticle, RESOURCE_DIR);
        });

    }
}

