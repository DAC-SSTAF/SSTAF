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

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Simple utility to processing tasks concurrently and processing the results as they become
 * available
 */
public class CompletionServiceAdapter {
    /**
     * Simplifies the idiomatic pattern for using a CompletionService into a single method.
     *
     * @param callables       a List of the Callables to process
     * @param executorService the Executor that will process the Callable tasks.
     * @param consumer        a Consumer that processes each Future
     * @param <T>             the return type of the call() method in the Callable.
     */
    public static <T> void processCompletions(final List<Callable<T>> callables,
                                              final ExecutorService executorService,
                                              Consumer<T> consumer) {
        try {
            CompletionService<T> completionService = new ExecutorCompletionService<T>(executorService);
            callables.forEach(completionService::submit);
            Future<T> future;
            int remaining = callables.size();
            do {
                future = completionService.take();
                T value = future.get();
                --remaining;
                consumer.accept(value);
            } while (remaining > 0);

        } catch (InterruptedException | ExecutionException e) {
            throw new SSTAFException("Processing failed!", e);
        }
    }
}

