package io.github.basicmark.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ReferenceInventory implements ConfigurationSerializable {
	ItemStack[] inventory;
	ItemStack helmet;
	ItemStack chestplate;
	ItemStack leggings;
	ItemStack boots;
	
	public ReferenceInventory(Player player) {
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
	}
	
	public ReferenceInventory(Map<String, Object> values) {
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

	public Map<String, Object> serialize() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		
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

		return values;
	}
	
	public void load(Player player) {
		player.getInventory().clear();
		player.getInventory().setContents(inventory);
		player.getInventory().setHelmet(helmet);
		player.getInventory().setChestplate(chestplate);
		player.getInventory().setLeggings(leggings);
		player.getInventory().setBoots(boots);
	}
}
