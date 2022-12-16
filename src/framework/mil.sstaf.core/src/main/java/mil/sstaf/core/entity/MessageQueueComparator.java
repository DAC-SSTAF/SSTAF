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

import java.util.Comparator;

public class MessageQueueComparator implements Comparator<Message> {
    @Override
    public int compare(Message msg1, Message msg2) {
        if (msg1 instanceof EntityEvent && msg2 instanceof EntityEvent) {
            EntityEvent event1 = (EntityEvent) msg1;
            EntityEvent event2 = (EntityEvent) msg2;
            return compareEvents(event1, event2);
        } else if (msg1 instanceof EntityEvent) {
            return 1;
        } else if (msg2 instanceof EntityEvent) {
            return -1;
        } else {
            return compareMessages(msg1, msg2);
        }
    }

    private int compareEvents(EntityEvent event1, EntityEvent event2) {
        if (event1.getEventTime_ms() < event2.getEventTime_ms()) {
            return -1;
        } else if (event1.getEventTime_ms() > event2.getEventTime_ms()) {
            return 1;
        } else {
            return compareMessages(event1, event2);
        }
    }

    private int compareMessages(Message msg1, Message msg2) {
        if (msg1 == null) return 1;
        else if (msg2 == null) return -1;
        else {
            Address source1 = msg1.getSource();
            Address source2 = msg2.getSource();
            if (source1 != null && source1.equals(source2)) {
                return Long.compare(msg1.getSequenceNumber(), msg2.getSequenceNumber());
            } else {
                return Address.COMPARATOR.compare(source1, source2);
            }
        }
    }

}

