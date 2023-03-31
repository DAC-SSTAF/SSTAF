package mil.sstaf.core.features;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.Random;

/**
 * Base class for all configurations that can be applied to a {@code Feature.}
 *
 * The original approach was to pass in a {@code JSONObject} and a seed.
 * Converting to Lombok and Jackson made it easier to move deserialization into
 * the framework and provide the {@code Feature} with a real object.
 *
 * {@code Features}s are still responsible for validating their configuration
 * values.
 */
@Jacksonized
@SuperBuilder(toBuilder = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public class FeatureConfiguration {
    @Getter
    @Setter
    private long seed;

    public FeatureConfiguration() {
        this.seed = new Random(System.currentTimeMillis() ^ System.nanoTime()).nextLong();
    }
}
