package com.redstoner.modules.seen;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.earth2me.essentials.utils.DateUtil;
import com.nemez.cmdmgr.Command;
import com.nemez.cmdmgr.Command.AsyncType;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.misc.JsonManager;
import com.redstoner.misc.Main;
import com.redstoner.misc.Utils;
import com.redstoner.modules.Module;
import com.redstoner.modules.datamanager.DataManager;

@AutoRegisterListener
@Commands(CommandHolderType.File)
@Version(major = 4, minor = 0, revision = 0, compatible = 4)
public class Seen implements Module, Listener
{
	HashMap<UUID, JSONArray> names = new HashMap<>();
	HashMap<UUID, JSONArray> ips = new HashMap<>();
	
	@Override
	public void postEnable()
	{
		Module.super.postEnable();
		
	}
	
	@SuppressWarnings("deprecation")
	@Command(hook = "seen", async = AsyncType.ALWAYS)
	public boolean seen(CommandSender sender, String player)
	{
		ArrayList<String> message = new ArrayList<>();
		OfflinePlayer p = Bukkit.getPlayer(player);
		if (p == null)
		{
			p = Bukkit.getOfflinePlayer(player);
			if (p != null)
				p = Bukkit.getOfflinePlayer(p.getUniqueId());
		}
		if (p == null || !p.hasPlayedBefore())
		{
			getLogger().message(sender, true, "That player has never joined the server!");
			return true;
		}
		boolean cansee = (sender instanceof Player ? p instanceof Player && ((Player) sender).canSee((Player) p)
				: true);
		boolean online = cansee ? p instanceof Player : false;
		String state;
		long timestamp;
		if (online)
		{
			state = "&aonline";
			timestamp = (long) DataManager.getData(p.getUniqueId().toString(), "lastjoined");
		}
		else
		{
			state = "&coffline";
			timestamp = (long) DataManager.getOrDefault(p.getUniqueId().toString(), "lastquit", p.getLastPlayed());
		}
		String time = DateUtil.formatDateDiff(timestamp);
		message.add("The player &e" + p.getName() + " &7has been " + state + " &7since " + time + "&7.");
		JSONArray _names;
		if (online)
		{
			if (DataManager.getState((Player) p, "afk"))
				message.add("This player is currently &eAFK");
			String reason = (String) DataManager.getOrDefault(p.getUniqueId().toString(), "AFK", "afk_reason", "");
			if (reason.length() >= 1)
				message.add(" &5- " + reason);
			if (DataManager.getState((Player) p, "vanished"))
				message.add("This player is currently &evanished&7!");
			_names = names.get(p.getUniqueId());
		}
		else
		{
			_names = loadNames(p.getUniqueId());
		}
		if (_names != null && _names.size() > 1)
			message.add("This player has also been known as: &e"
					+ _names.toJSONString().replaceAll("[\"\\[\\]]", "").replace(",", "&7, &e"));
		if (sender.hasPermission("utils.seen.ip"))
		{
			JSONArray _ips;
			if (online)
				_ips = ips.get(p.getUniqueId());
			else
				_ips = loadIPs(p.getUniqueId());
			if (_names != null && _names.size() > 0)
				message.add("This player has joined with the following IPs: &e"
						+ _ips.toJSONString().replaceAll("[\"\\[\\]]", "").replace(",", "&7, &e"));
		}
		getLogger().message(sender, message.toArray(new String[] {}));
		return true;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		DataManager.setData(event.getPlayer(), "lastjoined", System.currentTimeMillis());
		DataManager.setData(event.getPlayer(), "ip", event.getPlayer().getAddress().getHostName());
		loadData(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		DataManager.setData(event.getPlayer(), "lastquit", System.currentTimeMillis());
		unloadData(event.getPlayer());
	}
	
	@SuppressWarnings("unchecked")
	public void loadData(Player player)
	{
		File jsonfile = new File(Main.plugin.getDataFolder(), "/seen/" + Utils.getID(player));
		JSONObject json = JsonManager.getObject(jsonfile);
		if (json == null)
		{
			json = new JSONObject();
			json.put("names", new JSONArray());
			json.put("ips", new JSONArray());
		}
		JSONArray names = (JSONArray) json.get("names");
		if (!names.contains(player.getName()))
			names.add(player.getName());
		json.put("names", names);
		
		JSONArray ips = (JSONArray) json.get("ips");
		String ip = player.getAddress().getHostName();
		if (!ips.contains(ip))
			ips.add(ip);
		json.put("ips", ips);
		
		this.names.put(player.getUniqueId(), names);
		this.ips.put(player.getUniqueId(), ips);
		JsonManager.save(json, jsonfile);
	}
	
	public JSONArray loadNames(UUID uuid)
	{
		File jsonfile = new File(Main.plugin.getDataFolder(), "/seen/" + uuid);
		JSONObject json = JsonManager.getObject(jsonfile);
		if (json == null)
			return null;
		else
			return (JSONArray) json.get("names");
	}
	
	public JSONArray loadIPs(UUID uuid)
	{
		File jsonfile = new File(Main.plugin.getDataFolder(), "/seen/" + uuid);
		JSONObject json = JsonManager.getObject(jsonfile);
		if (json == null)
			return null;
		else
			return (JSONArray) json.get("ips");
	}
	
	public void unloadData(Player player)
	{
		this.names.remove(player.getUniqueId());
	}
}
