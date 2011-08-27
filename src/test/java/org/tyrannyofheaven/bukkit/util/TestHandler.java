package org.tyrannyofheaven.bukkit.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.tyrannyofheaven.bukkit.util.command.Command;
import org.tyrannyofheaven.bukkit.util.command.Option;

public class TestHandler {

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

}
