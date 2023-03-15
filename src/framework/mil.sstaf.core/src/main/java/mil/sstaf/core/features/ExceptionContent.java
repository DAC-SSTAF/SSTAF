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
public class ExceptionContent extends HandlerContent {

    @Getter
    private final String errorDescription;

    @Getter
    private final Throwable thrown;

}
