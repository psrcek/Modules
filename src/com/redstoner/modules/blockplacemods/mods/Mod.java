package com.redstoner.modules.blockplacemods.mods;

import java.util.Set;

import org.bukkit.entity.Player;

import com.redstoner.modules.blockplacemods.util.CommandException;

public interface Mod<T>
{
	String getName();
	
	String getDescription();
	
	Set<String> getAliases();
	
	Object getDefault();
	
	String runCommand(Player sender, String[] args) throws CommandException;
	
	void register();
	
	void unregister();
}
