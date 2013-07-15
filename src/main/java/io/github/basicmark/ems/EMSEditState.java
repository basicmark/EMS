package io.github.basicmark.ems;

import org.bukkit.Location;

public class EMSEditState {
	private boolean editing;
	public EMSArena arena;
	public Location pos1;
	public Location pos2;
	
	public EMSEditState() {
		this.editing = false;
		this.arena = null;
	}
	
	public boolean openArenaForEdit(EMSArena arena) {
		if (this.editing && (this.arena != arena)) {
			return false;
		}
		this.editing = true;

		if (this.arena == arena) {
			// We're already editing this arena so nothing to do
			return true;
		} else if (this.arena != null) {
			// We're editing another arena already!
			return false;
		}

		boolean success = arena.editOpen();
		if (success) {
			this.arena = arena;
		}
		return success;
	}

	public boolean closeArenaForEdit() {
		this.editing = false;
		if (this.arena != null) {
			this.arena.editClose();
			this.arena = null;
			return true;
		}
		return false;
	}
	
	public String getArenaEditing() {
		if (this.editing) {
			return arena.getName();
		}
		return "none";
	}
}
