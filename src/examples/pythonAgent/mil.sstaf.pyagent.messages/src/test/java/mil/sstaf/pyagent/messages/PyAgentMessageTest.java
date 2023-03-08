package mil.sstaf.pyagent.messages;

import org.junit.jupiter.api.*;


import java.util.List;


public class PyAgentMessageTest {

    @Nested
    @DisplayName("Happy Path")
    class HappyPath {
        @Test
        @DisplayName("Confirm that a CountLettersRequest can be made")
        void test1() {
            List<String> args = List.of("Bonita Springs", "Naples", "Estero",
                    "Fort Myers");
            CountLettersRequest cr = CountLettersRequest.builder().args(args).build();
            for (int i = 0; i < cr.args.size(); ++i) {
                Assertions.assertEquals(args.get(i), cr.args.get(i));
            }
        }

        @Test
        @DisplayName("Confirm that a CountLettersRequest can be made one string at a time")
        void test2() {
            List<String> args = List.of("Bonita Springs", "Naples", "Estero",
                    "Fort Myers");
            CountLettersRequest.CountLettersRequestBuilder<?, ?> crb = CountLettersRequest.builder();

            for (String s : args) {
                crb.arg(s);
            }
            CountLettersRequest cr = crb.build();
            for (int i = 0; i < cr.args.size(); ++i) {
                Assertions.assertEquals(args.get(i), cr.args.get(i));
            }
        }
    }
}
