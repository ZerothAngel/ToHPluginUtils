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
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Test;
import org.tyrannyofheaven.bukkit.util.command.HandlerExecutor;
import org.tyrannyofheaven.bukkit.util.command.ParseException;
import org.tyrannyofheaven.bukkit.util.permissions.PermissionException;

public class CommandTest {

    @Test
    public void testMetaData() throws Exception {
        // Some mock objects
        Plugin dummyPlugin = new JavaPlugin() {
            @Override
            public void onDisable() {
            }
            @Override
            public void onEnable() {
            }
        };

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
        };
        HandlerExecutor ce = new HandlerExecutor(dummyPlugin, new MyHandler());
        
        // No positional params, boolean flag
        Assert.assertTrue(ce.execute(dummySender, "hello", new String[0]));
        Assert.assertEquals("Hello World!\n", out.toString()); out.delete(0, out.length());

        Assert.assertTrue(ce.execute(dummySender, "hello", new String[] { "-f" }));
        Assert.assertEquals("Hello World!\nWith flag!\n", out.toString()); out.delete(0, out.length());

        Assert.assertTrue(ce.execute(dummySender, "greetings", new String[0]));
        Assert.assertEquals("Hello World!\n", out.toString()); out.delete(0, out.length());

        Assert.assertTrue(ce.execute(dummySender, "greetings", new String[] { "-f" }));
        Assert.assertEquals("Hello World!\nWith flag!\n", out.toString()); out.delete(0, out.length());
        
        // Required positional param, flag with value
        boolean good = false;
        try { ce.execute(dummySender, "greet", new String[0]); } catch (ParseException e) { good = true; }
        Assert.assertTrue(good);

        good = false;
        try { ce.execute(dummySender, "greet", new String[] { "-o" }); } catch (ParseException e) { good = true; }
        Assert.assertTrue(good);

        good = false;
        try { ce.execute(dummySender, "greet", new String[] { "-o", "foo" }); } catch (ParseException e) { good = true; }
        Assert.assertTrue(good);

        Assert.assertTrue(ce.execute(dummySender, "greet", new String[] { "-o", "foo", "bar" }));
        Assert.assertEquals("Hello, bar\nWith option = foo!\n", out.toString()); out.delete(0, out.length());

        Assert.assertTrue(ce.execute(dummySender, "greet", new String[] { "-o", "foo", "bar", "garply" }));
        Assert.assertEquals("Hello, bar\nWith option = foo!\n", out.toString()); out.delete(0, out.length());

        // Positional argument that starts with -
        Assert.assertTrue(ce.execute(dummySender, "greet", new String[] { "--", "-garply" }));
        Assert.assertEquals("Hello, -garply\n", out.toString()); out.delete(0, out.length());

        // @Rest
        Assert.assertTrue(ce.execute(dummySender, "say", new String[] { "Hello", "there" }));
        Assert.assertEquals("Hello there\n", out.toString()); out.delete(0, out.length());
        
        // No flags, so should not parse -Hello as one.
        Assert.assertTrue(ce.execute(dummySender, "say", new String[] { "-Hello", "there" }));
        Assert.assertEquals("-Hello there\n", out.toString()); out.delete(0, out.length());

        // @SubCommand
        Assert.assertFalse(ce.execute(dummySender, "foo", new String[0]));

        Assert.assertTrue(ce.execute(dummySender, "foo", new String[] { "hello" }));
        Assert.assertEquals("Hello from the foo sub-command!\n", out.toString()); out.delete(0, out.length());
        
        good = false;
        permissions.clear();
        try {
            ce.execute(dummySender, "secret", new String[0]);
        }
        catch (PermissionException e) {
            good = true;
        }
        Assert.assertTrue(good);
        
        permissions.add("foo.secret");
        Assert.assertTrue(ce.execute(dummySender, "secret", new String[0]));
        Assert.assertEquals("Spike has a crush on Rarity\n", out.toString()); out.delete(0, out.length());
    }

}
