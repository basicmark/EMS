package io.github.basicmark.ems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class EMSArenaLoader extends ConfigurationSerialization {
	private File configDir;
	
	public EMSArenaLoader(String configPath) {
		super(null);
		this.configDir = new File(configPath);
	}
	
	protected String getConfigPath(String worldName, String arenaName) {
		return configDir + "/" + worldName + "/" + arenaName + ".yml";
	}
	
	protected File getConfigFile(String worldName, String arenaName) {
		return new File(getConfigPath(worldName, arenaName));
	}
	
	protected YamlConfiguration getConfig(String worldName, String arenaName) throws FileNotFoundException, IOException, InvalidConfigurationException {
		YamlConfiguration result = new YamlConfiguration();
		result.load(getConfigFile(worldName, arenaName));
		return result;
	}
	
	protected void saveConfig(String worldName, String arenaName, EMSArena arena) throws IOException {
		YamlConfiguration result = new YamlConfiguration();
		result.set("arena", arena);
		result.save(getConfigFile(worldName, arenaName));
	}
	
	public EMSArena load(String worldName, String arenaName) {
		try {
			Bukkit.getLogger().info("[EMS] Loading " + arenaName);
			return (EMSArena) getConfig(worldName, arenaName).get("arena");
		} catch(FileNotFoundException e) {
			return null;
		} catch(Exception e) {
			Bukkit.getLogger().log(Level.SEVERE, "[EMS] Failed to load arena " + arenaName, e);
			return null;
		}
	}
	
	public Set<EMSArena> loadArenas(String worldName) {
		Set<EMSArena> arenas = new HashSet<EMSArena>();
		Bukkit.getLogger().info("[EMS] Loading arena's for " + worldName);

		File dir = new File(configDir + "/" + worldName);
		if(!dir.exists()) {
			return arenas;
		}
	
		final HashSet<String> arenaConfigs = new HashSet<String>();
		final String extension = ".yml";
		final int length = extension.length();
		
		dir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				int n = name.length();
				if(name.endsWith(extension) && n > length) {
					arenaConfigs.add(name.substring(0, n - length));			
				}
				return false;
			}
		});
		
		Iterator<String> i = arenaConfigs.iterator();
		while(i.hasNext()) {
			String arenaName = i.next();
			arenas.add(load(worldName, arenaName));
		}
		
		return arenas;
	}
	
	public void save(EMSArena arena) {
		if(arena == null) {
			return;
		}
		try {
			saveConfig(arena.getWorld(), arena.getName(), arena);
		} catch(IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, "[EMS] Failed to save arena " + arena.getName(), e);
			return;
		}
	}

	public void delete(EMSArena arena) {
		getConfigFile(arena.getWorld(), arena.getName()).delete();
	}
}
