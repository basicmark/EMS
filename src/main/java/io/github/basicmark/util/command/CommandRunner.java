package io.github.basicmark.util.command;

import org.bukkit.command.CommandSender;

public interface CommandRunner {
	boolean run(CommandSender sender, String[] args);
}
