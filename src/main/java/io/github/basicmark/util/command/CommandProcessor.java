package io.github.basicmark.util.command;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandProcessor {
	HashMap<String, commandData> commands;
	String prefix;
	String prefixString;
	
	public CommandProcessor(String prefix) {
		this.prefix = prefix;
		this.prefixString = "[" + prefix + "] ";
		commands = new HashMap<String, commandData>();
		
		add("help", new CommandProcessorHelp(), 0, "This help page", false, null);
	}

	public void add(String commandName, CommandRunner commandFunction, int argCount, String help, boolean mustBePlayer, String permissionNode) {
		commandData newCmd = new commandData(commandFunction, argCount, argCount, help, mustBePlayer, permissionNode);
		commands.put(commandName, newCmd);
	}

	public void add(String commandName, CommandRunner commandFunction, int argMin, int argMax, String help, boolean mustBePlayer, String permissionNode) {
		commandData newCmd = new commandData(commandFunction, argMin, argMax, help, mustBePlayer, permissionNode);
		commands.put(commandName, newCmd);
	}
	
	public boolean run(CommandSender sender, String args[]) {
		// Do we know this (sub)command?
		if (args.length == 0) {
			return false;
		}
		
		commandData cmdData = commands.get(args[0]);
		if (cmdData == null) {
			return false;
		}

		// We do so run it
		String[] cmdArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
	
		if ((cmdArgs.length < cmdData.argMin) || (cmdArgs.length > cmdData.argMax)) {
			sender.sendMessage(ChatColor.RED + prefixString + "Got " + args.length + " arguments, expecting " + cmdData.argMin + " - " + cmdData.argMax);
			sender.sendMessage(ChatColor.RED + prefixString + "Expected " + cmdData.help);
			return true;
		}

		if (cmdData.mustBePlayer) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + prefixString + "This command must be issued from a player");
				return true;
			}
		}
		
		if (cmdData.permissionNode != null) {
			if (!sender.hasPermission(cmdData.permissionNode)) {
				sender.sendMessage(ChatColor.RED + prefixString + "You don't have permission to run this command.");
				return true;
			}
		}
		
		return cmdData.commandFunction.run(sender, cmdArgs);
	}
	
	public class commandData {
		private CommandRunner commandFunction;
		private int argMin;
		private int argMax;
		private String help;
		private boolean mustBePlayer;
		private String permissionNode;
		
		public commandData(CommandRunner commandFunction, int argMin, int argMax, String help, boolean mustBePlayer, String permissionNode) {
			this.commandFunction = commandFunction;
			this.argMin = argMin;
			this.argMax = argMax;
			this.help = help;
			this.mustBePlayer = mustBePlayer;
			this.permissionNode = permissionNode;
		}
	}
	
	public class CommandProcessorHelp implements CommandRunner {
		public boolean run(CommandSender sender, String[] args) {
			Iterator<String> i = commands.keySet().iterator();

			sender.sendMessage(ChatColor.GREEN + prefix + " - help page");
			while (i.hasNext()) {
				String cmdName = i.next();
				commandData cmdData = commands.get(cmdName);
				// Only show the commands the player/console can run
				if (sender instanceof Player) {
					if (cmdData.permissionNode != null) {
						if (sender.hasPermission(cmdData.permissionNode)) {
							sender.sendMessage(ChatColor.GREEN + "/" + prefix.toLowerCase() + " " + cmdName + " " + cmdData.help);
						}
					}
				} else {
					if (!cmdData.mustBePlayer) {
						sender.sendMessage(ChatColor.GREEN + "/" + prefix.toLowerCase() + " " + cmdName + " " + cmdData.help);
					}
				}
					
			}
			return true;
		}
	}
}
