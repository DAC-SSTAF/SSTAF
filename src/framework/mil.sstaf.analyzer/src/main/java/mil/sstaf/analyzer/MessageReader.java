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

import lombok.Getter;
import mil.sstaf.core.util.SSTAFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

public class MessageReader implements Supplier<String> {

    @Getter
    private final BufferedReader reader;

    @Getter

    private int openCurlies = 0;

    @Getter
    private int openSquares = 0;

    private static final Logger logger = LoggerFactory.getLogger(MessageReader.class);

    public MessageReader(BufferedReader reader) {
        this.reader = Objects.requireNonNull(reader, "reader");
    }

    String scanAndCleanLine(String line) {
        String clean = line.replaceAll("\\R", " ");
        for (char c : clean.toCharArray()) {
            if (c == '{') ++openCurlies;
            else if (c == '}') --openCurlies;
            else if (c == '[') ++openSquares;
            else if (c == ']') --openSquares;
        }
        if (openCurlies < 0) {
            throw new SSTAFException("Encountered extra '}' in '" + clean + "'");
        }
        if (openSquares < 0) {
            throw new SSTAFException("Encountered extra ']' in '" + clean + "'");
        }

        return clean;
    }

    public String get() {
        logger.info("getting...");
        StringBuilder sb = new StringBuilder();
        try {
            while (true) {
                String line = reader.readLine();
                logger.info("Read {}", line);
                if (line == null) {
                    break;
                } else {
                    String clean = scanAndCleanLine(line);
                    sb.append(clean);
                    if (openCurlies == 0 && openSquares == 0) {
                        break;
                    }
                }
            }
            if (openCurlies != 0 || openSquares != 0) {
                throw new SSTAFException("Malformed JSON command: '" + sb + "'");
            }
            return sb.toString();
        } catch (IOException ioe) {
            throw new SSTAFException("Could not read message",ioe);
        }
    }

}

