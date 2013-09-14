package io.github.basicmark.ems;

import io.github.basicmark.config.ConfigUtils;
import io.github.basicmark.config.PlayerState;
import io.github.basicmark.config.PlayerStateLoader;
import io.github.basicmark.ems.EMSArenaState;
import io.github.basicmark.ems.arenaevents.EMSArenaEvent;
import io.github.basicmark.ems.arenaevents.EMSAutoEnd;
import io.github.basicmark.ems.arenaevents.EMSCheckTeamPlayerCount;
import io.github.basicmark.ems.arenaevents.EMSClearRegion;
import io.github.basicmark.ems.arenaevents.EMSEventBlock;
import io.github.basicmark.ems.arenaevents.EMSLightningEffect;
import io.github.basicmark.ems.arenaevents.EMSMessenger;
import io.github.basicmark.ems.arenaevents.EMSPotionEffect;
import io.github.basicmark.ems.arenaevents.EMSSpawnEntity;
import io.github.basicmark.ems.arenaevents.EMSTeleport;
import io.github.basicmark.ems.arenaevents.EMSTimer;
import io.github.basicmark.ems.arenaevents.EMSTracker;
import io.github.basicmark.util.DeferChunkWork;
import io.github.basicmark.util.PlayerDeathInventory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.Location;

public class EMSArena implements ConfigurationSerializable {
	// Static data
	protected String name;
	protected EMSArenaState arenaState;
	protected Location lobby;
	protected boolean requiresTeamLobby;
	protected boolean requiresTeamSpawn;
	protected boolean saveInventory;
	protected boolean saveXP;
	protected boolean saveHealth;
	protected boolean keepInvAfterEvent;
	protected boolean lobbyRespawn;
	protected boolean keepInvAfterDeath;
	protected HashMap<String, EMSTeam> teams;
	protected List<EMSArenaEvent> events;
	protected Location joinSignLocation;
	protected String welcomeMessage;
	protected String leaveMessage;
	protected boolean autoStartEnable;
	protected int autoStartMinPlayer;
	protected int autoStartCountdown;
	
	
	/*
	 *  Note:
	 *  playersInLobby can be accessed from multiple threads (server thread & client threads
	 *  for chat message processing) and thus must use a thread safe set implementation
	 */
	private Set<Player> playersInLobby;
	private HashMap<Player, Location> playersLoc;
	protected Set<Location> protections;
	protected HashMap<Player, PlayerDeathInventory> playersDeathInv;
	protected int fullTeams;
	protected Set<Player> readyPlayers;
	protected DeferChunkWork deferredBlockUpdates;
	
	PlayerStateLoader playerLoader;
	JavaPlugin plugin;	// Required to schedule the player teleport after death
	
	protected void commonInit() {
		// Allocate resources for managing static data
		this.teams = new HashMap<String, EMSTeam>();
		this.events = new ArrayList<EMSArenaEvent>();

		// Allocate resources for managing dynamic data
		this.playersInLobby = new HashSet<Player>();	//new ConcurrentSkipListSet<Player>();
		this.playersLoc = new HashMap<Player, Location>();
		this.protections = new HashSet<Location>();
		this.playersDeathInv = new HashMap<Player, PlayerDeathInventory>();
		this.readyPlayers = new HashSet<Player>();
		this.deferredBlockUpdates = new DeferChunkWork();
		this.fullTeams = 0;
	}
	
	public EMSArena(String name) {
		commonInit();

		// Init all the "static" data to their default values
		this.name = name;
		this.arenaState = EMSArenaState.CLOSED;
		this.lobby = null;
		this.requiresTeamLobby = false;
		this.requiresTeamSpawn = false;
		this.saveInventory = true;
		this.saveXP = true;
		this.saveHealth= true; 
		this.keepInvAfterEvent = false;
		this.lobbyRespawn = false;
		this.keepInvAfterDeath = false;
		this.joinSignLocation = null;
		this.welcomeMessage = null;
		this.welcomeMessage = null;
		this.autoStartEnable = false;
		this.autoStartMinPlayer = 0;
		this.autoStartCountdown = 0;
		
		// Add tracking and auto-end by default
		events.add(new EMSTracker(this));
		events.add(new EMSAutoEnd(this));
	}

