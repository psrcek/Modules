package com.redstoner.modules.message;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.nemez.cmdmgr.Command;
import com.nemez.cmdmgr.Command.AsyncType;
import com.nemez.cmdmgr.CommandManager;
import com.redstoner.annotations.Version;
import com.redstoner.coremods.moduleLoader.ModuleLoader;
import com.redstoner.misc.BroadcastFilter;
import com.redstoner.misc.Main;
import com.redstoner.misc.Utils;
import com.redstoner.modules.Module;
import com.redstoner.modules.socialspy.Socialspy;

@Version(major = 3, minor = 3, revision = 2, compatible = 3)
public class Message implements Module
{
	HashMap<CommandSender, CommandSender> replyTargets = new HashMap<CommandSender, CommandSender>();
	
	@Override
	public void postEnable()
	{
		CommandManager.registerCommand(getClass().getResourceAsStream("Message.cmd"), this, Main.plugin);
	}
	
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
			Utils.sendErrorMessage(sender, null, "That player couldn't be found!");
			return true;
		}
		else
		{
			message = Utils.colorify(message, sender);
			if (ModuleLoader.getModule("Socialspy") != null)
				Socialspy.spyBroadcast(sender, p, message, "/m", new BroadcastFilter()
				{
					@Override
					public boolean sendTo(CommandSender recipient)
					{
						return !(recipient.equals(sender) || recipient.equals(target));
					}
				});
			Utils.sendMessage(sender, "&6[&cme &6-> " + Utils.getName(p) + "&6] ", "§f" + message, '&');
			Utils.sendMessage(p, "&6[" + Utils.getName(sender) + " &6-> &cme&6] ", "§f" + message, '&');
			replyTargets.put(sender, p);
			replyTargets.put(p, sender);
		}
		return true;
	}
	
	@Command(hook = "reply", async = AsyncType.ALWAYS)
	public boolean reply(CommandSender sender, String message)
	{
		CommandSender target = replyTargets.get(sender);
		if (target == null || ((target instanceof OfflinePlayer) && !((OfflinePlayer) target).isOnline()))
		{
			Utils.sendErrorMessage(sender, null, "You don't have anyone to reply to!");
			return true;
		}
		else
		{
			message = Utils.colorify(message, sender);
			if (ModuleLoader.getModule("Socialspy") != null)
				Socialspy.spyBroadcast(sender, target, message, "/m", new BroadcastFilter()
				{
					@Override
					public boolean sendTo(CommandSender recipient)
					{
						return !(recipient.equals(sender) || recipient.equals(target));
					}
				});
			Utils.sendMessage(sender, "&6[&cme &6-> " + Utils.getName(target) + "&6] ", "§f" + message, '&');
			Utils.sendMessage(target, "&6[" + Utils.getName(sender) + " &6-> &cme&6] ", "§f" + message, '&');
		}
		replyTargets.put(sender, target);
		replyTargets.put(target, sender);
		return true;
	}
}
