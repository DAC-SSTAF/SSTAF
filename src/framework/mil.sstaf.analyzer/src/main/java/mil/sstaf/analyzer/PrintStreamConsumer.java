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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Implements {@code Consumer} to write a {@code String} to the output.
 */
public class PrintStreamConsumer implements Consumer<String> {

    private static final Logger logger = LoggerFactory.getLogger(PrintStreamConsumer.class);

    private final PrintStream outputWriter;

    public PrintStreamConsumer(PrintStream outputWriter) {
        this.outputWriter = Objects.requireNonNull(outputWriter, "outputWriter");
    }

    @Override
    public void accept(String s) {
        String toSend = s + "\n";
        logger.debug("Writing {}", toSend);
        outputWriter.println(toSend);
        outputWriter.flush();
        if (outputWriter.checkError()) {
            logger.error("Error writing to print stream");
            throw new SSTAFException("Error writing message");
        }
    }
}

