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

package mil.devcom_sc.ansur.messages;

import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.util.SSTAFException;

/**
 * Enumeration of all of the fields in the ANSUR II database.
 * <p>
 * Note that the names of the keys do not match the headers in the CSV files. Most of the headers are all lower-case
 * and difficult to read, so each key includes a separate headerLabel value.
 */
public enum ValueKey {
    SUBJECT_ID("subjectid",
            Integer.class,
            "A unique number for each participant measured in the anthropometric survey, ranging from 10027 to 920103, not inclusive"),
    //
    // Note the inconsistent use of Subject/Subjects (implied "'s"). This is necessary
    // to be consistent with the data files.
    //
    SUBJECTS_BIRTH_LOCATION("SubjectsBirthLocation",
            String.class,
            "Subject's Birth Location; a U.S. state or foreign country"),
    SUBJECT_NUMERIC_RACE("SubjectNumericRace",
            Integer.class,
            "Subject Numeric Race; a single or multi-digit code indicating a "
                    + "subject’s self-reported race or races (verified through interview). Where 1 = White, "
                    + "2 = Black, 3 = Hispanic, 4 = Asian, 5 = Native American, 6 = Pacific Islander, 8 = Other"),
    ETHNICITY("Ethnicity",
            String.class,
            "self-reported ethnicity (verified through interview); e.g. 'Mexican', 'Vietnamese'"),
    DOD_RACE("DODRace",
            Integer.class,
            "Department of Defense Race; a single digit indicating a subject’s self-reported preferred single race where "
                    + "selecting multiple races is not an option. This variable is intended to be comparable to the "
                    + "Defense Manpower Data Center demographic data. Where 1 = White, 2 = Black, 3 = Hispanic, 4 = Asian,"
                    + "5 = Native American, 6 = Pacific Islander, 8 = Other"),
    GENDER("Gender",
            String.class,
            "'Male' or 'Female'"),
    AGE("Age",
            Integer.class,
            "Participant’s age in years"),
    HEIGHT_IN("Heightin",
            Integer.class,
            "Height in Inches; self-reported, comparable to measured 'stature'"),
    WEIGHT_LBS("Weightlbs",
            Integer.class,
            "Weight in Pounds; self-reported, comparable to measured 'weightkg'"),
    WRITING_PREFERENCE("WritingPreference",
            String.class,
            "Writing Preference; 'Right hand', 'Left hand', or " +
                    "'Either hand (No preference)'"),
    DATE("Date",
            String.class,
            "Date the participant was measured, ranging from '04-Oct-10' to '05-Apr-12'"),
    INSTALLATION("Installation",
            String.class,
            " U.S. Army installation where the measurement occurred, e.g. 'Fort Hood', 'Camp Shelby'"),
    COMPONENT("Component",
            String.class,
            "'Army National Guard','Army Reserve', or 'Regular Army'"),
    BRANCH("Branch",
            String.class,
            "'Combat Arms', 'Combat Support', or 'Combat Service Support'"),
    PRIMARY_MOS("PrimaryMOS",
            String.class,
            "Primary Military Occupational Specialty"),
    ABDOMINAL_EXTENSION_DEPTH_SITTING("abdominalextensiondepthsitting",
            Integer.class,
            "Abdominal Extension Depth, Sitting"),
    ACROMIAL_HEIGHT("acromialheight",
            Integer.class,
            "Acromial Height"),
    ACROMION_RADIALE_LENGTH("acromionradialelength",
            Integer.class,
            "Acromion-Radiale Length"),
    ANKLE_CIRCUMFERENCE("anklecircumference",
            Integer.class,
            "Ankle Circumference"),
    AXILLA_HEIGHT("axillaheight",
            Integer.class,
            "Axilla Height"),
    BALL_OF_FOOT_CIRCUMFERENCE("balloffootcircumference",
            Integer.class,
            "Ball of Foot Circumference"),
    BALL_OF_FOOT_LENGTH("balloffootlength",
            Integer.class,
            "Ball of Foot Length"),
    BIACROMIAL_BREADTH("biacromialbreadth",
            Integer.class,
            "Biacromial Breadth"),
    BICEPS_CIRCUMFERENCE_FLEXED("bicepscircumferenceflexed",
            Integer.class,
            "Biceps Circumference, Flexed"),
    BICRISTAL_BREADTH("bicristalbreadth",
            Integer.class,
            "Bicristal Breadth"),
    BIDELTOID_BREADTH("bideltoidbreadth",
            Integer.class,
            "Bideltoid Breadth"),
    BIMALLEOLAR_BREADTH("bimalleolarbreadth",
            Integer.class,
            "Bimalleolar Breadth"),
    BITRAGION_CHIN_ARC("bitragionchinarc",
            Integer.class,
            "Bitragion Chin Arc"),
    BITRAGION_SUBMANDIBULAR_ARC("bitragionsubmandibulararc",
            Integer.class,
            "Bitragion Submandibular Arc"),
    BIZYGOMATIC_BREADTH("bizygomaticbreadth",
            Integer.class,
            "Bizygomatic Breadth"),
    BUTTOCK_CIRCUMFERENCE("buttockcircumference",
            Integer.class,
            "Buttock Circumference"),
    BUTTOCK_DEPTH("buttockdepth",
            Integer.class,
            "Buttock Depth"),
    BUTTOCK_HEIGHT("buttockheight",
            Integer.class,
            "Buttock Height"),
    BUTTOCK_KNEE_LENGTH("buttockkneelength",
            Integer.class,
            "Buttock-Knee Length"),
    BUTTOCK_POPLITEAL_LENGTH("buttockpopliteallength",
            Integer.class,
            "Buttock-Popliteal Length"),
    CALF_CIRCUMFERENCE("calfcircumference",
            Integer.class,
            "Calf Circumference"),
    CERVICALE_HEIGHT("cervicaleheight",
            Integer.class,
            "Cervical Height"),
    CHEST_BREADTH("chestbreadth",
            Integer.class,
            "Chest Breadth"),
    CHEST_CIRCUMFERENCE("chestcircumference",
            Integer.class,
            "Chest Circumference"),
    CHEST_DEPTH("chestdepth",
            Integer.class,
            "Chest Depth"),
    CHEST_HEIGHT("chestheight",
            Integer.class,
            "Chest Height"),
    CROTCH_HEIGHT("crotchheight",
            Integer.class,
            "Crotch Height"),
    CROTCH_LENGTH_OMPHALION("crotchlengthomphalion",
            Integer.class,
            "Crotch Length (Omphalion)"),
    CROTCH_LENGTH_POSTERIOR_OMPHALION("crotchlengthposterioromphalion",
            Integer.class,
            "Crotch Length, Posterior (Omphalion)"),
    EAR_BREADTH("earbreadth",
            Integer.class,
            "Ear Breadth"),
    EAR_LENGTH("earlength",
            Integer.class,
            "Ear Length"),
    EAR_PROTRUSION("earprotrusion",
            Integer.class,
            "Ear Protrusion"),
    ELBOW_REST_HEIGHT("elbowrestheight",
            Integer.class,
            "Elbow Rest Height"),
    EYE_HEIGHT_SITTING("eyeheightsitting",
            Integer.class,
            "Eye Height, Sitting"),
    FOOT_BREADTH_HORIZONTAL("footbreadthhorizontal",
            Integer.class,
            "Foot Breadth, Horizontal"),
    FOOT_LENGTH("footlength",
            Integer.class,
            "Foot Length"),
    FOREARM_CENTER_OF_GRIP_LENGTH("forearmcenterofgriplength",
            Integer.class,
            "Forearm-Center of Grip Length"),
    FOREARM_CIRCUMFERENCE_FLEXED("forearmcircumferenceflexed",
            Integer.class,
            "Forearm Circumference, Flexed"),
    FOREARM_FOREARM_BREADTH("forearmforearmbreadth",
            Integer.class,
            "Forearm-Forearm Breadth"),
    FOREARM_HAND_LENGTH("forearmhandlength",
            Integer.class,
            "Forearm -Hand Length"),
    FUNCTIONAL_LEG_LENGTH("functionalleglength",
            Integer.class,
            "Functional Leg Length"),
    HAND_BREADTH("handbreadth",
            Integer.class,
            "Hand Breadth"),
    HAND_CIRCUMFERENCE("handcircumference",
            Integer.class,
            "Hand Circumference"),
    HAND_LENGTH("handlength",
            Integer.class,
            "Hand Length"),
    HEAD_BREADTH("headbreadth",
            Integer.class,
            "Head Breadth"),
    HEAD_CIRCUMFERENCE("headcircumference",
            Integer.class,
            "Head Circumference"),
    HEAD_LENGTH("headlength",
            Integer.class,
            "Head Length"),
    HEEL_ANKLE_CIRCUMFERENCE("heelanklecircumference",
            Integer.class,
            "Heel-Ankle Circumference"),
    HEEL_BREADTH("heelbreadth",
            Integer.class,
            "Heel Breadth"),
    HIP_BREADTH("hipbreadth",
            Integer.class,
            "Hip Breadth"),
    HIP_BREADTH_SITTING("hipbreadthsitting",
            Integer.class,
            "Hip Breadth, Sitting"),
    ILIOCRISTALE_HEIGHT("iliocristaleheight",
            Integer.class,
            "Iliocristale Height"),
    INTERPUPILLARY_BREADTH("interpupillarybreadth",
            Integer.class,
            "Interpupillary Breadth"),
    INTERSCYE_I("interscyei",
            Integer.class,
            "Interscye I"),
    INTERSCYE_II("interscyeii",
            Integer.class,
            "Interscye II"),
    KNEE_HEIGHT_MIDPATELLA("kneeheightmidpatella",
            Integer.class,
            "Knee Height, Midpatella"),
    KNEE_HEIGHT_SITTING("kneeheightsitting",
            Integer.class,
            "Knee Height, Sitting"),
    LATERAL_FEMORAL_EPICONDYLE_HEIGHT("lateralfemoralepicondyleheight",
            Integer.class,
            "Lateral Femoral Epicondyle Height"),
    LATERAL_MALLEOLUS_HEIGHT("lateralmalleolusheight",
            Integer.class,
            "Lateral Malleolus Height"),
    LOWER_THIGH_CIRCUMFERENCE("lowerthighcircumference",
            Integer.class,
            "Lower Thigh Circumference"),
    MENTON_SELLION_LENGTH("mentonsellionlength",
            Integer.class,
            "Menton-Sellion Length"),
    NECK_CIRCUMFERENCE("neckcircumference",
            Integer.class,
            "Neck Circumference"),
    NECK_CIRCUMFERENCE_BASE("neckcircumferencebase",
            Integer.class,
            "Neck Circumference, Base"),
    OVERHEAD_FINGERTIP_REACH_SITTING("overheadfingertipreachsitting",
            Integer.class,
            "Overhead Fingertip Reach, Sitting"),
    PALM_LENGTH("palmlength",
            Integer.class,
            "Palm Length"),
    POPLITEAL_HEIGHT("poplitealheight",
            Integer.class,
            "Popliteal Height"),
    RADIALE_STYLION_LENGTH("radialestylionlength",
            Integer.class,
            "Radiale-Stylion Length"),
    SHOULDER_CIRCUMFERENCE("shouldercircumference",
            Integer.class,
            "Shoulder Circumference"),
    SHOULDER_ELBOW_LENGTH("shoulderelbowlength",
            Integer.class,
            "Shoulder-Elbow Length"),
    SHOULDER_LENGTH("shoulderlength",
            Integer.class,
            "Shoulder Length"),
    SITTING_HEIGHT("sittingheight",
            Integer.class,
            "Sitting Height"),
    SLEEVE_LENGTH_SPINE_WRIST("sleevelengthspinewrist",
            Integer.class,
            "Sleeve Length: Spine-Wrist"),
    SLEEVE_OUTSEAM("sleeveoutseam",
            Integer.class,
            "Sleeve Outseam"),
    SPAN("span",
            Integer.class,
            "Span"),
    STATURE("stature",
            Integer.class,
            "Stature"),
    SUPRASTERNALE_HEIGHT("suprasternaleheight",
            Integer.class,
            "Suprasternale Height"),
    TENTH_RIB_HEIGHT("tenthribheight",
            Integer.class,
            "Tenth Rib Height"),
    THIGH_CIRCUMFERENCE("thighcircumference",
            Integer.class,
            "Thigh Circumference"),
    THIGH_CLEARANCE("thighclearance",
            Integer.class,
            "Thigh Clearance"),
    THUMBTIP_REACH("thumbtipreach",
            Integer.class,
            "Thumbtip Reach"),
    TIBIAL_HEIGHT("tibialheight",
            Integer.class,
            "Tibiale Height"),
    TRAGION_TOP_O_FHEAD("tragiontopofhead",
            Integer.class,
            "Tragion-Top of Head"),
    TROCHANTERION_HEIGHT("trochanterionheight",
            Integer.class,
            "Trochanterion Height"),
    VERTICAL_TRUNK_CIRCUMFERENCE_USA("verticaltrunkcircumferenceusa",
            Integer.class,
            "Vertical Trunk Circumference (USA)"),
    WAISTBACK_LENGTH("waistbacklength",
            Integer.class,
            "Waist Back Length (Omphalion)"),
    WAIST_BREADTH("waistbreadth",
            Integer.class,
            "Waist Breadth"),
    WAIST_CIRCUMFERENCE("waistcircumference",
            Integer.class,
            "Waist Circumference (Omphalion)"),
    WAIST_DEPTH("waistdepth",
            Integer.class,
            "Waist Depth"),
    WAIST_FRONT_LENGTH_SITTING("waistfrontlengthsitting",
            Integer.class,
            "Waist Front Length, Sitting"),
    WAIST_HEIGHT_OMPHALION("waistheightomphalion",
            Integer.class,
            "Waist Height (Omphalion)"),
    WEIGHT_KG("weightkg",
            Integer.class,
            "Weight (in kg*10)"),
    WRIST_CIRCUMFERENCE("wristcircumference",
            Integer.class,
            "Wrist Circumference"),
    WRIST_HEIGHT("wristheight",
            Integer.class,
            "Wrist Height");

    private final String headerLabel;
    private final String description;
    private final Class<?> type;

    /**
     * Constructor
     *
     * @param headerLabel the name, also column header label
     * @param description the description
     */
    ValueKey(final String headerLabel, final Class<?> type, final String description) {
        this.headerLabel = headerLabel;
        this.type = type;
        this.description = description;
    }

    /**
     * Finds the ValueKey that matches the provided header name.
     *
     * @param name the name to match.
     * @return the matching ValueKey
     */
    public static ValueKey matchHeaderName(final String name) {
        for (ValueKey key : ValueKey.values()) {
            if (key.getHeaderLabel().equalsIgnoreCase(name)) {
                return key;
            }
        }
        throw new SSTAFException("Could not ANSUR ValueKey to match " + name);
    }

    /**
     * Provides the name of the measurement which is also the column label in the CSV file.
     *
     * @return the name
     */
    public String getHeaderLabel() {
        return headerLabel;
    }

    /**
     * Returns the type for this field.
     *
     * @return the type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Provides the description of the measurement.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }
}

