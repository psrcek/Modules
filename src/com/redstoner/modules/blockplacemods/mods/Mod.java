package com.redstoner.modules.blockplacemods.mods;

import com.redstoner.modules.blockplacemods.util.CommandException;
import org.bukkit.entity.Player;

import java.util.Set;

public interface Mod
{
	String getName();
	
	String getDescription();
	
	Set<String> getAliases();
	
	Object getDefault();
	
	String runCommand(Player sender, String[] args) throws CommandException;
	
	void registerListeners();
	
	void unregisterListeners();
}
