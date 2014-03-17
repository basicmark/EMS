package io.github.basicmark.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class ReferenceInventoryLoader extends ConfigurationSerialization {
	private File configDir;
	
	public ReferenceInventoryLoader(String configPath) {
		super(null);
		this.configDir = new File(configPath);
	}

	protected String getConfigPath(String invName) {
		return configDir + "/" + invName + ".yml";
	}
	
	protected File getConfigFile(String invName) {
		return new File(getConfigPath(invName));
	}
	
	protected YamlConfiguration getConfig(String invName) throws FileNotFoundException, IOException, InvalidConfigurationException {
		YamlConfiguration result = new YamlConfiguration();
		result.load(getConfigFile(invName));
		return result;
	}
	
	protected void saveConfig(String invName, ReferenceInventory inv) throws IOException {
		YamlConfiguration result = new YamlConfiguration();
		result.set("referenceinventory", inv);
		result.save(getConfigFile(invName));
	}
	
	public ReferenceInventory load(String invName) {
		try {
			return (ReferenceInventory) getConfig(invName).get("referenceinventory");
		} catch(FileNotFoundException e) {
			return null;
		} catch(Exception e) {
			return null;
		}
	}
	
	public void save(String invName, ReferenceInventory inv) {
		if(invName == null) {
			return;
		}
		try {
			saveConfig(invName, inv);
		} catch(IOException e) {
			return;
		}
	}

	public void delete(String invName) {
		getConfigFile(invName).delete();
	}
	
	public boolean exists(String invName) {
		return getConfigFile(invName).exists();
	}
}

