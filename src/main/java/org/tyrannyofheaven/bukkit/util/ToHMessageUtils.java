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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.ChatPaginator;

/**
 * Convenience methods mainly for displaying info via {@link CommandSender#sendMessage(String)}.
 * 
 * @author zerothangel
 */
public class ToHMessageUtils {

    // Number of lines per page
    private static final int LINES_PER_PAGE = ChatPaginator.CLOSED_CHAT_PAGE_HEIGHT;

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

    /**
     * Broadcasts the message to every user with the given permission. Supports
     * {@link String#format(String, Object...)} formatting and multiple lines.
     * 
     * @param server the server
     * @param permission the permission required to receive the broadcast
     * @param format the message format
     * @param args format args
     * @return number of users who received the message. In the case of a multi-line
     *   message, this is the number of users who received the last line.
     */
    public static int broadcast(Plugin plugin, String permission, String format, Object... args) {
        String message = String.format(format, args);
        int count = 0;
        for (String line : message.split("\n")) {
            count = plugin.getServer().broadcast(line, permission);
        }
        return count;
    }

    /**
     * Broadcasts the message to all players. Supports {@link String#format(String, Object...)}
     * formatting and multiple lines.
     * 
     * @param server the server
     * @param format the message format
     * @param args format args
     * @return number of users who received the message. In the case of a multi-line
     *   message, this is the number of users who received the last line.
     */
    public static int broadcastMessage(Plugin plugin, String format, Object... args) {
        return broadcast(plugin, Server.BROADCAST_CHANNEL_USERS, format, args);
    }

    /**
     * Broadcasts the message to all admins. Supports {@link String#format(String, Object...)}
     * formatting and multiple lines.
     * 
     * @param server the server
     * @param format the message format
     * @param args format args
     * @return number of users who received the message. In the case of a multi-line
     *   message, this is the number of users who received the last line.
     */
    public static int broadcastAdmin(Plugin plugin, String format, Object... args) {
        return broadcast(plugin, Server.BROADCAST_CHANNEL_ADMINISTRATIVE, format, args);
    }

    /**
     * Display a bunch of lines, automatically paginating if necessary. Only bothers
     * paginating if sender is a Player and the number of lines is greater than
     * the size of a page.
     * 
     * @param plugin the plugin
     * @param sender the CommandSender to display the lines to
     * @param lines the lines to display
     */
    public static void displayLines(Plugin plugin, CommandSender sender, List<String> lines) {
        if (lines.isEmpty()) return;

        if (sender instanceof Player) {
            // Word wrap long lines
            List<String> outputLines = new ArrayList<String>(lines.size());
            for (String line : lines) {
                String[] wrapped = ChatPaginator.wordWrap(line, ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH);
                for (String wrap : wrapped)
                    outputLines.add(wrap);
            }
            lines = outputLines;

            if (lines.size() > LINES_PER_PAGE) {
                Conversation convo = new ConversationFactory(plugin)
                .withFirstPrompt(new PagerPrompt(lines, LINES_PER_PAGE))
                .withLocalEcho(false)
                .buildConversation((Conversable)sender);

                convo.begin();
                return;
            }
            
            // Fall through...
        }

        // Don't bother with pager
        for (String line : lines) {
            sender.sendMessage(line);
        }
    }

}
