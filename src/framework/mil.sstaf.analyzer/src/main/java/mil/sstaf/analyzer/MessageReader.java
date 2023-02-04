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

    private static final Logger logger = LoggerFactory.getLogger(MessageReader.class);
    @Getter
    private final BufferedReader reader;
    private Candidate candidate;

    public MessageReader(BufferedReader reader) {
        this.reader = Objects.requireNonNull(reader, "reader");
    }

    public int getOpenCurlies() {
        return candidate == null ? 0 : candidate.getOpenCurlies();
    }

    public int getOpenSquares() {
        return candidate == null ? 0 : candidate.getOpenSquares();
    }

    public String get() {
        logger.debug("getting...");
        candidate = new Candidate();
        try {
            boolean done = false;
            while (reader.ready() && !done) {
                String line = reader.readLine();
                if (line == null) {
                    if (!candidate.isEmpty()) {
                        throw new SSTAFException("Malformed JSON, got '"
                        +candidate.getString() + "'");
                    }
                    break;
                }  else {
                    done = candidate.addLine(line);
                }
            }
            if (candidate.isDone()) {
                return candidate.getString();
            } else {
                return null;
            }
        } catch (IOException ioe) {
            return null;
        }
    }

    static class Candidate {

        private StringBuilder sb = new StringBuilder();
        @Getter
        private int openCurlies = 0;

        @Getter
        private int openSquares = 0;

        boolean addLine(String line) {
            Objects.requireNonNull(line, "line is null");
            if (line.length() > 0){
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
                sb.append(clean);
            }
            return isDone();
        }

        boolean isDone() {
            return sb.length() > 0 && openCurlies == 0 && openSquares == 0;
        }

        String getString() {
            return sb.toString();
        }

        boolean isEmpty() {
            return sb.length() == 0;
        }
    }
}

