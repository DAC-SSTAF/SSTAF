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

package mil.sstaf.core.integration;

import mil.sstaf.core.entity.EntityHandle;
import mil.sstaf.core.features.*;
import mil.sstaf.core.configuration.SSTAFConfiguration;
import mil.sstaf.core.util.SSTAFException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class ResolverIntegrationTest {

    @BeforeEach
    public void setup() {
        System.setProperty(SSTAFConfiguration.SSTAF_CONFIGURATION_PROPERTY,
                "src" + File.separator +
                        "integrationTest" + File.separator +
                        "resources" + File.separator +
                        "EmptyConfiguration.json");
    }

    @Test
    @DisplayName("Confirm that generateServiceReport() provides a useful report")
    public void testGenerateServiceReport() {

        Assertions.assertDoesNotThrow(() -> {
            String report = Loaders.generateServiceReport(Feature.class);
            Assertions.assertNotNull(report);
            System.out.println(report);
            Assertions.assertEquals(8, countMatches(report, "Implemented"));
            Assertions.assertEquals(1, countMatches(report, "mil.sstaftest.mocks.pinky.Pinky"));
            Assertions.assertEquals(2, countMatches(report, "[mil.sstaftest.mocks.pinky.Pinky]"));
        });
    }

    private int countMatches(final String source, final String word) {
        String[] temp = source.split("\\s+");
        int count = 0;
        for (String s : temp) {
            if (word.equals(s))
                count++;
        }
        return count;
    }

    @Test
    @DisplayName("Confirm that multiple services with cross-linked @Requires can be resolved and loaded")
    void multilayerLoadWithCircularRequirementWorks() {

        FeatureSpecification jbSpec = FeatureSpecification.builder().featureName("James Bond")
                .majorVersion(7).minorVersion(0).requireExact(false).build();

        ConcurrentMap<FeatureSpecification, Feature> featureCache = new ConcurrentHashMap<>();

        Map<String, FeatureConfiguration> configurationMap = new HashMap<>();
        for (String s : new String[]{"Echo", "Delta", "Charlie","Bravo","Alpha", "James Bond"}) {
            configurationMap.put(s, FeatureConfiguration.builder().build());
        }
        EntityHandle eh = EntityHandle.makeDummyHandle();
        Resolver resolver = new Resolver(featureCache, configurationMap, eh, 31415, ModuleLayer.boot());
        Agent jamesBond = (Agent) resolver.loadAndResolveDependencies(jbSpec);
        Assertions.assertDoesNotThrow(jamesBond::init);
    }

    @Test
    @DisplayName("Force a Resolver failure and confirm the error message is correct")
    void testResolverFailure1() {
        FeatureSpecification tomBombadil = FeatureSpecification.builder().featureName("Tom Bombadil")
                .majorVersion(1).minorVersion(0).requireExact(false).build();

        Resolver resolver = Resolver.makeTransientResolver();

        SSTAFException boom = Assertions.assertThrows(SSTAFException.class,
                () -> resolver.loadAndResolveDependencies(tomBombadil));

        String message = boom.getMessage();
        Assertions.assertTrue(message.contains("Tom Bombadil"));
        Assertions.assertTrue(message.contains("Feature"));
    }

}

