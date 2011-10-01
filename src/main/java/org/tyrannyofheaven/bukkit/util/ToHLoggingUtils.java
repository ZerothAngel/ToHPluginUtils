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

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

/**
 * More convenient and consistent logging.
 * 
 * @author zerothangel
 */
public class ToHLoggingUtils {

    private ToHLoggingUtils() {
        throw new AssertionError("Don't instantiate me!");
    }

    // Create a log message
    private static String createLogMessage(Plugin plugin, String format, Object... args) {
        if (format == null)
            return null;
        else
            return String.format("[%s] %s", plugin.getDescription().getName(), String.format(format, args));
    }

    // Retrieve logger associated with a plugin
    private static Logger getLogger(Plugin plugin) {
        return Logger.getLogger(plugin.getClass().getName());
    }

    /**
     * Log a message.
     * 
     * @param plugin the plugin
     * @param level the log level
     * @param format the format message
     * @param args the format args. Note that if the last argument is a
     *   Throwable, it is removed and passed to {@link java.util.logging.Logger#log(Level, String, Throwable)}.
     *   It will not be available for the format.
     */
    public static void log(Plugin plugin, Level level, String format, Object... args) {
        Logger logger = getLogger(plugin);
        if (logger.isLoggable(level)) { // Avoid unnecessary String.format() calls
            if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
                // Last argument is a Throwable, treat accordingly
                logger.log(level, createLogMessage(plugin, format, Arrays.copyOf(args, args.length - 1)), (Throwable)args[args.length - 1]);
            }
            else {
                logger.log(level, createLogMessage(plugin, format, args));
            }
        }
    }

    /**
     * Log a message at INFO level.
     * 
     * @param plugin the plugin
     * @param format the format message
     * @param args the format args. Note that if the last argument is a
     *   Throwable, it is removed and passed to {@link java.util.logging.Logger#log(Level, String, Throwable)}.
     *   It will not be available for the format.
     */
    public static void log(Plugin plugin, String format, Object... args) {
        log(plugin, Level.INFO, format, args);
    }

    /**
     * Log a message at FINE level.
     * 
     * @param plugin the plugin
     * @param format the format message
     * @param args the format args. Note that if the last argument is a
     *   Throwable, it is removed and passed to {@link java.util.logging.Logger#log(Level, String, Throwable)}.
     *   It will not be available for the format.
     */
    public static void debug(Plugin plugin, String format, Object... args) {
        log(plugin, Level.FINE, format, args);
    }

    /**
     * Log a message at WARNING level.
     * 
     * @param plugin the plugin
     * @param format the format message
     * @param args the format args. Note that if the last argument is a
     *   Throwable, it is removed and passed to {@link java.util.logging.Logger#log(Level, String, Throwable)}.
     *   It will not be available for the format.
     */
    public static void warn(Plugin plugin, String format, Object... args) {
        log(plugin, Level.WARNING, format, args);
    }

    /**
     * Log a message at SEVERE level.
     * 
     * @param plugin the plugin
     * @param format the format message
     * @param args the format args. Note that if the last argument is a
     *   Throwable, it is removed and passed to {@link java.util.logging.Logger#log(Level, String, Throwable)}.
     *   It will not be available for the format.
     */
    public static void error(Plugin plugin, String format, Object... args) {
        log(plugin, Level.SEVERE, format, args);
    }

}
