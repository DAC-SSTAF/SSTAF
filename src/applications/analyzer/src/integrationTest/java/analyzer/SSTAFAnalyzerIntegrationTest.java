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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class SSTAFAnalyzerIntegrationTest {

    static final String endSessionMsg = "{ \"class\" : \"mil.sstaf.analyzer.commands.EndSession\" }\n";
    static final String getEntitiesMsg = "{ \"class\" : \"mil.sstaf.analyzer.commands.GetEntities\"}\n";
    static final boolean HEAVY = true; // Boolean.getBoolean("HEAVY_TEST");
    static Path extractedPath;
    static Path extractedLibPath;
    static Path userDir;
    static Path baseDir;
    static Path inputDir;
    static Path resourceDir;

    /**
     * Unzip the zip file to the destination directory
     *
     * @param zipFilePath
     * @param destDir
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
                System.out.println("Unzipping to " + entryPath.normalize().toAbsolutePath());
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
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        return processBuilder;
    }

    @ParameterizedTest(name = "{index} ==> Testing with entity file ''{0}''")
    @MethodSource("getGoodEntityFiles")
    @DisplayName("Confirm that good entity files can be loaded")
    void goodFiles(String entityFile) {
        assertDoesNotThrow(() -> {
            Process p = makeProcessWithArg(entityFile);
            Thread.sleep(2000);
            OutputStreamWriter osw = new OutputStreamWriter(p.getOutputStream());
            BufferedWriter writer = new BufferedWriter(osw);
            InputStreamReader isr = new InputStreamReader(p.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            System.out.println("Sending " + getEntitiesMsg);
            writer.write(getEntitiesMsg);
            writer.flush();

            while (true) {
                String in = reader.readLine();
                if (in != null) {
                    System.out.printf("Received - %s\n", in);
                    break;
                } else {
                    Thread.sleep(100);
                }
            }
            System.out.println("Writing " + endSessionMsg);
            writer.write(endSessionMsg);
            writer.flush();
            boolean gotExitMessage = false;
            while (p.isAlive()) {
                String in2 = reader.readLine();
                System.out.println("Read " + in2);
                if (in2 != null) {
                    System.out.printf("Read - %s\n", in2);
                    if (in2.contains("EndSession")) {
                        gotExitMessage = true;
                    }
                } else {
                    Thread.sleep(100);
                }
            }
            assertEquals(0, p.exitValue());
            assertTrue(gotExitMessage);
        });
    }


    private static List<String> getBadEntityFiles() {
        List<String> files = List.of(
                Path.of(inputDir.toString(), "badEntityFiles", "emptyFile.json").toString(),
                Path.of(inputDir.toString(), "badEntityFiles", "somethingOtherThanEntity.json").toString()
        );
        return files;
    }

    @Nested
    @DisplayName("Test start-up failure modes")
    class FailureTests {
        @Test
        @DisplayName("Confirm that an Analyzer started without arguments runs but exits with code 255.")
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

    private String makeEntityCommand(String command) {
        return "{ \"class\" : \"mil.sstaf.analyzer.commands.Command\", " +
                "\"path\" : \"Test Platoon:Squad C:Fire Team Bravo:AR\", " +
                "\"content\" : {" +
                command +
                "} }\n";
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
                Thread.sleep(2000);
                OutputStreamWriter osw = new OutputStreamWriter(p.getOutputStream());
                BufferedWriter writer = new BufferedWriter(osw);
                InputStreamReader isr = new InputStreamReader(p.getInputStream());
                BufferedReader reader = new BufferedReader(isr);
                System.out.println("Sending " + getEntitiesMsg);
                writer.write(getEntitiesMsg);
                writer.flush();

                while (true) {
                    String in = reader.readLine();
                    if (in != null) {
                        System.out.printf("Received - %s\n", in);
                        break;
                    } else {
                        Thread.sleep(100);
                    }
                }
                String query = makeEntityCommand(
                        "\"class\" : \"mil.devcom_sc.ansur.messages.GetValueMessage\", \"key\" : \"EAR_LENGTH\""
                );
                writer.write(query);
                writer.flush();
                System.out.println("Writing " + query);
                while (true) {
                    String in = reader.readLine();
                    if (in != null) {
                        System.out.printf("Received - %s\n", in);
                        break;
                    } else {
                        Thread.sleep(100);
                    }
                }

                System.out.println("Writing " + endSessionMsg);
                writer.write(endSessionMsg);
                writer.flush();
                boolean gotExitMessage = false;
                while (p.isAlive()) {
                    String in2 = reader.readLine();
                    System.out.println("Read " + in2);
                    if (in2 != null) {
                        System.out.printf("Read - %s\n", in2);
                        if (in2.contains("EndSession")) {
                            gotExitMessage = true;
                        }
                    } else {
                        Thread.sleep(100);
                    }
                }
                assertEquals(0, p.exitValue());
                assertTrue(gotExitMessage);
            });
        }
    }
}


