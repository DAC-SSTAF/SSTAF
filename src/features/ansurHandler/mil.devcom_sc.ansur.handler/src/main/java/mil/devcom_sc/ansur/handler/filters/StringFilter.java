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

import lombok.Getter;
import lombok.ToString;
import mil.devcom_sc.ansur.api.constraints.StringConstraint;
import org.apache.commons.csv.CSVRecord;

import java.util.Objects;

/**
 * An implementation of {@code BaseFilter} that matches against
 * a range of Doubles.
 */
@ToString
public class StringFilter extends BaseFilter {
    @Getter
    private final String matches;

    public StringFilter(StringConstraint constraint) {
        super(constraint);
        Objects.requireNonNull(constraint.getMatches());
        this.matches = constraint.getMatches();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(CSVRecord record) {
        String valueString = record.get(getProperty().getHeaderLabel());
        return valueString.matches(matches);
    }
}
