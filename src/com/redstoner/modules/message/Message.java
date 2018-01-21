package com.redstoner.modules.message;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.nemez.cmdmgr.Command;
import com.nemez.cmdmgr.Command.AsyncType;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.coremods.moduleLoader.ModuleLoader;
import com.redstoner.misc.BroadcastFilter;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.misc.Utils;
import com.redstoner.modules.Module;
import com.redstoner.modules.datamanager.DataManager;
import com.redstoner.modules.ignore.Ignore;
import com.redstoner.modules.socialspy.Socialspy;

@Commands(CommandHolderType.File)
@Version(major = 4, minor = 0, revision = 3, compatible = 4)
public class Message implements Module
{
	HashMap<CommandSender, CommandSender> replyTargets = new HashMap<>();
	HashMap<Player, String> toggles = new HashMap<Player, String>();
	
	@Command(hook = "message", async = AsyncType.ALWAYS)
	public boolean message(CommandSender sender, String target, String message)
	{
		CommandSender p;
		if (target.equalsIgnoreCase("console"))
			p = Bukkit.getConsoleSender();
		else
			p = Bukkit.getPlayer(target);
		if (p == null)
		{
			getLogger().message(sender, true, "That player couldn't be found!");
			return true;
		}
		else if (ModuleLoader.exists("Ignore")? !Ignore.getIgnoredBy(sender).sendTo(p) : true) {
			getLogger().message(sender, true, Utils.getName(p) + " has ignored you. Your message was not sent.");
			return true;
		}
		else
		{
			if (ModuleLoader.getModule("Socialspy") != null)
				Socialspy.spyBroadcast(sender, p, message, "/m", new BroadcastFilter()
				{
					@Override
					public boolean sendTo(CommandSender recipient)
					{
						return !(recipient.equals(sender) || recipient.equals(p));
					}
				});
			
			net.nemez.chatapi.click.Message m = new net.nemez.chatapi.click.Message(sender, null);
			m.appendText("&6[&cme &6-> " + Utils.getName(p) + "&6] " + "§f" + message);
			m.send();
			
			net.nemez.chatapi.click.Message m2 = new net.nemez.chatapi.click.Message(p, null);
			m2.appendText("&6[" + Utils.getName(sender) + " &6-> &cme&6] " + "§f" + message);
			m2.send();
			
			replyTargets.put(sender, p);
			replyTargets.put(p, sender);
			
			if (DataManager.getState(p, "afk"))
			{
				getLogger().message(sender, "&5That player is currently AFK and may not respond!");
			}
		}
		return true;
	}
	
	@Command(hook = "reply", async = AsyncType.ALWAYS)
	public boolean reply(CommandSender sender, String message)
	{
		CommandSender target = replyTargets.get(sender);
		if (target == null || ((target instanceof OfflinePlayer) && !((OfflinePlayer) target).isOnline()))
		{
			getLogger().message(sender, true, "You don't have anyone to reply to!");
			return true;
		}
		else if (ModuleLoader.exists("Ignore")? !Ignore.getIgnoredBy(sender).sendTo(target) : true) {
			getLogger().message(sender, true, Utils.getName(target) + " has ignored you. Your message was not sent.");
			return true;
		}
		else
		{
			if (ModuleLoader.getModule("Socialspy") != null)
				Socialspy.spyBroadcast(sender, target, message, "/r", new BroadcastFilter()
				{
					@Override
					public boolean sendTo(CommandSender recipient)
					{
						return !(recipient.equals(sender) || recipient.equals(target));
					}
				});
			
			net.nemez.chatapi.click.Message m = new net.nemez.chatapi.click.Message(sender, null);
			m.appendText("&6[&cme &6-> " + Utils.getName(target) + "&6] " + "§f" + message);
			m.send();
			
			net.nemez.chatapi.click.Message m2 = new net.nemez.chatapi.click.Message(target, null);
			m2.appendText("&6[" + Utils.getName(sender) + " &6-> &cme&6] " + "§f" + message);
			m2.send();
		}
		replyTargets.put(sender, target);
		replyTargets.put(target, sender);
		return true;
	}
	
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
	
	@SuppressWarnings("unlikely-arg-type")
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		toggles.remove(event.getPlayer());
		String player = event.getPlayer().getName();
		if (toggles.containsValue(player))
			for (Entry<Player, String> entry : toggles.entrySet())
				if (entry.getValue().equals(player))
				{
					toggles.remove(player);
					getLogger().message(entry.getKey(),
							"We removed your pmtoggle for &6" + player + "&7, as they left the game.");
				}
	}
}
