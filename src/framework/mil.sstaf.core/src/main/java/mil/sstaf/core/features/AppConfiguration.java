package mil.sstaf.core.features;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@SuperBuilder
@Jacksonized
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public class AppConfiguration extends FeatureConfiguration {

    @Getter
    @Singular
    private List<String> resources;

    @Getter
    @Singular
    private List<String> processArgs;

    @Getter
    private AppSupport.Mode mode;

    @Getter
    private Class<? extends Feature> resourceOwner;

}
