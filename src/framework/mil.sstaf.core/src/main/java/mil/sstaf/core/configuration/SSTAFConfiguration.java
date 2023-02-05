/*
 * Copyright (c) 2022
 * United States Government as represented by the U.S. Army DEVCOM Analysis Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mil.sstaf.core.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.json.JsonLoader;
import mil.sstaf.core.module.ModuleLayerDefinition;
import mil.sstaf.core.module.ModuleLayerSupport;
import mil.sstaf.core.util.SSTAFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Contains global configuration information for the SSTAF environment.
 * <p>
 * {@code SSTAFConfiguration} provides a global environment for SSTAF, through
 * which other SSTAF components can access system-wide settings.
 * <p>
 * The motivation for {@code SSTAFConfiguration} was as to provide the ability
 * to creatie a root {@link ModuleLayer} for all SSTAF components. This root
 * layer is used to load SSTAF plugin modules and is the parent layer for the
 * {@code ModuleLayer}s that can be created for each {@code Entity}.
 * <p>
 * The location of the {@code SSTAFConfiguration} is specified through either
 * the {@code mil.sstaf.configuration} JVM property, or the
 * {@code SSTAF_CONFIGURATION} shell environment variable. The JVM property
 * takes precedence.
 */
@SuperBuilder
@Jacksonized
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public class SSTAFConfiguration {

    public static final String SSTAF_CONFIGURATION_PROPERTY = "mil.sstaf.configuration";
    public static final String SSTAF_CONFIGURATION_ENV = "SSTAF_CONFIGURATION";
    private static final Logger logger = LoggerFactory.getLogger(SSTAFConfiguration.class);
    private static final Lock lock = new ReentrantLock();
    private static final AtomicReference<SSTAFConfiguration> instance = new AtomicReference<>();
    public static boolean requireConfiguration = false;
    @Getter
    @Builder.Default
    @JsonIgnore
    ModuleLayer rootLayer = null;
    @Getter
    private ModuleLayerDefinition moduleLayerDefinition;

    /**
     * Provides the {@code SSTAFConfiguration}, loading it if necessary.
     *
     * @return the SSTAFConfiguration
     */
    public static SSTAFConfiguration getInstance() {
        lock.lock();
        SSTAFConfiguration sstafConfiguration = instance.get();
        if (sstafConfiguration == null) {
            sstafConfiguration = loadConfiguration();
        }
        lock.unlock();
        return sstafConfiguration;
    }

    private static void setInstance(SSTAFConfiguration config) {
        lock.lock();
        instance.set(config);
        lock.unlock();
    }

    private static boolean isFileValid(String configFileName) {
        if (configFileName != null && configFileName.length() > 0) {
            File file = new File(configFileName);
            return file.exists() && file.canRead();
        }
        return false;
    }

    static SSTAFConfiguration loadConfiguration() {
        String defaultFilename = System.getProperty("user.home") + File.separator +
                "SSTAFData" + File.separator + "config.json";
        String[] locations = {
                System.getProperty(SSTAF_CONFIGURATION_PROPERTY),
                System.getenv(SSTAF_CONFIGURATION_ENV),
                defaultFilename};
        SSTAFConfiguration sstafConfiguration = null;
        for (String filename : locations) {
            if (isFileValid(filename)) {
                sstafConfiguration = from(new File(filename));
                break;
            }
        }
        if (SSTAFConfiguration.requireConfiguration) {
            throw new SSTAFException(
                    String.format("Could not find a SSTAF configuration file in: %s",
                            Arrays.toString(locations)));
        } else if (sstafConfiguration == null) {
            // 2022-04-12 : RAB : Make a default empty configuration.
            sstafConfiguration = SSTAFConfiguration.builder().build();
            sstafConfiguration.init();
        }
        instance.set(sstafConfiguration);
        return sstafConfiguration;
    }

    /**
     * Clears the SSTAFConfiguration
     */
    static void reset() {
        lock.lock();
        instance.set(null);
        lock.unlock();
    }

    private static SSTAFConfiguration from(File file) {
        JsonLoader loader = new JsonLoader();
        SSTAFConfiguration config = (SSTAFConfiguration) loader.load(Path.of(file.getAbsolutePath()));
        config.init();
        return config;
    }

    static SSTAFConfiguration from(JsonNode node, Path sourceFile) {
        JsonLoader loader = new JsonLoader();
        SSTAFConfiguration config = (SSTAFConfiguration) loader.load(node, sourceFile);
        config.init();
        setInstance(config);
        return config;
    }

    protected void init() {
        rootLayer = ModuleLayerSupport.makeModuleLayer(ModuleLayer.boot(),
                moduleLayerDefinition, this.getClass().getClassLoader());
    }

}
