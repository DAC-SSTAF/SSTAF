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

import mil.sstaf.core.features.ExceptionCommand;
import mil.sstaf.core.features.StringContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageQueueComparatorTest {
    @Test
    void errorResponsesOrderedCorrectly() {
        MessageQueueComparator comp = new MessageQueueComparator();
        Entity testEntity1 = TestEntity.builder().build();
        Entity testEntity2 = TestEntity.builder().build();

        testEntity1.setForce(Force.BLUE);
        testEntity2.setForce(Force.BLUE);

        var b1 = ErrorResponse.builder()
                .content(ExceptionCommand.builder().thrown(new Throwable()).build())
                .source(Address.makeExternalAddress(testEntity1.getHandle()))
                .destination(Address.makeExternalAddress(testEntity2.getHandle()));
        var err1 = b1.build();

        var b2 = ErrorResponse.builder()
                .source(Address.makeExternalAddress(testEntity2.getHandle()))
                .destination(Address.makeExternalAddress(testEntity1.getHandle()))
                .content(ExceptionCommand.builder().thrown(new Throwable()).build());
        var err2 = b2.build();

        assertEquals(-1, comp.compare(err1, err2));
    }

    @Test
    @SuppressWarnings(value="all")
    void entityEventsOrderedCorrectly() {
        MessageQueueComparator comp = new MessageQueueComparator();
        Entity testEntity1 = TestEntity.builder().build();
        Entity testEntity2 = TestEntity.builder().build();
        testEntity1.setForce(Force.BLUE);
        testEntity2.setForce(Force.BLUE);

        var b1 = EntityEvent.builder()
                .eventTime_ms(10300)
                .source(Address.makeExternalAddress(testEntity1.getHandle()))
                .destination(Address.makeExternalAddress(testEntity2.getHandle()))
                .content(StringContent.builder().value("").build());
        EntityEvent entityEvent1 = b1.build();

        var b2 = EntityEvent.builder();
        b2.eventTime_ms(15000);
        b2.source(Address.makeExternalAddress(testEntity1.getHandle()));
        b2.destination(Address.makeExternalAddress(testEntity2.getHandle()));
        b2.content(StringContent.builder().value("").build());
        EntityEvent entityEvent2 = b2.build();

        var b3 = EntityEvent.builder();
        b3.eventTime_ms(10300);
        b3.source(Address.makeExternalAddress(testEntity2.getHandle()));
        b3.destination(Address.makeExternalAddress(testEntity1.getHandle()));
        b3.content(StringContent.builder().value("'").build());
        EntityEvent entityEvent3 = b3.build();

        assertEquals(-1, comp.compare(entityEvent1, entityEvent2));
        assertEquals(-1, comp.compare(entityEvent1, entityEvent3));
        assertEquals(0, comp.compare(entityEvent1, entityEvent1));
        assertEquals(1, comp.compare(entityEvent2, entityEvent1));
        assertEquals(1, comp.compare(entityEvent3, entityEvent1));

    }

    @Test
    void eventsAndMessagesOrderedCorrectly() {
        MessageQueueComparator comp = new MessageQueueComparator();
        Entity testEntity1 = TestEntity.builder().build();
        Entity testEntity2 = TestEntity.builder().build();
        testEntity1.setForce(Force.BLUE);
        testEntity2.setForce(Force.BLUE);

        var b1 = ErrorResponse.builder()
                .content(ExceptionCommand.builder().thrown(new Throwable()).build())
                .source(Address.makeExternalAddress(testEntity1.getHandle()))
                .destination(Address.makeExternalAddress(testEntity2.getHandle()));
        ErrorResponse err1 = b1.build();

        var b2 = ErrorResponse.builder()
                .content(ExceptionCommand.builder().thrown(new Throwable()).build())
                .source(Address.makeExternalAddress(testEntity2.getHandle()))
                .destination(Address.makeExternalAddress(testEntity1.getHandle()));
        ErrorResponse err2 = b2.build();

        var b3 = EntityEvent.builder()
                .eventTime_ms(0)
                .source(Address.makeExternalAddress(testEntity1.getHandle()))
                .destination(Address.makeExternalAddress(testEntity2.getHandle()))
                .content(StringContent.builder().value("").build());
        EntityEvent entityEvent1 = b3.build();

        var b4 = EntityEvent.builder()
                .eventTime_ms(5000)
                .source(Address.makeExternalAddress(testEntity1.getHandle()))
                .destination(Address.makeExternalAddress(testEntity2.getHandle()))
                .content(StringContent.builder().value("").build());
        EntityEvent entityEvent2 = b4.build();

        var b5 = EntityEvent.builder()
                .eventTime_ms(10300)
                .source(Address.makeExternalAddress(testEntity2.getHandle()))
                .destination(Address.makeExternalAddress(testEntity1.getHandle()))
                .content(StringContent.builder().value("").build());
        EntityEvent entityEvent3 = b5.build();

        assertEquals(1, comp.compare(entityEvent1, err1));
        assertEquals(1, comp.compare(entityEvent2, err1));
        assertEquals(1, comp.compare(entityEvent3, err1));
        assertEquals(1, comp.compare(entityEvent1, err2));
        assertEquals(1, comp.compare(entityEvent2, err2));
        assertEquals(1, comp.compare(entityEvent3, err2));
        assertEquals(-1, comp.compare(err1, entityEvent1));
        assertEquals(-1, comp.compare(err1, entityEvent2));
        assertEquals(-1, comp.compare(err1, entityEvent3));
        assertEquals(-1, comp.compare(err2, entityEvent1));
        assertEquals(-1, comp.compare(err2, entityEvent2));
        assertEquals(-1, comp.compare(err2, entityEvent3));
        assertEquals(-1, comp.compare(err2, entityEvent3));
    }
}

