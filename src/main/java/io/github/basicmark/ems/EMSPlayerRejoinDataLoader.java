package io.github.basicmark.ems;

import io.github.basicmark.ems.EMSPlayerRejoinData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class EMSPlayerRejoinDataLoader extends ConfigurationSerialization {
	private File configDir;
	
	public EMSPlayerRejoinDataLoader(String configPath) {
		super(null);
		this.configDir = new File(configPath);
	}

	protected String getConfigPath(String player) {
		return configDir + "/" + player + ".yml";
	}
	
	protected File getConfigFile(String player) {
		return new File(getConfigPath(player));
	}
	
	protected YamlConfiguration getConfig(String player) throws FileNotFoundException, IOException, InvalidConfigurationException {
		YamlConfiguration result = new YamlConfiguration();
		result.load(getConfigFile(player));
		return result;
	}
	
	protected void saveConfig(String player, EMSPlayerRejoinData state) throws IOException {
		YamlConfiguration result = new YamlConfiguration();
		result.set("player", state);
		result.save(getConfigFile(player));
	}
	
	public EMSPlayerRejoinData load(String player) {
		try {
			Bukkit.getLogger().info("[EMS] Loading player data for " + player);
			return (EMSPlayerRejoinData) getConfig(player).get("player");
		} catch(FileNotFoundException e) {
			return null;
		} catch(Exception e) {
			Bukkit.getLogger().log(Level.SEVERE, "[EMS] Failed to load player data for " + player, e);
			return null;
		}
	}
	
	public void save(String player, EMSPlayerRejoinData state) {
		if(player == null) {
			return;
		}
		try {
			saveConfig(player, state);
		} catch(IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, "[EMS] Failed to save player data for " + player, e);
			return;
		}
	}

	public void delete(String player) {
		getConfigFile(player).delete();
	}

	public void deleteAll() {
		File dir = new File(configDir.toString());
		if(!dir.exists()) {
			return;
		}
	
		final HashSet<String> playerReloadDatas = new HashSet<String>();
		final String extension = ".yml";
		final int length = extension.length();
		
		dir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				int n = name.length();
				if(name.endsWith(extension) && n > length) {
					playerReloadDatas.add(name.substring(0, n - length));			
				}
				return false;
			}
		});
		
		Iterator<String> i = playerReloadDatas.iterator();
		while(i.hasNext()) {
			String playerName = i.next();
			delete(playerName);
		}
	}
}
