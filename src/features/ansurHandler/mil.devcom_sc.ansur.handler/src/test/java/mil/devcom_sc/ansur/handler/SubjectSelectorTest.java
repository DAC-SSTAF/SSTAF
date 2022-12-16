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

package mil.devcom_sc.ansur.handler;

import mil.devcom_sc.ansur.api.constraints.Constraint;
import mil.devcom_sc.ansur.api.constraints.IntegerConstraint;
import mil.devcom_sc.ansur.api.constraints.StringConstraint;
import mil.devcom_sc.ansur.handler.filters.Filter;
import mil.devcom_sc.ansur.handler.filters.FilterFactory;
import mil.devcom_sc.ansur.handler.filters.IntegerFilter;
import mil.devcom_sc.ansur.handler.filters.StringFilter;
import mil.devcom_sc.ansur.messages.ValueKey;
import mil.sstaf.core.util.SSTAFException;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SubjectSelectorTest {
    @Test
    public void matchSubjectWorks() {
        Assertions.assertDoesNotThrow(() -> {

            List<InputStream> streams = getStreamList();
            
            IntegerConstraint constraint = IntegerConstraint.builder()
                    .propertyName(ValueKey.SUBJECT_ID.getHeaderLabel())
                    .upperBound(10127).lowerBound(10127).build();

            IntegerFilter filter = new IntegerFilter(constraint);
            List<Filter> filters = List.of(filter);

            Map<ValueKey, Object> subject = SubjectSelector.select(streams, filters, 1234);
            Assertions.assertNotNull(subject);
            Assertions.assertEquals(1371, subject.get(ValueKey.ACROMIAL_HEIGHT));
        });
    }

    @Test
    public void noMatchThrows() {
        Assertions.assertThrows(SSTAFException.class, () -> {
            List<InputStream> streams = getStreamList();
            IntegerConstraint constraint = IntegerConstraint.builder().propertyName(ValueKey.WAIST_BREADTH.getHeaderLabel())
                    .lowerBound(123456).build();
            IntegerFilter filter = new IntegerFilter(constraint);
            List<Filter> filters = List.of(filter);
            Map<ValueKey, Object> subject = SubjectSelector.select(streams, filters, 1234);
        });
    }

    @Test
    public void anyMaleGivesSomething() {
        Assertions.assertDoesNotThrow(() -> {
            List<InputStream> streams = getStreamList();
            StringConstraint constraint = StringConstraint.builder().propertyName(ValueKey.GENDER.getHeaderLabel())
                    .matches("Male").build();
            StringFilter filter = new StringFilter(constraint);
            List<Filter> filters = List.of(filter);
            Map<ValueKey, Object> subject = SubjectSelector.select(streams, filters, 1234);
            Assertions.assertNotNull(subject);
            Assertions.assertEquals("Male", subject.get(ValueKey.GENDER));
            Assertions.assertNotEquals("Female", subject.get(ValueKey.GENDER));
        });
    }

    @Test
    public void anyFemaleGivesSomething() {
        Assertions.assertDoesNotThrow(() -> {
            List<InputStream> streams = getStreamList();
            StringConstraint constraint = StringConstraint.builder().propertyName(ValueKey.GENDER.getHeaderLabel())
                    .matches("Female").build();
            StringFilter filter = new StringFilter(constraint);
            List<Filter> filters = List.of(filter);
            Map<ValueKey, Object> subject = SubjectSelector.select(streams, filters, 1234);
            Assertions.assertNotNull(subject);
            Assertions.assertEquals("Female", subject.get(ValueKey.GENDER));
            Assertions.assertNotEquals("Male", subject.get(ValueKey.GENDER));
        });
    }

    @Test
    public void noFiltersGivesSomething() {
        Assertions.assertDoesNotThrow(() -> {
            List<InputStream> streams = getStreamList();
            List<Filter> filters = List.of();
            Map<ValueKey, Object> subject = SubjectSelector.select(streams, filters, 1234);
            Assertions.assertNotNull(subject);
            Integer value = (Integer) subject.get(ValueKey.CHEST_CIRCUMFERENCE);
            Assertions.assertTrue(value > 10);
        });
    }

    @Test
    public void all11BsAreFound() {
        Assertions.assertDoesNotThrow(() -> {
            StringConstraint contraint = StringConstraint.builder().propertyName(ValueKey.PRIMARY_MOS.getHeaderLabel())
                    .matches("11B").build();
            StringFilter filter= new StringFilter(contraint);
            List<Filter> filters = List.of(filter);
            List<CSVRecord> subjects = SubjectSelector.findAllMatches(getStreamList(), filters);
            Assertions.assertNotNull(subjects);
            Assertions.assertEquals(671, subjects.size());
        });
    }


    @Test
    public void multipleFiltersWork() {
        Assertions.assertDoesNotThrow(() -> {
            List<InputStream> streams = getStreamList();
            List<Constraint> constraints = List.of(
                    StringConstraint.builder().propertyName(ValueKey.GENDER.getHeaderLabel())
                            .matches("Male").build(),
                    IntegerConstraint.builder().propertyName(ValueKey.BALL_OF_FOOT_LENGTH.getHeaderLabel()).
                            lowerBound(198).upperBound(198).build(),
                    IntegerConstraint.builder().propertyName(ValueKey.FOOT_LENGTH.getHeaderLabel()).
                            lowerBound(270).upperBound(270).build(),
                    IntegerConstraint.builder().propertyName(ValueKey.LOWER_THIGH_CIRCUMFERENCE.getHeaderLabel()).
                            lowerBound(390).upperBound(395).build(),
                    StringConstraint.builder().propertyName(ValueKey.SUBJECTS_BIRTH_LOCATION.getHeaderLabel())
                            .matches(".*uck.*").build()  // Kentucky!
            );
            List<Filter> filters = FilterFactory.from(constraints);
            Map<ValueKey, Object> subject = SubjectSelector.select(streams, filters, 1234);
            Assertions.assertNotNull(subject);
            //
            // It should be this guy.
            //
            Assertions.assertEquals(10303, subject.get(ValueKey.SUBJECT_ID));
        });
    }

    private List<InputStream> getStreamList() {
        List<String> resources = List.of("src/main/resources/ansur/ANSUR II MALE Public.csv", "src/main/resources/ansur/ANSUR II FEMALE Public.csv");
        return resources.stream().map(this::makeStream).collect(Collectors.toList());
    }

    private InputStream makeStream(String fileName) {
        try {
            return new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}

