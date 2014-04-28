package io.github.basicmark.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerState implements ConfigurationSerializable {
	ItemStack[] inventory;
	ItemStack helmet;
	ItemStack chestplate;
	ItemStack leggings;
	ItemStack boots;
	int experience;
	double health;
	int food;
	float saturation;
	boolean restoreInventory;
	boolean restoreXP;
	boolean restoreHealth;
	String name;
	
	public PlayerState(Player player, boolean restoreInventory, boolean restoreXP, boolean restoreHealth) {
		/* Save all the vital states of the player */
		this.inventory = new ItemStack[player.getInventory().getSize()];
		for (int i=0;i<inventory.length;i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (stack != null) {
				this.inventory[i] = new ItemStack(player.getInventory().getItem(i));
			} else {
				this.inventory[i] = null;
			}
		}
		this.helmet = player.getInventory().getHelmet();
		this.chestplate = player.getInventory().getChestplate();
		this.leggings = player.getInventory().getLeggings();
		this.boots = player.getInventory().getBoots();
		recalcTotalExp(player);
		this.experience = player.getTotalExperience();
		this.health = player.getHealth();
		this.food = player.getFoodLevel();
		this.saturation = player.getSaturation();
		this.restoreInventory = restoreInventory;
		this.restoreXP = restoreXP;
		this.restoreHealth = restoreHealth;
		this.name = player.getName();

		/* Check what needs to be set to the "default" settings */
		if (restoreInventory) {
			player.getInventory().clear();
			player.getInventory().setHelmet(null);
			player.getInventory().setChestplate(null);
			player.getInventory().setLeggings(null);
			player.getInventory().setBoots(null);
		}
		if (restoreXP) {
			player.setExp(0);
			player.setLevel(0);
			player.setTotalExperience(0);
		}
		if (restoreHealth) {
			player.setHealth(player.getMaxHealth());
			player.setFoodLevel(20);
			player.setSaturation(20);
		}
	}
	
	public PlayerState(Map<String, Object> values) {
		this.restoreInventory = (boolean) values.get("restoreinventory");
		this.restoreXP = (boolean) values.get("restorexp");
		this.restoreHealth = (boolean) values.get("restorehealth");

		if (restoreInventory) {
			int invsize = (int) values.get("invsize");
			this.inventory = new ItemStack[invsize];
			for(int i=0;i<invsize;i++) {
				try {
					this.inventory[i] = (ItemStack) values.get("invslot"+i);
				} catch (Exception e) {
					this.inventory[i] = null;
				}
			}
			this.helmet = (ItemStack) values.get("helmet");
			this.chestplate = (ItemStack) values.get("chestplate");
			this.leggings = (ItemStack) values.get("leggings");
			this.boots = (ItemStack) values.get("boots");
		}
		
		if (restoreXP) {
			this.experience = (int) values.get("experience");
		}

		if (restoreHealth) {
			this.health = (double) values.get("health");
			this.food = (int) values.get("food");
			this.saturation =  Float.parseFloat((String) values.get("saturation"));
		}
	}

	public Map<String, Object> serialize() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();

		/* Store the player name just in case we need to check the config files by hand */ 
		values.put("name", name);
		values.put("restoreinventory", restoreInventory);
		values.put("restorexp", restoreXP);
		values.put("restorehealth", restoreHealth);
		
		if (restoreInventory) {
			values.put("invsize", inventory.length);
			for (int i=0;i<inventory.length;i++) {
				ItemStack stack = inventory[i];
				if (stack != null) {
					values.put("invslot"+i, stack);
				}
			}
			values.put("helmet", helmet);
			values.put("chestplate", chestplate);
			values.put("leggings", leggings);
			values.put("boots", boots);
		}

		if (restoreXP) {
			values.put("experience", experience);
		}

		if (restoreHealth) {
			values.put("health", health);
			values.put("food", food);
			values.put("saturation", Float.toString(saturation));
		}
		return values;
	}
	
	public void restore(Player player) {
		if (restoreInventory) {
			player.getInventory().clear();
			player.getInventory().setContents(inventory);
			player.getInventory().setHelmet(helmet);
			player.getInventory().setChestplate(chestplate);
			player.getInventory().setLeggings(leggings);
			player.getInventory().setBoots(boots);
		}
		
		if (restoreXP) {
			player.setExp(0);
			player.setLevel(0);
			player.setTotalExperience(0);
			player.giveExp(experience);
		}
		
		if (restoreHealth) {
			player.setHealth(health);
			player.setFoodLevel(food);
			player.setSaturation(saturation);
		}
	}

	/*
	 * The following functions are taken from:
	 * 
	 * https://github.com/feildmaster/ControlORBle/blob/master/src/main/java/com/feildmaster/lib/expeditor/Editor.java
	 * 
	 * This is because the Minecraft XP doesn't seem to work in a sane way. If recalcTotalExp isn't called
	 * then after enchanting the players total XP would be that before the enchantment as it seems to only
	 * update the total XP on some events and enchanting is not one of the :( 
	 */
	public int getExp(Player player) {
		return (int) (getExpToLevel(player) * player.getExp());
	}
	
	public int getTotalExp(Player player) {
		return getTotalExp(player, false);
	}

	public int getTotalExp(Player player, boolean recalc) {
		if (recalc) {
			recalcTotalExp(player);
		}
		return player.getTotalExperience();
	}

	public int getLevel(Player player) {
		return player.getLevel();
	}

	public int getExpToLevel(Player player) {
		return player.getExpToLevel();
	}

	public int getExpToLevel(int level) {
		return level >= 30 ? 62 + (level - 30) * 7 : (level >= 15 ? 17 + (level - 15) * 3 : 17);
	}
	
	private void recalcTotalExp(Player player) {
		int total = getExp(player);
		for (int i = 0; i < player.getLevel(); i++) {
			total += getExpToLevel(i);
		}
		player.setTotalExperience(total);
	}
}