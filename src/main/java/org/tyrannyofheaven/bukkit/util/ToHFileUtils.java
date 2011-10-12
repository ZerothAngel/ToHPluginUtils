/*
 * Copyright 2011 ZerothAngel <zerothangel@tyrannyofheaven.org>
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.tyrannyofheaven.bukkit.util.configuration.AnnotatedYamlConfiguration;

/**
 * File utilities.
 * 
 * @author zerothangel
 */
public class ToHFileUtils {

    // Size of buffer for copyFile()
    private static final int COPY_BUFFER_SIZE = 4096;

    // Configuration version property key
    private static final String CONFIG_VERSION_KEY = "config-version";

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

    /**
     * Return the specified Configuration. The Configuration may pull its defaults
     * from the given resource file. (Note: the resource is loaded relative to the
     * plugin class.) The Configuration may also use comments read from the given
     * comments file.
     *  
     * @param plugin the plugin
     * @param configDir the parent directory of the config file
     * @param configName the name of the config file
     * @param mustExist set to true if the file must exist (will throw FileNotFoundException if config not present)
     * @param defaultsName the path of the defaults resource, relative to the plugin class. May be <code>null</code>.
     * @param commentsName the path of the comments resource, relative to the plugin class. May be <code>null</code>.
     * @return the Configuration object, with defaults and comments appropriately set
     * @throws FileNotFoundException if mustExist is true and the config file is not found
     */
    public static FileConfiguration getConfig(Plugin plugin, File configDir, String configName, boolean mustExist, String defaultsName, String commentsName) throws FileNotFoundException {
        File configFile = new File(configDir, configName);

        AnnotatedYamlConfiguration config = new AnnotatedYamlConfiguration();
        try {
            config.load(configFile);
        }
        catch (FileNotFoundException e) {
            if (mustExist)
                throw e;
            // Otherwise, ignore...
        }
        catch (IOException e) {
            ToHLoggingUtils.error(plugin, "Error reading configuration %s", configFile, e);
        }
        catch (InvalidConfigurationException e) {
            ToHLoggingUtils.error(plugin, "Error parsing configuration %s", configFile, e);
        }

        // Set defaults if present
        if (defaultsName != null) {
            InputStream defaultsInput = plugin.getClass().getResourceAsStream(defaultsName);
            if (defaultsInput != null) {
                Configuration defaults = YamlConfiguration.loadConfiguration(defaultsInput);
                config.setDefaults(defaults);
            }
        }

        // Set root-level comments, if appropriate file is present
        if (commentsName != null) {
            InputStream commentsInput = plugin.getClass().getResourceAsStream(commentsName);
            if (commentsInput != null) {
                Configuration comments = YamlConfiguration.loadConfiguration(commentsInput);
                Map<String, String> commentsMap = new HashMap<String, String>();
                for (Map.Entry<String, Object> entry : comments.getValues(false).entrySet()) {
                    commentsMap.put(entry.getKey(), entry.getValue().toString());
                }
                config.setComments(commentsMap);
            }
        }

        return config;
    }

    /**
     * Return the specified Configuration. The Configuration may pull its defaults
     * from the given resource file. (Note: the resource is loaded relative to the
     * plugin class.) The Configuration may also use comments read from the given
     * comments file.
     *  
     * @param plugin the plugin
     * @param configDir the parent directory of the config file
     * @param configName the name of the config file
     * @param defaultsName the path of the defaults resource, relative to the plugin class. May be <code>null</code>.
     * @param commentsName the path of the comments resource, relative to the plugin class. May be <code>null</code>.
     * @return the Configuration object, with defaults and comments appropriately set
     */
    public static FileConfiguration getConfig(Plugin plugin, File configDir, String configName, String defaultsName, String commentsName) {
        try {
            return getConfig(plugin, configDir, configName, false, defaultsName, commentsName);
        }
        catch (FileNotFoundException e) {
            // Should never get here because we set mustExist to false
            throw new AssertionError();
        }
    }

    /**
     * Fetch the plugin's standard Configuration (from <code>plugins/plugin-name/config.yml</code>)
     * and set it up with the appropriate defaults and comments resources, if present.
     * The defaults and comments resources are expected to be at config.yml and
     * config-comments.yml respectively, in the same package as the plugin's class.
     * 
     * @param plugin the plugin
     * @return the Configuration object, appropriately configured with defaults and comments
     */
    public static FileConfiguration getConfig(Plugin plugin) {
        return getConfig(plugin, plugin.getDataFolder(), "config.yml", "config.yml", "config-comments.yml");
    }

    /**
     * Attempt to save a FileConfiguration.
     * 
     * @param plugin the plugin
     * @param config the FileConfiguration to save
     * @param configDir the parent directory of the file
     * @param configName the config filename
     */
    public static void saveConfig(Plugin plugin, FileConfiguration config, File configDir, String configName) {
        File newConfigFile = new File(configDir, configName + ".new");

        // First try saving
        try {
            config.save(newConfigFile);
        }
        catch (IOException e) {
            ToHLoggingUtils.error(plugin, "Error saving configuration %s", newConfigFile, e);
            return;
        }
        
        File backupConfigFile = new File(configDir, configName + "~");

        // Delete old backup (might be necessary on some platforms)
        if (backupConfigFile.exists() && !backupConfigFile.delete()) {
            ToHLoggingUtils.error(plugin, "Error deleting configuration %s", backupConfigFile);
            // Continue despite failure
        }
        
        File configFile = new File(configDir, configName);

        // If only we had access to hardlinks, this could all be atomic.
        
        // Back up old config
        if (configFile.exists() && !configFile.renameTo(backupConfigFile)) {
            ToHLoggingUtils.error(plugin, "Error renaming %s to %s", configFile, backupConfigFile);
            return; // no backup, abort
        }

        // Rename new file to config
        if (!newConfigFile.renameTo(configFile)) {
            ToHLoggingUtils.error(plugin, "Error renaming %s to %s", newConfigFile, configFile);
            // Nothing else to do
        }
    }

    /**
     * Save a FileConfiguration as the plugin's standard config.yml.
     * 
     * @param plugin the plugin
     * @param config the FileConfiguration
     */
    public static void saveConfig(Plugin plugin, FileConfiguration config) {
        saveConfig(plugin, config, plugin.getDataFolder(), "config.yml");
    }

    /**
     * Upgrade the standard configuration file, if necessary.
     * 
     * @param plugin the plugin
     * @param config the FileConfiguration
     * @param currentVersion the expected version, should be > 0
     */
    public static void upgradeConfig(Plugin plugin, FileConfiguration config) {
        Configuration defaults = config.getDefaults();
        if (defaults == null)
            throw new IllegalStateException("config does not have defaults");
        int currentVersion = defaults.getInt(CONFIG_VERSION_KEY);
        int configVersion = config.getInt(CONFIG_VERSION_KEY);
        if (!config.isSet(CONFIG_VERSION_KEY) || configVersion < currentVersion) {
            ToHLoggingUtils.log(plugin, "Upgrading config.yml");

            // Update version
            config.set(CONFIG_VERSION_KEY, currentVersion);

            // Save old copyDefaults value & set to true
            boolean copyDefaults = config.options().copyDefaults();
            config.options().copyDefaults(true);

            // Save config
            saveConfig(plugin, config);

            // Restore old copyDefaults
            config.options().copyDefaults(copyDefaults);
        }
    }

}
