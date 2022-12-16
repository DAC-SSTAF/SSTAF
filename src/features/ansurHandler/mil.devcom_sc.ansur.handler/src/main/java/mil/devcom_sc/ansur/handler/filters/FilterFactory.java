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

import mil.devcom_sc.ansur.api.constraints.Constraint;
import mil.devcom_sc.ansur.api.constraints.DoubleConstraint;
import mil.devcom_sc.ansur.api.constraints.IntegerConstraint;
import mil.devcom_sc.ansur.api.constraints.StringConstraint;
import mil.sstaf.core.util.SSTAFException;

import java.util.ArrayList;
import java.util.List;

public class FilterFactory {

    public static Filter from(Constraint c) {
        Filter filter;
        if (c instanceof DoubleConstraint) {
            filter = new DoubleFilter((DoubleConstraint) c);
        } else if (c instanceof IntegerConstraint) {
            filter = new IntegerFilter((IntegerConstraint) c);
        } else if (c instanceof StringConstraint) {
            filter = new StringFilter((StringConstraint) c);
        } else {
            throw new SSTAFException("Huh?");
        }
        return filter;
    }

    public static List<Filter> from(Iterable<Constraint> constraints) {
        List<Filter> filters = new ArrayList<>();
        for(var c : constraints) {
           filters.add(from(c));
        }
        return filters;
    }
}

