package mil.sstaf.pyagent.integration;


import mil.sstaf.core.configuration.SSTAFConfiguration;
import mil.sstaf.core.features.FeatureConfiguration;
import mil.sstaf.core.features.FeatureSpecification;
import mil.sstaf.pyagent.api.PyAgent;
import mil.sstaftest.util.BaseFeatureIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class PyAgentIntegrationTest extends BaseFeatureIntegrationTest<PyAgent, FeatureConfiguration> {


    PyAgentIntegrationTest() {
        super(PyAgent.class, getSpec());
    }

    private static FeatureSpecification getSpec() {
        return FeatureSpecification.builder()
                .featureClass(PyAgent.class)
                .featureName("PyAgent")
                .majorVersion(1)
                .minorVersion(0)
                .requireExact(true)
                .build();
    }

    @BeforeEach
    public void setup() {
        System.setProperty(SSTAFConfiguration.SSTAF_CONFIGURATION_PROPERTY,
                "src" + File.separator +
                        "integrationTest" + File.separator +
                        "resources" + File.separator +
                        "PyAgentConfiguration.json");
    }


    @Nested
    @DisplayName("Test PyAgent-specific deployment and startup issues")
    class PythonStuff {
        @Test
        @DisplayName("Check that python scripts install correctly and works")
        void test1() {
            PyAgent pyAgent = loadAndResolveFeature();
            assertDoesNotThrow(() -> {
                pyAgent.configure(FeatureConfiguration.builder().build());
                pyAgent.init();

                List<String> args = List.of("Biscuits", "Gravy", "Waffles", "French", "Toast",
                        "Honey", "Nut", "Cheerios");
                int correct = getCorrect(args);


                int result = pyAgent.countLetters(args);
                assertEquals(correct, result);

                args = List.of("Pooh", "Piglet", "Tigger", "Rabbit",
                        "Kanga", "Roo");
                correct = getCorrect(args);

                result = pyAgent.countLetters(args);
                assertEquals(correct, result);

            });
        }

        private int getCorrect(List<String> args) {
            int sum = 0;
            for (var s : args) {
                sum += s.length();
            }
            return sum;
        }
    }
}