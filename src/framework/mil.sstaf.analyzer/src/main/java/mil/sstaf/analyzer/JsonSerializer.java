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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mil.sstaf.analyzer.messages.BaseAnalyzerCommand;
import mil.sstaf.analyzer.messages.BaseAnalyzerResult;
import mil.sstaf.core.util.SSTAFException;

import java.util.Objects;
import java.util.function.Function;

public class JsonSerializer implements Function<BaseAnalyzerResult, String> {

    ObjectMapper objectMapper;

    public JsonSerializer() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public String apply(BaseAnalyzerResult o) {
        Objects.requireNonNull(o);
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new SSTAFException("Could not serialize " + o.getClass().getName(), e);
        }
    }
}

