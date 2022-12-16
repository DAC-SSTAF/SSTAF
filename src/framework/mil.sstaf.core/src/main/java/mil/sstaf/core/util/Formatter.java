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

package mil.sstaf.core.util;

/**
 * Utility methods for pretty-printing things.
 */
public class Formatter {

    public static String millisToDHMS(long time_ms) {
        long toSeconds = 1000;
        long toMinutes = 60 * toSeconds;
        long toHours = 60 * toMinutes;
        long toDays = 24 * toHours;

        long days = time_ms / toDays;
        long rem1 = time_ms % toDays;
        long hours = rem1 / toHours;
        long rem2 = rem1 % toHours;
        long minutes = rem2 / toMinutes;
        long rem3 = rem2 % toMinutes;
        double seconds = (double) rem3 / (double) toSeconds;

        return String.format("%d:%d:%d:%.3f", days, hours, minutes, seconds);
    }
}

