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

package mil.sstaf.session;

import mil.sstaf.core.json.JsonLoader;
import mil.sstaf.session.control.Session;
import mil.sstaf.session.control.SessionConfiguration;
import mil.sstaf.session.control.EntityController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class SessionFactoryTest {
    @Test
    void canReadGoodSession() {
        Assertions.assertDoesNotThrow(() -> {
                    Path sessionPath = Path.of("src/test/resources/SessionFactoryTest/session1.json");
                    Path entityPath = Path.of("src/test/resources/EntityControllerFactoryTest/EntityConfig1.json");

                    JsonLoader loader = new JsonLoader();
                    SessionConfiguration sessionConfiguration = loader.load(sessionPath, SessionConfiguration.class);

                    EntityController entityController = EntityController.from(entityPath.toFile());

                    Session session = Session.of(sessionConfiguration, entityController);
                }
        );
    }
}

