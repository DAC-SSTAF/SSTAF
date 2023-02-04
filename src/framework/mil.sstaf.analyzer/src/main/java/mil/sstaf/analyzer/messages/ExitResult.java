package mil.sstaf.analyzer.messages;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
@EqualsAndHashCode(callSuper = true)
public class ExitResult extends BaseAnalyzerResult{
    long exitTime;
}
