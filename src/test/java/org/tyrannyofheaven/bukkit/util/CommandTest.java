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

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.junit.Test;
import org.tyrannyofheaven.bukkit.util.command.HandlerExecutor;
import org.tyrannyofheaven.bukkit.util.command.ParseException;
import org.tyrannyofheaven.bukkit.util.permissions.PermissionException;

public class CommandTest {

    @Test
    public void testHandlerExecutor() throws Exception {
        // Some mock objects
        MyPlugin plugin = new MyPlugin();

        final StringBuilder out = new StringBuilder();
        final Set<String> permissions = new HashSet<String>();

        CommandSender dummySender = new CommandSender() {
            @Override
            public Server getServer() {
                return null;
            }
            @Override
            public void sendMessage(String message) {
                out.append(message);
                out.append('\n');
            }
            @Override
            public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
                return null;
            }
            @Override
            public PermissionAttachment addAttachment(Plugin plugin) {
                return null;
            }
            @Override
            public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
                return null;
            }
            @Override
            public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
                return null;
            }
            @Override
            public Set<PermissionAttachmentInfo> getEffectivePermissions() {
                return null;
            }
            @Override
            public boolean hasPermission(String name) {
                return permissions.contains(name);
            }
            @Override
            public boolean hasPermission(Permission perm) {
                return false;
            }
            @Override
            public boolean isPermissionSet(String name) {
                return false;
            }
            @Override
            public boolean isPermissionSet(Permission perm) {
                return false;
            }
            @Override
            public void recalculatePermissions() {
            }
            @Override
            public void removeAttachment(PermissionAttachment attachment) {
            }
            @Override
            public boolean isOp() {
                return false;
            }
            @Override
            public void setOp(boolean value) {
            }
            @Override
            public String getName() {
                return null;
            }
        };
        HandlerExecutor<MyPlugin> he = new HandlerExecutor<MyPlugin>(plugin, new MyHandler());
        
        // No positional params, boolean flag
        he.execute(dummySender, "hello", "hello", new String[0], null);
        Assert.assertEquals("Hello World!\n", out.toString()); out.delete(0, out.length());

        he.execute(dummySender, "hello", "hello", new String[] { "-f" }, null);
        Assert.assertEquals("Hello World!\nWith flag!\n", out.toString()); out.delete(0, out.length());

        he.execute(dummySender, "hello", "greetings", new String[0], null);
        Assert.assertEquals("Hello World!\n", out.toString()); out.delete(0, out.length());

        he.execute(dummySender, "hello", "greetings", new String[] { "-f" }, null);
        Assert.assertEquals("Hello World!\nWith flag!\n", out.toString()); out.delete(0, out.length());
        
        // Required positional param, flag with value
        boolean good = false;
        try { he.execute(dummySender, "greet", "greet", new String[0], null); } catch (ParseException e) { good = true; }
        Assert.assertTrue(good);

        good = false;
        try { he.execute(dummySender, "greet", "greet", new String[] { "-o" }, null); } catch (ParseException e) { good = true; }
        Assert.assertTrue(good);

        good = false;
        try { he.execute(dummySender, "greet", "greet", new String[] { "-o", "foo" }, null); } catch (ParseException e) { good = true; }
        Assert.assertTrue(good);

        he.execute(dummySender, "greet", "greet", new String[] { "-o", "foo", "bar" }, null);
        Assert.assertEquals("Hello, bar\nWith option = foo!\n", out.toString()); out.delete(0, out.length());

        he.execute(dummySender, "greet", "greet", new String[] { "-o", "foo", "bar", "garply" }, null);
        Assert.assertEquals("Hello, bar\nWith option = foo!\n", out.toString()); out.delete(0, out.length());

        // Positional argument that starts with -
        he.execute(dummySender, "greet", "greet", new String[] { "--", "-garply" }, null);
        Assert.assertEquals("Hello, -garply\n", out.toString()); out.delete(0, out.length());

        // String[] parameter
        he.execute(dummySender, "say", "say", new String[] { "Hello", "there" }, null);
        Assert.assertEquals("Hello there\n", out.toString()); out.delete(0, out.length());
        
        // No flags, so should not parse -Hello as one.
        he.execute(dummySender, "say", "say", new String[] { "-Hello", "there" }, null);
        Assert.assertEquals("-Hello there\n", out.toString()); out.delete(0, out.length());

        // Sub-command
        good = false;
        try { he.execute(dummySender, "foo", "foo", new String[0], null); } catch (ParseException e) { good = true; }
        Assert.assertTrue(good);

        he.execute(dummySender, "foo", "foo", new String[] { "hello" }, null);
        Assert.assertEquals("Hello from the foo sub-command!\n", out.toString()); out.delete(0, out.length());
        
        good = false;
        permissions.clear();
        try {
            he.execute(dummySender, "secret", "secret", new String[0], null);
        }
        catch (PermissionException e) {
            good = true;
        }
        Assert.assertTrue(good);
        
        permissions.add("foo.secret");
        he.execute(dummySender, "secret", "secret", new String[0], null);
        Assert.assertEquals("Spike has a crush on Rarity\nyay!\n", out.toString()); out.delete(0, out.length());
    }

}
