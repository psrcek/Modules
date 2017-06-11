package com.redstoner.modules.naming;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.Version;
import com.redstoner.misc.Utils;
import com.redstoner.modules.Module;

import net.md_5.bungee.api.ChatColor;

@Version(major = 2, minor = 1, revision = 3, compatible = 2)

public class Naming implements Module
{
	@Command(hook = "anvil")
	public void anvil(CommandSender sender)
	{
		Player player = (Player) sender;
		Inventory inv = Bukkit.getServer().createInventory(player, InventoryType.ANVIL);
		player.openInventory(inv);
	}
	
	@Command(hook = "name")
	public void name(CommandSender sender, String name)
	{
		name = ChatColor.translateAlternateColorCodes('&', name);
		ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
		{
			Utils.sendErrorMessage(sender, null, "You can not rename that item!");
			return;
		}
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		Utils.sendMessage(sender, null, "Name set to " + name);
	}
	
	@Command(hook = "lore")
	public void lore(CommandSender sender, boolean append, String lore)
	{
		ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
		{
			Utils.sendErrorMessage(sender, null, "You can not change the lore of that item!");
			return;
		}
		List<String> currentLore;
		if (append)
			currentLore = meta.getLore();
		else
			currentLore = new ArrayList<String>();
		if (currentLore == null)
			currentLore = new ArrayList<String>();
		lore = ChatColor.translateAlternateColorCodes('&', lore);
		currentLore.add(lore);
		meta.setLore(currentLore);
		item.setItemMeta(meta);
		Utils.sendMessage(sender, null, "Lore set to " + lore);
	}
	
	// @noformat
	@Override
	public String getCommandString()
	{
		return "command anvil {\n" + 
		"	[empty] {\n" + 
		"		run anvil;\n" + 
		"		type player;\n" + 
		"		help Opens anvil GUI.;\n" + 
		"		perm utils.anvil;\n" + 
		"	}\n" + 
		"}\n" +
		"command name {\n" + 
		"	[string:name...] {\n" + 
		"		run name name;\n" + 
		"		type player;\n" + 
		"		help Names item in hand.;\n" + 
		"		perm utils.name;\n" + 
		"	}\n" + 
		"}\n" +
		"command lore {\n" + 
		"    [optional:-a] [string:lore...] {\n" + 
		"		run lore -a lore;\n" + 
		"		type player;\n" + 
		"		help Adds lore to item in hand.;\n" + 
		"		perm utils.lore;\n" + 
		"	}\n" + 
		"}";
	}
	// @format
}
