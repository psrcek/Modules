package com.redstoner.modules.pmtoggle;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.nemez.cmdmgr.Command;
import com.nemez.cmdmgr.Command.AsyncType;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.modules.Module;

@Commands(CommandHolderType.String)
@AutoRegisterListener
@Version(major = 4, minor = 0, revision = 0, compatible = 4)
public class Pmtoggle implements Module, Listener
{
	HashMap<Player, String> toggles = new HashMap<Player, String>();
	
	@Command(hook = "pmtoggle_off", async = AsyncType.ALWAYS)
	public boolean pmtoggle_off(CommandSender sender)
	{
		Player player = (Player) sender;
		if (toggles.remove(player) != null)
			getLogger().message(player, "Your pmtoggle was removed!");
		else
			getLogger().message(player, "You didn't have pmtoggle enabled! Use /pmtoggle <player> to enabled it.");
		return true;
	}
	
	@Command(hook = "pmtoggle", async = AsyncType.ALWAYS)
	public boolean pmtoggle(CommandSender sender, String player)
	{
		Player p = Bukkit.getPlayer(player);
		if (p == null && !player.equals("CONSOLE"))
		{
			getLogger().message(sender, "§cThat player couldn't be found!");
			return true;
		}
		toggles.put((Player) sender, player);
		getLogger().message(sender, "Locked your pmtoggle onto §6" + player + "§7.");
		return true;
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		if (toggles.containsKey(player))
		{
			Bukkit.dispatchCommand(player, "m " + toggles.get(player) + " " + event.getMessage());
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		toggles.remove(event.getPlayer());
		String player = event.getPlayer().getName();
		if (toggles.containsValue(player))
		{
			for (Entry<Player, String> entry : toggles.entrySet())
			{
				if (entry.getValue().equals(player))
				{
					toggles.remove(player);
					getLogger().message(entry.getKey(),
							"We removed your pmtoggle for &6" + player + "&7, as they left the game.");
				}
			}
		}
	}
	
	// @noformat
	@Override
	public String getCommandString()
	{
		return "command pmtoggle {\n" + 
				"    [empty] {\n" + 
				"        help Turns off your toggle.;\n" + 
				"        type player;\n" + 
				"        run pmtoggle_off;\n" + 
				"    }\n" + 
				"    [string:player] {\n" + 
				"        help Turns on your pmtoggle and locks onto <player>.;\n" + 
				"        type player;\n" + 
				"        run pmtoggle player;\n" + 
				"    }\n" + 
				"}";
	}
	// @format
}
