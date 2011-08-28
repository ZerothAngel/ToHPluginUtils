package org.tyrannyofheaven.bukkit.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.tyrannyofheaven.bukkit.util.command.Command;
import org.tyrannyofheaven.bukkit.util.command.Option;
import org.tyrannyofheaven.bukkit.util.command.Rest;
import org.tyrannyofheaven.bukkit.util.command.SubCommand;

public class TestHandler {

    private final FooHandler fooHandler = new FooHandler();

    @Command({"hello", "greetings"})
    public void hello(CommandSender sender, @Option("-f") boolean flag) {
        sender.sendMessage(ChatColor.GREEN + "Hello World!");
        if (flag)
            sender.sendMessage(ChatColor.YELLOW + "With flag!");
    }

    @Command("greet")
    public void greet(CommandSender sender, @Option("name") String name, @Option("-o") String opt) {
        sender.sendMessage(ChatColor.GREEN + "Hello, " + name);
        if (opt != null)
            sender.sendMessage(ChatColor.YELLOW + "With option = " + opt + "!");
    }

    @Command("say")
    public void say(CommandSender sender, @Rest String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i < (args.length - 1))
                sb.append(' ');
        }
        sender.sendMessage(ChatColor.GREEN + sb.toString());
    }

    @SubCommand("foo")
    public FooHandler foo() {
        return fooHandler;
    }

    // Sub-command handler
    public static class FooHandler {
        
        @Command("hello")
        public void hello(CommandSender sender) {
            sender.sendMessage(ChatColor.GREEN + "Hello from the foo sub-command!");
        }

    }

}
