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

import mil.sstaf.analyzer.messages.BaseAnalyzerCommand;
import mil.sstaf.core.json.JsonLoader;
import mil.sstaf.core.util.SSTAFException;

import java.nio.file.Path;
import java.util.function.Function;

/**
 * Deserializes a JSON strings into a {@code SessionTasks}
 *
 */
public class JsonDeserializer implements Function<String, BaseAnalyzerCommand> {

    private final JsonLoader jsonLoader = new JsonLoader();
    private final Path fakeFile;

    /**
     * Constructor
     */
    public JsonDeserializer() {
        String cwd = System.getProperty("user.dir");
        fakeFile =Path.of(cwd, "fakeFile.json");
    }

    /**
     * Deserializes the {@code String} into a {@code SessionTas}.
     * A {@code SSTAFException} is thrown if the JSON can not be read into
     * a {@code BaseAnalyzerCommand}
     *
     * @param jsonString the JSON string to be deserialized.
     * @return A SessionTask.
     */
    @Override
    public BaseAnalyzerCommand apply(String jsonString) {
        try {
            return jsonLoader.load(jsonString, BaseAnalyzerCommand.class, fakeFile);
        } catch (Exception e) {
            throw new SSTAFException("Could not deserialize '" + jsonString
                    + "' into a BaseAnalyzerCommand", e);
        }
    }
}

