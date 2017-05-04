package com.redstoner.modules.scoreboard;

import java.io.File;

import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;

import com.redstoner.misc.JsonManager;
import com.redstoner.misc.Main;

public class ProjectManager
{
	private static File dataFile = new File(Main.plugin.getDataFolder(), "projectIDs.json");
	private static JSONObject IDs;
	
	static
	{
		IDs = JsonManager.getObject(dataFile);
		if (IDs == null)
			IDs = new JSONObject();
	}
	
	/** This method generates a unique identifier for a project by name.
	 * 
	 * @param name The name of the project.
	 * @return The project identifier, as [name]#[ID]. */
	@SuppressWarnings("unchecked")
	public static String getNextID(String name)
	{
		Object raw = IDs.get(name);
		int i = 1;
		if (raw != null)
			i = (int) raw + 1;
		IDs.put(name, i);
		save();
		return name + "#" + i;
	}
	
	/** Saves the current state into a file. */
	private static void save()
	{
		JsonManager.save(IDs, dataFile);
	}
	
	/** This method finds the currently active project by a CommandSender and will return it as part of the tree.
	 * 
	 * @param sender The sender of whom the project is to be found.
	 * @return the currently active project of the sender or null, if none are active. */
	public static Project getCurrentProject(CommandSender sender)
	{
		return null;
	}
}
