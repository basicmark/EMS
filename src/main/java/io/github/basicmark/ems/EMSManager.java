package io.github.basicmark.ems;

import io.github.basicmark.config.PlayerState;
import io.github.basicmark.config.ReferenceInventory;
import io.github.basicmark.ems.arenaevents.EMSAutoEnd;
import io.github.basicmark.ems.arenaevents.EMSCheckTeamPlayerCount;
import io.github.basicmark.ems.arenaevents.EMSClearRegion;
import io.github.basicmark.ems.arenaevents.EMSEventBlock;
import io.github.basicmark.ems.arenaevents.EMSFillRegion;
import io.github.basicmark.ems.arenaevents.EMSLightningEffect;
import io.github.basicmark.ems.arenaevents.EMSMessenger;
import io.github.basicmark.ems.arenaevents.EMSPotionEffect;
import io.github.basicmark.ems.arenaevents.EMSSpawnEntity;
import io.github.basicmark.ems.arenaevents.EMSTeleport;
import io.github.basicmark.ems.arenaevents.EMSTimer;
import io.github.basicmark.ems.arenaevents.EMSTracker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

public class EMSManager {
	static {
		ConfigurationSerialization.registerClass(EMSArena.class);
		ConfigurationSerialization.registerClass(EMSTeam.class);
		ConfigurationSerialization.registerClass(EMSTimer.class);
		ConfigurationSerialization.registerClass(EMSTracker.class);
		ConfigurationSerialization.registerClass(EMSEventBlock.class);
		ConfigurationSerialization.registerClass(EMSPotionEffect.class);
		ConfigurationSerialization.registerClass(EMSMessenger.class);
		ConfigurationSerialization.registerClass(EMSClearRegion.class);
		ConfigurationSerialization.registerClass(EMSTeleport.class);
		ConfigurationSerialization.registerClass(EMSSpawnEntity.class);
		ConfigurationSerialization.registerClass(PlayerState.class);
		ConfigurationSerialization.registerClass(EMSAutoEnd.class);
		ConfigurationSerialization.registerClass(EMSCheckTeamPlayerCount.class);
		ConfigurationSerialization.registerClass(EMSLightningEffect.class);
		ConfigurationSerialization.registerClass(EMSPlayerRejoinData.class);
		ConfigurationSerialization.registerClass(EMSFillRegion.class);
		ConfigurationSerialization.registerClass(ReferenceInventory.class);
	}
	EMSArenaLoader loader;
	HashMap<Player, EMSEditState> arenaEditState;
	HashMap<String, EMSArena> arenas;
	EMSCommands commands;
	EMSListener listener;
	JavaPlugin plugin;

	// The manager gets created when the plugin is enabled
	public EMSManager(JavaPlugin plugin) {
		this.plugin = plugin;

		// Create the dynamic data storage
		arenaEditState = new HashMap<Player, EMSEditState>();
		arenas = new HashMap<String, EMSArena>();

		// Create the command hander
		commands = new EMSCommands(this);
		
		// Create the event hander and register it
		listener = new EMSListener(this);
		plugin.getServer().getPluginManager().registerEvents(listener, plugin);

		loader = new EMSArenaLoader(plugin.getDataFolder() + "/arenas");
		
		// Load the arenas for the worlds that are already loaded
		List<World> worlds = Bukkit.getWorlds();
		Iterator<World> i = worlds.iterator();
		while(i.hasNext()) {
			World world = i.next();
			loadWorld(world);
		}
	}

	// We're being disabled
	public void shutdown() {
		Iterator<EMSArena> i = arenas.values().iterator();

		/* Unload all the loaded arenas */
		while (i.hasNext()) {
			EMSArena arena = i.next();
			String arenaName = arena.getWorld();

			Bukkit.getLogger().info("[EMS] Unloading " + arenaName);
			arena.disable(false);
			arena.destroy();
			i.remove();
		}
	}
	
	// Command hander for the plugin
	public boolean processCommand(CommandSender sender, String[] args) {
		return commands.processCommand(sender, args);
	}
	
