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
import org.tyrannyofheaven.bukkit.util.command.TOHCommandExecutor;

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
        CommandSender dummySender = new CommandSender() {
            @Override
            public Server getServer() {
                return null;
            }
            @Override
            public void sendMessage(String message) {
                System.out.println(message);
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
        TOHCommandExecutor ce = new TOHCommandExecutor(dummyPlugin, new TestHandler());
        
        // No positional params, boolean flag
        Assert.assertTrue(ce.onCommand(dummySender, null, "hello", new String[] {}));
        Assert.assertTrue(ce.onCommand(dummySender, null, "hello", new String[] { "-f" }));
        Assert.assertTrue(ce.onCommand(dummySender, null, "greetings", new String[] {}));
        Assert.assertTrue(ce.onCommand(dummySender, null, "greetings", new String[] { "-f" }));
        
        // Required positional param, flag with value
        Assert.assertFalse(ce.onCommand(dummySender, null, "greet", new String[] { }));
        Assert.assertFalse(ce.onCommand(dummySender, null, "greet", new String[] { "-o" }));
        Assert.assertFalse(ce.onCommand(dummySender, null, "greet", new String[] { "-o", "foo" }));
        Assert.assertTrue(ce.onCommand(dummySender, null, "greet", new String[] { "-o", "foo", "bar" }));
        Assert.assertTrue(ce.onCommand(dummySender, null, "greet", new String[] { "-o", "foo", "bar", "garply" }));
        
        // @Rest
        Assert.assertTrue(ce.onCommand(dummySender, null, "say", new String[] { "Hello", "there" }));
    }

}
