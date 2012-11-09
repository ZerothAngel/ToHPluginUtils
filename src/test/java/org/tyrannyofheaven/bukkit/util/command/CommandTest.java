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
package org.tyrannyofheaven.bukkit.util.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;
import org.junit.Before;
import org.junit.Test;
import org.tyrannyofheaven.bukkit.util.permissions.PermissionException;

public class CommandTest {

    private final StringBuilder out = new StringBuilder();
    
    private final Set<String> permissions = new HashSet<String>();

    private final CommandSender dummySender;

    private final HandlerExecutor<MyPlugin> he;

    public CommandTest() {
        // Some mock objects
        MyPlugin plugin = new MyPlugin();

        dummySender = new CommandSender() {
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
            @Override
            public void sendMessage(String[] arg0) {
            }
        };
        he = new HandlerExecutor<MyPlugin>(plugin, new MyHandler());
    }

    @Before
    public void setUp() {
        out.delete(0, out.length());
        permissions.clear();
    }

    @Test
    public void testHandlerExecutor() throws Throwable {
        // No positional params, boolean flag
        he.execute(dummySender, "hello", "hello", new String[0]);
        Assert.assertEquals("Hello World!\n", out.toString()); out.delete(0, out.length());

        he.execute(dummySender, "hello", "hello", new String[] { "-f" });
        Assert.assertEquals("Hello World!\nWith flag!\n", out.toString()); out.delete(0, out.length());

        he.execute(dummySender, "hello", "greetings", new String[0]);
        Assert.assertEquals("Hello World!\n", out.toString()); out.delete(0, out.length());

        he.execute(dummySender, "hello", "greetings", new String[] { "-f" });
        Assert.assertEquals("Hello World!\nWith flag!\n", out.toString()); out.delete(0, out.length());
        
        // Required positional param, flag with value
        boolean good = false;
        try { he.execute(dummySender, "greet", "greet", new String[0]); } catch (ParseException e) { good = true; }
        Assert.assertTrue(good);

        good = false;
        try { he.execute(dummySender, "greet", "greet", new String[] { "-o" }); } catch (ParseException e) { good = true; }
        Assert.assertTrue(good);

        good = false;
        try { he.execute(dummySender, "greet", "greet", new String[] { "-o", "foo" }); } catch (ParseException e) { good = true; }
        Assert.assertTrue(good);

        he.execute(dummySender, "greet", "greet", new String[] { "-o", "foo", "bar" });
        Assert.assertEquals("Hello, bar\nWith option = foo!\n", out.toString()); out.delete(0, out.length());

        he.execute(dummySender, "greet", "greet", new String[] { "-o", "foo", "bar", "garply" });
        Assert.assertEquals("Hello, bar\nWith option = foo!\n", out.toString()); out.delete(0, out.length());

        // Positional argument that starts with -
        he.execute(dummySender, "greet", "greet", new String[] { "--", "-garply" });
        Assert.assertEquals("Hello, -garply\n", out.toString()); out.delete(0, out.length());

        // String[] parameter
        he.execute(dummySender, "say", "say", new String[] { "Hello", "there" });
        Assert.assertEquals("Hello there\n", out.toString()); out.delete(0, out.length());
        
        // No flags, so should not parse -Hello as one.
        he.execute(dummySender, "say", "say", new String[] { "-Hello", "there" });
        Assert.assertEquals("-Hello there\n", out.toString()); out.delete(0, out.length());

        // Sub-command
        good = false;
        try { he.execute(dummySender, "foo", "foo", new String[0]); } catch (ParseException e) { good = true; }
        Assert.assertTrue(good);

        he.execute(dummySender, "foo", "foo", new String[] { "hello" });
        Assert.assertEquals("Hello from the foo sub-command!\n", out.toString()); out.delete(0, out.length());
        
        good = false;
        permissions.clear();
        try {
            he.execute(dummySender, "secret", "secret", new String[0]);
        }
        catch (PermissionException e) {
            good = true;
        }
        Assert.assertTrue(good);
        
        permissions.add("foo.secret");
        he.execute(dummySender, "secret", "secret", new String[0]);
        Assert.assertEquals("Spike has a crush on Rarity\nyay!\n", out.toString()); out.delete(0, out.length());

        // Long/short flags
        he.execute(dummySender, "garply", "garply", new String[0]); // nothing
        Assert.assertEquals("no flag with option = null\n", out.toString()); out.delete(0, out.length());

        he.execute(dummySender, "garply", "garply", new String[] { "-f" });
        Assert.assertEquals("have flag with option = null\n", out.toString()); out.delete(0, out.length());

        he.execute(dummySender, "garply", "garply", new String[] { "--flag" });
        Assert.assertEquals("have flag with option = null\n", out.toString()); out.delete(0, out.length());

        good = false;
        try {
            he.execute(dummySender, "garply", "garply", new String[] { "-o" });
        }
        catch (MissingValueException e) {
            good = e.getOptionMetaData().getName().equals("-o");
        }
        Assert.assertTrue(good);

        good = false;
        try {
            he.execute(dummySender, "garply", "garply", new String[] { "--option" });
        }
        catch (MissingValueException e) {
            good = e.getOptionMetaData().getName().equals("-o");
        }
        Assert.assertTrue(good);

        he.execute(dummySender, "garply", "garply", new String[] { "-o", "blah" });
        Assert.assertEquals("no flag with option = blah\n", out.toString()); out.delete(0, out.length());

        he.execute(dummySender, "garply", "garply", new String[] { "--option", "blah" });
        Assert.assertEquals("no flag with option = blah\n", out.toString()); out.delete(0, out.length());
        
        // Multi-flags
        he.execute(dummySender, "garply", "garply", new String[] { "-f", "-o", "blah" });
        Assert.assertEquals("have flag with option = blah\n", out.toString()); out.delete(0, out.length());

        he.execute(dummySender, "garply", "garply", new String[] { "-o", "blah", "-f"});
        Assert.assertEquals("have flag with option = blah\n", out.toString()); out.delete(0, out.length());

        good = false;
        try {
            he.execute(dummySender, "garply", "garply", new String[] { "-fo" });
        }
        catch (MissingValueException e) {
            good = e.getOptionMetaData().getName().equals("-o");
        }
        Assert.assertTrue(good);

        good = false;
        try {
            he.execute(dummySender, "garply", "garply", new String[] { "-of" });
        }
        catch (MissingValueException e) {
            good = e.getOptionMetaData().getName().equals("-o");
        }
        Assert.assertTrue(good);

        he.execute(dummySender, "garply", "garply", new String[] { "-fo", "blah" });
        Assert.assertEquals("have flag with option = blah\n", out.toString()); out.delete(0, out.length());

        he.execute(dummySender, "garply", "garply", new String[] { "-of", "blah" });
        Assert.assertEquals("have flag with option = blah\n", out.toString()); out.delete(0, out.length());
        
        // Dual multi-args
        he.execute(dummySender, "garply", "garply", new String[] { "-fo", "blah", "-t", "garply" });
        Assert.assertEquals("have flag with option = blah\ngarply\n", out.toString()); out.delete(0, out.length());

        he.execute(dummySender, "garply", "garply", new String[] { "-o", "blah", "-tf", "garply" });
        Assert.assertEquals("have flag with option = blah\ngarply\n", out.toString()); out.delete(0, out.length());

        good = false;
        try {
            he.execute(dummySender, "garply", "garply", new String[] { "-oft", "blah" });
        }
        catch (MissingValueException e) {
            good = e.getOptionMetaData().getName().equals("-t");
        }
        Assert.assertTrue(good);

        he.execute(dummySender, "garply", "garply", new String[] { "-oft", "blah", "garply" });
        Assert.assertEquals("have flag with option = blah\ngarply\n", out.toString()); out.delete(0, out.length());

        he.execute(dummySender, "garply", "garply", new String[] { "-tfo", "blah", "garply" });
        Assert.assertEquals("have flag with option = garply\nblah\n", out.toString()); out.delete(0, out.length());
    }

