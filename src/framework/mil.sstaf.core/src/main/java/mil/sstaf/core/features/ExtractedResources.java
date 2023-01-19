package mil.sstaf.core.features;

import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 * Container for information about resources that have been extracted to the file systems.
 */
public class ExtractedResources {
    public final File tempDir;
    public final Map<String, File> resourceFiles;

    ExtractedResources(File tempDir, Map<String, File> resourceFiles) {
        this.tempDir = tempDir;
        this.resourceFiles = Collections.unmodifiableMap(resourceFiles);
    }
}
