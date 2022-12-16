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

package mil.sstaf.physiology.agent;

import mil.sstaf.blackboard.api.Blackboard;
import mil.sstaf.core.entity.Address;
import mil.sstaf.core.features.BaseAgent;
import mil.sstaf.core.features.HandlerContent;
import mil.sstaf.core.features.ProcessingResult;
import mil.sstaf.core.features.Requires;
import mil.sstaf.physiology.api.PhysiologyManagement;
import mil.sstaf.physiology.api.PhysiologyState;
import mil.sstaf.physiology.models.api.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

public class PhysiologyAgent extends BaseAgent implements PhysiologyManagement {
    public static final String FEATURE_NAME = "Physiology Agent";
    public static final int MAJOR_VERSION = 0;
    public static final int MINOR_VERSION = 1;
    public static final int PATCH_VERSION = 0;

    private static final Logger logger = LoggerFactory.getLogger(PhysiologyAgent.class);

    @Requires
    Blackboard blackboard;

    @Requires()
    CognitionModel brainModel;
    @Requires()
    CardiovascularModel heartModel;
    @Requires()
    RespirationModel respirationModel;
    @Requires()
    EnergyModel energyModel;
    @Requires()
    HydrationModel hydrationModel;
    @Requires()
    MusculatureModel musculatureModel;
    @Requires()
    VisionModel visionModel;

    public PhysiologyAgent() {
        super(FEATURE_NAME, MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION,
                false, "Unified physiology");
    }

    @Override
    public void init() {
        super.init();
        logger.trace("Initializing PhysiologyAgent");
        Objects.requireNonNull(blackboard);
        Objects.requireNonNull(brainModel);
        Objects.requireNonNull(heartModel);
        Objects.requireNonNull(respirationModel);
        Objects.requireNonNull(hydrationModel);
        Objects.requireNonNull(musculatureModel);
        Objects.requireNonNull(energyModel);
        Objects.requireNonNull(visionModel);
    }

    @Override
    public PhysiologyState getState(long time_ms) {
        PhysiologyState.PhysiologyStateBuilder<?,?> builder = PhysiologyState.builder();
        builder.owner(ownerHandle);
        builder.timestamp_ms(time_ms);
        builder.cardiovascularMetrics(heartModel.evaluate(time_ms));
        builder.cognitionMetrics(brainModel.evaluate(time_ms));
        builder.energyMetrics(energyModel.evaluate(time_ms));
        builder.hydrationMetrics(hydrationModel.evaluate(time_ms));
        builder.musculatureMetrics(musculatureModel.evaluate(time_ms));
        builder.respirationMetrics(respirationModel.evaluate(time_ms));
        builder.visionMetrics(visionModel.evaluate(time_ms));
        return builder.build();
    }

    /**
     * Activate the Agent to perform a function at the specified time.
     *
     * @param currentTime_ms the simulation time.
     * @return a {@code ProcessingResult} containing internal and external {@code Message}s
     */
    @Override
    public ProcessingResult tick(long currentTime_ms) {
        PhysiologyState physiologyState = getState(currentTime_ms);
        return ProcessingResult.empty();
    }

    /**
     * Provides a {@code List} of all of the message content {@code Class}es to which this
     * {@code Handle} responds.
     * <p>
     * Handlers can respond to multiple message classes. Only one Handler may be associated with
     * a message class.
     *
     * @return a {@code List} of {@code Class} objects.
     */
    @Override
    public List<Class<? extends HandlerContent>> contentHandled() {
        return List.of();
    }

    /**
     * Performs the processing associated with the given argument at the specified time.
     * <p>
     * Both the scheduled time and current simulation time are reported. For an {@code EntityEvent}
     * the scheduled time value will be less than or equal the current simulation time. For an
     * {@code EntityAction} scheduled time will be the same as the current time. Providing both times
     * enables the Handler to adjust for any time difference between when an event was intended to occur
     * and when it was processed.
     *
     * @param arg              the message content
     * @param scheduledTime_ms the time for which an event was scheduled.
     * @param currentTime_ms   the current simulation time
     * @param from             the sources of the {@code Message}
     * @param id               the message sequence numner
     * @param respondTo        to where to send the results.
     * @return a {@code ProcessingResult} that contains the results of the processing
     */
    @Override
    public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms, Address from, long id, Address respondTo) {
        return buildUnsupportedMessageResponse(arg,id, getAddress(), new Throwable());
    }

}

