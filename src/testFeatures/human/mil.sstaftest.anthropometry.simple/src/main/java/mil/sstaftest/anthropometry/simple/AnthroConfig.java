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

package mil.sstaftest.anthropometry.simple;

import mil.sstaf.core.features.BaseFeature;
import mil.sstaf.core.features.FeatureConfiguration;
import mil.sstaf.core.util.SSTAFException;
import mil.sstaftest.anthropometry.api.AnthroConfiguration;
import mil.sstaftest.anthropometry.api.Anthropometry;
import mil.sstaftest.anthropometry.api.Handedness;
import mil.sstaftest.anthropometry.api.Sex;

/**
 * Provides anthropometric configuration values
 */
public class AnthroConfig extends BaseFeature implements Anthropometry {
    public static final String FEATURE_NAME = "Simple Anthropometry";
    public static final int MAJOR_VERSION = 0;
    public static final int MINOR_VERSION = 1;
    public static final int PATCH_VERSION = 0;

    static final Sex DEFAULT_SEX = Sex.MALE;
    static final int DEFAULT_AGE = 20;
    static final double DEFAULT_HEIGHT_CM = 175;
    static final double DEFAULT_SPAN_CM = 175;
    static final double DEFAULT_WEIGHT_KG = 90;

    private Sex sex;
    private Handedness handedness;
    private int age;
    private double height_cm;
    private double span_cm;
    private double weight_kg;


    public AnthroConfig() {
        super(FEATURE_NAME, MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION,
                false, "A simple representation of anthropometry");
        sex = DEFAULT_SEX;
        age = DEFAULT_AGE;
        height_cm = DEFAULT_HEIGHT_CM;
        span_cm = DEFAULT_SPAN_CM;
        weight_kg = DEFAULT_WEIGHT_KG;
        handedness = Handedness.RIGHT;
    }

    @Override
    public Sex getSex() {
        return sex;
    }

    void setSex(Sex sex) {
        this.sex = sex;
    }

    public Handedness getHandedness() {
        return handedness;
    }

    void setHandedness(Handedness handedness) {
        this.handedness = handedness;
    }

    @Override
    public int getAge() {
        return age;
    }

    void setAge(int age) {
        this.age = age;
    }

    @Override
    public double getHeight_cm() {
        return height_cm;
    }

    void setHeight_cm(double height_cm) {
        this.height_cm = height_cm;
    }

    @Override
    public double getSpan_cm() {
        return span_cm;
    }

    void setSpan_cm(double span_cm) {
        this.span_cm = span_cm;
    }

    @Override
    public double getWeight_kg() {
        return weight_kg;
    }

    void setWeight_kg(double weight_kg) {
        this.weight_kg = weight_kg;
    }

    @Override
    public String toString() {
        return "AnthroConfig{" +
                "sex=" + sex +
                ", age=" + age +
                ", height_cm=" + height_cm +
                ", span_cm=" + span_cm +
                ", weight_kg=" + weight_kg +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnthroConfig that = (AnthroConfig) o;
        return age == that.age &&
                Double.compare(that.height_cm, height_cm) == 0 &&
                Double.compare(that.span_cm, span_cm) == 0 &&
                Double.compare(that.weight_kg, weight_kg) == 0 &&
                sex == that.sex;
    }

    @Override
    public void init() throws SSTAFException {
        super.init();
    }

    @Override
    public void configure(FeatureConfiguration configuration) {
        super.configure(configuration);
        if (configuration instanceof AnthroConfiguration) {
            AnthroConfiguration ac = (AnthroConfiguration) configuration;
            setHandedness(ac.getHandedness());
            setSex(ac.getSex());
            setSpan_cm(ac.getSpan());
            setHeight_cm(ac.getHeight());
            setWeight_kg(ac.getWeight());
            setAge(ac.getAge());
        }
    }
}

