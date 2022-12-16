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

import mil.devcom_sc.ansur.api.ANSURConfiguration;
import mil.devcom_sc.ansur.api.ANSURIIAnthropometry;
import mil.devcom_sc.ansur.api.constraints.Constraint;
import mil.devcom_sc.ansur.handler.filters.Filter;
import mil.devcom_sc.ansur.handler.filters.FilterFactory;
import mil.devcom_sc.ansur.messages.*;
import mil.sstaf.core.entity.Address;
import mil.sstaf.core.entity.Message;
import mil.sstaf.core.features.BaseHandler;
import mil.sstaf.core.features.FeatureConfiguration;
import mil.sstaf.core.features.HandlerContent;
import mil.sstaf.core.features.ProcessingResult;
import mil.sstaf.core.util.SSTAFException;
import org.apache.commons.math3.random.MersenneTwister;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * Implementation of a SSTAF {@code Handler} that provides access to
 * the ANSUR II dataset.
 * <p>
 * The ANSUR II data is included in the module as two comma-separated value
 * files.
 */
public class ANSURIIHandler extends BaseHandler implements ANSURIIAnthropometry {

    /*
     * Keys
     */
    public static final String CK_SUBJECT_ID = "subjectID";
    public static final String CK_CONSTRAINTS = "constraints";
    public static final String FEATURE_NAME = "ANSUR Anthropometry";
    private Map<ValueKey, Object> subjectMap;
    private double height_cm = -1;
    private double span_cm = -1;
    private double weight_kg = -1;
    private Sex sex = null;
    private Handedness handedness = null;

    public ANSURIIHandler() {
        super(FEATURE_NAME, 1, 0, 0, true, "Provides ANSUR II data");
    }

    @Override
    public void configure(FeatureConfiguration configuration) {
        super.configure(configuration);

        if (configuration instanceof ANSURConfiguration) {
            ANSURConfiguration ansurConfiguration = (ANSURConfiguration) configuration;
            List<Constraint> constraints = ansurConfiguration.getConstraints();
            List<Filter> filters = FilterFactory.from(constraints);

            MersenneTwister rng = new MersenneTwister(configuration.getSeed());

            List<String> resourceNames = List.of("/ansur/ANSUR II MALE Public.csv",
                    "/ansur/ANSUR II FEMALE Public.csv");
            List<InputStream> streams = new ArrayList<>(resourceNames.size());
            for (String s : resourceNames) {
                InputStream is;
                try {
                    is = getClass().getModule().getResourceAsStream(s);
                } catch (IOException e) {

                    throw new SSTAFException(e);
                }
                streams.add(is);
            }
            subjectMap = SubjectSelector.select(streams, filters, rng.nextLong());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        if (isInitialized()) return;
        super.init();
    }

    /**
     * Provides the Sex of the selected subject
     *
     * @return the sex
     */
    @Override
    public Sex getSex() {
        if (sex == null) {
            String s = (String) subjectMap.get(ValueKey.GENDER);
            sex = Sex.valueOf(s.toUpperCase());
        }
        return sex;
    }

    /**
     * Provides the height (stature) of the given subject in cm.
     *
     * @return the height.
     */
    @Override
    public double getHeight_cm() {
        if (height_cm < 0) {
            Integer height_mm = (Integer) subjectMap.get(ValueKey.STATURE);
            this.height_cm = height_mm / 10.0;
        }
        return height_cm;
    }

    /**
     * Provides the span of the given subject in cm.
     *
     * @return the span.
     */
    @Override
    public double getSpan_cm() {
        if (span_cm < 0) { // not initialized
            Integer span_mm = (Integer) subjectMap.get(ValueKey.SPAN);
            this.span_cm = span_mm / 10.0;
        }
        return span_cm;
    }

    /**
     * Provides the weight if the subject in kg.
     *
     * @return the weight
     */
    @Override
    public double getWeight_kg() {
        if (weight_kg < 0) {
            Integer weight_dg = (Integer) subjectMap.get(ValueKey.WEIGHT_KG);
            this.weight_kg = weight_dg / 10.0;
        }
        return weight_kg;
    }

    /**
     * Retrieves the Handedness (LEFT or RIGHT) of the subject
     *
     * @return the Handedness
     */
    @Override
    public Handedness getHandedness() {
        if (handedness == null) {
            Object o = subjectMap.get(ValueKey.WRITING_PREFERENCE);
            if (o instanceof String) {
                String s = (String) o;
                if (s.contains("Left")) {
                    handedness = Handedness.LEFT;
                }
            } else {
                handedness = Handedness.RIGHT; //default for now
            }
        }
        return handedness;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Class<? extends HandlerContent>> contentHandled() {
        return List.of(GetValueMessage.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms, Address from,
                                    long sequence, Address respondTo) {
        System.err.println("WE'RE HERE - " + arg);
        if (arg instanceof GetValueMessage) {
            GetValueMessage msg = (GetValueMessage) arg;
            Objects.requireNonNull(msg.key);
            GetValueResponse.GetValueResponseBuilder<?,?> builder =
                    GetValueResponse.builder();
            ValueKey key = msg.key;
            builder.valueKey(key);
            if (key.getType().equals(String.class)) {
                System.err.println("String query");
                Optional<String> ov = getStringValue(key);
                ov.ifPresent(builder::stringValue);
            } else if (key.getType().equals(Double.class)) {
                System.err.println("Double query");
                Optional<Double> ov = getDoubleValue(key);
                ov.ifPresent(builder::doubleValue);
            } else if (key.getType().equals(Integer.class)) {
                System.err.println("Integer query");
                Optional<Integer> ov = getIntegerValue(key);
                ov.ifPresent(builder::intValue);
            }
            GetValueResponse response = builder.build();
            System.err.println("Response is " + response);
            Message out = buildNormalResponse(response, sequence, respondTo);
            return ProcessingResult.of(out);
        } else {
            throw new SSTAFException(arg.getClass().getName() + " is not supported");
        }
    }

    /**
     * Provides a value from the subject
     *
     * @param key the key for the value to retrieve
     * @return the value wrapped in an Optional.
     */
    @Override
    public Object getValue(ValueKey key) {
        return subjectMap.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getStringValue(ValueKey key) {
        Object val = subjectMap.get(key);
        if (val instanceof String) return Optional.of((String) val);
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Double> getDoubleValue(ValueKey key) {
        Object val = subjectMap.get(key);
        if (val instanceof Double) return Optional.of((Double) val);
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Integer> getIntegerValue(ValueKey key) {
        Object val = subjectMap.get(key);
        if (val instanceof Integer) return Optional.of((Integer) val);
        return Optional.empty();
    }
}

