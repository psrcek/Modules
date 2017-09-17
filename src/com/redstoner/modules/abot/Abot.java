package com.redstoner.modules.abot;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.misc.JsonManager;
import com.redstoner.misc.Main;
import com.redstoner.modules.Module;

@Commands(CommandHolderType.String)
@AutoRegisterListener
@Version(major = 4, minor = 0, revision = 0, compatible = 4)
public class Abot implements Module, Listener
{
	private File answerFile = new File(Main.plugin.getDataFolder(), "abot.json");
	JSONArray answers;
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		for (Object rawObject : answers)
		{
			JSONObject entry = (JSONObject) rawObject;
			JSONArray regexes = (JSONArray) entry.get("regex");
			for (Object regex : regexes)
			{
				if (event.getMessage().toLowerCase().matches((String) regex))
				{
					Object hideperm = entry.get("hide-perm");
					if (hideperm == null || !event.getPlayer().hasPermission((String) hideperm))
					{
						event.setCancelled(true);
						getLogger().message(event.getPlayer(), (String) entry.get("message"));
						return;
					}
				}
			}
		}
	}
	
	@Command(hook = "abot_reload")
	public void loadAnswers(CommandSender sender)
	{
		answers = JsonManager.getArray(answerFile);
		if (answers == null)
			answers = new JSONArray();
		getLogger().message(sender, "Loaded the abot.json file!");
	}
	
	@Override
	public boolean onEnable()
	{
		loadAnswers(Bukkit.getConsoleSender());
		return true;
	}
	
	// @noformat
	@Override
	public String getCommandString()
	{
		return "command abot {\n" +
				"	reload {" +
				"		help Reloads answers from the .json file.;\n" +
				"		run abot_reload;\n" +
				"		perm utils.abot.reload;" +
				"	}\n" +
				"}";
	}
	// format
}
