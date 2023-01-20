package mil.sstaf.pyagent.messages;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.features.HandlerContent;

import java.util.List;

@SuperBuilder
@Jacksonized
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
@EqualsAndHashCode(callSuper = true)
public class CountLettersRequest extends HandlerContent {

    @Getter
    @Singular
    List<String> args;
}
