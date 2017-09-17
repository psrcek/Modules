package com.redstoner.modules.tag;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nemez.cmdmgr.Command;
import com.nemez.cmdmgr.Command.AsyncType;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.misc.JsonManager;
import com.redstoner.misc.Main;
import com.redstoner.modules.Module;

@Commands(CommandHolderType.String)
@Version(major = 4, minor = 0, revision = 0, compatible = 4)
public class Tag implements Module
{
	private File tagLocation = new File(Main.plugin.getDataFolder(), "tag.json");
	private JSONObject tags;
	
	@Override
	public boolean onEnable()
	{
		tags = JsonManager.getObject(tagLocation);
		if (tags == null)
			tags = new JSONObject();
		return true;
	}
	
	@Override
	public void onDisable()
	{
		saveTags();
	}
	
	@SuppressWarnings({"deprecation", "unchecked"})
	@Command(hook = "addtag", async = AsyncType.ALWAYS)
	public boolean addTag(CommandSender sender, String name, String tag)
	{
		OfflinePlayer player = Bukkit.getOfflinePlayer(name);
		if (player == null)
		{
			getLogger().message(sender, true, "That player doesn't exist!");
			return true;
		}
		UUID uuid = player.getUniqueId();
		JSONArray tagArray;
		if (tags.containsKey(uuid.toString()))
			tagArray = (JSONArray) tags.get(uuid.toString());
		else
			tagArray = new JSONArray();
		tagArray.add(tag);
		if (!tags.containsKey(uuid.toString()))
			tags.put(uuid.toString(), tagArray);
		getLogger().message(sender, "Successfully added note &e" + tag + " &7to player &e" + name + "&7!");
		saveTags();
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Command(hook = "deltag", async = AsyncType.ALWAYS)
	public boolean delTag(CommandSender sender, String name, int id)
	{
		if (id < 1)
		{
			getLogger().message(sender, true, "The ID you entered is too small, it must be at least 1!");
			return true;
		}
		OfflinePlayer player = Bukkit.getOfflinePlayer(name);
		if (player == null)
		{
			getLogger().message(sender, true, "That player doesn't exist!");
			return true;
		}
		UUID uuid = player.getUniqueId();
		if (!tags.containsKey(uuid.toString()))
		{
			getLogger().message(sender, true, "There are no notes about that player.");
			return true;
		}
		JSONArray tagArray = (JSONArray) tags.get(uuid.toString());
		int size = tagArray.size();
		if (size == 0)
		{
			getLogger().message(sender, true, "There are no notes about that player.");
			tags.remove(uuid.toString());
			saveTags();
			return true;
		}
		if (id > size)
		{
			getLogger().message(sender, true, "The number you entered is too big! It must be at most " + size + "!");
			return true;
		}
		getLogger().message(sender, "Successfully removed note: &e" + tagArray.remove(id - 1));
		if (tagArray.size() == 0)
			tags.remove(uuid.toString());
		saveTags();
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Command(hook = "checktag", async = AsyncType.ALWAYS)
	public boolean checkTags(CommandSender sender, String name)
	{
		OfflinePlayer player = Bukkit.getOfflinePlayer(name);
		if (player == null)
		{
			getLogger().message(sender, true, "That player doesn't exist!");
			return true;
		}
		UUID uuid = player.getUniqueId();
		if (!tags.containsKey(uuid.toString()))
		{
			getLogger().message(sender, "There are no notes about that player.");
			return true;
		}
		JSONArray tagArray = (JSONArray) tags.get(uuid.toString());
		int size = tagArray.size();
		if (size == 0)
		{
			tags.remove(uuid.toString());
			saveTags();
			return true;
		}
		ArrayList<String> message = new ArrayList<String>();
		message.add("There are &e" + size + "&7 notes about this player:");
		for (int i = 0; i < size; i++)
			message.add("&a" + (i + 1) + "&8: &e" + tagArray.get(i));
		getLogger().message(sender, message.toArray(new String[] {}));
		return true;
	}
	
	public void saveTags()
	{
		JsonManager.save(tags, tagLocation);
	}
	
	// @noformat
	@Override
	public String getCommandString()
	{
		return "command tag {\n" + 
				"    add [string:player] [string:tag...] {\n" + 
				"        help Tags a player.;\n" + 
				"        run addtag player tag;\n" + 
				"        perm utils.tag;\n" + 
				"    }\n" + 
				"    del [string:player] [int:id] {\n" + 
				"        help Removes a tag.;\n" + 
				"        run deltag player id;\n" + 
				"        perm utils.tag;\n" + 
				"    }\n" + 
				"    check [string:player] {\n" + 
				"        help Lists all tags of a player.;\n" + 
				"        run checktag player;\n" + 
				"        perm utils.tag;\n" + 
				"    }\n" + 
				"}";
	}
	// @format
}
