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

package mil.sstaf.session.control;

import mil.sstaf.core.entity.EntityHandle;
import mil.sstaf.session.messages.BaseSessionCommand;
import mil.sstaf.session.messages.Command;
import mil.sstaf.session.messages.Event;
import mil.sstaf.session.messages.SessionTickResult;

import java.util.Objects;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Client interface to the Soldier and Squad Trade-space Analysis Framework
 * <p>
 * The Session class encapsulates the entire SSTAF system behind a simple message-based interface. The
 * intent of Session is to enable the capability of SSTAF to be enhanced without client applications
 * needing to change. It is expected that the Session class will remain very stable.
 */

public final class Session implements AutoCloseable {

    private final boolean asynch;
    private final EntityController entityController;
    private final ExecutorService executorService;

    private long currentTime_ms;

    private Session(SessionConfiguration sessionConfiguration, EntityController entityController) {
        Objects.requireNonNull(sessionConfiguration, "sessionConfiguration");
        Objects.requireNonNull(entityController, "entityController");
        this.entityController = entityController;
        this.asynch = sessionConfiguration.isAsync();
        this.executorService = this.asynch ? Executors.newSingleThreadExecutor() : null;
    }

    public static Session of(SessionConfiguration sessionConfiguration, EntityController entityConfig) {
        return new Session(sessionConfiguration, entityConfig);
    }

    /**
     * Submits a BaseSessionCommand or SSTAFEvent to the environment.
     * <p>
     * When submitted, the BaseSessionCommand or SSTAFEvent is routed to the Entity specified
     * by the EntityHandle within the commend.
     *
     * @param command The command to process.
     */
    public void submit(final BaseSessionCommand command) {
        if (command instanceof Event) {
            Event event = (Event) command;
            entityController.submitEvent(event);
        } else {
            entityController.submitCommand((Command) command);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
        if (entityController != null) {
            entityController.shutdown();
        }
        if (executorService != null) {
            this.executorService.shutdown();
        }
    }

    /**
     * Advances the simulation to the specified time and processes all pending commands
     * and events.
     * <p>
     * Tick() advances the SSTAF simulation clock and triggers all Entities within the
     * environment to tick forward to the same time. When each Entity ticks, it wll
     * process all enqueued commands and any events that are scheduled up to the
     * provided currentTime value. The results for all of the Entities are presented back
     * to the caller as a List of SSTAFResults.
     *
     * @param currentTime_ms the new current simulation time.
     * @return a {@code SessionTickResult} that contains the next event time and outbound messages.
     */
    public SessionTickResult tick(final long currentTime_ms) {
        if (asynch) {
            throw new IllegalStateException("Session was configured for asynchronous use");
        }
        return entityController.tick(currentTime_ms);
    }

    /**
     * Executes a tick without advancing the simulation clock.
     * <p>
     * TickAgain() forces all {@code Entity} instances to process pending
     * messages using the last simulation time. This method is primarily
     * intended to process state queries.
     *
     * @return a {@code SessionTickResult} that contains the next event time and outbound messages.
     */
    public SessionTickResult tickAgain() {
        if (asynch) {
            throw new IllegalStateException("Session was configured for asynchronous use");
        }
        return entityController.tickAgain();
    }


    /**
     * Processes a tick asynchronously using an ExecutorService.
     *
     * @param currentTime_ms the new current simulation time.
     * @return A Future for the results of the tick();
     */
    @SuppressWarnings("unused")
    public Future<SessionTickResult> asyncTick(final long currentTime_ms) {
        if (!asynch) {
            throw new IllegalStateException("Attempted to use asyncTick() when Session was not configured for asynchronous use");
        }
        return executorService.submit(new TickCallable(currentTime_ms));
    }

    /**
     * Produces a {@code SessionTickResult} that includes the current simulation time
     * but no {@code BaseSessionResult}s.
     *
     * @return an empty {@code SessionTickResult}
     */
    public SessionTickResult getEmptyTickResult() {
        return SessionTickResult.builder().nextEventTime_ms(currentTime_ms).build();
    }


    /**
     * Provides a SortedSet of EntityHandles for all of the Entities in
     * the environment
     *
     * @return a SortedSet of EntityHandles
     */
    public SortedSet<EntityHandle> getEntities() {
        return entityController.getSimulationEntityHandles();
    }

    public EntityController getEntityController() {
        return entityController;
    }

    /**
     * Callable for executing a tick (duh!)
     */
    private class TickCallable implements Callable<SessionTickResult> {

        public TickCallable(long currentTime_ms) {
            Session.this.currentTime_ms = currentTime_ms;
        }

        @Override
        public SessionTickResult call() {
            return tick(currentTime_ms);
        }


    }
}