    @Test
    public void testTabCompletion() throws Throwable {
        TypeCompleter myTypeCompleter = new TypeCompleter() {
            @Override
            public List<String> complete(Class<?> clazz, String arg, CommandSender sender, String partial) {
                if (clazz == String.class) {
                    if (StringUtil.startsWithIgnoreCase("ZerothAngel", partial)) {
                        return Collections.singletonList("ZerothAngel");
                    }
                }
                return Collections.emptyList();
            }
        };

        Map<String, TypeCompleter> typeCompleterRegistry = Collections.singletonMap("myCompleter", myTypeCompleter);

        // No further args
        testCompletions(he.getTabCompletions(dummySender, "hello", "hello", new String[] { "" }, null, null, typeCompleterRegistry));

        // Flag
        testCompletions(he.getTabCompletions(dummySender, "hello", "hello", new String[] { "-" }, null, null, typeCompleterRegistry),
                "--", "-f");

        // Custom TestCompleter, empty query
        testCompletions(he.getTabCompletions(dummySender, "greet", "greet", new String[] { "" }, null, null, typeCompleterRegistry),
                "ZerothAngel");

        // Custom TestCompleter, query doesn't match
        testCompletions(he.getTabCompletions(dummySender, "greet", "greet", new String[] { "a" }, null, null, typeCompleterRegistry));

        // Custom TestCompleter, query match
        testCompletions(he.getTabCompletions(dummySender, "greet", "greet", new String[] { "zero" }, null, null, typeCompleterRegistry),
                "ZerothAngel");

        // Flag
        testCompletions(he.getTabCompletions(dummySender, "greet", "greet", new String[] { "-" }, null, null, typeCompleterRegistry),
                "--", "-o");

        // Flag value
        testCompletions(he.getTabCompletions(dummySender, "greet", "greet", new String[] { "-o", "" }, null, null, typeCompleterRegistry),
                "<value>");
        
        // Flag after positional
        testCompletions(he.getTabCompletions(dummySender, "greet", "greet", new String[] { "ZerothAngel", "-" }, null, null, typeCompleterRegistry));

        // Sub-command argument
        testCompletions(he.getTabCompletions(dummySender, "bar", "bar", new String[] { "" }, null, null, typeCompleterRegistry),
                "<name>");
        
        // Sub-command itself
        testCompletions(he.getTabCompletions(dummySender, "bar", "bar", new String[] { "foo", "" }, null, null, typeCompleterRegistry),
                "greet");
        testCompletions(he.getTabCompletions(dummySender, "bar", "bar", new String[] { "foo", "gr" }, null, null, typeCompleterRegistry),
                "greet");
        testCompletions(he.getTabCompletions(dummySender, "bar", "bar", new String[] { "foo", "b" }, null, null, typeCompleterRegistry));
        
        // Sub-command
        testCompletions(he.getTabCompletions(dummySender, "bar", "bar", new String[] { "foo", "greet", "" }, null, null, typeCompleterRegistry));

        // Sub-command flag
        testCompletions(he.getTabCompletions(dummySender, "bar", "bar", new String[] { "foo", "greet", "-" }, null, null, typeCompleterRegistry),
                "--", "-o");
        
        // Sub-command flag value
        testCompletions(he.getTabCompletions(dummySender, "bar", "bar", new String[] { "foo", "greet", "-o", "" }, null, null, typeCompleterRegistry),
                "<option>");
        
        // Varargs
        testCompletions(he.getTabCompletions(dummySender, "say", "say", new String[] { "" }, null, null, typeCompleterRegistry),
                "ZerothAngel");
        testCompletions(he.getTabCompletions(dummySender, "say", "say", new String[] { "a" }, null, null, typeCompleterRegistry));
        testCompletions(he.getTabCompletions(dummySender, "say", "say", new String[] { "ZerothAngel", "" }, null, null, typeCompleterRegistry),
                "ZerothAngel");
    }

    private void testCompletions(List<String> actual, String... expected) throws Throwable {
        Set<String> actualSet = new HashSet<String>(actual);
        Set<String> expectedSet = new HashSet<String>(Arrays.asList(expected));
        Assert.assertEquals(expectedSet, actualSet);
    }

}
