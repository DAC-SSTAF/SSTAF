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

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Utilities for random numbers.
 */
public class RNGUtilities {

    /**
     * Generates a new random number seed from a parent generator.
     * <p>
     * The purpose of this method is to repeatedly create seeds for randomized sub-problems
     * that are not immediately adjacent to the parent generator. For most generators, seeding with
     * the next value from the parent nearly synchronizes the child with the parent, resulting in
     * overlaps in the draws. This method XORs two draws to try to get to a different part of the
     * random draw space.
     *
     * @param randomGenerator the parent random number generator
     * @return the seed for the sub-problem.
     */
    public static long generateSubSeed(final RandomGenerator randomGenerator) {
        long val1 = randomGenerator.nextLong();
        long val2 = randomGenerator.nextLong();
        return val1 ^ val2;
    }
}