	@SuppressWarnings("unchecked")
	public EMSArena(Map<String, Object> values) {
		commonInit();

		// Load all the state data from the object data
		this.name = (String) values.get("name");
		this.lobby = ConfigUtils.DeserializeLocation((Map<String, Object>) values.get("lobby"));
		this.requiresTeamLobby = (boolean) values.get("requiresteamlobby");

		/* Some older config's don't have the restore info so set it to the default */
		try {
			this.saveInventory = (boolean) values.get("saveinventory");
		} catch (Exception e) {
			this.saveInventory = true;
		}
		try {
			this.saveXP = (boolean) values.get("savexp");
		} catch (Exception e) {
			this.saveXP = true;
		}
		try {
			this.saveHealth = (boolean) values.get("savehealth");
		} catch (Exception e) {
			this.saveHealth = true;
		}
		try {
			this.keepInvAfterEvent = (boolean) values.get("keepinvafterevent");
		} catch (Exception e) {
			this.keepInvAfterEvent = false;
		}
		try {
			this.lobbyRespawn = (boolean) values.get("lobbyrespawn");
		} catch (Exception e) {
			this.lobbyRespawn = false;
		}
		try {
			this.keepInvAfterDeath = (boolean) values.get("keepinvafterdeath");
		} catch (Exception e) {
			this.keepInvAfterDeath = false;
		}
		try {
			this.autoStartEnable = (boolean) values.get("autostartenable");
		} catch (Exception e) {
			this.autoStartEnable = false;
		}
		try {
			this.autoStartMinPlayer = (int) values.get("autostartminplayer");
		} catch (Exception e) {
			this.autoStartMinPlayer = 0;
		}
		try {
			this.autoStartCountdown = (int) values.get("autostartcountdown");
		} catch (Exception e) {
			this.autoStartCountdown = 0;
		}
		
		
		// Get optional config data for join sign
		Map<String, Object> joinSignData;
		try {
			joinSignData = (Map<String, Object>) values.get("joinsign");
		} catch (Exception e) {
			joinSignData = null;
		}

		this.joinSignLocation = ConfigUtils.DeserializeLocation(joinSignData);
		if (this.joinSignLocation != null) {
			addProtection(joinSignLocation);
		}
		
		try {
			this.welcomeMessage = (String) values.get("welcomemessage");
		} catch (Exception e) {
			welcomeMessage = null;
		} 

		try {
			this.leaveMessage = (String) values.get("leavemessage");
		} catch (Exception e) {
			leaveMessage = null;
		}
		
		this.teams = new HashMap<String, EMSTeam>();
		int teamCount = (int) values.get("teamcount");
		for (int i=0;i<teamCount;i++) {
			EMSTeam newTeam = new EMSTeam((Map<String, Object>) values.get("team"+i), this);
			
			this.teams.put(newTeam.getName(), newTeam);
		}

		// Events are optional
		int eventCount = 0;
		try {
			eventCount = (int) values.get("eventcount");
		} catch (Exception e) {
			eventCount = 0;
		}
		for (int i=0;i<eventCount;i++) {
			EMSArenaEvent newEvent = (EMSArenaEvent) values.get("event"+i);
			newEvent.setArena(this);
			this.events.add(newEvent);
		}
		
		// Update the signs to reflect the loaded state
		updateStatus(EMSArenaState.fromString((String) values.get("state")));
	}

	public Map<String, Object> serialize() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("name", name);
		values.put("state", arenaState.toString().toLowerCase());
		if (lobby != null) {
			values.put("lobby", ConfigUtils.SerializeLocation(lobby));
		}

		/* Player management settings */
		values.put("saveinventory", saveInventory);
		values.put("savexp", saveXP);
		values.put("savehealth", saveHealth);
		values.put("keepinvafterevent", keepInvAfterEvent);
		values.put("lobbyrespawn", lobbyRespawn);
		values.put("keepinvafterdeath", keepInvAfterDeath);
		values.put("autostartenable", autoStartEnable);
		values.put("autostartminplayer", autoStartMinPlayer);
		values.put("autostartcountdown", autoStartCountdown);

		if (joinSignLocation != null) {
			values.put("joinsign", ConfigUtils.SerializeLocation(joinSignLocation));
		}

		if (welcomeMessage != null) {
			values.put("welcomemessage", welcomeMessage);
		}
		if (leaveMessage != null) {
			values.put("leavemessage", leaveMessage);
		}
		
		/* Team management settings */
		values.put("requiresteamlobby", requiresTeamLobby);
		values.put("teamcount", teams.size());

		Iterator<EMSTeam> it = teams.values().iterator();
		int j=0;
		while (it.hasNext()) {
			EMSTeam team = it.next();

			values.put("team" + j, team.serialize());
			j++;
		}

		/* Event settings */
		values.put("eventcount", events.size());
		Iterator<EMSArenaEvent> ie = events.iterator();
		j=0;
		while (ie.hasNext()) {
			EMSArenaEvent event = ie.next();
			values.put("event" + j, event);
			j++;
		}

