package mil.sstaf.core.features;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoadersTest {
    @Nested
    @DisplayName("Test the Happy Path")
    class HappyTests {

        @Test
        @DisplayName("Check that getHelpWithServices() is helpful")
        public void test1() {
            FeatureSpecification fs =
                    FeatureSpecification.builder()
                            .featureName("Wawa")
                            .majorVersion(0)
                            .minorVersion(1)
                            .requireExact(false).build();

            String s = Loaders.getHelpWithServices(fs, BaseAgent.class, ModuleLayer.boot());
            assertTrue (s.contains(System.getProperty("user.dir")));
            System.err.println(s);
        }


    }
}
