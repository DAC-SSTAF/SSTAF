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

public interface Entity extends MessageDriven {
    /**
     * The delimiter to use between levels in the path.
     */
    String ENTITY_PATH_DELIMITER = ":";

    void injectInFeatures(Object object);

    EntityHandle getHandle();

    /**
     * Provides the current path (organizational hierarchy) for this {@code Entity}
     *
     * @return the path
     */
    String getPath();

    String getName();

    Force getForce();

    void setForce(Force force);

    void checkInit();

    int getInboundQueueDepth();

    long runAgents(long currentTime_ms);

    void init();

    boolean canHandle(Class<?> contentClass);

    long processMessages(long currentTime_ms);

    void sendErrorResponse(long id, String message, Throwable exception,
                           Address destination);


}

