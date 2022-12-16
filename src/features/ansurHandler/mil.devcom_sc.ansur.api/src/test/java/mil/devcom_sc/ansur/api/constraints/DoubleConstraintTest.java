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

package mil.devcom_sc.ansur.api.constraints;

import mil.sstaf.core.json.JsonLoader;
import mil.sstaf.core.util.SSTAFException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class DoubleConstraintTest {


    @Test
    public void noPropertyNameThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            DoubleConstraint constraint = DoubleConstraint.builder()
                    .lowerBound(6)
                    .upperBound(13).build();
        });
    }


    @Test
    public void factoryWorks() {
        Assertions.assertDoesNotThrow(() -> {
            String json = "{ \"class\":\"mil.devcom_sc.ansur.api.constraints.DoubleConstraint\",\"propertyName\":\"cervicaleheight\", \"lowerBound\":3.9, \"upperBound\":4.9}";
            DoubleConstraint constraint = new JsonLoader().load(json, DoubleConstraint.class, Path.of("Bob"));
        });

    }

    @Test
    public void noPropertyNameThrows2() {
        Assertions.assertThrows(SSTAFException.class, () -> {
            String json = "{ \"class\":\"mil.devcom_sc.ansur.api.constraints.DoubleConstraint\", \"lowerBound\":3.9, \"upperBound\":4.9}";
            DoubleConstraint constraint = new JsonLoader().load(json, DoubleConstraint.class, Path.of("Bob"));
        });
    }

}

