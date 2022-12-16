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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class CompletionServiceAdapterTest {

    public static final int NUM_VALUES = 2345;

    @Test
    void testCSA() {
        ExecutorService executorService = Executors.newFixedThreadPool(16);

        Random r = new Random(NUM_VALUES);
        List<Callable<Long>> callables = new ArrayList<>(NUM_VALUES);

        Accumulator accumulatorOrig = new Accumulator();
        for (int i = 0; i < NUM_VALUES; ++i) {
            Task t = new Task();
            t.value = Math.abs(r.nextInt());
            callables.add(t);
            accumulatorOrig.add(t.value);
        }

        Accumulator accumulator = new Accumulator();
        CompletionServiceAdapter.processCompletions(callables, executorService, accumulator::add);
        Assertions.assertEquals(accumulatorOrig.total, accumulator.total);
    }

    static class Accumulator {
        long total = 0;

        void add(long value) {
            total += value;
        }
    }

    static class Task implements Callable<Long> {

        long value = 0;

        @Override
        public Long call() throws Exception {
            Thread.sleep(value % 17);
            return value;
        }
    }
}


