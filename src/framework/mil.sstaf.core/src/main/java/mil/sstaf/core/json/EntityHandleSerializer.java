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
