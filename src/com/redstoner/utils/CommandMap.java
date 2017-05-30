package com.redstoner.utils;

import java.lang.reflect.Field;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;

public class CommandMap
{
	@SuppressWarnings("unchecked")
	public static Map<String, Command> getCommandMap() throws ReflectiveOperationException, ClassCastException
	{
		Field field = SimplePluginManager.class.getDeclaredField("commandMap");
		field.setAccessible(true);
		Object map = field.get(Bukkit.getPluginManager());
		field = SimpleCommandMap.class.getDeclaredField("knownCommands");
		field.setAccessible(true);
		return (Map<String, Command>) field.get(map);
	}
}
