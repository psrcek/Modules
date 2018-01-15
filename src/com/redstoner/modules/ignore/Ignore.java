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
@Version(major = 4, minor = 0, revision = 0, compatible = 4)
public class Ignore implements Module{
	
	
	@Command(hook = "unignore")
	public boolean unignore(CommandSender sender, String player)
	{
		return ignore(sender,player,false);
	}
	
	@Command(hook = "ignore")
	public boolean ignore(CommandSender sender, String player)
	{
		return ignore(sender, player, true);
	}
	
	@Command(hook = "list")
	public boolean list(CommandSender sender) {
		getLogger().message(sender, "§7You are currently ignoring:");
		
		JSONArray ignores = (JSONArray) DataManager.getOrDefault(sender, "ignores", new JSONArray());
		
		String players;
		if ( ignores.isEmpty() )
			players = " §7Nobody \\o/";
		
		OfflinePlayer pi = Bukkit.getOfflinePlayer(UUID.fromString((String)ignores.get(0)));
		players = " §3" + pi.getName() + "§7";
		
		for (int i = 1; i < ignores.size(); i++) {
			OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString((String)ignores.get(i)));
			players += ", §3" + p.getName() + "§7"; 
		}
		Message m = new Message(sender, null);
		m.appendText(players);
		m.send();
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean ignore(CommandSender sender, String player, boolean allowIgnore)
	{
		JSONArray isIgnoredBy;
		JSONArray ignores = (JSONArray) DataManager.getOrDefault(sender, "ignores", new JSONArray());
		String uuidOfSender = ((Player)sender).getUniqueId().toString();
		String uuidOfPlayer = player;
		
		if ( sender.getName().equals(player) )
		{
			getLogger().message(sender, true, "You can't ignore yourself. :P");
			return true;
		}
		
		if ( Utils.isUUID(player) )
		{
			isIgnoredBy = (JSONArray) DataManager.getOrDefault(player, "isIgnoredBy", new JSONArray());
			
			if (isIgnoredBy.contains(uuidOfSender))
			{
				isIgnoredBy.remove(uuidOfSender);
				ignores.remove(uuidOfPlayer);
				getLogger().message(sender, "§7You are no longer ignoring §3" + player + "§7.");
			}
			else if (allowIgnore)
			{
				isIgnoredBy.add(uuidOfSender);
				ignores.add(uuidOfPlayer);
				getLogger().message(sender, "§7You are now ignoring §3" + player + "§7.");
			}
			else
			{
				getLogger().message(sender, "§7You weren't ignoring §3" + player + "§7.");
				return true;
			}
			
			DataManager.setData(player, "isIgnoredBy", isIgnoredBy);
			DataManager.setData(sender, "ignores", ignores);
		}
		else
		{
			Player toBeIgnored = Bukkit.getPlayer(player);
			if (toBeIgnored != null)
			{
				isIgnoredBy = (JSONArray) DataManager.getOrDefault(toBeIgnored, "isIgnoredBy", new JSONArray());
				uuidOfPlayer = toBeIgnored.getUniqueId().toString();
			}
			else
			{
				getLogger().message(sender, true, "§3" + player + "§7 isn't online. To ignore them, use their UUID.");
				return true;
			}
			
			if (isIgnoredBy.contains(uuidOfSender))
			{
				isIgnoredBy.remove(uuidOfSender);
				ignores.remove(uuidOfPlayer);
				getLogger().message(sender, "§7You are no longer ignoring §3" + toBeIgnored.getDisplayName() + "§7.");
			}
			else if (allowIgnore)
			{
				isIgnoredBy.add(uuidOfSender);
				ignores.add(uuidOfPlayer);
				getLogger().message(sender, "§7You are now ignoring §3" + toBeIgnored.getDisplayName() + "§7.");
			}
			else
			{
				getLogger().message(sender, "§7You weren't ignoring §3" + toBeIgnored.getDisplayName() + "§7.");
				return true;
			}
			
			DataManager.setData(toBeIgnored, "isIgnoredBy", isIgnoredBy);
			DataManager.setData(sender, "ignores", ignores);
		}
		
		
		return true;
	}
	
	public static BroadcastFilter getIgnoredBy(CommandSender sender) {
		try
	    {
	        Module mod = ModuleLoader.getModule("Ignore");
	        Method m = mod.getClass().getDeclaredMethod("_getIgnoredBy", CommandSender.class,
	                Object.class);
	        m.setAccessible(true);
	        return (BroadcastFilter) m.invoke(mod, sender);
	    }
	    catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
	            | InvocationTargetException e)
	    {}
		return null;
	}
	
	@SuppressWarnings("unused")
	private BroadcastFilter _getIgnoredBy(CommandSender sender) {
		JSONArray isIgnoredBy = (JSONArray) DataManager.getOrDefault(sender, "isIgnoredBy", new JSONArray());
		return new BroadcastFilter() {
			@Override
			public boolean sendTo(CommandSender recipient) {
				
				if (recipient instanceof Player)
				{
					Player player = (Player) recipient;
					
					if (player.hasPermission("utils.ignore.override"))
						return true;
					return !isIgnoredBy.contains( player.getUniqueId().toString() );
				}
				else
					return true;
			}
		};
	}
	
		
}
