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

import mil.sstaf.core.features.*;
import mil.sstaf.core.util.Injector;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    private static final String ERROR_MSG = "I MEANT TO DO THAT!";

    @Test
    void canCreateEntity() {
        Entity e = TestEntity.builder().build();
        assertNotNull(e);
    }

    @Test
    void entityCanAcceptMessages() {
        BaseEntity e = TestEntity.builder().build();
        e.init();
        assertNotNull(e);
        var b = EntityAction.builder();
        b.content(StringContent.of("Banana"));
        b.source(Address.makeExternalAddress(e.getHandle()));
        b.destination(Address.makeExternalAddress(e.getHandle()));
        EntityAction em = b.build();
        e.receive(em);
        assertEquals(1, e.getInboundQueueDepth());
    }

    @Test
    void entityCanAcceptEvents() {
        BaseEntity e = TestEntity.builder().build();
        e.init();
        assertNotNull(e);
        var b = EntityEvent.builder();
        b.content(StringContent.of("Banana"));
        b.eventTime_ms(10000);
        b.source(Address.makeExternalAddress(e.getHandle()));
        b.destination(Address.makeExternalAddress(e.getHandle()));
        EntityEvent em = b.build();
        e.receive(em);
        assertEquals(1, e.getInboundQueueDepth());
    }

    @Test
    void processEventsProcessesAllMessageAndOnlyTimelyEvents() {
        Entity from = TestEntity.builder().build();
        from.setForce(Force.BLUE);
        BaseEntity to = TestEntity.builder().build();
        to.setForce(Force.BLUE);
        assertNotNull(from);
        assertNotNull(to);

        to.init();
        from.init();

        for (int i = 1; i <= 10; ++i) {
            var b = EntityEvent.builder();
            b.content(StringContent.of("Banana-" + i));
            b.eventTime_ms(i * 1000);
            b.destination(Address.makeExternalAddress(to.getHandle()));
            b.source(Address.makeExternalAddress(from.getHandle()));
            b.respondTo(Address.makeExternalAddress(from.getHandle()));
            EntityEvent em = b.build();
            to.receive(em);
        }

        for (int i = 0; i < 3; ++i) {
            var b = EntityAction.builder();
            b.content(StringContent.of("Message" + i));
            b.destination(Address.makeExternalAddress(to.getHandle()));
            b.source(Address.makeExternalAddress(from.getHandle()));
            b.respondTo(Address.makeExternalAddress(from.getHandle()));
            EntityAction em = b.build();
            to.receive(em);
        }

        to.processMessages(1000);
        List<Message> out = to.takeOutbound();

        assertEquals(4, out.size());

        to.processMessages(2000);
        out = to.takeOutbound();
        assertEquals(1, out.size());

        to.processMessages(20000);
        out = to.takeOutbound();
        assertEquals(8, out.size());
    }

    @Test
    void registeringAProcessingHandlerWorks() {

        Handler ps =
                new BaseHandler("Thing", 0, 0, 0, false,
                        "it") {

                    @Override
                    public List<Class<? extends HandlerContent>> contentHandled() {
                        return List.of(StringContent.class);
                    }

                    @Override
                    public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms, Address from, long id, Address respondTo) {
                        if (arg instanceof StringContent) {
                            String string = ((StringContent) arg).getMessage();
                            Message out = this.buildNormalResponse(StringContent.of(string), id, respondTo);
                            return ProcessingResult.of(out);
                        }
                        return ProcessingResult.empty();
                    }
                };

        Entity from = TestEntity.builder().build();
        from.setForce(Force.BLUE);
        BaseEntity to = TestEntity.builder().build();
        to.setForce(Force.BLUE);
        assertNotNull(from);
        assertNotNull(to);

        Injector.inject(ps, from.getHandle());

        to.getFeatureManager().register(ps);
        to.init();
        from.init();

        var b = EntityEvent.builder();
        b.content(StringContent.of("Banana-1"));
        b.eventTime_ms(10300);
        b.destination(Address.makeExternalAddress(to.getHandle()));
        b.source(Address.makeExternalAddress(from.getHandle()));
        b.respondTo(Address.makeExternalAddress(from.getHandle()));
        EntityEvent em = b.build();
        to.receive(em);

        to.processMessages(11000);

        List<Message> out = to.takeOutbound();

        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof MessageResponse);

        MessageResponse mr = (MessageResponse) out.get(0);
        assertEquals(StringContent.of("Banana-1"), mr.getContent());
        assertEquals(em.getSequenceNumber(), mr.getMessageID());
    }

    @Test
    void ifAHandlerThrowsAnErrorIsProduced() {

        Handler ps =
                new BaseHandler("Banana", 0, 0, 0, false,
                        "") {

                    @Override
                    public List<Class<? extends HandlerContent>> contentHandled() {
                        return List.of(StringContent.class);
                    }

                    @Override
                    public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms,
                                                    Address from, long id, Address respondTo) {
                        throw new RuntimeException(ERROR_MSG);
                    }
                };

        Entity from = TestEntity.builder().build();
        from.setForce(Force.BLUE);
        BaseEntity to = TestEntity.builder().build();
        to.setForce(Force.BLUE);
        assertNotNull(from);
        assertNotNull(to);
        Injector.inject(ps, from.getHandle());

        to.getFeatureManager().register(ps);

        to.init();
        from.init();

        var b = EntityEvent.builder();
        b.content(StringContent.of("Banana-1"));
        b.eventTime_ms(10300);
        b.destination(Address.makeExternalAddress(to.getHandle()));
        b.source(Address.makeExternalAddress(from.getHandle()));
        b.respondTo(Address.makeExternalAddress(from.getHandle()));
        EntityEvent em = b.build();
        to.receive(em);

        to.processMessages(11000);

        List<Message> out = to.takeOutbound();

        assertEquals(1, out.size());
        assertTrue(out.get(0) instanceof ErrorResponse);

        ErrorResponse er = (ErrorResponse) out.get(0);
        assertNotNull(er);
        assertNotNull(er.getContent());
        assertEquals(ExceptionCommand.class, er.getContent().getClass());
        ExceptionCommand exceptionContent = (ExceptionCommand) er.content;
        assertNotNull(exceptionContent.getThrown());
        assertEquals(ERROR_MSG, exceptionContent.getThrown().getMessage());
        System.out.println(er.getErrorDescription());
        assertTrue(er.getErrorDescription().contains("Error at time 11000 ms, processing"));
    }

}

