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
package analyzer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mil.devcom_sc.ansur.messages.GetValueMessage;
import mil.devcom_sc.ansur.messages.ValueKey;
import mil.sstaf.analyzer.messages.CommandList;
import mil.sstaf.analyzer.messages.Mode;
import mil.sstaf.core.features.HandlerContent;
import mil.sstaf.session.messages.Command;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class SSTAFAnalyzerIntegrationTest {

    static final String endSessionMsg = "{ \"class\" : \"mil.sstaf.analyzer.messages.Exit\" }\n";
    static final String getEntitiesMsg = "{ \"class\" : \"mil.sstaf.analyzer.messages.GetEntities\"}\n";

    static final boolean HEAVY = Boolean.getBoolean("HEAVY_TEST");
    private static final Logger logger = LoggerFactory.getLogger(SSTAFAnalyzerIntegrationTest.class);
    static Path extractedPath;
    static Path extractedLibPath;
    static Path userDir;
    static Path baseDir;
    static Path inputDir;
    static Path resourceDir;

    static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Unzip the zip file to the destination directory
     *
     * @param zipFilePath the path to the ZIP file
     * @param destDir     the directory into which the ZIP file will be unzipped
     */
    static void unzip(Path zipFilePath, Path destDir) throws IOException {
        File dir = destDir.toFile();
        // create output directory if it doesn't exist
        if (!dir.exists()) {
            Files.createDirectories(destDir);
        }
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath.toFile());
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                Path entryPath = Path.of(destDir.toString(), fileName);
                logger.info("Unzipping to {}", entryPath.normalize().toAbsolutePath());
                if (ze.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    File newFile = entryPath.toFile();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Path extractDistribution() throws IOException {
        String cwdString = System.getProperty("user.dir");

        Path analyzerZip = Path.of(cwdString,
                "build", "distributions", "sstaf-analyzer.zip");
        Path tmpDir = Path.of(cwdString, "build", "tmp");
        Path expandedDir = Files.createTempDirectory(tmpDir, "SSTAFAnalyzerTest");
        unzip(analyzerZip.normalize().toAbsolutePath(),
                expandedDir.normalize().toAbsolutePath());
        return Path.of(expandedDir.toString(), "sstaf-analyzer");
    }

    @BeforeAll
    static void beforeAll() {
        try {
            userDir = Path.of(System.getProperty("user.dir"));
            baseDir = Path.of(userDir.toString(), "..", "..");
            inputDir = Path.of(baseDir.toString(), "testInput");
            resourceDir = Path.of(userDir.toString(), "src",
                    "integrationTest", "resources");
            extractedPath = extractDistribution();
            extractedLibPath = Path.of(extractedPath.toString(), "lib").normalize().toAbsolutePath();
        } catch (IOException e) {
            fail("Could not extract distribution");
        }
    }


    private static List<String> getGoodEntityFiles() {
        List<String> files;
        if (HEAVY) {
            files = List.of(
                    Path.of(inputDir.toString(), "goodEntityFiles", "OneSoldier.json").toString(),
                    Path.of(inputDir.toString(), "goodEntityFiles", "FourSoldiers.json").toString(),
                    Path.of(inputDir.toString(), "goodEntityFiles", "OneFireTeam.json").toString(),
                    Path.of(inputDir.toString(), "goodEntityFiles", "OneSquad.json").toString(),
                    Path.of(inputDir.toString(), "goodEntityFiles", "TwoSquads.json").toString(),
                    Path.of(inputDir.toString(), "goodEntityFiles", "OnePlatoon.json").toString(),
                    Path.of(inputDir.toString(), "goodEntityFiles", "BlueRedGray.json").toString()
            );
        } else {
            files = List.of(
                    Path.of(inputDir.toString(), "goodEntityFiles", "OneSoldier.json").toString()
            );
        }
        return files;
    }

    private static List<String> getBadEntityFiles() {
        return List.of(
                Path.of(inputDir.toString(), "badEntityFiles", "emptyFile.json").toString(),
                Path.of(inputDir.toString(), "badEntityFiles", "somethingOtherThanEntity.json").toString()
        );
    }

    private static void gotExpectedMessage(Process p, BufferedReader reader, String match) throws IOException, InterruptedException {

        boolean gotMessage = false;
        while (p.isAlive()) {
            String in = reader.readLine();
            if (in != null) {
                System.out.printf("Looking for %s - Received - %s\n", match, in);
                if (in.contains(match)) {
                    gotMessage = true;
                    break;
                }
            } else {
                Thread.sleep(100);
            }
        }
        assertTrue(gotMessage);
    }

    private static void sendMessage(BufferedWriter writer, String message) throws IOException {
        System.out.printf("Sending - %s\n", message);
        writer.write(message);
        writer.newLine();
        writer.flush();
    }

    String makeModulePath() {
        StringBuilder sb = new StringBuilder();
        sb.append("--module-path=");
        sb.append(extractedLibPath.toString());


        List<String> modulePaths = List.of();
        for (Iterator<String> iter = modulePaths.iterator(); iter.hasNext(); ) {
            String module = iter.next();
            Path path = Path.of(userDir.toString(), "..", "..", module, "build", "libs").normalize().toAbsolutePath();
            sb.append(path);
            if (iter.hasNext()) sb.append(":");
        }
        return sb.toString();
    }

    private Process makeProcessWithArg(String arg) throws IOException {
        ProcessBuilder processBuilder = makeDefaultProcessBuilder(arg);
        return processBuilder.start();
    }

    private Process makeProcessWithNoArgs() throws IOException {
        ProcessBuilder processBuilder = makeDefaultProcessBuilder(null);
        return processBuilder.start();
    }

    private ProcessBuilder makeDefaultProcessBuilder(String arg) {
        String cwdString = System.getProperty("user.dir");

        Path errorPath = Path.of(cwdString, "build",
                "tmp", "SSTAFAnalyzerIntegrationTest-stderr");
        ProcessBuilder processBuilder = new ProcessBuilder();

        String modulePath = makeModulePath();
        List<String> commandElements = new ArrayList<>();
        commandElements.add("java");
        commandElements.add(modulePath);
        commandElements.add("--module");
        commandElements.add("mil.sstaf.analyzer/mil.sstaf.analyzer.Main");
        if (arg != null) {
            commandElements.add(arg);
        }
        processBuilder.command(commandElements);
        processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(errorPath.toFile()));
        return processBuilder;
    }

    @ParameterizedTest(name = "{index} ==> Testing with entity file ''{0}''")
    @MethodSource("getGoodEntityFiles")
    @DisplayName("Confirm that good entity files can be loaded")
    void goodFiles(String entityFile) {
        assertDoesNotThrow(() -> {
            Process p = makeProcessWithArg(entityFile);
            OutputStreamWriter osw = new OutputStreamWriter(p.getOutputStream());
            BufferedWriter writer = new BufferedWriter(osw);
            InputStreamReader isr = new InputStreamReader(p.getInputStream());
            BufferedReader reader = new BufferedReader(isr);

            sendMessage(writer, getEntitiesMsg);
            gotExpectedMessage(p, reader, "GetEntitiesResult");
            sendMessage(writer, endSessionMsg);
            gotExpectedMessage(p, reader, "ExitResult");

        });
    }

    private String makeEntityCommand(HandlerContent content) throws JsonProcessingException {
        Command command = Command.builder()
                .recipientPath("Test Platoon:Squad C:Fire Team Bravo:AR")
                .content(content).build();
        CommandList cl = CommandList.builder().command(command)
                .mode(Mode.SUBMIT_AND_DISPATCH).build();
        return objectMapper.writeValueAsString(cl);
    }

    @Nested
    @DisplayName("Test start-up failure modes")
    class FailureTests {
        @Test
        @DisplayName("Confirm that an Analyzer started without arguments runs but exits with code 1.")
        void testOne() {
            assertDoesNotThrow(() -> {
                Process p = makeProcessWithNoArgs();
                while (p.isAlive()) {
                    Thread.sleep(1000);
                }
                assertEquals(1, p.exitValue());
            });
        }

        @ParameterizedTest(name = "{index} ==> Testing with entity file ''{0}''")
        @MethodSource("analyzer.SSTAFAnalyzerIntegrationTest#getBadEntityFiles")
        @DisplayName("Confirm that bad entity files result in an immediate exit with code 1")
        void badFiles(String entityFile) {
            assertDoesNotThrow(() -> {
                Process p = makeProcessWithArg(entityFile);
                while (p.isAlive()) {
                    Thread.sleep(1000);
                }
                assertEquals(1, p.exitValue());
            });
        }

    }

    @DisplayName("Confirm that messages can be sent and results received")
    @Nested
    class MessageTests {
        @Test
        @DisplayName("Confirm ANSUR query works")
        void messageHandlingTest() {
            assertDoesNotThrow(() -> {
                String entityFile = Path.of(inputDir.toString(), "goodEntityFiles", "OnePlatoon.json").toString();
                Process p = makeProcessWithArg(entityFile);
                OutputStreamWriter osw = new OutputStreamWriter(p.getOutputStream());
                BufferedWriter writer = new BufferedWriter(osw);
                InputStreamReader isr = new InputStreamReader(p.getInputStream());
                BufferedReader reader = new BufferedReader(isr);

                sendMessage(writer, getEntitiesMsg);
                gotExpectedMessage(p, reader, "GetEntitiesResult");

                //
                // Test all ANSUR values.
                //
                for (ValueKey key : ValueKey.values()) {
                    String query = makeEntityCommand(GetValueMessage.of(key));
                    sendMessage(writer, query);
                    gotExpectedMessage(p, reader, "TickResult");
                }

                sendMessage(writer, endSessionMsg);
                gotExpectedMessage(p, reader, "ExitResult");
            });
        }

        @Test
        @DisplayName("Check the AddEntryRequest, GetEntryRequest and RemoveEntryRequest blackboard commands")
        void issue7TestCorrected() {
            assertDoesNotThrow(
                    () -> {
                        String entityFile = Path.of(inputDir.toString(), "goodEntityFiles", "OnePlatoon.json").toString();
                        Process p = makeProcessWithArg(entityFile);
                        OutputStreamWriter osw = new OutputStreamWriter(p.getOutputStream());
                        BufferedWriter writer = new BufferedWriter(osw);
                        InputStreamReader isr = new InputStreamReader(p.getInputStream());
                        BufferedReader reader = new BufferedReader(isr);

                        sendMessage(writer, getEntitiesMsg);
                        gotExpectedMessage(p, reader, "GetEntitiesResult");

                        //
                        // Check AddEntryRequest command
                        //
                        String bbCommand = "{\"class\":\"mil.sstaf.analyzer.messages.CommandList\"," +
                                "\"commands\":[{\"class\":\"mil.sstaf.session.messages.Command\"," +
                                "\"recipientPath\":\"BLUE:Test Platoon:PL\"," + // changed to match test input
                                "\"content\":" +
                                "{\"class\":\"mil.sstaf.blackboard.api.AddEntryRequest\"," +
                                "\"key\":\"Age\"," +
                                "\"value\":{\"class\":\"mil.sstaf.core.features.IntContent\",\"value\":45},\"timestamp_ms\":1,\"expiration_ms\":100}}]," +
                                "\"mode\":\"TICK\",\"time_ms\":1}";
                        sendMessage(writer, bbCommand);
                        gotExpectedMessage(p, reader, "AddEntryResponse");

                        //
                        // Check GetEntryRequest command
                        //
                        String bbCommand2 = "{\"class\":\"mil.sstaf.analyzer.messages.CommandList\"," +
                                "\"commands\":[{\"class\":\"mil.sstaf.session.messages.Command\"," +
                                "\"recipientPath\":\"BLUE:Test Platoon:PL\"," + // changed to match test input
                                "\"content\":" +
                                "{\"class\":\"mil.sstaf.blackboard.api.GetEntryRequest\"," +
                                "\"time_ms\":\"50\",\"type\":\"mil.sstaf.core.features.IntContent\",\"key\":\"Age\"}}]," +
                                "\"mode\":\"TICK\",\"time_ms\":2}";
                        sendMessage(writer, bbCommand2);
                        gotExpectedMessage(p, reader, "GetEntryResponse");

                        //
                        // Check RemoveEntryRequest command
                        //
                        String bbCommand3 = "{\"class\":\"mil.sstaf.analyzer.messages.CommandList\"," +
                                "\"commands\":[{\"class\":\"mil.sstaf.session.messages.Command\"," +
                                "\"recipientPath\":\"BLUE:Test Platoon:PL\"," + // changed to match test input
                                "\"content\":" +
                                "{\"class\":\"mil.sstaf.blackboard.api.RemoveEntryRequest\"," +
                                "\"key\":\"Age\"}}]," +
                                "\"mode\":\"TICK\",\"time_ms\":3}";
                        sendMessage(writer, bbCommand3);
                        gotExpectedMessage(p, reader, "RemoveEntryResponse");

                        sendMessage(writer, endSessionMsg);
                        gotExpectedMessage(p, reader, "ExitResult");
                    });
        }

        @Test
        @DisplayName("Check John's ANSUR query")
        void issue8TestBefore() {
            logger.warn("********** EXPECT AN EXCEPTION!! **********");
            assertThrows(AssertionFailedError.class, () -> {
                String entityFile = Path.of(inputDir.toString(), "goodEntityFiles", "OnePlatoon.json").toString();
                Process p = makeProcessWithArg(entityFile);
                OutputStreamWriter osw = new OutputStreamWriter(p.getOutputStream());
                BufferedWriter writer = new BufferedWriter(osw);
                InputStreamReader isr = new InputStreamReader(p.getInputStream());
                BufferedReader reader = new BufferedReader(isr);

                sendMessage(writer, getEntitiesMsg);
                gotExpectedMessage(p, reader, "GetEntitiesResult");

                //
                // Check provided command
                //
                String ansurQuery = "{\"class\":\"mil.sstaf.analyzer.messages.CommandList\"," +
                        "\"commands\":[{\"class\":\"mil.sstaf.session.messages.Command\"," +
                        "\"recipientPath\":\"BLUE:Test Platoon:PL\"," + // changed to match test input
                        "\"content\":" +
                        "{\"class\":\"mil.devcom_sc.ansur.messages.GetValueMessage\"," +
                        "\"valueKey\":\"GENDER\"}}]," +
                        "\"mode\":\"TICK\",\"time_ms\":2000}";
                sendMessage(writer, ansurQuery);
                gotExpectedMessage(p, reader, "TickResult");
            });

        }

        @Test
        @DisplayName("Check the corrected ANSUR query")
        void issue8TestCorrected() {
            assertDoesNotThrow(
                    () -> {
                        String entityFile = Path.of(inputDir.toString(), "goodEntityFiles", "OnePlatoon.json").toString();
                        Process p = makeProcessWithArg(entityFile);
                        OutputStreamWriter osw = new OutputStreamWriter(p.getOutputStream());
                        BufferedWriter writer = new BufferedWriter(osw);
                        InputStreamReader isr = new InputStreamReader(p.getInputStream());
                        BufferedReader reader = new BufferedReader(isr);

                        sendMessage(writer, getEntitiesMsg);
                        gotExpectedMessage(p, reader, "GetEntitiesResult");

                        //
                        // Check provided command
                        //
                        String ansurQuery = "{\"class\":\"mil.sstaf.analyzer.messages.CommandList\"," +
                                "\"commands\":[{\"class\":\"mil.sstaf.session.messages.Command\"," +
                                "\"recipientPath\":\"BLUE:Test Platoon:PL\"," + // changed to match test input
                                "\"content\":" +
                                "{\"class\":\"mil.devcom_sc.ansur.messages.GetValueMessage\"," +
                                "\"key\":\"GENDER\"}}]," +
                                "\"mode\":\"TICK\",\"time_ms\":2000}";
                        sendMessage(writer, ansurQuery);
                        gotExpectedMessage(p, reader, "TickResult");
                        sendMessage(writer, endSessionMsg);
                        gotExpectedMessage(p, reader, "ExitResult");
                    });
        }

        @Test
        @DisplayName("Check the corrected nextEventTime_ms result after event is SUBMIT_ONLY")
        void issue15TestCorrected() {
            assertDoesNotThrow(
                    () -> {
                        String entityFile = Path.of(inputDir.toString(), "goodEntityFiles", "OnePlatoon.json").toString();
                        Process p = makeProcessWithArg(entityFile);
                        OutputStreamWriter osw = new OutputStreamWriter(p.getOutputStream());
                        BufferedWriter writer = new BufferedWriter(osw);
                        InputStreamReader isr = new InputStreamReader(p.getInputStream());
                        BufferedReader reader = new BufferedReader(isr);

                        sendMessage(writer, getEntitiesMsg);
                        gotExpectedMessage(p, reader, "GetEntitiesResult");

                        // Submit event for tick 2025 at tick 2000, make sure next event is at 2025
                        String ansurQuery = "{\"class\":\"mil.sstaf.analyzer.messages.CommandList\"," +
                                "\"commands\":[{\"class\":\"mil.sstaf.session.messages.Event\"," +
                                "\"eventTime_ms\":2025," +
                                "\"recipientPath\":\"BLUE:Test Platoon:PL\"," + // changed to match test input
                                "\"content\":" +
                                "{\"class\":\"mil.devcom_sc.ansur.messages.GetValueMessage\"," +
                                "\"key\":\"GENDER\"}}]," +
                                "\"mode\":\"SUBMIT_ONLY\",\"time_ms\":2000}";
                        sendMessage(writer, ansurQuery);
                        gotExpectedMessage(p, reader, "\"nextEventTime_ms\":2025");

                        // Tick to 2500, make sure next event is now Long.max
                        String tickMessage = "{\"class\":\"mil.sstaf.analyzer.messages.Tick\"," +
                                "\"time_ms\":2500}";
                        sendMessage(writer, tickMessage);
                        gotExpectedMessage(p, reader, "\"nextEventTime_ms\":9223372036854775807");

                        sendMessage(writer, endSessionMsg);
                        gotExpectedMessage(p, reader, "ExitResult");
                    });
        }

        @Test
        @DisplayName("Check EquipmentManager command to attempt Shoot on gun that does not exist")
        void issue10ShootInvalidGunTest() {
            assertDoesNotThrow(
                    () -> {
                        String entityFile = Path.of(inputDir.toString(), "goodEntityFiles", "OnePlatoon.json").toString();
                        Process p = makeProcessWithArg(entityFile);
                        OutputStreamWriter osw = new OutputStreamWriter(p.getOutputStream());
                        BufferedWriter writer = new BufferedWriter(osw);
                        InputStreamReader isr = new InputStreamReader(p.getInputStream());
                        BufferedReader reader = new BufferedReader(isr);

                        sendMessage(writer, getEntitiesMsg);
                        gotExpectedMessage(p, reader, "GetEntitiesResult");

                        //
                        // Check provided command
                        //
                        String shootCmd = "{\"class\":\"mil.sstaf.analyzer.messages.CommandList\"," +
                                "\"commands\":[{\"class\":\"mil.sstaf.session.messages.Command\"," +
                                "\"recipientPath\":\"BLUE:Test Platoon:PL\"," + // changed to match test input
                                "\"content\":" +
                                "{\"class\":\"mil.devcom_dac.equipment.messages.Shoot\"," +
                                "\"gun\":\"M5\",\"numToShoot\":1}}]," +
                                "\"mode\":\"TICK\",\"time_ms\":2000}";
                        sendMessage(writer, shootCmd);

                        // should be an exception with gun not found error.
                        gotExpectedMessage(p, reader, "Gun M5 was not found");

                        sendMessage(writer, endSessionMsg);
                        gotExpectedMessage(p, reader, "ExitResult");
                    });
        }
    }
}


