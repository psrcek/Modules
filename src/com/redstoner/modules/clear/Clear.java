package com.redstoner.modules.clear;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.modules.Module;

@Commands(CommandHolderType.String)
@Version(major = 4, minor = 0, revision = 0, compatible = 4)
public class Clear implements Module
{
	@Command(hook = "clear")
	public boolean clearInventory(CommandSender sender)
	{
		Player player = (Player) sender;
		Inventory inv = player.getInventory();
		for (int i = 0; i < 36; i++)
			inv.clear(i);
		getLogger().message(sender, "Cleared your inventory!");
		return true;
	}
	
	@Command(hook = "clearother")
	public boolean clearOtherInventory(CommandSender sender, String name)
	{
		Player player = Bukkit.getPlayer(name);
		if (player == null)
			getLogger().message(sender, true, "That player couldn't be found!");
		else
		{
			Inventory inv = player.getInventory();
			for (int i = 0; i < 36; i++)
				inv.clear(i);
			getLogger().message(sender, "Cleared " + player.getDisplayName() + "&7's inventory!");
		}
		return true;
	}
	
	// @noformat
	@Override
	public String getCommandString()
	{
		return "command clear{\n" + 
				"    [empty] {\n" + 
				"        help Clears your inventory;\n" + 
				"        type player;\n" + 
				"        perm utils.clear;\n" + 
				"        run clear;\n" + 
				"    }\n" + 
				"    [string:player] {\n" + 
				"        help Clears someone elses inventory;\n" + 
				"        perm utils.admin.clear;\n" + 
				"        run clearother player;\n" + 
				"    }\n" + 
				"}";
	}
	// @format
}
