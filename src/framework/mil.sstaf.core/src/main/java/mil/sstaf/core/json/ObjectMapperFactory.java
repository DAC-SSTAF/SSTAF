package mil.sstaf.core.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;

/**
 * Used to configure JsonContext and JsonReferenceProcessor
 */
public interface ObjectMapperFactory {

    /**
     * Creates configured {@link ObjectMapper}
     *
     * @param path the {@link Path}
     * @return configured {@link ObjectMapper}
     */
    public ObjectMapper create(Path path);

}

