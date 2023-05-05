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

package mil.sstaf.analyzer;

import mil.sstaf.core.configuration.SSTAFConfiguration;
import mil.sstaf.session.control.Session;
import mil.sstaf.session.control.SessionConfiguration;
import mil.sstaf.session.control.EntityController;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static java.lang.System.exit;

/**
 * The entry point for the Analyzer
 */
public class Main {
    public static final int ERROR_NO_ARGS = 1;
    public static final int ERROR_ENTITY_FILE_NOT_READABLE = 2;

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    /**
     * The Main method
     */
    public static void main(String[] args) {

        if (args.length < 1 || args.length > 2) {
            printUsage();
            exit(ERROR_NO_ARGS);
        }

        boolean disableTimeout = false;
        String entityFileName = args[0];
        if (entityFileName.equals("--notimeout") && args.length == 2)
        {
            entityFileName = args[1];
            disableTimeout = true;
        } else if (entityFileName == "--notimeout" && args.length == 1)
        {
            printUsage();
            exit(ERROR_NO_ARGS);
        }

        File entityFile = new File(entityFileName);
        if (!entityFile.exists() | !entityFile.canRead()) {
            System.err.printf("Entity file '%s' does not exist or is not readable\n", entityFileName);
            exit(ERROR_ENTITY_FILE_NOT_READABLE);
        }

        EntityController entityController = EntityController.from(entityFile);
        SSTAFConfiguration config = SSTAFConfiguration.getInstance();
        SessionConfiguration sessionConfiguration;
        if (config instanceof SessionConfiguration) {
            sessionConfiguration = (SessionConfiguration) config;
        } else {
            sessionConfiguration = SessionConfiguration.builder().build();
            logger.info("Creating default configuration");
        }
        Session session = Session.of(sessionConfiguration, entityController);
        Analyzer analyzer = Analyzer.fromSystemIO(session);
        analyzer.setDisableTimeout(disableTimeout);
        logger.info("Starting analyzer loop");
        int rv;
        try {
            rv = analyzer.start();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        System.exit(rv);
    }

    /**
     * Print Usage.
     */
    public static void printUsage() {
        System.err.println("Usage: sstaf-analyzer [-Dmil.sstaf.configuration=configFile] [--notimeout]  entityFile");

    }

}