		return values;
	}
	
	public void setPlugin(JavaPlugin plugin) {
		this.plugin = plugin;
		playerLoader = new PlayerStateLoader(plugin.getDataFolder()+"/players");
	}

	public String getName() {
		return name;
	}
	
	public EMSArenaState getState() {
		return arenaState;
	}
	
	public String getWorld() {
		return lobby.getWorld().getName(); 
	}
	
	// Arena setup functions
	public void setLobby(Player player) {
		lobby = player.getLocation();
	}
	
	public void setLobbyRespawn(boolean lobbyRespawn) {
		this.lobbyRespawn = lobbyRespawn;
	}
	
	public void addTeam(String name, String displayName) {
		EMSTeam newTeam = new EMSTeam(name, displayName, this);
		teams.put(name, newTeam);
	}

	public void setRequiresTeamLobby(boolean required) {
		requiresTeamLobby = required;
	}

	public void setRequiresTeamSpawn(boolean required) {
		requiresTeamSpawn = required;
	}
	
	public void setWelcomeMessage(String message) {
		welcomeMessage = message;
	}

	public void setLeaveMessage(String message) {
		leaveMessage = message;
	}
	
	public boolean removeTeam(String teamName) {
		if (!teams.containsKey(teamName)) {
			return false;
		}

		teams.remove(teamName);
		return true;
	}
	
	public boolean setTeamLobby(String teamName, Player player) {
		if (!teams.containsKey(teamName)) {
			return false;
		}
		
		EMSTeam team = teams.get(teamName);
		team.setLobby(player);
		return true;
	}

	public boolean addTeamSpawn(String teamName, Player player) {
		if (!teams.containsKey(teamName)) {
			return false;
		}
		
		EMSTeam team = teams.get(teamName);
		team.addSpawn(player);
		return true;
	}

	public boolean clearTeamSpawn(String teamName) {
		if (!teams.containsKey(teamName)) {
			return false;
		}
		
		EMSTeam team = teams.get(teamName);
		team.clearSpawns();
		return true;
	}

	public int getTeamSpawnCount(String teamName) {
		if (!teams.containsKey(teamName)) {
			return 0;
		}
		
		EMSTeam team = teams.get(teamName);
		return team.getSpawnCount();
	}
	
	public boolean setTeamSpawnMethod(String teamName, String method) {
		if (!teams.containsKey(teamName)) {
			return false;
		}
		
		EMSTeam team = teams.get(teamName);
		return team.setSpawnMethod(method);
	}
	
	public boolean setTeamCap (String teamName, int capSize) {
		if (!teams.containsKey(teamName)) {
			return false;
		}
		
		EMSTeam team = teams.get(teamName);
		return team.setCap(capSize);
	}
	
	public boolean setSaveInventory(boolean save) {
		this.saveInventory = save;
		return true;
	}
	
	public boolean setSaveXP(boolean save) {
		this.saveXP = save;
		return true;
	}
	
	public boolean setSaveHealth(boolean save) {
		this.saveHealth = save;
		return true;
	}

	public boolean setKeepInvAfterEvent(boolean keep) {
		this.keepInvAfterEvent = keep;
		return true;
	}
	
	public boolean setKeepInvAfterDeath(boolean keep) {
		this.keepInvAfterDeath = keep;
		return true;
	}
	
	public boolean arenaSetAutoStart(boolean enable, int minPlayer, int countdown) {
		this.autoStartEnable = enable;
		this.autoStartMinPlayer = minPlayer;
		this.autoStartCountdown = countdown;
		return true;
	}

	public boolean listEvents(Player player) {
		Iterator<EMSArenaEvent> i = events.iterator();
		int j = 0;
		while (i.hasNext()) {
			EMSArenaEvent event = i.next();
			player.sendMessage(j + ") " + event.getListInfo());
			j++;
		}
		return true;
	}
	
	public boolean removeEvent(Player player, int eventIndex) {
		if (eventIndex > events.size()) {
			return false;
		}

		EMSArenaEvent removal = events.get(eventIndex);
		removal.destroy();
		events.remove(eventIndex);
		return true;
	}
	
	public boolean addMessage(String eventTrigger, String message) {
		EMSMessenger messageEvent = new EMSMessenger(this, eventTrigger, message);
		events.add(messageEvent);
		return true;
	}

	public boolean addTimer(String eventTrigger, String createName, boolean inSeconds, boolean repeat, String timeString, String displayName) {
		EMSTimer timerEvent = new EMSTimer(this, eventTrigger, createName, inSeconds, repeat, timeString, displayName);
		if (!timerEvent.parseTimeString()) {
			return false;
		}
		events.add(timerEvent);
		return true;
	}
	
	public boolean addEventBlock(Block block, String eventTrigger) {
		boolean inverted;
		if (block.getType() == Material.IRON_BLOCK) {
			inverted = false;
		} else if (block.getType() == Material.REDSTONE_BLOCK) {
			inverted = true;
		} else {
			return false;
		}

		EMSEventBlock eventBlock = new EMSEventBlock(this, block.getLocation(), eventTrigger, inverted);
		events.add(eventBlock);
		return true;
	}
	
	public boolean addClearRegion(String triggerName, Location pos1, Location pos2) {
		EMSClearRegion eventClearRegion = new EMSClearRegion(this, triggerName, pos1, pos2);
		events.add(eventClearRegion);
		return true;
	}
	
	public boolean addTeleport(String triggerName, Location location) {
		EMSTeleport teleport = new EMSTeleport(this, triggerName, location);
		events.add(teleport);
		return true;
	}
	
	public boolean addEntitySpawn(String eventTrigger, Location location, EntityType entity, int count) {
		EMSSpawnEntity spawnEntity = new EMSSpawnEntity(this, eventTrigger, location, entity, count);
		events.add(spawnEntity);
		return true;	
	}
	
	public boolean addCheckTeamPlayerCount(boolean team, String eventTrigger, int count, String createEvent) {
		EMSCheckTeamPlayerCount checkTeamPlayerCount = new EMSCheckTeamPlayerCount(this, team, eventTrigger, count, createEvent);
		events.add(checkTeamPlayerCount);
		return true;
	}
	
	public boolean addLightningEffect(String eventTrigger, Location location) {
		EMSLightningEffect lightningEffect = new EMSLightningEffect(this, eventTrigger, location);
		events.add(lightningEffect);
		return true;
	}
	
	public boolean addPotionEffect(String eventTrigger, PotionEffectType effect, int duration, int amplifier) {
		EMSPotionEffect potionEffect = new EMSPotionEffect(this, eventTrigger, effect, duration, amplifier);
		events.add(potionEffect);
		return true;
	}
	
	// Sign management
	private boolean setJoinSign(Location location) {
		if (location != null) {
			if (joinSignLocation != null) {
				// Removing protection on the old location
				removeProtection(joinSignLocation);
			}
			// Adding the join sign so add it to the protected block list
			addProtection(location);
			joinSignLocation = location;
			updateStatusSign();
		} else {
			// Removing the join sign so remove it from the protected block list
			if (joinSignLocation != null) {
				removeProtection(joinSignLocation);
			}
			joinSignLocation = null;
		}
		return false;
	}

	private Location getJoinSign() {
		return joinSignLocation;
	}
	
	public void updateStatusSign() {
		if (getJoinSign() == null) {
			return;
		}
		
		String lines[] = new String[4];
		lines[0] = arenaState.toColourString();
		lines[1] = name;
		lines[2] = "";
		lines[3] = playersLoc.size() + " players";
		updateSign(getJoinSign(), lines);
	}

	
	public boolean signUpdated(Block block, String[] lines) {
		String line2 = lines[2];
		if (line2.toLowerCase().equals("[join]")) {
			setJoinSign(block.getLocation());
			return true;
		} else if (teams.containsKey(line2)) {
			EMSTeam team = teams.get(line2);

			team.setJoinSign(block.getLocation());
			return true;
		}

		return false;
	}
	
	public void updateStatus(EMSArenaState newState) {
		Iterator<EMSTeam> i = teams.values().iterator();
		
		// Update the per team signs
		arenaState = newState;

		// Update the arena join sign
		updateStatusSign();

		// Update the per team signs
		while(i.hasNext()) {
			EMSTeam team = i.next();
			team.updateStatus(newState);
		}
	}

	public boolean signClicked(Sign sign, Player player) {
		boolean consumeEvent = false;
		String teamDisplayName = sign.getLine(2);		

		// If there is no team name then it must be an arena join sign
		if (teamDisplayName.equals("")) {
			if (playerJoinArena(player)) {
				consumeEvent = true;
			}
		} else {
			if (playerJoinTeam(player, teamDisplayName)) {
				consumeEvent = true;
			}
		}

		return consumeEvent;
	}
	
	public boolean blockBroken(Player player, Location location) {
		if (arenaState != EMSArenaState.EDITING) {
			// We're not editing so just check if we know this block
			if (protections.contains(location)) {
				player.sendMessage(ChatColor.RED + "[EMS] That block is protected in arena " + name);
				return true;
			}
		} else {
			// Check if the block is the join sign of the arena
			if (getJoinSign() != null) {
				if (getJoinSign().equals(location)) {
					setJoinSign(null);
				}
			}
			
			// Check if the block is the join sign of a team
			Iterator<EMSTeam> i = teams.values().iterator();
			while (i.hasNext()) {
				EMSTeam team = i.next();
				if (team.getJoinSign() != null) {
					if (team.getJoinSign().equals(location)) {
						team.setJoinSign(null);
					}
				}
			}
		}

		return false;
	}
	
	public void chunkLoad(Chunk chunk) {
		 deferredBlockUpdates.chunkLoad(chunk);
	}
	
	public boolean editOpen() {
		if (arenaState == EMSArenaState.EDITING) {
			// The arena is already in edit state
			return false;
		} else if (arenaState != EMSArenaState.CLOSED) {
			disable();
		}
		
		updateStatus(EMSArenaState.EDITING);
		return true;
	}
	
	public void editClose() {
		// Set the arena to closed after its been edited
		updateStatus(EMSArenaState.CLOSED);
	}

	public boolean enable() {
		// Check if the setup is complete
		if (lobby == null) {
			// We require a lobby for the arena
			return false;
		}
		if (teams.isEmpty()) {
			// We need at least one team
			return false;
		}

		if (requiresTeamLobby) {
			Iterator<EMSTeam> i = teams.values().iterator();

			while(i.hasNext()) {
				EMSTeam team = i.next();
				if (!team.hasLobby() || !team.hasJoinSign()) {
					// All teams need a lobby and a way to get there
					return false;
				}
			}
		}

		if (requiresTeamSpawn) {
			Iterator<EMSTeam> i = teams.values().iterator();

			while(i.hasNext()) {
				EMSTeam team = i.next();
				if (!team.hasSpawn()) {
					// All teams need a lobby
					return false;
				}
			}
		}
		updateStatus(EMSArenaState.OPEN);
		return true;
	}
	
	public void disable() {
		// Disable all the teams
		endEvent();

		// Then remove the players from the arena
		Iterator<Player> pi = playersInLobby.iterator();
		while(pi.hasNext()) {
			Player player = pi.next();

			player.sendMessage(ChatColor.RED + "[EMS] The arena you're in is being disabled");
			playerLeaveArenaDo(player);
			pi.remove();
		}
		updateStatus(EMSArenaState.CLOSED);
	}

	public void destroy() {
		Iterator<EMSArenaEvent> i = events.iterator();
		
		/*
		 * Destroy any events which where created
		 */
		while (i.hasNext()) {
			EMSArenaEvent removal = i.next();
			removal.destroy();
		}
	}
	
	public void forceTeamCap(int capSize) {
		Iterator<EMSTeam> i = teams.values().iterator();
		// We can only adjust the cap while the arena is open
		if (arenaState != EMSArenaState.OPEN) {
			return;
		}

		while(i.hasNext()) {
			EMSTeam team = i.next();
			team.setForceCap(capSize);
		}
	}
	
	public void startEvent(CommandSender sender) {	
		// Only start an event if the previous state of the arena was open
		if (arenaState != EMSArenaState.OPEN) {
			sender.sendMessage(ChatColor.RED + "[EMS] The event is already in progress");
		} else {
			startEvent();
		}
	}
	
	protected void startEvent()
	{
		Iterator<EMSTeam> i = teams.values().iterator();

		while(i.hasNext()) {
			EMSTeam team = i.next();
			team.spawnPlayers();
		}

		updateStatus(EMSArenaState.ACTIVE);
		signalEvent("start");
	}
	
	public void endEvent() {
		// Ending an event is always possible, even if one hasn't started

		// Stop all the arena events
		Iterator<EMSArenaEvent> ie = events.iterator();
		while(ie.hasNext()) {
			EMSArenaEvent event = ie.next();
			event.cancelEvent();
		}	

		Iterator<EMSTeam> it = teams.values().iterator();
		while(it.hasNext()) {
			EMSTeam team = it.next();
			team.removeAllPlayers();
		}

		updateStatus(EMSArenaState.OPEN);
	}

	public boolean startTracking() {
		if (arenaState == EMSArenaState.ACTIVE) {
			signalEvent("start-tracking");
			return true;
		}
		return false;
	}
	
	public void endTracking() {
		if (arenaState == EMSArenaState.ACTIVE) {
			signalEvent("end-tracking");
		}
	}
	
	// Player functions
	@SuppressWarnings("deprecation")
	public boolean playerJoinArena(Player player) {
		if (!arenaCommandValid(player, false)) {
			return false;
		}

		playersInLobby.add(player);
		playersLoc.put(player, player.getLocation());
		teleportPlayer(player, lobby);
		playerLoader.save(player, new PlayerState(player, saveInventory, saveXP, saveHealth));
		player.sendMessage(ChatColor.GREEN + "[EMS] You have joined " + name);
		if (welcomeMessage != null) {
			player.sendMessage(ChatColor.GOLD + welcomeMessage);
		}
		
		// Force an inventory update as if the play joins via a sign although their inventory
		// gets cleared the player will still see it a ghost form, the only way to do this is
		// to use a deprecated function :(
		player.updateInventory();
		
		// Update the arena status sign
		updateStatusSign();
		return true;
	}

	public boolean playerInArena(Player player) {
		if (playersInLobby.contains(player)) {
			return true;
		}

		Iterator<EMSTeam> i = teams.values().iterator();
		while(i.hasNext()) {
			EMSTeam team = i.next();
			if (team.isPlayerInTeam(player)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Set the player in the right state when they leave the arena
	 */
	public void playerLeaveArenaDo(Player player) {
		player.sendMessage(ChatColor.GREEN + "[EMS] You have now left the arena");
		if (leaveMessage != null) {
			player.sendMessage(ChatColor.GOLD + leaveMessage);
		}
		teleportPlayer(player,playersLoc.get(player));
		playersLoc.remove(player);
		playersDeathInv.remove(player);
		PlayerState state = playerLoader.load(player);
		if (state != null) {
			state.restore(player);
			playerLoader.delete(player);
		}

		// Update the arena status sign
		updateStatusSign();
	}
	
	/*
	 * Player is leaving the arena remove them from the lobby/team
	 * and set their state right
	 */
	public void playerLeaveArena(Player player) {
		if (!arenaCommandValid(player, true)) {
			return;
		}
	
		/*
		 *  Remove the player from the team they are in (if any) which will
		 *  kick them back into the lobby
		 */
		playerLeaveTeam(player);
		
		/*
		 * Then check if they are in the lobby and remove them from their
		 */
		if (playersInLobby.contains(player)) {
			playersInLobby.remove(player);
			playerLeaveArenaDo(player);
		}
		
		/*
		 * If the player left an active event then signal to that event
		 * that a player has left so it can act accordingly
		 */
		if (arenaState == EMSArenaState.ACTIVE) {
			signalEvent("player-leave");
		}
	}

	public boolean playerJoinTeam(Player player, String teamDisplayName) {		
		if (!playersInLobby.contains(player)) {
			// Only if a player is in the lobby can they join a team
			player.sendMessage(ChatColor.RED + "You're not in the lobby!");
			return false;
		}

		// The the event is not open then you can't join a team
		if (arenaState != EMSArenaState.OPEN) {
			player.sendMessage(ChatColor.RED + " arena is " + arenaState.toString().toLowerCase());
			return false;
		}
		
		Iterator<EMSTeam> i = teams.values().iterator();
		while (i.hasNext()) {
			EMSTeam team = i.next();

			if (team.getDisplayName().equals(teamDisplayName)) {
				if (team.addPlayer(player)) {
					playersInLobby.remove(player);
					if ((fullTeams == teams.size()) && autoStartEnable) {
						startEvent();
					}
				} else {
					player.sendMessage(ChatColor.RED + " failed to add you to team " + teamDisplayName);
				}
				return true;
			}
		}
		return false;
	}

	public void playerLeaveTeam(Player player) {
		if (!arenaCommandValid(player, true)) {
			return;
		}
		
		// Check all teams for the player and remove them if found
		Iterator<EMSTeam> i = teams.values().iterator();
		while(i.hasNext()) {
			EMSTeam team = i.next();
			if (team.isPlayerInTeam(player)) {
				team.removePlayer(player);
				player.sendMessage(ChatColor.GREEN + "[EMS] Leaving team " + team.getName());
				return;
			}
		}

		return;
	}
	
	public List<String> getPlayers() {
		List<String> playerData = new ArrayList<String>();
		
		
		playerData.add(ChatColor.WHITE + "Lobby");
		Iterator<Player> pi=playersInLobby.iterator();
		while (pi.hasNext()) {
			Player player = pi.next();
			playerData.add("-" + player.getName());
		}

		Iterator<EMSTeam> i = teams.values().iterator();
		while(i.hasNext()) {
			EMSTeam team = i.next();

			playerData.add(team.getDisplayName());
			List<String> tmpTeamMembers = team.getPlayerList();
			Iterator<String> tpi =  tmpTeamMembers.iterator();
			while (tpi.hasNext()) {
				String player = tpi.next();
				if (autoStartEnable && (autoStartMinPlayer != 0) && (arenaState.equals(EMSArenaState.OPEN))) {
					String readyMessage = " (not ready)";
					Player p = plugin.getServer().getPlayer(player);

					if (readyPlayers.contains(p)) {
						readyMessage = " (ready)";
					}
					playerData.add("-" + player + readyMessage);
				} else {
					playerData.add("-" + player);
				}
			}
		}	
		return playerData;
	}
	
	public boolean playerDeath(Player player) {
		if (playerInArena(player)) {
			boolean inTeam = false;
			Iterator<EMSTeam> i = teams.values().iterator();
			while(i.hasNext()) {
				EMSTeam team = i.next();
				if (team.isPlayerInTeam(player)) {
					team.playerDeath(player);

					// Reset the health here before the death screen gets displayed
					player.setHealth(player.getMaxHealth());
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, (Runnable) new EMSDeathRunner(player, this), 1);
					if (keepInvAfterDeath) {
						playersDeathInv.put(player, new PlayerDeathInventory(player));
					}

					/*
					 * Although the player is in limbo for 1 tick we can signal death & leave
					 * events here as the player has already been removed from the team
					 */
					signalEvent("player-death");
					signalEvent("player-leave");
					inTeam = true;
				}
			}
			if ((!inTeam) && lobbyRespawn) {
				// Reset the health here before the death screen gets displayed
				player.setHealth(player.getMaxHealth());
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, (Runnable) new EMSDeathRunner(player, this), 1);
				if (keepInvAfterDeath) {
					playersDeathInv.put(player, new PlayerDeathInventory(player));
				}
			}
			return keepInvAfterDeath;
		}
		return false;
	}
	
	public void playerRespawn(Player player) {
		if (playerInArena(player) && keepInvAfterDeath && playersDeathInv.containsKey(player)) {
			PlayerDeathInventory deathInv = playersDeathInv.get(player);
			deathInv.restore();
			playersDeathInv.remove(player);
		}
	}
	
	public EMSChatResponse playerChat(Player player, String message) {
		EMSChatResponse response = null;

		if (playerInArena(player)) {	
			String[] stringArray = message.split("\\s", 2);
			if (!stringArray[0].equalsIgnoreCase("shout")) {
				if (playersInLobby.contains(player)) {
					broadcast("[" + ChatColor.GREEN + "lobby" + ChatColor.RESET + "] " + player.getDisplayName() + ": " + message);
				} else {
					Iterator<EMSTeam> i = teams.values().iterator();
					while(i.hasNext()) {
						EMSTeam team = i.next();
						if (team.isPlayerInTeam(player)) {
							team.broadcast("[" + team.getDisplayName() + ChatColor.RESET + "] " + player.getDisplayName() + ": " + ChatColor.RESET + message);
							
							/*
							 * If the arena is open then players can toggle their ready state if this arena has
							 * auto-start enabled with a min player limit set
							 */
							if (arenaState.equals(EMSArenaState.OPEN) && autoStartEnable && (autoStartMinPlayer != 0)) {
								if (stringArray[0].equalsIgnoreCase("ready")) {
									if (!readyPlayers.contains(player)) {
										readyPlayers.add(player);
										broadcast(ChatColor.GRAY + player.getName() + " is ready");
									} else {
										readyPlayers.remove(player);
										broadcast(ChatColor.GRAY + player.getName() + " is not ready");
									}

									if (readyPlayers.size() >= autoStartMinPlayer) {
										startEvent();
									} else {
										broadcast(ChatColor.GRAY + "" + (autoStartMinPlayer - readyPlayers.size()) + " more player(s) required");
									}
								}
							}
						}
					}
				}
				/*
				 * The message was from a player in this arena and wasn't a shout so EMS has
				 * consumed the message and thus we cancel the event
				 */
				response = new EMSChatResponse(true, null);
			} else if (stringArray.length == 2){
				/*
				 * Although the player is in an arena it had shout prefix so remove the shout
				 * part of the message and allow the event to continue
				 */
				response = new EMSChatResponse(false, stringArray[1]);
			}
		}
		return response;
	}
	
	public void sendToLobby(Player player) {
		// Unlike playerJoinArena the previous location of the player should not be saved 
		playersInLobby.add(player);
		readyPlayers.remove(player);
		restorePlayer(player);
		teleportPlayer(player, lobby);
	}
	
	public void broadcast(String message) {
		Iterator<Player> ip = playersInLobby.iterator();
		while (ip.hasNext()) {
			Player player = ip.next();
			player.sendMessage(message);
		}

		Iterator<EMSTeam> it = teams.values().iterator();
		while(it.hasNext()) {
			EMSTeam team = it.next();
			team.broadcast(message);
		}
	}
	
	public void signalEvent(String eventName) {
		Iterator<EMSArenaEvent> i = events.iterator();

		while(i.hasNext()) {
			EMSArenaEvent event = i.next();
			event.signalEvent(eventName);
		}
	}
	
	public void addProtection(Location location) { 
		protections.add(location);
	}

	public void removeProtection(Location location) { 
		protections.remove(location);
	}
	
	public int getActiveTeamCount() {
		Iterator<EMSTeam> i = teams.values().iterator();
		int activeTeams = 0;

		while(i.hasNext()) {
			EMSTeam team = i.next();
			if (team.getPlayerCount() > 0) {
				activeTeams++;
			}
		}
		return activeTeams;
	}

	public int getActivePlayerCount() {
		Iterator<EMSTeam> i = teams.values().iterator();
		int activePlayers = 0;

		while(i.hasNext()) {
			EMSTeam team = i.next();

			activePlayers += team.getPlayerCount();
		}
		return activePlayers;
	}
	
	public JavaPlugin getPlugin() {
		return plugin;
	}
	
	public Set<Player> getActivePlayers() {
		Set<Player> players = new HashSet<Player>();
		Iterator<EMSTeam> i = teams.values().iterator();
		
		while(i.hasNext()) {
			EMSTeam team = i.next();
			players.addAll(team.getPlayers());
		}
		return players;
	}
	
	public void teleportPlayer(Player player, Location location) {
		player.leaveVehicle();
		player.teleport(location);
	}
	
	static public void updateSignDo(Location location, String[] lines) {
		Block block = location.getBlock();

		if ((block.getType() == Material.WALL_SIGN) || (block.getType() == Material.SIGN_POST)) {
			BlockState state = block.getState();
			Sign sign = (Sign) state;

			sign.setLine(0, lines[0]);
			sign.setLine(1, lines[1]);
			sign.setLine(2, lines[2]);
			sign.setLine(3, lines[3]);
			sign.update(true);	
		}
	}
	
	public void updateSign(Location location, String[] lines) {
		if (location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
			updateSignDo(location, lines);
		} else {
 			deferredBlockUpdates.addWork(location, new EMSSignUpdate(location, lines));
		}
	}
	
	public void updateTeamFullStatus(boolean full) {
		if (full) {
			fullTeams++;
		} else {
			fullTeams--;
		}
	}
	
	// Private functions
	private void restorePlayer(Player player) {
		//Reset the player to a good state after their death
		player.setHealth(player.getMaxHealth());
		player.setFireTicks(0);
		for (PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
		player.setVelocity(new Vector(0, 0, 0));

		/* Clear down a players inventory when they rejoin the lobby if required */
		if (!keepInvAfterEvent) {
			player.getInventory().clear();
			player.getInventory().setHelmet(null);
			player.getInventory().setChestplate(null);
			player.getInventory().setLeggings(null);
			player.getInventory().setBoots(null);
		}
	}

	private boolean arenaCommandValid(Player player, boolean shouldBeInArena) {
		if (arenaState == EMSArenaState.CLOSED) {
			player.sendMessage(ChatColor.RED + "[EMS] " + name + " is not open");
			return false;
		}

		if (shouldBeInArena) {
			if (!playerInArena(player)) {
				player.sendMessage(ChatColor.RED + "[EMS] You're not in an arena!");
				return false;
			}
		} else {
			if (playerInArena(player)) {
				player.sendMessage(ChatColor.RED + "[EMS] You're already in this arena!");
				return false;
			}
		}
		return true;
	}

	public class EMSDeathRunner implements Runnable {
		Player player;
		EMSArena arena;

		public EMSDeathRunner(Player player, EMSArena arena) {
			this.player = player;
			this.arena = arena;
		}

		@Override
		public void run() {
			// Create the respawn event which is now missing as we reset the health to full on death
			arena.sendToLobby(player);
			Bukkit.getPluginManager().callEvent(new PlayerRespawnEvent(player, player.getLocation(), false));
		}
	}
	
	private class EMSSignUpdate implements Runnable {
		Location location;
		String[] lines;
		
		public EMSSignUpdate(Location location, String[] lines) {
			this.location = location;
			this.lines = lines;
		}
		
		public void run() {
				EMSArena.updateSignDo(location, lines);
		}
	}
}
