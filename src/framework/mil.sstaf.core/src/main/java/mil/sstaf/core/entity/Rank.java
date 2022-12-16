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

package mil.sstaf.core.entity;

import java.util.Objects;

public enum Rank {
    E1("PVT"),
    E2("PV2"),
    E3("PVC"),
    E4("CPL"),
    E5("SGT"),
    E6("SSG"),
    E7("SFC"),
    E8("MSG"),
    E9("SGM"),
    WO1("WO1"),
    WO2("CW2"),
    WO3("CW3"),
    WO4("CW4"),
    WO5("CW5"),
    O1("2LT"),
    O2("1LT"),
    O3("CPT"),
    O4("MAJ"),
    O5("LTC"),
    O6("COL"),
    O7("BG"),
    O8("MG"),
    O9("LTG"),
    O10("GEN");

    private final String code;

    Rank(String abbreviation) {
        this.code = Objects.requireNonNull(abbreviation);
    }

    public static Rank findMatch(String code) {
        for (Rank rank : values()) {
            if (rank.matches(code)) {
                return rank;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public boolean matches(String code) {
        return this.name().equals(code) || this.code.equals(code);
    }
}

