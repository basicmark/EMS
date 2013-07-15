package io.github.basicmark.ems;

import org.bukkit.ChatColor;

public enum EMSArenaState {
	 OPEN(ChatColor.GREEN), CLOSED(ChatColor.DARK_RED), ACTIVE(ChatColor.GOLD), EDITING(ChatColor.LIGHT_PURPLE);
	 
	 private final ChatColor prefixColour;
	 
	 private EMSArenaState(ChatColor colour) {
		 prefixColour = colour;
	 }
	 
	 @Override
	 public String toString() {
	   //only capitalise the first letter
	   String s = super.toString();
	   return s.substring(0, 1) + s.substring(1).toLowerCase();
	 }
	 
	 public String toColourString() {
		 String colourString = prefixColour + toString();
		 return colourString;
	 }
	 
	 public static EMSArenaState fromString(String string) {
		 if (string != null) {
			 for (EMSArenaState state : EMSArenaState.values()) {
		        if (string.equalsIgnoreCase(state.toString())) {
		        	return state;
		        }
			 }
		 }
		 throw new IllegalArgumentException("No constant with text " + string + " found");
	}
}