	// Functionality the manager exposes
	public boolean arenaCreate(Player player, String arenaName) {
		EMSEditState editState = getArenaEditState(player, false);
		if (editState == null) {
			return true;
		}
		
		if (arenas.containsKey(arenaName)) {
			player.sendMessage(ChatColor.RED + "[EMS] Arena called " + arenaName + " already exists!");
		}

		player.sendMessage(ChatColor.YELLOW + "Creating new arena " + arenaName);
		EMSArena newarena = new EMSArena(arenaName);
		newarena.setPlugin(plugin);
		newarena.setManager(this);
		arenas.put(arenaName, newarena);

		if (!editState.openArenaForEdit(newarena)) {
			player.sendMessage(ChatColor.RED + "[EMS] Faild open the arena for edit as your already editing");
		}
		return true;
	}

	public boolean openForEdit(CommandSender sender, String arenaName) {
		if (!arenas.containsKey(arenaName)) {
			sender.sendMessage(ChatColor.RED + "[EMS] An arena called " + arenaName + "does not exist");
			return true;
		}
		
		EMSArena arena = arenas.get(arenaName);
		if (arena == null) {
			sender.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting arena " + arenaName);
			return true;
		}
		
		Player player = (Player) sender;
		EMSEditState editState = getArenaEditState(player, false);
		if (editState == null) {
			return true;
		}

		if (editState.openArenaForEdit(arena)) {
			sender.sendMessage(ChatColor.GREEN + "[EMS] You now have " + arenaName + " opened for edit");
		} else {
			sender.sendMessage(ChatColor.RED + "[EMS] Someone else has " + arenaName + " opened for edit!");
		}
		return true;
	}

	public boolean closeForEdit(Player player) {	
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			return true;
		}
		/*
		 *  Get the arena before closing for edit as this will clear the arena
		 *  from the edit state
		 */
		EMSArena arena = editState.arena;

