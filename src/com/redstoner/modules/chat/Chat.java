package com.redstoner.modules.chat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.coremods.moduleLoader.ModuleLoader;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.misc.Utils;
import com.redstoner.modules.Module;
import com.redstoner.modules.datamanager.DataManager;
import com.redstoner.modules.ignore.Ignore;

@Commands(CommandHolderType.File)
@AutoRegisterListener
@Version(major = 4, minor = 0, revision = 1, compatible = 4)
public class Chat implements Module, Listener
{
	
	@Override
	public void firstLoad()
	{
		Module.super.firstLoad();
		DataManager.setConfig("chat", " %n §7→§r %m");
		DataManager.setConfig("me", " §7- %n §7⇦ %m");
		DataManager.setConfig("say", " §7[§9%n§7]:§r %m");
		DataManager.setConfig("shrug", " %n §7→§r %m ¯\\_(ツ)_/¯");
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		String message = event.getMessage();
		event.setCancelled(true);
		broadcastFormatted("chat", player, message);
	}
	
	@Command(hook = "me")
	public boolean me(CommandSender sender, String message)
	{
		broadcastFormatted("me", sender, message);
		return true;
	}
	
	@Command(hook = "chat")
	public boolean chat(CommandSender sender, String message)
	{
		broadcastFormatted("chat", sender, message);
		return true;
	}
	
	@Command(hook = "say")
	public boolean say(CommandSender sender, String message)
	{
		String name;
		if (sender instanceof Player)
			name = ((Player) sender).getName();
		else
			name = "§9CONSOLE";
		broadcastFormatted("say", sender, message, name);
		return true;
	}
	
	@Command(hook = "sayn")
	public boolean say(CommandSender sender, String name, String message)
	{
		broadcastFormatted("say", sender, message, name);
		return true;
	}
	
	@Command(hook = "shrug")
	public boolean shrug(CommandSender sender, String message)
	{
		broadcastFormatted("shrug", sender, message);
		return true;
	}
	
	@Command(hook = "mute")
	public boolean mute(CommandSender sender, String player)
	{
		Player p = Bukkit.getPlayer(player);
		if (p == null)
		{
			getLogger().message(sender, true, "That player couldn't be found!");
			return true;
		}
		DataManager.setData(p, "muted", true);
		getLogger().message(sender, "Muted player &e" + Utils.getName(p) + "&7!");
		getLogger().message(p, "You have been &cmuted&7!");
		return true;
	}
	
	@Command(hook = "unmute")
	public boolean unmute(CommandSender sender, String player)
	{
		Player p = Bukkit.getPlayer(player);
		if (p == null)
		{
			getLogger().message(sender, true, "That player couldn't be found!");
			return true;
		}
		DataManager.setData(p, "muted", false);
		getLogger().message(sender, "Unmuted player &e" + Utils.getName(p) + "&7!");
		getLogger().message(p, "You have been &aunmuted&7!");
		return true;
	}
	
	public boolean broadcastFormatted(String format, CommandSender sender, String message)
	{
		return broadcastFormatted(format, sender, message, Utils.getName(sender));
	}
	
	public boolean broadcastFormatted(String format, CommandSender sender, String message, String name)
	{
		if ((boolean) DataManager.getOrDefault(sender, "muted", false))
		{
			Utils.noPerm(sender, "You have been muted!");
			getLogger().info(" &7User &e" + Utils.getName(sender) + " &7tried to &e" + format + " &7(&e" + message
					+ "&7) while being &cmuted&7.");
			return false;
		}
		String raw = (String) DataManager.getConfigOrDefault(format, " %n §7→§r %m");
		String formatted = raw.replace("%n", name).replace("%m", message);
		Utils.broadcast("", formatted, ModuleLoader.exists("Ignore") ? Ignore.getIgnoredBy(sender) : null);
		return true;
	}
}
