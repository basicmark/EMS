package io.github.basicmark.ems;

import org.bukkit.ChatColor;

public enum EMSTeamState {
	 OPEN(ChatColor.GREEN), FULL(ChatColor.RED), CLOSED(ChatColor.DARK_RED), ACTIVE(ChatColor.GOLD), EDITING(ChatColor.LIGHT_PURPLE);
	 
	 private final ChatColor prefixColour;
	 
	 private EMSTeamState(ChatColor colour) {
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
	 
	 public static EMSTeamState fromString(String string) {
		 if (string != null) {
			 for (EMSTeamState state : EMSTeamState.values()) {
		        if (string.equalsIgnoreCase(state.toString())) {
		        	return state;
		        }
			 }
		 }
		 throw new IllegalArgumentException("No constant with text " + string + " found");
	}
	 
	 public static EMSTeamState fromArenaState(EMSArenaState arenaState) {
		 switch (arenaState) {
		 	case OPEN:	return EMSTeamState.OPEN;
		 	case CLOSED: return EMSTeamState.CLOSED;
		 	case ACTIVE: return EMSTeamState.ACTIVE;
		 	case EDITING: return EMSTeamState.EDITING;
		 }
		 // Not required but we need to return something at the end of the method
		 return EMSTeamState.CLOSED;
	 }
}
