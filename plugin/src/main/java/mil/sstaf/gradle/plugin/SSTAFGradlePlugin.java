package mil.sstaf.gradle.plugin;

import mil.sstaf.gradle.plugin.javamodules.ExtraModuleInfoTransform;
import mil.sstaf.gradle.plugin.resourcemgmt.MessageDigestTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.jvm.internal.JvmEcosystemUtilities;

import javax.inject.Inject;

/**
 * Entry point of our plugin that should be applied in the root project.
 */
abstract public class SSTAFGradlePlugin implements Plugin<Project> {

    JvmEcosystemUtilities utilities;

    @Inject
    public SSTAFGradlePlugin(JvmEcosystemUtilities jvmEcosystemUtilities) {
        this.utilities = jvmEcosystemUtilities;
    }


    @Override
    public void apply(Project project) {
        //
        // Register the plugin extension as 'sstaf {}' configuration block
        //
        SSTAFPluginExtension extension = project.getObjects().newInstance(SSTAFPluginExtension.class);
        project.getExtensions().add(SSTAFPluginExtension.class, "sstaf", extension);

        //
        // Register tasks
        //
        project.getTasks().register("hashResources", MessageDigestTask.class, task -> {
            task.getHashAlgorithm().set(extension.getHashAlgorithm().getOrElse("MD5"));
        });

        // setup the transform for all projects in the build
        project.getPlugins().withType(JavaPlugin.class).configureEach(javaPlugin -> {
            configureTransform(project, extension);
        });

        //IntegrationTestSetup.apply(project);
    }

    private void configureTransform(Project project, SSTAFPluginExtension extension) {
        Attribute<String> artifactType = Attribute.of("artifactType", String.class);
        Attribute<Boolean> javaModule = Attribute.of("javaModule", Boolean.class);

        // compile and runtime classpath express that they only accept modules by requesting the javaModule=true attribute
        project.getConfigurations().matching(this::isResolvingJavaPluginConfiguration).all(
                c -> c.getAttributes().attribute(javaModule, true));

        // all Jars have a javaModule=false attribute by default; the transform also recognizes modules and returns them without modification
        project.getDependencies().getArtifactTypes().getByName("jar").getAttributes().attribute(javaModule, false);

        // register the transform for Jars and "javaModule=false -> javaModule=true"; the plugin extension object fills the input parameter
        project.getDependencies().registerTransform(ExtraModuleInfoTransform.class, t -> {
            t.parameters(p -> {
                p.setModuleInfo(extension.getModuleInfo());
                p.setAutomaticModules(extension.getAutomaticModules());
            });
            t.getFrom().attribute(artifactType, "jar").attribute(javaModule, false);
            t.getTo().attribute(artifactType, "jar").attribute(javaModule, true);
        });
    }

    private boolean isResolvingJavaPluginConfiguration(Configuration configuration) {
        if (!configuration.isCanBeResolved()) {
            return false;
        }
        return configuration.getName().endsWith(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME.substring(1))
                || configuration.getName().endsWith(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME.substring(1))
                || configuration.getName().endsWith(JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME.substring(1));
    }
}
