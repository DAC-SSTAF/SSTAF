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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import mil.sstaf.core.features.Agent;
import mil.sstaf.core.features.Handler;

import java.util.Comparator;
import java.util.Objects;

/**
 * @author Ron Bowers
 * @version 1.0
 * @see mil.sstaf.core.entity.FeatureManager
 * @see MessageDriven
 * @since 1.0
 * <p>
 * Specifies an address that can be used to route a {@link Message}.
 * </p>
 * <p>
 * A {@code Message} in SSTAF can be routed to a specific {@link Handler} or {@link Agent} within a
 * specific {@link BaseEntity}. To accomplish this, the {@code Address} contains an {@link EntityHandle} to specify the
 * {@code Entity} and a {@code String} to specify the name of the {@code Handler}. {@code Addresses} are used to
 * refer both to the source and destination of a {@code Message}.
 * </p>
 * <p>There are two flavors of {@code Address}, <em>internal</em> and <em>external</em>. An internal {@code Address}
 * specifies only a {@code Handler} name. A {@code Message} that uses an internal {@code Address} object as
 * its destination will be retained within the source {@code Entity} and routed directly to the
 * destination {@code Handler}. Sending {@code Message}s within an {@code Entity} is the preferred way of invoking
 * an asynchronous command or query within an {@code Entity}. An external {@code Address} contains an
 * {@code EntityHandle}. A {@code Message} that uses an external {@code Address}
 * object for its destination {@code Address} will be routed to the specified {@code Entity}. If the {@code Address}
 * also contains a {@code Handler} name, the {@code Message} will be delivered to that {@code Handler}. Otherwise, the
 * {@code Message} will go to the {@code Handler} that accepts the content of the {@code Message}.
 * </p>
 */
@Builder
@EqualsAndHashCode
@JsonIgnoreProperties("sourceDir")
public final class Address {

    /**
     * A convenient instance of {@code AddressComparator}
     */
    public static final Comparator<Address> COMPARATOR = new AddressComparator();
    //
    // Special addresses
    //
    /**
     * A special {@code Address} that, if used as a destination, causes a {@code Message} to be dropped.
     */
    public static final Address NOWHERE = Address.builder().entityHandle(null).handlerName("NOWHERE").build();

    /**
     * The handle to the {@code Entity} part of the address
     */
    public final EntityHandle entityHandle;

    /**
     * The name of the handler.
     */
    public final String handlerName;


    /**
     * <p>
     * Creates an {@code Address} that can be routed to a different {@code Entity}
     * than from which it originated.
     * </p><p>
     * Within the receiving Entity, the message will be dispatched to whichever {@code Handler}
     * responds to the contents of the {@code Message}.
     * </p>
     *
     * @param entityHandle the {@code EntityHandle} for the {@code Entity}
     * @return a new {@code Address}
     */
    public static Address makeExternalAddress(final EntityHandle entityHandle) {
        Objects.requireNonNull(entityHandle, "EntityHandle was null");
        return builder().entityHandle(entityHandle).handlerName(null).build();
    }

    /**
     * Creates an {@code Address} that is used to route messages within an {@code Entity}.
     *
     * @param handlerName the {@code Handler} that should receive the {@code Message}.
     * @return a new {@code Address}
     */
    public static Address makeInternalAddress(final String handlerName) {
        Objects.requireNonNull(handlerName, "HandlerName was null");
        return builder().entityHandle(null).handlerName(handlerName).build();
    }

    /**
     * Creates a fully-specified {@code Address} that can route messages either
     * internally or externally.
     *
     * @param entityHandle the {@code EntityHandle} for the Entity
     * @param handlerName  the {@code Handler} that should receive the message.
     * @return a new Address
     */
    public static Address makeAddress(final EntityHandle entityHandle, final String handlerName) {
        Objects.requireNonNull(entityHandle, "EntityHandle was null");
        Objects.requireNonNull(handlerName, "HandlerName was null");
        return builder().entityHandle(entityHandle).handlerName(handlerName).build();
    }


    /**
     * Returns whether or not the {@code Address} is an internal-only address.
     *
     * @return true if the {@code Address} is internal
     */
    public boolean isInternal() {
        return entityHandle == null;
    }

    /**
     * Returns whether or not the {@code Address} is an external-capable address.
     * Note that an {@code Entity} can use an external address to talk to itself.
     *
     * @return true if the {@code Address} is an external address.
     */
    public boolean isExternal() {
        return entityHandle != null;
    }

    /**
     * Comparator for ordering Addresses
     */
    static class AddressComparator implements Comparator<Address> {
        /**
         * <p>Compares two {@code Address} objects.</p>
         *
         * <p>Internal {@code Address}es precede externals. If both have {@code EntityHandle}s those are compared.
         * If the {@code EntityHandles}s are equal, the {@code Handler} names are compared.
         */
        @Override
        public int compare(Address o1, Address o2) {
            Objects.requireNonNull(o1, "Address must not be null");
            Objects.requireNonNull(o2, "Address must not be null");

            if (o1 == o2) {
                return 0;
            } else if (o1.isInternal() && o2.isExternal()) {
                return -1;
            } else if (o1.isExternal() && o2.isInternal()) {
                return 1;
            } else {
                int c = EntityHandle.comparator.compare(o1.entityHandle, o2.entityHandle);
                if (c == 0) {
                    String h1 = o1.handlerName;
                    String h2 = o2.handlerName;
                    if (h1 == null && h2 == null) return 0;
                    if (h2 == null) return -1;
                    if (h1 == null) return 1;
                    return Integer.compare(h1.compareTo(h2), 0);
                } else {
                    return c;
                }
            }
        }
    }
}

