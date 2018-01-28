package com.redstoner.modules.vanish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.misc.Utils;
import com.redstoner.modules.Module;
import com.redstoner.modules.datamanager.DataManager;

@Commands(CommandHolderType.File)
@AutoRegisterListener
@Version(major = 4, minor = 1, revision = 0, compatible = 4)
public class Vanish implements Module, Listener
{
	private ArrayList<UUID> vanished = new ArrayList<>();
	List<String> imouted = new ArrayList<String>();
	private HashMap<UUID, ArrayList<UUID>> vanishOthers = new HashMap<>();
	
	@Override
	public void migrate(Version old)
	{
		Module.super.migrate(old);
		if ((old.major() == 4) && (old.minor() == 0) && (old.revision() <= 1))
		{
			DataManager.setConfig("indicator", "&7[V]");
		}
	}
	
	@Command(hook = "vanish")
	public boolean vanish(CommandSender sender)
	{
		UUID uid = ((Player) sender).getUniqueId();
		if (vanished.contains(uid))
		{
			vanished.remove(uid);
			getLogger().message(sender, "You are no longer vanished!");
			unvanishPlayer((Player) sender);
		}
		else
		{
			vanished.add(uid);
			getLogger().message(sender, "You are now vanished!");
			vanishPlayer((Player) sender);
		}
		return true;
	}
	
	@Command(hook = "vanish_on")
	public boolean vanishOn(CommandSender sender)
	{
		UUID uid = ((Player) sender).getUniqueId();
		if (vanished.contains(uid))
			getLogger().message(sender,
					"You were already vanished, however we refreshed the vanish for you just to be sure!");
		else
		{
			vanished.add(uid);
			getLogger().message(sender, "You are now vanished!");
		}
		vanishPlayer((Player) sender);
		return true;
	}
	
	@Command(hook = "vanish_off")
	public boolean vanishOff(CommandSender sender)
	{
		UUID uid = ((Player) sender).getUniqueId();
		if (!vanished.contains(uid))
			getLogger().message(sender,
					"You were not vanished, however we refreshed the vanish for you just to be sure!");
		else
		{
			vanished.remove(uid);
			getLogger().message(sender, "You are no longer vanished!");
		}
		unvanishPlayer((Player) sender);
		return true;
	}
	
	@Command(hook = "vanish_other")
	public boolean vanishOther(CommandSender sender, String name)
	{
		Player player = Bukkit.getPlayer(name);
		if (player == null)
		{
			getLogger().message(sender, "&cPlayer &6" + name + " &ccould not be found!");
			return true;
		}
		UUID uid = player.getUniqueId();
		if (player.hasPermission("utils.vanish"))
		{
			if (vanished.contains(uid))
			{
				vanished.remove(uid);
				getLogger().message(sender, "Successfully unvanished &e" + player.getDisplayName());
				getLogger().message(player, "You are no longer vanished!");
			}
			else
			{
				vanished.add(uid);
				getLogger().message(sender, "Successfully vanished &e" + player.getDisplayName());
				getLogger().message(player, "You are now vanished!");
			}
			return true;
		}
		for (Entry<UUID, ArrayList<UUID>> entry : vanishOthers.entrySet())
		{
			if (entry.getValue().contains(uid))
			{
				entry.getValue().remove(uid);
				getLogger().message(sender, "Successfully unvanished &e" + player.getDisplayName());
				getLogger().message(player, "You are no longer vanished!");
				if (entry.getValue().size() == 0)
					vanishOthers.remove(entry.getKey());
				return true;
			}
		}
		UUID uuid = ((Player) sender).getUniqueId();
		ArrayList<UUID> toAddTo = vanishOthers.get(uuid);
		if (toAddTo == null)
			toAddTo = new ArrayList<>();
		toAddTo.add(uid);
		vanishOthers.put(uuid, toAddTo);
		getLogger().message(sender, "Successfully vanished &e" + player.getDisplayName());
		getLogger().message(player, "You are now vanished!");
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		DataManager.setState(player, "vanished", vanished.contains(player.getUniqueId()));
		if (vanished.contains(player.getUniqueId()))
		{
			for (Player p : Bukkit.getOnlinePlayers())
			{
				if (!p.hasPermission("utils.vanish"))
					p.hidePlayer(player);
			}
			event.setJoinMessage(null);
		}
		if (player.hasPermission("utils.vanish"))
			return;
		for (UUID uid : vanished)
		{
			Player p = Bukkit.getPlayer(uid);
			if (p == null)
				continue;
			player.hidePlayer(p);
		}
		for (Entry<UUID, ArrayList<UUID>> entry : vanishOthers.entrySet())
		{
			for (UUID uid : entry.getValue())
			{
				Player p = Bukkit.getPlayer(uid);
				if (p == null)
					continue;
				player.hidePlayer(p);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeave(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		UUID uid = player.getUniqueId();
		if (vanished.contains(player.getUniqueId()))
		{
			event.setQuitMessage(null);
		}
		if (vanishOthers.containsKey(uid))
		{
			ArrayList<UUID> toUnvanish = vanishOthers.remove(uid);
			for (UUID uuid : toUnvanish)
			{
				Player p = Bukkit.getPlayer(uuid);
				if (p != null)
					unvanishPlayer(p);
			}
		}
		boolean wasVanished = false;
		for (Entry<UUID, ArrayList<UUID>> entry : vanishOthers.entrySet())
		{
			if (entry.getValue().contains(uid))
			{
				entry.getValue().remove(uid);
				wasVanished = true;
				break;
			}
		}
		if (wasVanished)
			unvanishPlayer(player);
	}
	
	@SuppressWarnings("deprecation")
	private void vanishPlayer(Player player)
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if (!p.hasPermission("utils.vanish"))
				p.hidePlayer(player);
		}
		DataManager.setState(player, "vanished", true);
		DataManager.setData(Utils.getID(player), "Seen", "lastquit", System.currentTimeMillis());
	}
	
	@SuppressWarnings("deprecation")
	private void unvanishPlayer(Player player)
	{
		for (Player p : Bukkit.getOnlinePlayers())
			p.showPlayer(player);
		DataManager.setState(player, "vanished", false);
		DataManager.setData(Utils.getID(player), "Seen", "lastjoined", System.currentTimeMillis());
	}
	
	@Command(hook = "imout")
	public void onImoutCommand(CommandSender sender)
	{
		String symbol;
		Player s = (Player) sender;
		String name = sender.getName();
		if (imouted.contains(name))
		{
			symbol = "§a§l+";
			getLogger().message(sender, "§eWelcome back! You are no longer hidden", "");
			s.performCommand("vanish off");
			s.performCommand("act off");
			imouted.remove(name);
		}
		else
		{
			symbol = "§c§l-";
			getLogger().message(sender, "§e§oPoof!§e You are now gone!", "");
			s.performCommand("vanish on");
			s.performCommand("act on");
			imouted.add(name);
		}
		Utils.broadcast(symbol, " §7" + name, null);
	}
}
