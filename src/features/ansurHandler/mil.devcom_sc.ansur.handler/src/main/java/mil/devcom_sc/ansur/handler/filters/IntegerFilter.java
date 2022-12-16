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
import mil.devcom_sc.ansur.api.constraints.IntegerConstraint;
import org.apache.commons.csv.CSVRecord;

/**
 * An implementation of {@code BaseFilter} that matches against
 * a range of integers.
 */
@ToString
public class IntegerFilter extends BaseFilter {

    @Getter
    private final int lowerBound;
    @Getter
    private final int upperBound;
    @Getter
    private final int equals;

    private final boolean useEquals;

    public IntegerFilter(IntegerConstraint constraint) {
        super(constraint);

        if (constraint.getEquals() != 0) {
            this.equals = constraint.getEquals();
            this.useEquals = true;
        } else if (constraint.getLowerBound() == constraint.getUpperBound()) {
            this.equals = constraint.getUpperBound();
            this.useEquals = true;
        } else {
            this.equals = constraint.getEquals();
            this.useEquals = false;
        }

        int lowerBound;
        int upperBound;
        if (constraint.getUpperBound() > constraint.getLowerBound()) {
            upperBound = constraint.getUpperBound();
            lowerBound = constraint.getLowerBound();
        } else if (constraint.getUpperBound() == 0) {
            upperBound = Integer.MAX_VALUE;
            lowerBound = constraint.getLowerBound();
        } else if (constraint.getLowerBound() == 0) {
            upperBound = constraint.getUpperBound();
            lowerBound = Integer.MIN_VALUE;
        } else {
            upperBound = constraint.getLowerBound();
            lowerBound = constraint.getUpperBound();
        }
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(CSVRecord record) {
        String valueString = record.get(getProperty().getHeaderLabel());
        int val = Integer.parseInt(valueString);
        if (useEquals) {
            return val == equals;
        } else {
            return val >= lowerBound && val <= upperBound;
        }
    }

}
