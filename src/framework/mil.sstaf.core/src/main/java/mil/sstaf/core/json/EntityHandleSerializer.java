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

package mil.sstaf.core.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import mil.sstaf.core.entity.EntityHandle;

import java.io.IOException;

/**
 * Jackson serializer for {@code EntityHandle}s
 */
public class EntityHandleSerializer extends StdSerializer<EntityHandle> {
    public EntityHandleSerializer() {
        super(EntityHandle.class);
    }

    protected EntityHandleSerializer(Class<EntityHandle> t) {
        super(t);
    }

    protected EntityHandleSerializer(JavaType type) {
        super(type);
    }

    protected EntityHandleSerializer(Class<?> t, boolean dummy) {
        super(t, dummy);
    }

    protected EntityHandleSerializer(StdSerializer<?> src) {
        super(src);
    }

    @Override
    public void serialize(EntityHandle value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        String path = value.getPath();
        gen.writeString(path);
    }
}

