package com.redstoner.modules.ignore;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;

import com.nemez.cmdmgr.Command;
import com.nemez.cmdmgr.Command.AsyncType;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.coremods.moduleLoader.ModuleLoader;
import com.redstoner.misc.BroadcastFilter;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.misc.Utils;
import com.redstoner.modules.Module;
import com.redstoner.modules.datamanager.DataManager;

import net.nemez.chatapi.click.Message;

@Commands(CommandHolderType.File)
@AutoRegisterListener
@Version(major = 4, minor = 0, revision = 0, compatible = 4)
public class Ignore implements Module
{
	
	@Command(hook = "unignore", async = AsyncType.ALWAYS)
	public boolean unignore(CommandSender sender, String player)
	{
		return ignore(sender, player, false);
	}
	
	@Command(hook = "ignore", async = AsyncType.ALWAYS)
	public boolean ignore(CommandSender sender, String player)
	{
		return ignore(sender, player, true);
	}
	
	@Command(hook = "list", async = AsyncType.ALWAYS)
	public boolean list(CommandSender sender)
	{
		getLogger().message(sender, "§7You are currently ignoring:");
		
		JSONArray ignores = (JSONArray) DataManager.getOrDefault(sender, "ignores", new JSONArray());
		
		if (ignores.isEmpty())
		{
			new Message(sender, null).appendText(" §7Nobody \\o/").send();
			return true;
		}
		
		String players;
		OfflinePlayer pi = Bukkit.getOfflinePlayer(UUID.fromString((String) ignores.get(0)));
		players = " §3" + pi.getName() + "§7";
		
		for (int i = 1; i < ignores.size(); i++)
		{
			OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString((String) ignores.get(i)));
			players += ", §3" + p.getName() + "§7";
		}
		
		Message m = new Message(sender, null);
		m.appendText(players);
		m.send();
		return true;
	}
	
	@SuppressWarnings({"unchecked", "deprecation"})
	public boolean ignore(CommandSender sender, String player, boolean allowIgnore)
	{
		JSONArray ignores = (JSONArray) DataManager.getOrDefault(sender, "ignores", new JSONArray());
		
		Player p = Utils.isUUID(player) ? Bukkit.getPlayer(UUID.fromString(player)) : Bukkit.getPlayer(player);
		
		OfflinePlayer op = Utils.isUUID(player) ? Bukkit.getOfflinePlayer(UUID.fromString(player))
				: Bukkit.getOfflinePlayer(player);
		
		String pName = p != null ? p.getDisplayName() : op.getName();
		String pUUID = p != null ? p.getUniqueId().toString() : op.getUniqueId().toString();
		String sUUID = ((Player) sender).getUniqueId().toString();
		
		if (pUUID.equals(sUUID))
		{
			getLogger().message(sender, true, "§7You can't ignore yourself :P");
			return true;
		}
		
		if (ignores.contains(pUUID))
		{
			ignores.remove(pUUID);
			getLogger().message(sender, "§7You are no longer ignoring §3" + pName + "§7.");
		}
		else if (!allowIgnore)
		{
			getLogger().message(sender, "§7You weren't ignoring §3" + pName + "§7.");
		}
		else
		{
			ignores.add(pUUID);
			getLogger().message(sender, "§7You are now ignoring §3" + pName + "§7.");
		}
		DataManager.setData(sender, "ignores", ignores);
		return true;
		
	}
	
	public static BroadcastFilter getIgnoredBy(CommandSender sender)
	{
		try
		{
			Module mod = ModuleLoader.getModule("Ignore");
			Method m = mod.getClass().getDeclaredMethod("_getIgnoredBy", CommandSender.class);
			m.setAccessible(true);
			return (BroadcastFilter) m.invoke(mod, sender);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
		return null;
	}
	
	@SuppressWarnings("unused")
	private BroadcastFilter _getIgnoredBy(CommandSender sender)
	{
		return new BroadcastFilter()
		{
			
			private final String sUUID = sender instanceof Player ? ((Player) sender).getUniqueId().toString()
					: "CONSOLE";
			
			@Override
			public boolean sendTo(CommandSender recipient)
			{
				if (sUUID.equals("CONSOLE"))
					return true;
				
				if (recipient instanceof Player)
				{
					Player player = (Player) recipient;
					
					if (sender.hasPermission("utils.ignore.override"))
						return true;
					
					JSONArray ignores = (JSONArray) DataManager.getOrDefault(recipient, "ignores", new JSONArray());
					return !ignores.contains(sUUID);
				}
				else
					return true;
			}
		};
	}
	
}
