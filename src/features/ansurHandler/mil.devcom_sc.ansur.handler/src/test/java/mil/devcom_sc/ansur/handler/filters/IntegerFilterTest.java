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

import mil.devcom_sc.ansur.api.constraints.IntegerConstraint;
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

public class IntegerFilterTest {

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
        return new CSVParser(reader, CSVFormat.DEFAULT.builder().
                setHeader().setSkipHeaderRecord(true).setIgnoreSurroundingSpaces(true).build());
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
            IntegerConstraint constraint = IntegerConstraint.builder()
                    .lowerBound(6)
                    .upperBound(13).build();
        });
    }

    @Test
    public void singleMatchTest() {
        Assertions.assertDoesNotThrow(() -> {
            IntegerConstraint constraint = IntegerConstraint.builder()
                    .propertyName(value2)
                    .lowerBound(6)
                    .upperBound(13).build();
            Filter filter = FilterFactory.from(constraint);
            List<CSVRecord> records = getParser().getRecords();
            Assertions.assertFalse(filter.matches(records.get(0)));
            Assertions.assertFalse(filter.matches(records.get(1)));
            Assertions.assertFalse(filter.matches(records.get(2)));
            Assertions.assertTrue(filter.matches(records.get(3)));
            Assertions.assertFalse(filter.matches(records.get(4)));
        });

        Assertions.assertDoesNotThrow(() -> {
            IntegerConstraint constraint = IntegerConstraint.builder()
                    .propertyName(value4)
                    .lowerBound(-20)
                    .upperBound(0).build();
            Filter filter = FilterFactory.from(constraint);

            List<CSVRecord> records = getParser().getRecords();
            Assertions.assertFalse(filter.matches(records.get(0)));
            Assertions.assertTrue(filter.matches(records.get(1)));
            Assertions.assertFalse(filter.matches(records.get(2)));
            Assertions.assertTrue(filter.matches(records.get(3)));
            Assertions.assertFalse(filter.matches(records.get(4)));
        });

        Assertions.assertDoesNotThrow(() -> {
            IntegerConstraint constraint = IntegerConstraint.builder()
                    .propertyName(value4)
                    .lowerBound(20)
                    .upperBound(-20).build();
            Filter filter = FilterFactory.from(constraint);
            List<CSVRecord> records = getParser().getRecords();
            Assertions.assertTrue(filter.matches(records.get(0)));
            Assertions.assertTrue(filter.matches(records.get(1)));
            Assertions.assertTrue(filter.matches(records.get(2)));
            Assertions.assertTrue(filter.matches(records.get(3)));
            Assertions.assertFalse(filter.matches(records.get(4)));
        });
    }

    @Test
    public void factoryWorks() {
        Assertions.assertDoesNotThrow(() -> {
            String json = String.format("{ \"class\":\"mil.devcom_sc.ansur.api.constraints.IntegerConstraint\",\"propertyName\":\"%s\", \"lowerBound\":6, \"upperBound\":13}", value2);

            IntegerConstraint constraint = new JsonLoader().load(json, IntegerConstraint.class, Path.of("bob"));
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
    public void missingPropertyThrows() {

        Assertions.assertThrows(SSTAFException.class, () -> {
            String json = "{ \"class\":\"mil.devcom_sc.ansur.api.constraints.IntegerConstraint\", \"lowerBound\":6, \"upperBound\":13}";
            IntegerConstraint constraint = new JsonLoader().load(json, IntegerConstraint.class, Path.of("bob"));
        });
    }

    @Test
    public void equalsWorks() {
        Assertions.assertDoesNotThrow(() -> {

            IntegerConstraint constraint = IntegerConstraint.builder()
                    .propertyName(value2)
                    .equals(7)
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
            IntegerConstraint constraint = IntegerConstraint.builder()
                    .propertyName("ThisIsBad")
                    .equals(7)
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

