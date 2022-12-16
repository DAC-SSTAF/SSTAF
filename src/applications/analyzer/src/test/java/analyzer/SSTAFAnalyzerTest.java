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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SSTAFAnalyzerTest {

    /**
     * Unzip the zip file to the destination directory
     *
     * @param zipFilePath
     * @param destDir
     */
    private void unzip(Path zipFilePath, Path destDir) throws IOException {
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

    Path extractDistribution() throws IOException {

        String cwdString = System.getProperty("user.dir");

        Path analyzerZip = Path.of(cwdString,
                "build", "distributions", "sstaf-analyzer.zip");
        Path tmpDir = Path.of(cwdString, "build", "tmp");
        Path expandedDir = Files.createTempDirectory(tmpDir, "SSTAFAnalyzerTest");
        unzip(analyzerZip.normalize().toAbsolutePath(),
                expandedDir.normalize().toAbsolutePath());
        return Path.of(expandedDir.toString(), "sstaf-analyzer");
    }

    void runItLinux(Path installationDir) throws IOException {

        Path binDir = Path.of(installationDir.toString(), "bin");

        Path logDir = Path.of(installationDir.toString(), "logs");
        Files.createDirectory(logDir);

        File outFile = Path.of(logDir.toString(), "stdout").toFile();
        File errFile = Path.of(logDir.toString(), "stderr").toFile();

        ProcessBuilder builder = new ProcessBuilder();
        builder.redirectOutput(outFile);
        builder.redirectError(errFile);

        builder.directory(binDir.toFile())
                .command("bash", "sstaf-analyzer", "fred");

        builder.start();
    }

    @Test
    @EnabledOnOs(value = {OS.LINUX, OS.MAC})
    void doit() throws Exception {
        Path installationDir = extractDistribution();
        runItLinux(installationDir);

    }

}

