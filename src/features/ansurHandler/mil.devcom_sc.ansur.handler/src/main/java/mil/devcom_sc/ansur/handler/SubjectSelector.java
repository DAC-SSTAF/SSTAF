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

import mil.devcom_sc.ansur.handler.filters.Filter;
import mil.devcom_sc.ansur.messages.ValueKey;
import mil.sstaf.core.util.SSTAFException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.random.MersenneTwister;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

/**
 * Utility class for selecting a single ANSUR subject given a list
 * of {@code Contraint}s
 */
public class SubjectSelector {
    /**
     * Selects a single subject from all ANSUR records.
     * <p>
     * ANSUR records are filtered using the list of {@code BaseFilter} objects. If multiple matches
     * are found, one is selected randomly.
     *
     * @param streams    the {@code InputStream}s from which to read the data
     * @param filters    the constraints to apply
     * @param randomSeed the seed for the random number generator
     * @return a {@code Map} that contains all of the fields for the subject
     */
    static Map<ValueKey, Object> select(final List<InputStream> streams,
                                        final List<Filter> filters,
                                        final long randomSeed) {

        List<CSVRecord> matches = findAllMatches(streams, filters);
        CSVRecord chosen = getChosen(matches, randomSeed);
        return parseSubject(chosen);
    }

    /**
     * Finds all {@code CSVRecord}s that match the criteria across multiple {@code InputStream}s.
     *
     * @param streams the InputStreams from which to read.
     * @param filters the {@code BaseFilter}s to apply
     * @return a List of CSVRecords that match the constraints.
     */
    static List<CSVRecord> findAllMatches(List<InputStream> streams, List<Filter> filters) {
        List<CSVRecord> matches = new ArrayList<>();
        for (InputStream stream : streams) {
            Objects.requireNonNull(stream, "stream");
            List<CSVRecord> csvRecords = findMatches(stream, filters);
            matches.addAll(csvRecords);
        }
        if (matches.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Filter f : filters) {
                sb.append("[").append(f).append("] ");
            }
            throw new SSTAFException("No subjects matched constraints of " + sb);
        }
        return matches;
    }

    /**
     * Selects one subject record from a list of records that have met the selection criteria
     *
     * @param matches    the list of subject records from which to choose.
     * @param randomSeed the seed for initializing the random number generator
     * @return the selected subject record
     */
    private static CSVRecord getChosen(final List<CSVRecord> matches, long randomSeed) {
        CSVRecord chosen;
        int numPossibilities = matches.size();
        if (numPossibilities == 0) {
            throw new SSTAFException("No subjects matched constraints");
        } else if (numPossibilities == 1) {
            chosen = matches.get(0);
        } else {
            MersenneTwister rng = new MersenneTwister(randomSeed);
            int selection = rng.nextInt(numPossibilities);
            chosen = matches.get(selection);
        }
        return chosen;
    }

    /**
     * Parses a subject record into a Map.
     *
     * @param chosen the record to parse
     * @return a Map full of ANSUR II metrics
     */
    static Map<ValueKey, Object> parseSubject(final CSVRecord chosen) {
        Map<ValueKey, Object> subjectMap = new EnumMap<>(ValueKey.class);
        for (ValueKey key : ValueKey.values()) {
            String valString = chosen.get(key.getHeaderLabel());
            if (key.getType() == Integer.class) {
                subjectMap.put(key, Integer.parseInt(valString));
            } else if (key.getType() == Double.class) {
                //
                // the ANSUR II database doesn't use double values yet.
                //
                subjectMap.put(key, Double.parseDouble(valString));
            } else {
                subjectMap.put(key, valString);
            }
        }
        return Collections.unmodifiableMap(subjectMap);
    }

    /**
     * Scans an {@code InputStream} for {@code CSVRecord}s that match the given {@code BaseFilter}s.
     *
     * @param inputStream the InputStream for the CSV data source
     * @param filters     the Constraints to use to filter the records
     * @return a List of matching CSVRecords
     */
    static List<CSVRecord> findMatches(final InputStream inputStream, final List<Filter> filters) {
        Objects.requireNonNull(inputStream, "inputStream ");
        List<CSVRecord> matches = new ArrayList<>();
        CSVRecord dup = null;
        try {
            Reader reader = new InputStreamReader(inputStream);
            CSVParser parser = new CSVParser(reader,
                    CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreSurroundingSpaces().withIgnoreHeaderCase());

            for (final CSVRecord record: parser) {
                boolean itMatches = true;
                dup = record;
                for (final Filter filter : filters) {
                    itMatches = filter.matches(record);
                    if (!itMatches) break;
                }
                if (itMatches) {
                    matches.add(record);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new SSTAFException("Failed to read " + dup, e);
        }
        return matches;
    }
}