		if (editState.closeArenaForEdit()) {
			loader.save(arena);
			player.sendMessage(ChatColor.GREEN + "[EMS] Saved changes to " + arena.getName());
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Save failed, did you have an arena opened for edit?");
		}
		return true;
	}

	public boolean arenaEnable(CommandSender sender, String arenaName) {
		EMSArena arena = getArena(sender, arenaName);
		if (arena == null) {
			return true;
		}
		
		if (!arena.enable()) {
			sender.sendMessage(ChatColor.RED + "[EMS] Failed to enable arena, maybe it's setup is incomplete?");
		} else {
			sender.sendMessage(ChatColor.GREEN + "[EMS] Arena enabled");
		}
		loader.save(arena);
		return true;
	}

	public boolean arenaDisable(CommandSender sender, String arenaName) {
		EMSArena arena = getArena(sender, arenaName);
		if (arena == null) {
			return true;
		}
		
		arena.disable(true);
		sender.sendMessage(ChatColor.GREEN + "[EMS] Arena disabled");
		loader.save(arena);
		return true;
	}

	public boolean arenaDelete(CommandSender sender, String arenaName) {
		EMSArena arena = getArena(sender, arenaName);
		if (arena == null) {
			return true;
		}
		
		arenas.remove(arena.getName());
		loader.delete(arena);
		return true;
	}

	public boolean arenaSetLobby(Player player) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			return true;
		}
		
		editState.arena.setLobby(player);
		player.sendMessage(ChatColor.GREEN + "[EMS] Arena lobby set");
		return true;
	}
	
	public boolean arenaSetLobbyRespawn(Player player, String respawnString) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}
		
		editState.arena.setLobbyRespawn(Boolean.parseBoolean(respawnString));
		player.sendMessage(ChatColor.GREEN + "[EMS] Set lobby respawn to " + respawnString);
		return true;
	}

	public boolean arenaSetTeamLobbyRequired(Player player, Boolean required) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			return true;
		}

		editState.arena.setRequiresTeamLobby(required);
		player.sendMessage(ChatColor.GREEN + "[EMS] Team lobby required set to " + required);
		return true;
	}

	public boolean arenaSetTeamSpawnRequired(Player player, Boolean required) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			return true;
		}

		editState.arena.setRequiresTeamSpawn(required);
		player.sendMessage(ChatColor.GREEN + "[EMS] Team spawn required set");
		
		return true;
	}
	
	public boolean arenaSetWelcomeMessage(Player player, String message) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			return true;
		}

		editState.arena.setWelcomeMessage(message);
		player.sendMessage(ChatColor.GREEN + "[EMS] Welcome message set");
		
		return true;
	}
	
	public boolean arenaSetLeaveMessage(Player player, String message) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			return true;
		}

		editState.arena.setLeaveMessage(message);
		player.sendMessage(ChatColor.GREEN + "[EMS] Leave message set");
		
		return true;
	}
	
	public boolean arenaAddTeam(Player player, String name, String displayName) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			return true;
		}

		editState.arena.addTeam(name, displayName);
		player.sendMessage(ChatColor.GREEN + "[EMS] Created team " + name);
		return true;
	}

	public boolean arenaRemoveTeam(Player player, String teamName) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			return true;
		}

		if (editState.arena.removeTeam(teamName)) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Removed team " + teamName);
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Failed to remove team " + teamName);
		}
		
		return true;
	}

	public boolean arenaSetTeamLobby(Player player, String teamName) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			return true;
		}

		editState.arena.setTeamLobby(teamName, player);
		player.sendMessage(ChatColor.GREEN + "[EMS] Set team lobby for " + teamName);
		return true;
	}

	public boolean arenaAddTeamSpawn(Player player, String teamName) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			return true;
		}

		editState.arena.addTeamSpawn(teamName, player);
		player.sendMessage(ChatColor.GREEN + "[EMS] Add team spawn for " + teamName + " which now has " + editState.arena.getTeamSpawnCount(teamName) + " spawns");
		return true;
	}

	public boolean arenaClearTeamSpawn(Player player, String teamName) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}

		editState.arena.clearTeamSpawn(teamName);
		player.sendMessage(ChatColor.GREEN + "[EMS] All team spawns for " + teamName + " cleared");
		return true;
	}

	public boolean arenaSetTeamSpawnMethod(Player player, String teamName, String method) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}

		if (editState.arena.setTeamSpawnMethod(teamName, method)) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Spawn method for " + teamName + " set to " + method);
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Spawn method " + method + " unknown");
		}
		return true;
	}
	
	public boolean arenaSetTeamCap(Player player, String teamName, String capSize) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}

		if (editState.arena.setTeamCap(teamName, Integer.parseInt(capSize))) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Cap for " + teamName + " set to " + capSize);
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Failed to set team cap");
		}
		return true;
	}
	
	public boolean arenaSetSaveInventory(Player player, String saveString) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}

		if (editState.arena.setSaveInventory(Boolean.parseBoolean(saveString))) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Saving of inventory set to " + saveString);
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Failed to set team cap");
		}
		return true;
	}

	public boolean arenaSetSaveXP(Player player, String saveString) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}

		if (editState.arena.setSaveXP(Boolean.parseBoolean(saveString))) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Saving of XP set to " + saveString);
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Failed to set team cap");
		}
		return true;
	}
	
	public boolean arenaSetSaveHealth(Player player, String saveString) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}

		if (editState.arena.setSaveHealth(Boolean.parseBoolean(saveString))) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Saving of health set to " + saveString);
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Failed to set team cap");
		}
		return true;
	}
	
	public boolean arenaSetKeepInvAfterEvent(Player player, String saveString) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}

		if (editState.arena.setKeepInvAfterEvent(Boolean.parseBoolean(saveString))) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Keep inv after event to " + saveString);
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Failed to set keep inv after event");
		}
		return true;
	}
	
	public boolean arenaSetKeepInvAfterDeath(Player player, String saveString) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}

		if (editState.arena.setKeepInvAfterDeath(Boolean.parseBoolean(saveString))) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Keep inv after death to " + saveString);
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Failed to set keep inv after death");
		}
		return true;
	}
	
	public boolean arenaSetAutoStart(Player player, String enableString, String minPlayerString, String countdownString) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}	

		boolean enable;
		int minPlayer;
		int countdown;
		
		try {
			enable = Boolean.parseBoolean(enableString);
		} catch(Exception e) {
			player.sendMessage(ChatColor.RED + "[EMS] Expecting true/false but got " + enableString);
			return true;
		}
		
		try {
			minPlayer = Integer.parseInt(minPlayerString);
		} catch(Exception e) {
			player.sendMessage(ChatColor.RED + "[EMS] Expecting a number for min player count but got " + minPlayerString);
			return true;
		}
		
		try {
			countdown = Integer.parseInt(countdownString);
		} catch(Exception e) {
			player.sendMessage(ChatColor.RED + "[EMS] Expecting a number for countdown but got " + minPlayerString);
			return true;
		}
		
		countdown = Integer.parseInt(countdownString);
		
		if (editState.arena.arenaSetAutoStart(enable, minPlayer, countdown)) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Set auto-start config");
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Failed to set auto-start config");
		}
		return true;
	}
	
	public boolean arenaSetAllowRejoin(Player player, boolean allowRejoin) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}	

		if (editState.arena.arenaAllowRejoin(allowRejoin)) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Set allow rejoin config");
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Failed to set allow rejoin config");
		}
		return true;
	}
	
	public boolean arenaSetTimeLimit(Player player, boolean limit, int time, int perPeroid) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}	

		if (limit && (time == 0)) {
			player.sendMessage(ChatColor.RED + "[EMS] Expecting a limit time");
			return true;
		}
		if ((!limit) && (time != 0)) {
			player.sendMessage(ChatColor.RED + "[EMS] Not expecting a limit time but got one");
			return true;
		}
		
		if (editState.arena.arenaTimeLimit(time, perPeroid)) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Set allow rejoin config");
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Failed to set allow rejoin config");
		}
		return true;
	}
	
	public boolean arenaSetDisableTeamChat(Player player, boolean disable) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}	
		
		if (editState.arena.arenaDisableTeamChat(disable)) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Set team chat config");
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Failed to set team chat config");
		}
		return true;
	}
	
	public boolean arenaSetTeamplayerRespawnLimit(Player player, String teamName, String teamPlayer, String countString) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}	

		boolean team;
		if (teamPlayer.equalsIgnoreCase("team")) {
			team = true;
		} else if (teamPlayer.equalsIgnoreCase("player")) {
			team = false;
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Expected team or player but got " + teamPlayer);
			return true;
		}
		
		int count = Integer.parseInt(countString);
		
		if (editState.arena.arenaSetTeamplayerRespawnLimit(teamName, team, count)) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Set team respawn limit");
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Failed to set team respawn limit");
		}
		return true;
	}
	
	public boolean arenaAddReferenceInventory(Player player, String name) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}	

		if (editState.arena.arenaAddReferenceInventory(player, name)) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Added reference inventory " + name);
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Failed to add reference inventory " + name);
		}
		return true;
	}
	
	public boolean arenaRemoveReferenceInventory(Player player, String name) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}	

		if (editState.arena.arenaRemoveReferenceInventory(name)) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Remove reference inventory " + name);
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Failed to remove reference inventory " + name);
		}
		return true;
	}
	
	public boolean arenaSetTeamReferenceInventory(Player player, String teamName, String invName) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}	

		if (editState.arena.arenaSetTeamReferenceInventory(teamName, invName)) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Set " + teamName + "'s reference inventory to " + invName);
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Failed to set " + teamName + "'s reference inventory to " + invName);
		}
		return true;
	}
	
	public boolean arenaListEvents(Player player) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}
		
		editState.arena.listEvents(player);
		return true;
	}

	public boolean arenaRemoveEvent(Player player, String event) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}
		
		if (editState.arena.removeEvent(player, Integer.parseInt(event))) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Event removed");
			editState.arena.listEvents(player);
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Failed to remove event (is the index in range?)");
		}
		return true;
	}
	
	public boolean arenaAddMessage(Player player, String triggerEvent, String message) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}
		
		if (editState.arena.addMessage(triggerEvent, message)) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Added message");
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Failed to add message");
		}
		return true;
	}

	public boolean arenaAddTimer(Player player, String eventTrigger, String createName, String timeUnit, String timerMode, String timeString, String displayName) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}

		// Check the time unit is valid
		boolean inSec;
		if (timeUnit.equalsIgnoreCase("min")) {
			inSec = false;
		} else if (timeUnit.equalsIgnoreCase("sec")) {
			inSec = true;
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Expected min or sec but got " + timeUnit);
			return true;
		}
		
		// Check the timer mode is valid
		boolean repeat;
		if (timerMode.equalsIgnoreCase("single")) {
			repeat = false;
		} else if (timerMode.equalsIgnoreCase("repeat")) {
			repeat = true;
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Expected single or repeat but got " + timerMode);
			return true;
		}
	
		if (editState.arena.addTimer(eventTrigger, createName, inSec, repeat, timeString, displayName)) {
			player.sendMessage(ChatColor.GREEN + "[EMS] Added timer " + createName + " with display name " + displayName);
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Failed to add timer, please check the time string (" + timeString + ") is valid for the timer mode (" + timerMode + ")");
		}
		return true;
	}
	
	public boolean  arenaAddPotionEffect(Player player, String eventTrigger, String effectName, String durationString, String amplifierString) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}

		PotionEffectType effect = PotionEffectType.getByName(effectName.toUpperCase());
		int duration = Integer.parseInt(durationString);
		int amplifier = Integer.parseInt(amplifierString);
		boolean found = false;
		for (PotionEffectType check: PotionEffectType.values()) {
			if (check != null) {
				if (check.getName().compareToIgnoreCase(effectName) == 0) {
					found = true;
					break;
				}
			}
		}
		if (!found) {
			String validNames = new String();
			for (PotionEffectType check: PotionEffectType.values()) {
				if (check != null) {
					validNames += check.getName() + ", ";
				}
			}
			player.sendMessage(ChatColor.RED + "[EMS] Potion effect " + effectName + " is no valid (valid values are " + validNames + ")");
			return true;
		}
		editState.arena.addPotionEffect(eventTrigger, effect, duration, amplifier);
		player.sendMessage(ChatColor.GREEN + "[EMS] Added potion effect " + effectName + " which is trigger by " + eventTrigger + " has length " + duration + " and amplifier " + amplifier);
		return true;
	}
	
	public boolean arenaAddEventBlock(Player player, String eventTrigger) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}

		Block block = player.getTargetBlock(null, 4);
		if (block == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Can't find block in range");
			return true;
		}

		Location location = block.getLocation();
		editState.arena.addEventBlock(block, eventTrigger);
		player.sendMessage(ChatColor.GREEN + "[EMS] Added event block @" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + " for " + eventTrigger);
		return true;
	}
	
	public boolean giveWand(Player player) {
		PlayerInventory inventory = player.getInventory();
		ItemStack wand = new ItemStack(Material.STICK, 1);
		ItemMeta wandMeta = wand.getItemMeta();
		wandMeta.setDisplayName("Wand");
		wand.setItemMeta(wandMeta);
		inventory.addItem(wand);
		return true;
	}
	
	public boolean arenaAddClearRegion(Player player, String eventTrigger) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}

		if ((editState.pos1 == null) || (editState.pos2 == null)) {
			player.sendMessage(ChatColor.RED + "[EMS] You need to select a region 1st");
		}
		
		if (!editState.pos1.getWorld().equals(editState.pos2.getWorld())) {
			player.sendMessage(ChatColor.RED + "[EMS] Regions must be in the same world!");
		}

		editState.arena.addClearRegion(eventTrigger, editState.pos1, editState.pos2);
		player.sendMessage(ChatColor.GREEN + "[EMS] Added clear region");
		return true;
	}
	
	public boolean arenaAddFillRegion(Player player, String eventTrigger, String blockType) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}

		if ((editState.pos1 == null) || (editState.pos2 == null)) {
			player.sendMessage(ChatColor.RED + "[EMS] You need to select a region 1st");
		}
		
		if (!editState.pos1.getWorld().equals(editState.pos2.getWorld())) {
			player.sendMessage(ChatColor.RED + "[EMS] Regions must be in the same world!");
		}

		editState.arena.addFillRegion(eventTrigger, editState.pos1, editState.pos2, blockType);
		player.sendMessage(ChatColor.GREEN + "[EMS] Added fill region");
		return true;
	}
	
	public boolean arenaAddTeleport(Player player, String eventTrigger) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}

		editState.arena.addTeleport(eventTrigger, player.getLocation());
		player.sendMessage(ChatColor.GREEN + "[EMS] Added teleport");
		return true;
	}
	
	public boolean arenaAddEntitySpawn(Player player, String eventTrigger, String mobName, String mobCount) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}
		
		// The initialised value should never get used as we bail out if the name is not valid
		EntityType mob = EntityType.CHICKEN;
		boolean found = false;
		for (EntityType check : EntityType.values()) {
			if (check != null) {
				if (check.getName() != null) {
					if (check.getName().compareToIgnoreCase(mobName) == 0) {
						found = true;
						mob = check;
						break;
					}
				}
			}
		}
		
		if (!found) {
			String validNames = new String();
			for (EntityType check : EntityType.values()) {
				if (check != null) {
					if (check.getName() != null) {
						validNames += check.getName() + ", ";
					}
				}
			}
			player.sendMessage(ChatColor.RED + "[EMS] Unknown entity type, valid mobs are (" + validNames + ")");
			return true;
		}
		int count = Integer.parseInt(mobCount);

		editState.arena.addEntitySpawn(eventTrigger, player.getLocation(), mob, count);
		player.sendMessage(ChatColor.GREEN + "[EMS] Added entity spawn");
		return true;
	}
	
	public boolean arenaAddCheckTeamPlayerCount(Player player, String teamPlayer, String eventTrigger, String countString, String createEvent) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}
		
		boolean team;
		if (teamPlayer.equalsIgnoreCase("team")) {
			team = true;
		} else if (teamPlayer.equalsIgnoreCase("player")) {
			team = false;
		} else {
			player.sendMessage(ChatColor.RED + "[EMS] Expected team or player but got " + teamPlayer);
			return true;
		}
		
		int count = Integer.parseInt(countString);
		
		editState.arena.addCheckTeamPlayerCount(team, eventTrigger, count, createEvent);
		player.sendMessage(ChatColor.GREEN + "[EMS] Added team/player check");
		return true;
	}
	
	public boolean arenaAddLightningEffect(Player player, String eventTrigger) {
		EMSEditState editState = getArenaEditState(player, true);
		if (editState == null) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return true;
		}
		
		Location location = player.getLocation();
		editState.arena.addLightningEffect(eventTrigger, location);
		player.sendMessage(ChatColor.GREEN + "[EMS] Added lightning effect @" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + " for " + eventTrigger);
		return true;
	}
	
	public boolean arenaJoin(Player player, String arenaName) {
		EMSArena arena = getArena(player, arenaName);
		if (arena == null) {
			return true;
		}

		arena.playerJoinArena(player);
		return true;
	}

	public boolean arenaLeave(Player player) {
		Iterator<EMSArena> i = arenas.values().iterator();
		while(i.hasNext()) {
			EMSArena arena = i.next();
			if (arena.playerInArena(player)) {
				arena.playerLeaveArena(player);
			}
		}

		return true;
	}

	public boolean arenaGetPlayers(CommandSender sender, String arenaName) {
		EMSArena arena = getArena(sender, arenaName);
		if (arena == null) {
			return true;
		}
		
		List<String> players = arena.getPlayersList();
		Iterator<String> i = players.iterator();
		while (i.hasNext()) {
			String line = i.next();
			sender.sendMessage(line);
		}
		return true;
	}
	
	public boolean arenaForceCapSize(CommandSender sender, String arenaName, String capSize) {
		EMSArena arena = getArena(sender, arenaName);
		if (arena == null) {
			return true;
		}
		
		arena.forceTeamCap(Integer.parseInt(capSize));
		sender.sendMessage(ChatColor.GREEN + "[EMS] Cap of " + capSize + " forced on all teams");
		return true;
	}

	public boolean arenaStart(CommandSender sender, String arenaName) {
		EMSArena arena = getArena(sender, arenaName);
		if (arena == null) {
			return true;
		}
		
		arena.startEvent(sender);
		sender.sendMessage(ChatColor.GREEN + "[EMS] Arena " + arenaName + " started");
		loader.save(arena);
		return true;
	}

	public boolean arenaEnd(CommandSender sender, String arenaName) {
		EMSArena arena = getArena(sender, arenaName);
		if (arena == null) {
			return true;
		}
		
		arena.endEvent();
		sender.sendMessage(ChatColor.GREEN + "[EMS] Arena " + arenaName + " ended");
		loader.save(arena);
		return true;
	}

	public boolean arenaStartTracking(CommandSender sender, String arenaName) {
		EMSArena arena = getArena(sender, arenaName);
		if (arena == null) {
			return true;
		}
		
		if (arena.startTracking()) {
			sender.sendMessage(ChatColor.GREEN + "[EMS] Arena " + arenaName + " tacking started");
		} else {
			sender.sendMessage(ChatColor.RED + "[EMS] Arena " + arenaName + " failed to start tacking, is it active?");
		}
		return true;
	}

	public boolean arenaEndTracking(CommandSender sender, String arenaName) {
		EMSArena arena = getArena(sender, arenaName);
		if (arena == null) {
			return true;
		}
		
		arena.endTracking();
		sender.sendMessage(ChatColor.GREEN + "[EMS] Arena " + arenaName + " tacking ended");
		return true;
	}
	
	public boolean arenaKickTeamplayer(CommandSender sender, String arenaName, String teamPlayer, String name) {
		EMSArena arena = getArena(sender, arenaName);
		if (arena == null) {
			return true;
		}
		
		boolean team;
		if (teamPlayer.equalsIgnoreCase("team")) {
			team = true;
		} else if (teamPlayer.equalsIgnoreCase("player")) {
			team = false;
		} else {
			sender.sendMessage(ChatColor.RED + "[EMS] Expected team or player but got " + teamPlayer);
			return true;
		}
		
		if (arena.KickTeamplayer(team, name)) {
			sender.sendMessage(ChatColor.GREEN + "[EMS] Kicked " + teamPlayer + " " + name);
		} else {
			sender.sendMessage(ChatColor.RED + "[EMS] Failed to kick " + teamPlayer + " " + name);
		}
		return true;
	}

	public void playerJoinServer(Player player) {
		arenaEditState.put(player, new EMSEditState());

		Iterator<EMSArena> i = arenas.values().iterator();

		while(i.hasNext()) {
			EMSArena arena = i.next();

			arena.playerJoinServer(player);
		}
	}

	public void playerLeaveServer(Player player) {
		arenaEditState.remove(player);

		Iterator<EMSArena> i = arenas.values().iterator();

		while(i.hasNext()) {
			EMSArena arena = i.next();

			arena.playerLeaveServer(player);
		}
	}

	public boolean playerDied(Player player) {
		Iterator<EMSArena> i = arenas.values().iterator();

		while(i.hasNext()) {
			EMSArena arena = i.next();

			if (arena.playerDeath(player)) {
				return true;
			}
		}
		return false;
	}
	
	public void playerRespawn(Player player) {
		Iterator<EMSArena> i = arenas.values().iterator();

		while(i.hasNext()) {
			EMSArena arena = i.next();

			arena.playerRespawn(player);
		}
	}
	
	public EMSChatResponse playerChat(Player player, String message) {
		Iterator<EMSArena> i = arenas.values().iterator();

		while(i.hasNext()) {
			EMSArena arena = i.next();

			EMSChatResponse response = arena.playerChat(player, message);
			if (response != null) {
				return response;
			}
		}
		return null;
	}

	public boolean signClicked(Sign sign, Player player) {
		String arenaName = sign.getLine(1);
		EMSArena arena = getArena(arenaName);

		if (arena != null) {		
			if (arena.signClicked(sign, player)) {
				return true;
			}
		}
		return false;
	}
	
	public void playerInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		Player player = event.getPlayer();
		
		// Processing for sign clicking
		if (action == Action.RIGHT_CLICK_BLOCK) {
			Block clickedBlock = event.getClickedBlock();

			if ((clickedBlock.getType() == Material.SIGN) || (clickedBlock.getType() == Material.SIGN_POST) || (clickedBlock.getType() == Material.WALL_SIGN)) {
				BlockState state = clickedBlock.getState();
				Sign sign = (Sign) state;

				// If we claimed the event then cancel it
				if (signClicked(sign, player)) {
					event.setCancelled(true);
				}
			}
		}
		
		// A stick with the name "Wand" is used as the selection tool
		if ((action == Action.LEFT_CLICK_BLOCK) || (action == Action.RIGHT_CLICK_BLOCK)) {
			PlayerInventory inventory = player.getInventory();
			ItemStack heldItem = inventory.getItem(inventory.getHeldItemSlot());

			if (heldItem != null) {
				if ((heldItem.getType() == Material.STICK) && (heldItem.getItemMeta().hasDisplayName()) && player.hasPermission("ems.editarena"))
				{
					if (heldItem.getItemMeta().getDisplayName().equals("Wand"))
					{
						Block clickedBlock = event.getClickedBlock();
						EMSEditState editState = getArenaEditState(player, true);
						Location location = clickedBlock.getLocation();
						if (editState == null) {
							return;
						}
						if (action == Action.LEFT_CLICK_BLOCK) {
							editState.pos1 = location;
							player.sendMessage(ChatColor.GREEN + "[EMS] Selected pos 1 @" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
							event.setCancelled(true);
						} else {
							editState.pos2 = location;
							player.sendMessage(ChatColor.GREEN + "[EMS] Selected pos 2 @" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
							event.setCancelled(true);
						}
					}
				}
			}
		}
	}

	public void signChanged(SignChangeEvent event) {
		Player player = event.getPlayer();
		String[] lines = event.getLines();
		Block block = event.getBlock();

		if (event.getLine(0).toLowerCase().equals("[ems]")) {
			// Only process the sign if the player has an edit session open
			EMSEditState editState = getArenaEditState(player, true);
			if (editState == null) {
				player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
				return;
			}

			String arenaName = event.getLine(1);
			EMSArena arena = getArena(arenaName);
			if (arena == editState.arena) {
				if (arena.signUpdated(block, lines)) {
					player.sendMessage(ChatColor.GREEN + "Created join sign");
					event.setCancelled(true);
				}
			} else {
				player.sendMessage(ChatColor.RED + "[EMS] Tried to create sign for " + arena.getName() + " but arena in edit was " + editState.arena.getName());
			}
		}
	}
	
	public boolean blockBroken(Player player, Location location) {
		Iterator<EMSArena> i = arenas.values().iterator();
		
		while(i.hasNext()) {
			EMSArena arena = i.next();

			if (arena.blockBroken(player, location)) {
				return true;
			}
		}
		return false;
	}
	
	public void loadWorld(World world) {
		Set<EMSArena> worldArenas = loader.loadArenas(world.getName());
		Iterator<EMSArena> i = worldArenas.iterator(); 
		while (i.hasNext()) {
			EMSArena arena = i.next();
			arena.setPlugin(plugin);
			arena.setManager(this);
			arenas.put(arena.getName(), arena);
		}
	}

	public void unloadWorld(World world) {
		Iterator<EMSArena> i = arenas.values().iterator();

		// The world is being unloaded so remove all the arenas in said world
		Bukkit.getLogger().info("[EMS] Unloading arena's for world " + world.getName());
		while (i.hasNext()) {
			EMSArena arena = i.next();
			String arenaName = arena.getWorld();
			if (arenaName.equals(world.getName())) {
				Bukkit.getLogger().info("[EMS] Unloading " + arenaName);
				arena.disable(false);
				i.remove();
			}
		}
	}
	
	public void chunkLoad(Chunk chunk) {
		Iterator<EMSArena> i = arenas.values().iterator();
		
		while(i.hasNext()) {
			EMSArena arena = i.next();

			arena.chunkLoad(chunk);
		}
	}
	
	public void chunkUnload(Chunk chunk) {
		Iterator<EMSArena> i = arenas.values().iterator();
		
		while(i.hasNext()) {
			EMSArena arena = i.next();

			arena.chunkUnload(chunk);
		}
	}
	
	public boolean reloadConfigs() {
		Iterator<EMSArena> i = arenas.values().iterator();

		/* Unload all the loaded arenas */
		while (i.hasNext()) {
			EMSArena arena = i.next();
			String arenaName = arena.getWorld();

			Bukkit.getLogger().info("[EMS] Unloading " + arenaName);
			arena.disable(false);
			arena.destroy();
			i.remove();
		}
		
		List<World> loadedWorlds = Bukkit.getWorlds();
		Iterator<World> iw = loadedWorlds.iterator();
		while (iw.hasNext()) {
			World world = iw.next();
			loadWorld(world);
		}
		return true;
	}
	
	public boolean playerInArena(Player player) {
		Iterator<EMSArena> i = arenas.values().iterator();

		while (i.hasNext()) {
			EMSArena arena = i.next();
			if (arena.playerInArena(player)) {
				return true;
			}
		}
		return false;
	}

	// Utility functions
	private EMSArena getArena(CommandSender sender, String arenaName) {
		if (!arenas.containsKey(arenaName)) {
			sender.sendMessage(ChatColor.RED + "[EMS] An arena called " + arenaName + "does not exist");
			return null;
		}
		return arenas.get(arenaName);
	}
	
	private EMSArena getArena(String arenaName) {
		if (!arenas.containsKey(arenaName)) {
			return null;
		}
		return arenas.get(arenaName);
	}
	
	private EMSEditState getArenaEditState(Player player, boolean shouldHaveArena) {
		if (!arenaEditState.containsKey(player)) {
			player.sendMessage(ChatColor.RED + "[EMS] Fatal error while getting edit state");
			return null;
		}

		EMSEditState editState = arenaEditState.get(player);
		if (shouldHaveArena) {
			if (editState.arena == null) {
				player.sendMessage(ChatColor.RED + "[EMS] You don't have an arena open for edit");
			} else {
				return editState;
			}
		} else {
			if (editState.arena != null) {
				player.sendMessage(ChatColor.RED + "[EMS] You already have an arena open for edit");
			} else {
				return editState;
			}			
		}
		return null;
	}
}
