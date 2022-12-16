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

package mil.devcom_sc.ansur.handler.filters;

import mil.devcom_sc.ansur.api.constraints.DoubleConstraint;
import mil.devcom_sc.ansur.messages.ValueKey;
import mil.sstaf.core.json.JsonLoader;
import mil.sstaf.core.util.SSTAFException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;

import static mil.devcom_sc.ansur.api.constraints.DoubleConstraint.builder;

public class DoubleFilterTest {



    String value1 = ValueKey.EAR_LENGTH.getHeaderLabel();
    String value2 = ValueKey.FOOT_LENGTH.getHeaderLabel();
    String value3 = ValueKey.SUBJECTS_BIRTH_LOCATION.getHeaderLabel();
    String value4 = ValueKey.WEIGHT_LBS.getHeaderLabel();
    String value5 = ValueKey.WAIST_DEPTH.getHeaderLabel();

    CSVParser getParser() throws IOException {
        String fakeCSV
                = String.format("%s,%s,%s,%s,%s\n",
                value1, value2, value3, value4, value5) +
                "1.5, 1, Stuff, 8, -4.5" + '\n' +
                "2.5, 3, Stuff, -8, 24.5" + '\n' +
                "3.5, 5, Stuff, 18, 34.5" + '\n' +
                "4.5, 7, Stuff, -18, 44.5" + '\n' +
                "5.5, 215, Stuff, 81, 564.5" + '\n';

        Reader reader = new StringReader(fakeCSV);
        return new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreSurroundingSpaces());
    }


    @Test
    public void parserCheck() throws IOException {

        Assertions.assertDoesNotThrow(() -> {
            CSVParser parser = getParser();
            List<CSVRecord> records = parser.getRecords();
            Assertions.assertEquals(5, records.size());

            List<String> headers = parser.getHeaderNames();
            Assertions.assertEquals(5, headers.size());

            Assertions.assertEquals("1.5", records.get(0).get(value1));
            Assertions.assertEquals("564.5", records.get(4).get(value5));
        });
    }

    @Test
    public void noPropertyNameThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            DoubleConstraint constraint = builder()
                    .lowerBound(6)
                    .upperBound(13).build();

            DoubleFilter df = new DoubleFilter(constraint);
        });
    }

    @Test
    public void singleMatchTest() {
        Assertions.assertDoesNotThrow(() -> {
            DoubleConstraint constraint = builder()
                    .propertyName(value1)
                    .lowerBound(2.73)
                    .upperBound(4.01).build();
            Filter filter = FilterFactory.from(constraint);
            List<CSVRecord> records = getParser().getRecords();
            Assertions.assertFalse(filter.matches(records.get(0)));
            Assertions.assertFalse(filter.matches(records.get(1)));
            Assertions.assertTrue(filter.matches(records.get(2)));
            Assertions.assertFalse(filter.matches(records.get(3)));
            Assertions.assertFalse(filter.matches(records.get(4)));
        });

        Assertions.assertDoesNotThrow(() -> {
            DoubleConstraint constraint = builder()
                    .propertyName(value5)
                    .lowerBound(-20)
                    .upperBound(0).build();
            Filter filter = FilterFactory.from(constraint);
            List<CSVRecord> records = getParser().getRecords();
            Assertions.assertTrue(filter.matches(records.get(0)));
            Assertions.assertFalse(filter.matches(records.get(1)));
            Assertions.assertFalse(filter.matches(records.get(2)));
            Assertions.assertFalse(filter.matches(records.get(3)));
            Assertions.assertFalse(filter.matches(records.get(4)));
        });

        Assertions.assertDoesNotThrow(() -> {
            DoubleConstraint constraint = builder()
                    .propertyName(value5)
                    .lowerBound(20)
                    .upperBound(-20).build();
            Filter filter = FilterFactory.from(constraint);
            List<CSVRecord> records = getParser().getRecords();
            Assertions.assertTrue(filter.matches(records.get(0)));
            Assertions.assertFalse(filter.matches(records.get(1)));
            Assertions.assertFalse(filter.matches(records.get(2)));
            Assertions.assertFalse(filter.matches(records.get(3)));
            Assertions.assertFalse(filter.matches(records.get(4)));
        });
    }

    @Test
    public void factoryWorks() {
        Assertions.assertDoesNotThrow(() -> {
            String json =
                    String.format("{ \"class\":\"mil.devcom_sc.ansur.api.constraints.DoubleConstraint\",\"propertyName\":\"%s\", \"lowerBound\":3.9, \"upperBound\":4.9}", value1);

            DoubleConstraint constraint = new JsonLoader().load(json, mil.devcom_sc.ansur.api.constraints.DoubleConstraint.class, Path.of("Bob"));
            Filter filter = FilterFactory.from(constraint);

            List<CSVRecord> records = getParser().getRecords();
            Assertions.assertFalse(filter.matches(records.get(0)));
            Assertions.assertFalse(filter.matches(records.get(1)));
            Assertions.assertFalse(filter.matches(records.get(2)));
            Assertions.assertTrue(filter.matches(records.get(3)));
            Assertions.assertFalse(filter.matches(records.get(4)));
        });

    }

    @Test
    public void noPropertyNameThrow() {
        Assertions.assertThrows(SSTAFException.class, () -> {
            String json = "{ \"class\":\"mil.devcom_sc.ansur.api.constraints.DoubleFilter\", \"lowerBound\":3.9, \"upperBound\":4.9}";
            mil.devcom_sc.ansur.api.constraints.DoubleConstraint constraint = new JsonLoader().load(json, mil.devcom_sc.ansur.api.constraints.DoubleConstraint.class, Path.of("Bob"));
        });
    }

    @Test
    public void builderWorks() {
        Assertions.assertDoesNotThrow(() -> {
            DoubleConstraint constraint = builder()
                    .propertyName(value1)
                    .lowerBound(3.9)
                    .upperBound(500)
                    .build();
            Filter filter = FilterFactory.from(constraint);

            List<CSVRecord> records = getParser().getRecords();
            Assertions.assertFalse(filter.matches(records.get(0)));
            Assertions.assertFalse(filter.matches(records.get(1)));
            Assertions.assertFalse(filter.matches(records.get(2)));
            Assertions.assertTrue(filter.matches(records.get(3)));
            Assertions.assertTrue(filter.matches(records.get(4)));
        });
    }


    @Test
    public void equalsWorks() {
        Assertions.assertDoesNotThrow(() -> {
            DoubleConstraint constraint = builder()
                    .propertyName(value1)
                    .equals(4.5)
                    .build();
            Filter filter = FilterFactory.from(constraint);
            List<CSVRecord> records = getParser().getRecords();
            Assertions.assertFalse(filter.matches(records.get(0)));
            Assertions.assertFalse(filter.matches(records.get(1)));
            Assertions.assertFalse(filter.matches(records.get(2)));
            Assertions.assertTrue(filter.matches(records.get(3)));
            Assertions.assertFalse(filter.matches(records.get(4)));
        });
    }

    @Test
    public void equalsWorks2() {
        Assertions.assertDoesNotThrow(() -> {
            DoubleConstraint constraint = builder()
                    .propertyName(value1)
                    .lowerBound(4.5)
                    .upperBound(4.5)
                    .build();
            Filter filter = FilterFactory.from(constraint);
            List<CSVRecord> records = getParser().getRecords();
            Assertions.assertFalse(filter.matches(records.get(0)));
            Assertions.assertFalse(filter.matches(records.get(1)));
            Assertions.assertFalse(filter.matches(records.get(2)));
            Assertions.assertTrue(filter.matches(records.get(3)));
            Assertions.assertFalse(filter.matches(records.get(4)));
        });
    }

    @Test
    public void badPropertyNameThrows() {
        Assertions.assertThrows(SSTAFException.class, () -> {
            DoubleConstraint constraint =
                    builder().propertyName("NotAValidProperty")
                            .lowerBound(1.0)
                            .upperBound(3.14)
                            .build();
            Filter filter = FilterFactory.from(constraint);
            List<CSVRecord> records = getParser().getRecords();
            Assertions.assertFalse(filter.matches(records.get(0)));
            Assertions.assertFalse(filter.matches(records.get(1)));
            Assertions.assertFalse(filter.matches(records.get(2)));
            Assertions.assertFalse(filter.matches(records.get(3)));
            Assertions.assertFalse(filter.matches(records.get(4)));
        });
    }

}

