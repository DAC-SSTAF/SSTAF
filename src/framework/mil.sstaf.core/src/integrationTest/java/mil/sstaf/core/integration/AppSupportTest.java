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

import mil.sstaf.core.features.*;
import mil.sstaf.core.util.SSTAFException;
import mil.sstaftest.simplemock.SimpleMockFeature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class AppSupportTest {


    @Test
    @DisplayName("Confirm that a transient AppSession can be created to run the 'upper.py' script")
    void toUpperTestTransient() {
        assertDoesNotThrow(() -> {
            AppConfiguration config = AppConfiguration.builder()
                    .resourceOwner(SimpleMockFeature.class)
                    .resource("mil/sstaftest/simplemock/upper.py")
                    .mode(AppSupport.Mode.TRANSIENT)
                    .build();
            AppAdapter helper = AppSupport.createAdapter(config);
            ResourceManager resourceManager = helper.getResourceManager();
            File py = resourceManager.getResourceFiles().get("mil/sstaftest/simplemock/upper.py");
            helper.setArgs(List.of("python3", py.getAbsolutePath()));
            exerciseApplication(helper);
        });
    }

    @Test
    @DisplayName("Confirm that a durable AppSession can be created to run the 'upper.py' script")
    void toUpperTestDurable() {

        try {
            AppConfiguration config = AppConfiguration.builder()
                    .resourceOwner(SimpleMockFeature.class)
                    .resource("mil/sstaftest/simplemock/upper.py")
                    .mode(AppSupport.Mode.DURABLE)
                    .build();
            AppAdapter helper = AppSupport.createAdapter(config);
            ResourceManager resourceManager = helper.getResourceManager();
            File py = resourceManager.getResourceFiles().get("mil/sstaftest/simplemock/upper.py");
            helper.setArgs(List.of("python3", py.getAbsolutePath()));

            exerciseApplication(helper);

        } catch (SSTAFException ie) {
            ie.printStackTrace();
            Assertions.fail();
        }
    }

    @Test
    @DisplayName("Confirm that command line arguments work as expected for durable sessions")
    void argsDurable() {

        assertDoesNotThrow(() -> {
            AppConfiguration config = AppConfiguration.builder()
                    .resourceOwner(SimpleMockFeature.class)
                    .resource("mil/sstaftest/simplemock/args.py")
                    .mode(AppSupport.Mode.DURABLE)
                    .build();
            AppAdapter helper = AppSupport.createAdapter(config);
            checkHelper(helper);
        });
    }

    @Test
    @DisplayName("Confirm that command line arguments work as expected for transient sessions")
    void argsTransient() {

        assertDoesNotThrow(() -> {
            AppConfiguration config = AppConfiguration.builder()
                    .resourceOwner(SimpleMockFeature.class)
                    .resource("mil/sstaftest/simplemock/args.py")
                    .mode(AppSupport.Mode.TRANSIENT)
                    .build();
            AppAdapter helper = AppSupport.createAdapter(config);
            checkHelper(helper);
        });
    }

    private void checkHelper(AppAdapter helper) throws IOException {
        ResourceManager resourceManager = helper.getResourceManager();
        File py = resourceManager.getResourceFiles().get("mil/sstaftest/simplemock/args.py");

        final String bilbo = "Bilbo";
        helper.setArgs(List.of("python3", py.getAbsolutePath(), bilbo));

        AppSession session = helper.activate();
        String arg0 = session.invoke("0");
        Assertions.assertEquals(py.getAbsolutePath(), arg0);

        String arg1 = session.invoke("1");
        Assertions.assertEquals(bilbo, arg1);

        final String frodo = "Frodo";
        helper.setArgs(List.of("python3", py.getAbsolutePath(), frodo));

        session = helper.activate();
        arg0 = session.invoke("0");
        Assertions.assertEquals(py.getAbsolutePath(), arg0);

        arg1 = session.invoke("1");
        Assertions.assertEquals(frodo, arg1);

        final String sam = "Sam";
        session = helper.activate(List.of("python3", py.getAbsolutePath(), sam));
        arg0 = session.invoke("0");
        Assertions.assertEquals(py.getAbsolutePath(), arg0);

        arg1 = session.invoke("1");
        Assertions.assertEquals(sam, arg1);
    }

    private void exerciseApplication(AppAdapter helper) {
        int count = 0;
        long deltaT = 0;

        for (int i = 0; i < 10; ++i) {
            long start = System.nanoTime();
            try (AppSession appSession = helper.activate()) {

                assertDoesNotThrow(() -> {
                    List<String> strings = List.of("Banana",
                            "sdSJD",
                            "IamtheveryModelofaModernMajorGeneral",
                            "@#%#^%$&@#@GDFG$grt2@$%#F2453S",
                            "OneRingtoRuleThemAll,OneRingtoFindThem");

                    for (String s : strings) {
                        String bob = appSession.invoke(s);
                        //System.out.println(s + " --> " + bob);
                        Assertions.assertEquals(s.toUpperCase(Locale.ROOT), bob);
                    }

                });

            } catch (Exception e) {
                e.printStackTrace();
                Assertions.fail();
            }
            long stop = System.nanoTime();
            deltaT += stop - start;
            ++count;
        }

        double avg_nano = (double) deltaT / (double) count;
        double avg_milli = avg_nano / 1000000.;

        System.out.printf("Executed %d iterations in %d ns average = %f ns / %f ms\n", count,
                deltaT, avg_nano, avg_milli);
    }
}

