package io.github.basicmark.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerDeathInventory {
	Player player;
	ItemStack[] inventory;
	ItemStack helmet;
	ItemStack chestplate;
	ItemStack leggings;
	ItemStack boots;
	
	public PlayerDeathInventory(Player player) {
		this.player = player;

		/* Save all the players inv */
		this.inventory = new ItemStack[player.getInventory().getSize()];
		for (int i=0;i<inventory.length;i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (stack != null) {
				this.inventory[i] = new ItemStack(player.getInventory().getItem(i));
			} else {
				this.inventory[i] = null;
			}
		}

		/* Save what the player is currently wearing */
		this.helmet = player.getInventory().getHelmet();
		this.chestplate = player.getInventory().getChestplate();
		this.leggings = player.getInventory().getLeggings();
		this.boots = player.getInventory().getBoots();
	}
	
	public void restore() {
		player.getInventory().clear();
		player.getInventory().setContents(inventory);
		player.getInventory().setHelmet(helmet);
		player.getInventory().setChestplate(chestplate);
		player.getInventory().setLeggings(leggings);
		player.getInventory().setBoots(boots);
	}
}
