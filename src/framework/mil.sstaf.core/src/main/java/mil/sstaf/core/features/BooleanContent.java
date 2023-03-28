package mil.sstaf.core.features;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@SuperBuilder
@Jacksonized
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
@EqualsAndHashCode(callSuper = true)
public class BooleanContent extends HandlerContent {
    @Getter
    private final Boolean value;

    public static BooleanContent of(Boolean v) {
        return builder().value(v).build();
    }
}
