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

package mil.sstaf.core.module;

import mil.sstaf.core.util.SSTAFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.module.Configuration;
import java.lang.module.FindException;
import java.lang.module.ModuleFinder;
import java.lang.module.ResolvedModule;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ModuleLayerSupport {
    private static final Logger logger = LoggerFactory.getLogger(ModuleLayerSupport.class);

    public static ModuleLayer makeModuleLayer(ModuleLayer parentLayer,
                                              ModuleLayerDefinition def,
                                              ClassLoader parentLoader) {
        ModuleLayer moduleLayer;

        if (def == null) {
            logger.info("Reusing module layer from SSTAFConfiguration");
            moduleLayer = parentLayer;
        } else if (def.getModules() == null || def.getModules().isEmpty()) {
            logger.info("Reusing module layer from SSTAFConfiguration");
            moduleLayer = parentLayer;
        } else {
            checkModules(def.getModules());
            List<Path> paths;

            if (def.getModulePaths() == null) {
                paths = List.of();
            } else {
                paths = new ArrayList<>(def.getModulePaths().size());
                for (Path p : def.getModulePaths()) {
                    if (p.isAbsolute()) {
                        paths.add(p);
                    } else {
                        Path baseDir = def.getSourceDir() == null ?
                                Path.of(System.getProperty("user.dir")) :
                                def.getSourceDir();
                        Path expanded = Path.of(baseDir.toString(), p.toString()).normalize();
                        paths.add(expanded);
                    }
                }
            }
            for (Path p : paths) {
                if (!p.toFile().exists()) {
                    throw new SSTAFException("Module path '" + p + "' does not exist.");
                }
            }
            try {
                logger.info("Creating new module layer. Parent ClassLoader is {}", parentLoader);
                Path[] pathArray = new Path[paths.size()];
                paths.toArray(pathArray);
                if (logger.isDebugEnabled()) {
                    logger.info("paths are {}", Arrays.toString(pathArray));
                    logger.info("modules are {}", def.getModules());
                }
                ModuleFinder moduleFinder = ModuleFinder.of(pathArray);
                Configuration configuration = Configuration.resolveAndBind(moduleFinder,
                        List.of(parentLayer.configuration()),
                        ModuleFinder.of(), def.getModules());
                moduleLayer = parentLayer.defineModulesWithOneLoader(configuration, parentLoader);
                logger.info("moduleLayer is {}", moduleLayer);
            } catch (FindException findException) {
                throw new SSTAFException("Could not find a module given paths " + paths, findException);
            }
        }
        return moduleLayer;
    }

    static void checkModules(Set<String> moduleNames) {
        for(String s : moduleNames) {
            if (s.endsWith(".jar")) {
                throw new SSTAFException("ModuleDefinition contains a jar file (" + s +") rather than a module name");
            }
        }
    }

    public static String makeModuleLayerReport(String prefix, ModuleLayer current) {
        if (current == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(" contains: ").append('\n');

        int count = 0;
        for (Module m : current.modules()) {
            sb.append("    ").append(m.getName()).append('\n');
            ++count;
        }
        if (count == 0) {
            sb.append("    layer is empty");
        }

        int n = 0;
        for (ModuleLayer p : current.parents()) {
            ++n;
            String res = makeModuleLayerReport(prefix+"->parent-"+n, p);
            sb.append(res);
        }
        return sb.toString();
    }
}

