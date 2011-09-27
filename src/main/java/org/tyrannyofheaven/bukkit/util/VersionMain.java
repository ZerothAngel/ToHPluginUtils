/*
 * Copyright 2011 Allan Saddi <allan@saddi.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tyrannyofheaven.bukkit.util;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.jar.JarInputStream;

/**
 * Locates our JAR file and outputs a few fields from its manifest.
 * 
 * @author asaddi
 */
public class VersionMain {

    public static void main(String[] args) throws IOException {
        CodeSource codeSource = VersionMain.class.getProtectionDomain().getCodeSource();
        URL url = codeSource.getLocation();
        if (url.toExternalForm().toLowerCase().endsWith(".jar")) { // Hmmm
            JarInputStream jis = new JarInputStream(url.openStream());
            String artifactId = jis.getManifest().getMainAttributes().getValue("Implementation-Title");
            String version = jis.getManifest().getMainAttributes().getValue("Implementation-Version");
            String build = jis.getManifest().getMainAttributes().getValue("Implementation-Build");
            if (version.contains("SNAPSHOT"))
                System.out.println(String.format("%s %s (build: %s)", artifactId, version, build));
            else
                System.out.println(String.format("%s %s", artifactId, version));
        }
        else {
            System.err.println("Unable to find my jar file!");
            System.exit(1);
        }
    }

}
