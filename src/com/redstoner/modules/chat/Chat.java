package com.redstoner.modules.chat;

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
import com.redstoner.modules.ignore.Ignore;

import net.nemez.chatapi.ChatAPI;

@Commands(CommandHolderType.File)
@AutoRegisterListener
@Version(major = 4, minor = 0, revision = 0, compatible = 4)
public class Chat implements Module, Listener{
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		String msg = event.getMessage();
		event.setCancelled(true);
		
		if  (player.hasPermission("utils.chat"))
			Utils.broadcast(" " + Utils.getName(player), " §7→§r " + ChatAPI.colorify(player, msg),
					ModuleLoader.exists("Ignore")? Ignore.getIgnoredBy(player) : null);
		else
			player.sendMessage("§8[§cServer§8] You don't have permission to chat.");
	}
	
	@Command(hook = "me")
	public boolean me(CommandSender sender, String text)
	{
		String name;
		if (sender instanceof Player)
			name = ((Player) sender).getDisplayName();
		else
			name = "§9" + sender.getName();
		text = ChatAPI.colorify(sender, text);
		Utils.broadcast(" §7- " + name + " §7⇦ ", text,
				ModuleLoader.exists("Ignore")? Ignore.getIgnoredBy(sender) : null);
		return true;
	}
	
	@Command(hook = "chat")
	public boolean chat(CommandSender sender, String message)
	{
		String name = Utils.getName(sender);
		Utils.broadcast(" " + name, " §7→§r " + ChatAPI.colorify(sender, message),
				ModuleLoader.exists("Ignore")? Ignore.getIgnoredBy(sender) : null);
		return true;
	}
	
	@Command(hook = "say")
	public boolean say(CommandSender sender, String message)
	{
		String name = Utils.getName(sender);
		Utils.broadcast(" §7[§9" + name.replaceAll("[^0-9a-zA-Z§&\\[\\]]", "") + "§7]: ",
				"§r" + ChatAPI.colorify(null, message),
				ModuleLoader.exists("Ignore")? Ignore.getIgnoredBy(sender) : null);
		return true;
	}
	
	@Command(hook = "sayn")
	public boolean say(CommandSender sender, String name, String message)
	{
		Utils.broadcast(" §7[§9" + ChatAPI.colorify(sender, name) + "§7]: ", "§r" + ChatAPI.colorify(null, message),
				ModuleLoader.exists("Ignore")? Ignore.getIgnoredBy(sender) : null);
		return true;
	}
	
	@Command(hook = "shrug")
	public boolean shrug(CommandSender sender, String message)
	{
		String name = Utils.getName(sender);
		Utils.broadcast(" " + name, " §7→§r " + ChatAPI.colorify(sender, message) + " ¯\\_(ツ)_/¯",
				ModuleLoader.exists("Ignore")? Ignore.getIgnoredBy(sender) : null);
		return true;
	}
}
