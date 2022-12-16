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

package mil.sstaf.core.json;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JsonConfig {

    private static final AtomicReference<JsonConfig> reference = new AtomicReference<>();
    private final Lock lock = new ReentrantLock();
    private Path rootDir;

    public static JsonConfig getInstance() {
        synchronized (reference) {
            JsonConfig jsonConfig = reference.get();
            if (jsonConfig == null) {
                jsonConfig = new JsonConfig();
                reference.set(jsonConfig);
            }
            return jsonConfig;
        }
    }

    private JsonConfig() {
        String cwd = System.getProperty("user.dir");
        rootDir = Path.of(cwd);
    }

    public void setRootDir(Path rootDir) {
        Objects.requireNonNull(rootDir);
        try {
            lock.lock();
            this.rootDir = rootDir;
        } finally {
            lock.unlock();
        }
    }

    public Path getRootDir() {
        try {
            lock.lock();
            return rootDir;
        } finally {
            lock.unlock();
        }
    }
}

