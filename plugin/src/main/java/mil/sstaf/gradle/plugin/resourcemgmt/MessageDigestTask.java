package mil.sstaf.gradle.plugin.resourcemgmt;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

/**
 * Task that generates {@code MessageDigest} reports for resources
 */
public abstract class MessageDigestTask extends DefaultTask {

    private final FileCollection sourceFiles;

    /**
     * Constructor
     */
    public MessageDigestTask() {
        super();
        setGroup("other");
        dependsOn("processResources");

        File bd = getProject().getBuildDir();
        String resourcesPath = bd.getAbsolutePath() + File.separator + "resources" + File.separator + "main";
        File resourcesDir = new File(resourcesPath);

        sourceFiles = this.getProject().fileTree(resourcesPath);

        doFirst(task -> {
            if (resourcesDir.exists()) {
                String ha = getHashAlgorithm().getOrElse("MD5");
                try {
                    Path basePath = Path.of(resourcesPath);
                    Files.walkFileTree(basePath, new MessageDigestVisitor(basePath, ha));
                } catch (NoSuchAlgorithmException | IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Input
    abstract public Property<String> getHashAlgorithm();

    @SkipWhenEmpty
    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public FileCollection getSourceFiles() {
        return this.sourceFiles;
    }

}
