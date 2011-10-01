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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Convenience methods mainly for displaying info via {@link CommandSender#sendMessage(String)}.
 * 
 * @author asaddi
 */
public class ToHMessageUtils {

    // Cache for colorize(). Feeling kinda iffy, but Strings are immutable...
    private static final ConcurrentMap<String, String> colorizeCache = new ConcurrentHashMap<String, String>();

    private ToHMessageUtils() {
        throw new AssertionError("Don't instantiate me!");
    }

    /**
     * Convenience method for sending messages. Supports {@link String#format(String, Object...)}
     * formatting and multiple lines.
     * 
     * @param sender the receiver of the message
     * @param format the message format string
     * @param args format arguments
     */
    public static void sendMessage(CommandSender sender, String format, Object... args) {
        String message = String.format(format, args);
        for (String line : message.split("\n")) {
            sender.sendMessage(line);
        }
    }

    // For colorize()
    private static enum ColorizeState {
        TEXT, COLOR_OPEN, COLOR_NAME, COLOR_CLOSE, COLOR_ESCAPE;
    }

    /**
     * Scans text for color tokens and replaces with the appropriate color
     * code. Might be more convenient than string concatenation? Tokens
     * consist of the color name enclosed in curly-braces. Braces may be
     * escaped by doubling.
     * 
     * @param text the text to colorize
     * @return the colorized result
     */
    public static String colorize(String text) {
        if (text == null) return null;

        // Works best with interned strings
        String cacheResult = colorizeCache.get(text);
        if (cacheResult != null) return cacheResult;

        StringBuilder out = new StringBuilder();

        ColorizeState state = ColorizeState.TEXT;
        StringBuilder color = null;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (state == ColorizeState.TEXT) {
                if (c == '{') {
                    state = ColorizeState.COLOR_OPEN;
                }
                else if (c == '}') {
                    state = ColorizeState.COLOR_CLOSE;
                }
                else if (c == '`') {
                    state = ColorizeState.COLOR_ESCAPE;
                }
                else {
                    out.append(c);
                }
            }
            else if (state == ColorizeState.COLOR_OPEN) {
                if (c == '{') { // Escaped bracket
                    out.append('{');
                    state = ColorizeState.TEXT;
                }
                else if (Character.isUpperCase(c)) {
                    // First character of color name
                    color = new StringBuilder();
                    color.append(c);
                    state = ColorizeState.COLOR_NAME;
                }
                else {
                    // Invalid
                    throw new IllegalArgumentException("Invalid color name");
                }
            }
            else if (state == ColorizeState.COLOR_NAME) {
                if (Character.isUpperCase(c) || c == '_') {
                    color.append(c);
                }
                else if (c == '}') {
                    ChatColor chatColor = ChatColor.valueOf(color.toString());
                    out.append(chatColor);
                    state = ColorizeState.TEXT;
                }
                else {
                    // Invalid
                    throw new IllegalArgumentException("Invalid color name");
                }
            }
            else if (state == ColorizeState.COLOR_CLOSE) {
                // Optional, but for sanity's sake, to keep brackets matched
                if (c == '}') {
                    out.append('}'); // Collapse to single bracket
                }
                else {
                    out.append('}');
                    out.append(c);
                }
                state = ColorizeState.TEXT;
            }
            else if (state == ColorizeState.COLOR_ESCAPE) {
                out.append(decodeColor(c));
                state = ColorizeState.TEXT;
            }
            else
                throw new AssertionError("Unknown ColorizeState");
        }
        
        // End of string
        if (state == ColorizeState.COLOR_CLOSE) {
            out.append('}');
        }
        else if (state != ColorizeState.TEXT) {
            // Was in the middle of color name
            throw new IllegalArgumentException("Invalid color name");
        }

        cacheResult = out.toString();
        colorizeCache.putIfAbsent(text, cacheResult);

        return cacheResult;
    }

    // Decode a color escape code. Same mapping as sk89q's plugins.
    private static String decodeColor(char c) {
        switch (c) {
        case '`':
            return "`";
        case '0':
            return ChatColor.BLACK.toString();
        case '1':
            return ChatColor.GRAY.toString();
        case '2':
            return ChatColor.DARK_GRAY.toString();
        case 'b':
            return ChatColor.BLUE.toString();
        case 'B':
            return ChatColor.DARK_BLUE.toString();
        case 'c':
            return ChatColor.AQUA.toString();
        case 'C':
            return ChatColor.DARK_AQUA.toString();
        case 'g':
            return ChatColor.GREEN.toString();
        case 'G':
            return ChatColor.DARK_GREEN.toString();
        case 'p':
            return ChatColor.LIGHT_PURPLE.toString();
        case 'P':
            return ChatColor.DARK_PURPLE.toString();
        case 'r':
            return ChatColor.RED.toString();
        case 'R':
            return ChatColor.DARK_RED.toString();
        case 'w':
            return ChatColor.WHITE.toString();
        case 'y':
            return ChatColor.YELLOW.toString();
        case 'Y':
            return ChatColor.GOLD.toString();
        default:
            throw new IllegalArgumentException("Invalid color code");
        }
    }

}
