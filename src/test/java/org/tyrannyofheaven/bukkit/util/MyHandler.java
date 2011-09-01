package org.tyrannyofheaven.bukkit.util;

import org.bukkit.command.CommandSender;
import org.tyrannyofheaven.bukkit.util.command.Command;
import org.tyrannyofheaven.bukkit.util.command.Option;
import org.tyrannyofheaven.bukkit.util.command.Require;
import org.tyrannyofheaven.bukkit.util.command.Rest;
import org.tyrannyofheaven.bukkit.util.command.SubCommand;

public class MyHandler {

    private final FooHandler fooHandler = new FooHandler();

    @Command({"hello", "greetings"})
    public void hello(CommandSender sender, @Option("-f") boolean flag) {
        sender.sendMessage("Hello World!");
        if (flag)
            sender.sendMessage("With flag!");
    }

    @Command("greet")
    public void greet(CommandSender sender, @Option("name") String name, @Option("-o") String opt) {
        sender.sendMessage("Hello, " + name);
        if (opt != null)
            sender.sendMessage("With option = " + opt + "!");
    }

    @Command("say")
    public void say(CommandSender sender, @Rest String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i < (args.length - 1))
                sb.append(' ');
        }
        sender.sendMessage(sb.toString());
    }

    @SubCommand("foo")
    public FooHandler foo() {
        return fooHandler;
    }

    // Sub-command handler
    public static class FooHandler {
        
        @Command("hello")
        public void hello(CommandSender sender) {
            sender.sendMessage("Hello from the foo sub-command!");
        }

    }

    @Command("secret")
    @Require("foo.secret")
    public void secret(CommandSender sender) {
        sender.sendMessage("Spike has a crush on Rarity");
    }

}
