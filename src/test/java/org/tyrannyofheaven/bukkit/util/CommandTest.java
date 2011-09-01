package org.tyrannyofheaven.bukkit.util;

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
                return false;
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
        Assert.assertTrue(ce.execute(dummySender, "hello", new String[] {}));
        Assert.assertEquals("Hello World!\n", out.toString()); out.delete(0, out.length());

        Assert.assertTrue(ce.execute(dummySender, "hello", new String[] { "-f" }));
        Assert.assertEquals("Hello World!\nWith flag!\n", out.toString()); out.delete(0, out.length());

        Assert.assertTrue(ce.execute(dummySender, "greetings", new String[] {}));
        Assert.assertEquals("Hello World!\n", out.toString()); out.delete(0, out.length());

        Assert.assertTrue(ce.execute(dummySender, "greetings", new String[] { "-f" }));
        Assert.assertEquals("Hello World!\nWith flag!\n", out.toString()); out.delete(0, out.length());
        
        // Required positional param, flag with value
        Assert.assertFalse(ce.execute(dummySender, "greet", new String[] { }));
        Assert.assertFalse(ce.execute(dummySender, "greet", new String[] { "-o" }));
        Assert.assertFalse(ce.execute(dummySender, "greet", new String[] { "-o", "foo" }));

        Assert.assertTrue(ce.execute(dummySender, "greet", new String[] { "-o", "foo", "bar" }));
        Assert.assertEquals("Hello, bar\nWith option = foo!\n", out.toString()); out.delete(0, out.length());

        Assert.assertTrue(ce.execute(dummySender, "greet", new String[] { "-o", "foo", "bar", "garply" }));
        Assert.assertEquals("Hello, bar\nWith option = foo!\n", out.toString()); out.delete(0, out.length());
        
        // @Rest
        Assert.assertTrue(ce.execute(dummySender, "say", new String[] { "Hello", "there" }));
        Assert.assertEquals("Hello there\n", out.toString()); out.delete(0, out.length());
        
        // @SubCommand
        Assert.assertFalse(ce.execute(dummySender, "foo", new String[] { }));

        Assert.assertTrue(ce.execute(dummySender, "foo", new String[] { "hello" }));
        Assert.assertEquals("Hello from the foo sub-command!\n", out.toString()); out.delete(0, out.length());
    }

}
