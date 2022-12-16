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

package mil.sstaf.analyzer.pipe;

import java.io.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PipeService {

    public static void printUsage() {
        String message = "Usage: java -p SSTAF_MODULE_DIR -m mil.sstaf.service/mil.sstaf.service.pipe.Main sessionConfigFile.json";
        System.err.println(message);
    }

    private final static Supplier<String> supplier = new Supplier<>() {
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        @Override
        public String get() {
            String rv = "";
            try {
                rv = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return rv;
        }
    };

    private final static Consumer<String> consumer = new Consumer<>() {
        final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));

        @Override
        public void accept(String s) {
            try {
                out.write(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public static void main(String[] args) {

        if (args.length < 1) {
            printUsage();
            System.exit(-1);
        }

        String configFile = args[0];
       // Session session = Session.factory(configFile).build();


    }
}

