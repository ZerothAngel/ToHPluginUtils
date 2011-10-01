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

import static org.tyrannyofheaven.bukkit.util.ToHLoggingUtils.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

/**
 * File utilities.
 * 
 * @author asaddi
 */
public class ToHFileUtils {

    // Size of buffer for copyFile()
    private static final int COPY_BUFFER_SIZE = 4096;

    private ToHFileUtils() {
        throw new AssertionError("Don't instantiate me!");
    }

    /**
     * Copy an InputStream to a file.
     * 
     * @param input the InputStream
     * @param outFile the output File
     * @throws IOException
     */
    public static void copyFile(InputStream input, File outFile) throws IOException {
        OutputStream os = new FileOutputStream(outFile);
        try {
            byte[] buffer = new byte[COPY_BUFFER_SIZE];
            int readLen;
            while ((readLen = input.read(buffer)) != -1) {
                os.write(buffer, 0, readLen);
            }
        }
        finally {
            os.close();
        }
    }

    /**
     * Copy a resource (using a class's classloader) to a file.
     * 
     * @param clazz the class
     * @param resourceName resource name relative to the class
     * @param outFile the output File
     * @throws IOException
     */
    public static void copyResourceToFile(Class<?> clazz, String resourceName, File outFile) throws IOException {
        InputStream is = clazz.getResourceAsStream(resourceName);
        try {
            copyFile(is, outFile);
        }
        finally {
            is.close();
        }
    }

    /**
     * Copies a resource relative to the Plugin class to a file.
     * 
     * @param plugin the plugin
     * @param resourceName resource name relative to plugin's class
     * @param outFile the output file
     * @return true if successful, false otherwise
     */
    public static boolean copyResourceToFile(Plugin plugin, String resourceName, File outFile) {
        try {
            copyResourceToFile(plugin.getClass(), resourceName, outFile);
            return true;
        }
        catch (IOException e) {
            log(plugin, Level.SEVERE, "Error copying %s to %s", resourceName, outFile, e);
            return false;
        }
    }

}
