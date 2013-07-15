package io.github.basicmark.ems;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class EMSPlugin extends JavaPlugin {
	EMSManager manager;

	public void onEnable(){
		getLogger().info("Enableing EMS");
		manager = new EMSManager(this);
	}
 
	public void onDisable(){
		getLogger().info("Disabling EMS");
		manager.shutdown();
	}

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if (cmd.getName().equalsIgnoreCase("ems")){
    		return manager.processCommand(sender, args);
    	}
    	return false;
    }
}
