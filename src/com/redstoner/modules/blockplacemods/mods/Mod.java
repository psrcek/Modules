package com.redstoner.modules.blockplacemods.mods;

import org.bukkit.entity.Player;

import com.redstoner.utils.CommandException;

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
