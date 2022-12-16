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

package mil.sstaf.analyzer;

import mil.sstaf.core.util.SSTAFException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Implements {@code Consumer} to write a {@code String} to the output file.
 */
public class FileConsumer implements Consumer<String> {

    private final BufferedWriter outputWriter;

    public FileConsumer(String fileName) throws IOException {
        Objects.requireNonNull(fileName, "fileName");
        FileWriter fw = new FileWriter(fileName);
        this.outputWriter = new BufferedWriter(fw);
    }

    @Override
    public void accept(String s) {
        try {
            outputWriter.write(s);
            outputWriter.flush();
        } catch (IOException e) {
            throw new SSTAFException("Error writing message", e);
        }
    }
}
