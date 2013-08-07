package io.github.basicmark.ems;

import io.github.basicmark.util.command.CommandProcessor;
import io.github.basicmark.util.command.CommandRunner;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EMSCommands {
		CommandProcessor cmdProc;
		EMSManager manager;

	public EMSCommands(EMSManager manager) {
		this.manager = manager;
		cmdProc = new CommandProcessor("EMS");

		// Arena management functions
		cmdProc.add("create",
					new EMSCreate(),
					1,
					"<arena name> :- Create a new arena",
					true,
					"ems.editarena");
		cmdProc.add("delete",
					new EMSDelete(),
					1,
					"<arena name> :- Delete an existing arena",
					true,
					"ems.editarena");
		cmdProc.add("open",
					new EMSEnableArena(),
					1,
					"<arena name> :- Enable an arena",
					false,
					"ems.enable");
		cmdProc.add("close",
					new EMSDisableArena(),
					1,
					"<arena name> :- Disable an arena",
					false,
					"ems.enable");
		
		cmdProc.add("reload",
					new EMSReloadConfigs(),
					0,
					":- reload all arena configs",
					false,
					"ems.editarena");

		// Arena modification functions
		cmdProc.add("start-edit",
					new EMSOpenArenaForEdit(),
					1,
					"<arena name> :- Open an arena editing session",
					true,
					"ems.editarena");
		cmdProc.add("set-lobby",
					new EMSSetLobby(),
					0,
					":- Set the lobby of the arena",
					true,
					"ems.editarena");
		cmdProc.add("set-lobby-respawn",
					new EMSSetLobbyRespawn(),
					1,
					"<true/false> :- Respawn a player in the lobby if they die there",
					true,
					"ems.editarena");
		cmdProc.add("set-team-lobby-required",
					new EMSSetTeamLobbyRequired(),
					1,
					"<true/false> :- Are team lobbys required for this arena?",
					true,
					"ems.editarena");
		cmdProc.add("set-team-spawn-required",
					new EMSSetTeamspawnRequired(),
					1,
					"<true/false> :- Are team spawn points required for this arena?",
					true,
					"ems.editarena");
		// Arena team modification functions
		cmdProc.add("add-team",
					new EMSAddTeam(),
					1,
					2,
					"<team display name> :- Add a new team to the arena",
					true,
					"ems.editarena");
		cmdProc.add("remove-team",
					new EMSRemoveTeam(),
					1,
					"<team name> :- Remove a team",
					true,
					"ems.editarena");
		cmdProc.add("set-team-lobby",
					new EMSSetTeamLobby(),
					1,
					"<team name> :- Set the lobby for the specified team",
					true,
					"ems.editarena");
		cmdProc.add("add-team-spawn",
					new EMSAddTeamSpawn(),
					1,
					"<team name> :- Add a spawn point for the specified team",
					true,
					"ems.editarena");
		cmdProc.add("clear-team-spawn", 
					new EMSClearTeamSpawn(),
					1,
					"<team name> :- Clear all spawn points for the specified team",
					true,
					"ems.editarena");
		cmdProc.add("set-team-spawn-method",
					new EMSSetTeamSpawnMethod(),
					2,
					"<team name> <spawn method> :- Set the spawn method for the specified team",
					true,
					"ems.editarena");
		cmdProc.add("set-team-cap",
					new EMSSetTeamCap(),
					2,
					"<team name> <cap> :- Set a cap on the numner of players that can join the specified team",
					true,
					"ems.editarena");
		cmdProc.add("set-save-inventory",
					new EMSSetSaveInventory(),
					1,
					"<true/false> :- Set if inventory should be saved and restored for this arena (default true)",
					true,
					"ems.editarena");
		cmdProc.add("set-save-xp",
					new EMSSetSaveXP(),
					1,
					"<true/false> :- Set if XP should be saved and restored for this arena (default true)",
					true,
					"ems.editarena");
		cmdProc.add("set-save-health",
					new EMSSetSaveHealth(),
					1,
					"<true/false> :- Set if health and hunger should be saved and restored for this arena (default true)",
					true,
					"ems.editarena");
		cmdProc.add("set-keep-inv-after-event",
					new EMSSetKeepInvAfterEvent(),
					1,
					"<true/false> :- Set if players inventory should be kept when the event is over (when they re-enter the lobby) (default false)",
					true,
					"ems.editarena");
		cmdProc.add("set-keep-inv-after-death",
					new EMSSetKeepInvAfterDeath(),
					1,
					"<true/false> :- Set if players inventory should be kept when they die (default false)",
					true,
					"ems.editarena");
		cmdProc.add("list-events",
					new EMSListEvents(),
					0,
					":- List the events in the arena your editing",
					true,
					"ems.editarena");
		cmdProc.add("remove-event",
					new EMSRemoveEvent(),
					1,
					":- Remove the specified event from the arena your editing",
					true,
					"ems.editarena");
		cmdProc.add("add-message",
					new EMSAddMessage(),
					1,
					50,
					"<trigger> :- Add a message",
					true,
					"ems.editarena");
		cmdProc.add("add-timer",
					new EMSAddTimer(),
					3,
					50,
					"<trigger> <created event> <list,of,times,in,min> [event name] :- Add a timer",
					true,
					"ems.editarena");
		cmdProc.add("add-event-block",
					new EMSAddEventBlock(),
					1,
					"<trigger> :- Add an event block (iron or redstone block)",
					true,
					"ems.editarena");
		cmdProc.add("add-potion-effect",
					new EMSAddPotionEffect(),
					3,
					4,
					"<trigger> <effect> <duration in seconds> [amplifier] :- Add a potion effect",
					true,
					"ems.editarena");
		cmdProc.add("wand",
					new EMSWand(),
					0,
					":- Give the player the EMS Wand for selecting regions",
					true,
					"ems.editarena");
		cmdProc.add("add-clear-region",
					new EMSAddClearRegion(),
					1,
					"<trigger> :- Add a region to be cleared (as selected by the stick wand)",
					true,
					"ems.editarena");
		cmdProc.add("add-teleport",
					new EMSAddTeleport(),
					1,
					"<trigger> :- Add a teleport event which will teleport all active players to your current location",
					true,
					"ems.editarena");
		cmdProc.add("add-entity-spawn",
					new EMSAddEntitySpawn(),
					3,
					"<trigger> <mob name> <num>:- Add a mob spawn event",
					true,
					"ems.editarena");
		cmdProc.add("add-check-teamplayer-count",
					new EMSAddCheckTeamPlayerCount(),
					4,
					"<team/player> <trigger> <count> <created event>:- Add a check for the number of teams/players remaining",
					true,
					"ems.editarena");		
		cmdProc.add("add-lightning-effect",
					new EMSAddLightningEffect(),
					1,
					"<trigger> :- Add lightning effect at your current location)",
					true,
					"ems.editarena");
		cmdProc.add("end-edit",
					new EMSCloseArenaForEdit(),
					0,
					":- Close the editing session your currently active on",
					true,
					"ems.editarena");

		// Host commands
		cmdProc.add("force-cap",
					new EMSForceCapSize(),
					2,
					"<arena name> <cap> :- Force all teams to be capped to cap",
					true,
					"ems.host");
		cmdProc.add("start",
					new EMSStartArena(),
					1,
					"<arena name> :- Start the event in the specified arena",
					true,
					"ems.host");		
		cmdProc.add("start-tracking",
					new EMSStartArenaTracking(),
					1,
					"<arena name> :- Start tracking in the specified arena",
					true,
					"ems.host");
		cmdProc.add("end-tracking",
					new EMSEndArenaTracking(),
					1,
					"<arena name> :- End tracking in the specified arena",
					true,
					"ems.host");
		cmdProc.add("end",
					new EMSEndArena(),
					1,
					"<arena name> :- End the event in the specified arena",
					true,
					"ems.host");
		
		// Use commands or signs?
		cmdProc.add("join",
					new EMSJoinArena(),
					1,
					"<arena name> :- Join the specified arena",
					true,
					"ems.play");
		cmdProc.add("leave",
					new EMSLeaveArena(),
					0,
					":- Leave the arena your currently in",
					true,
					"ems.play");
		cmdProc.add("players",
					new EMSArenaPlayers(),
					1,
					"<arena name> :- Show the specified arena",
					true,
					"ems.info");
	}
	
	public boolean processCommand(CommandSender sender, String[] args) {
		return cmdProc.run(sender, args);
	}

    public class EMSCreate implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String arenaName = args[0];
    		
    		return manager.arenaCreate(player, arenaName);
    	}
    }
    
    public class EMSOpenArenaForEdit implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String arenaName = args[0];

    		return manager.openForEdit(player, arenaName);
    	}
    }
   
    public class EMSCloseArenaForEdit implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;

    		return manager.closeForEdit(player);
    	}
    }
    
    public class EMSEnableArena implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {   		
    		String arenaName = args[0];

    		return manager.arenaEnable(sender, arenaName);
    	}
    }
    
    public class EMSDisableArena implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {  		
    		String arenaName = args[0];

    		return manager.arenaDisable(sender, arenaName);
    	}
    }
    
    public class EMSDelete implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		String arenaName = args[0];

    		return manager.arenaDelete(sender, arenaName);
    	}
    }

	// Arena management functions
	public class EMSSetLobby implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;

    		return manager.arenaSetLobby(player);
    	}
    }
	
	public class EMSSetLobbyRespawn implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String respawnString = args[0];

    		return manager.arenaSetLobbyRespawn(player, respawnString);
    	}
    }
		
	public class EMSSetTeamLobbyRequired implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String requiredString = args[0];
    		Boolean required;

    		if (requiredString.toLowerCase().equals("true")) {
    			required = true;
    		} else if (requiredString.toLowerCase().equals("false")) {
    			required = false;
    		} else {
    			sender.sendMessage(ChatColor.RED + "[EMS] Expecting true or false but got " + requiredString);
    			return true;
    		}
    		return manager.arenaSetTeamLobbyRequired(player, required);
    	}
    }

	public class EMSSetTeamspawnRequired implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {   		
    		Player player = (Player) sender;
    		String requiredString = args[0];
    		Boolean required;

    		if (requiredString.toLowerCase().equals("true")) {
    			required = true;
    		} else if (requiredString.toLowerCase().equals("false")) {
    			required = false;
    		} else {
    			sender.sendMessage(ChatColor.RED + "[EMS] Expecting true or false but got " + requiredString);
    			return true;
    		}
    		
    		return manager.arenaSetTeamSpawnRequired(player, required);
    	}
    }
	
	public class EMSAddTeam implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {   		
    		Player player = (Player) sender;
    		String name = args[0];
    		String displayName;

    		if (args.length == 2) {
    			displayName = args[1];
    		} else {
    			displayName = name;
    		}

    		return manager.arenaAddTeam(player, name, displayName);
    	}
    }

	public class EMSRemoveTeam implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String teamName = args[0];

    		return manager.arenaRemoveTeam(player, teamName);
    	}
    }

	public class EMSSetTeamLobby implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {		
    		Player player = (Player) sender;
    		String teamName = args[0];

    		return manager.arenaSetTeamLobby(player, teamName);
    	}
    }

	public class EMSAddTeamSpawn implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String teamName = args[0];
    		
    		return manager.arenaAddTeamSpawn(player, teamName);
    	}
    }

	public class EMSClearTeamSpawn implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String teamName = args[0];
    		
    		return manager.arenaClearTeamSpawn(player, teamName);
    	}
    }
	
	public class EMSSetTeamSpawnMethod implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String teamName = args[0];
    		String method = args[1];
    		
    		return manager.arenaSetTeamSpawnMethod(player, teamName, method);
    	}
    }

	public class EMSSetTeamCap implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String teamName = args[0];
    		String capSize = args[1];
    		
    		return manager.arenaSetTeamCap(player, teamName, capSize);
    	}
    }
	
	public class EMSSetSaveInventory implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String saveString = args[0];
    		
    		return manager.arenaSetSaveInventory(player, saveString);
    	}
    }
	
	public class EMSSetSaveXP implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String saveString = args[0];
    		
    		return manager.arenaSetSaveXP(player, saveString);
    	}
    }
	
	public class EMSSetSaveHealth implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String saveString = args[0];
    		
    		return manager.arenaSetSaveHealth(player, saveString);
    	}
    }
	
	public class EMSSetKeepInvAfterEvent implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String keepString = args[0];
    		
    		return manager.arenaSetKeepInvAfterEvent(player, keepString);
    	}
    }
	
	public class EMSSetKeepInvAfterDeath implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String keepString = args[0];
    		
    		return manager.arenaSetKeepInvAfterDeath(player, keepString);
    	}
    }
	
	
	public class EMSListEvents implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;

    		return manager.arenaListEvents(player);
    	}
    }

	public class EMSRemoveEvent implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String event = args[0];

    		return manager.arenaRemoveEvent(player, event);
    	}
    }
	
	public class EMSAddMessage implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String trigger = args[0];
    		String message = args[1];

    		// The message will have spaces which get split so rejoin the string
    		for (int i = 2;i<args.length;i++) {
    			message = message + " " + args[i];
    		}

    		return manager.arenaAddMessage(player, trigger, message);
    	}
    }
	
	public class EMSAddTimer implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
       		String eventTrigger = args[0];
    		String createName = args[1];
    		String inSeconds =  args[2];
    		String timeString = args[3];
    		String displayName = null;
    		
    		if (args.length >= 5) {
    			displayName = args[4];
    			for (int i = 5;i< args.length;i++) {
    				displayName = displayName + " " + args[i];
    			}
    		}
    		
    		return manager.arenaAddTimer(player, eventTrigger, createName, inSeconds, timeString, displayName);
    	}
	}

	public class EMSAddPotionEffect implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
       		String eventTrigger = args[0];
       		String effectName = args[1];
       		String duration = args[2];
       		String amplifier;
       		if (args.length > 3) {
       			amplifier = args[3];
       		} else {
       			amplifier = "0";
       		}
    		
    		return manager.arenaAddPotionEffect(player, eventTrigger, effectName, duration, amplifier);
    	}
	}
	
	public class EMSAddEventBlock implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
       		String eventTrigger = args[0];
    		
    		return manager.arenaAddEventBlock(player, eventTrigger);
    	}
	}

	public class EMSWand implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;

    		return manager.giveWand(player);
    	}
	}
    
	
    public class EMSAddClearRegion implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String eventTrigger = args[0];

    		return manager.arenaAddClearRegion(player, eventTrigger);
    	}
    }

    public class EMSAddTeleport implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String eventTrigger = args[0];

    		return manager.arenaAddTeleport(player, eventTrigger);
    	}
    }
    
    public class EMSAddEntitySpawn implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String eventTrigger = args[0];
    		String mobName =  args[1];
    		String mobCount =  args[2];

    		return manager.arenaAddEntitySpawn(player, eventTrigger, mobName, mobCount);
    	}
    }
    
    public class EMSAddCheckTeamPlayerCount implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String teamPlayer = args[0];
    		String eventTrigger = args[1];
    		String count =  args[2];
    		String createEvent = args[3];

    		return manager.arenaAddCheckTeamPlayerCount(player, teamPlayer, eventTrigger, count, createEvent);
    	}
    }
    
    public class EMSAddLightningEffect implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String eventTrigger = args[0];

    		return manager.arenaAddLightningEffect(player, eventTrigger);
    	}
    }
	
	// Player functions
	public class EMSJoinArena implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;
    		String arenaName = args[0];

    		return manager.arenaJoin(player, arenaName);
    	}
    }

	public class EMSLeaveArena implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		Player player = (Player) sender;

    		return manager.arenaLeave(player);
    	}
    }
	
	public class EMSArenaPlayers implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		String arenaName = args[0];

    		return manager.arenaGetPlayers(sender, arenaName);
    	}
    }
	
	// Event host functions
	public class EMSForceCapSize implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		String arenaName = args[0];
    		String capSize = args[1];

    		return manager.arenaForceCapSize(sender, arenaName, capSize);
    	}
    }
	
	public class EMSStartArena implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		String arenaName = args[0];

    		return manager.arenaStart(sender, arenaName);
    	}
    }

	public class EMSStartArenaTracking implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		String arenaName = args[0];

    		return manager.arenaStartTracking(sender, arenaName);
    	}
    }
	
	public class EMSEndArenaTracking implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		String arenaName = args[0];

    		return manager.arenaEndTracking(sender, arenaName);
    	}
    }

	public class EMSEndArena implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		String arenaName = args[0];

    		return manager.arenaEnd(sender, arenaName);
    	}
    }

	public class EMSReloadConfigs implements CommandRunner {
    	public boolean run(CommandSender sender, String args[]) {
    		return manager.reloadConfigs();
    	}
    }
}


