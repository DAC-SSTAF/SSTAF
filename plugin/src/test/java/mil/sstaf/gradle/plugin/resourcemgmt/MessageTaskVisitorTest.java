package mil.sstaf.gradle.plugin.resourcemgmt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

public class MessageTaskVisitorTest {

    @Nested
    @DisplayName("Test the 'happy path'")
    class HappyTests {
        @Test
        @DisplayName("Confirm that a file tree can be traversed and hashed.")
        public void test1() {
            String ha = "MD5";
            try {
                Path basePath = Path.of("src", "main", "java");
                Files.walkFileTree(basePath, new MessageDigestVisitor(basePath, ha));
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
