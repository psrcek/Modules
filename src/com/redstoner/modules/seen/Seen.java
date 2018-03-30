package com.redstoner.modules.seen;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
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
@Version(major = 4, minor = 0, revision = 6, compatible = 4)
public class Seen implements Module, Listener
{
	HashMap<UUID, JSONArray> names = new HashMap<>();
	HashMap<UUID, JSONArray> ips = new HashMap<>();
	
	@Override
	public void postEnable()
	{
		Module.super.postEnable();
		for (Player player : Bukkit.getOnlinePlayers())
			loadData(player);
	}
	
	@Command(hook = "seen", async = AsyncType.ALWAYS)
	public boolean seen(CommandSender sender, String player)
	{
		return seen(sender, player, false);
	}
	
	@SuppressWarnings("deprecation")
	@Command(hook = "seen2", async = AsyncType.ALWAYS)
	public boolean seen(CommandSender sender, String player, boolean show_ips)
	{
		ArrayList<String> message = new ArrayList<>();
		OfflinePlayer p = Bukkit.getPlayer(player);
		boolean cansee = (sender instanceof Player ? p instanceof Player && ((Player) sender).canSee((Player) p)
				: true);
		if (p == null)
		{
			p = Bukkit.getOfflinePlayer(player);
			if (p != null)
				p = Bukkit.getOfflinePlayer(p.getUniqueId());
		}
		if (p == null || (!p.isOnline() && !p.hasPlayedBefore()) || (!cansee && !p.getName().equalsIgnoreCase(player)))
		{
			getLogger().message(sender, true, "§e" + player + "§7 has never joined the server!");
			return true;
		}
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
		message.add("&e" + p.getName() + " &7has been " + state + " &7for &e" + time + "&7.");
		JSONArray _names;
		if (online)
		{
			if (DataManager.getState((Player) p, "afk"))
			{
				message.add("They're currently &eAFK&7:");
				String reason = (String) DataManager.getOrDefault(p.getUniqueId().toString(), "AFK", "afk_reason", "");
				if (reason.length() >= 1)
					message.add(" &5- " + reason);
			}
			if (DataManager.getState((Player) p, "vanished"))
				message.add("They're currently &evanished&7!");
			_names = names.get(p.getUniqueId());
		}
		else
		{
			_names = loadNames(p.getUniqueId());
		}
		if (_names != null && _names.size() > 1)
			message.add("They've also been known as: &e"
					+ _names.toJSONString().replaceAll("[\"\\[\\]]", "").replace(",", "&7, &e"));
		if (sender.hasPermission("utils.seen.ip"))
		{
			if (show_ips)
			{
				JSONArray _ips;
				if (online)
					_ips = ips.get(p.getUniqueId());
				else
					_ips = loadIPs(p.getUniqueId());
				if (_ips != null && _ips.size() > 0)
					message.add("They've joined with the following IPs: &e"
							+ _ips.toJSONString().replaceAll("[\"\\[\\]]", "").replace(",", "&7, &e"));
			}
			message.add(
					"Their current IP is: &a" + DataManager.getOrDefault(p.getUniqueId().toString(), "ip", "unknown"));
		}
		getLogger().message(sender, message.toArray(new String[] {}));
		return true;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		DataManager.setData(event.getPlayer(), "lastjoined", System.currentTimeMillis());
		DataManager.setData(event.getPlayer(), "ip", event.getPlayer().getAddress().getHostString());
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
		Thread t = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				File jsonfile = new File(Main.plugin.getDataFolder(), "/seen/" + Utils.getID(player) + ".json");
				JSONObject json = JsonManager.getObject(jsonfile);
				if (json == null)
				{
					json = new JSONObject();
					json.put("names", new JSONArray());
					json.put("ips", new JSONArray());
				}
				JSONArray lnames = (JSONArray) json.get("names");
				if (!lnames.contains(player.getName()))
					lnames.add(player.getName());
				json.put("names", lnames);
				
				JSONArray lips = (JSONArray) json.get("ips");
				String ip = player.getAddress().getHostString();
				if (!lips.contains(ip))
					lips.add(ip);
				json.put("ips", lips);
				
				names.put(player.getUniqueId(), lnames);
				ips.put(player.getUniqueId(), lips);
				JsonManager.save(json, jsonfile);
			}
		});
		t.start();
	}
	
	public void unloadData(Player player)
	{
		this.names.remove(player.getUniqueId());
		this.ips.remove(player.getUniqueId());
	}
	
	public JSONArray loadNames(UUID uuid)
	{
		File jsonfile = new File(Main.plugin.getDataFolder(), "/seen/" + uuid + ".json");
		JSONObject json = JsonManager.getObject(jsonfile);
		if (json == null)
			return null;
		else
			return (JSONArray) json.get("names");
	}
	
	public JSONArray loadIPs(UUID uuid)
	{
		File jsonfile = new File(Main.plugin.getDataFolder(), "/seen/" + uuid + ".json");
		JSONObject json = JsonManager.getObject(jsonfile);
		if (json == null)
			return null;
		else
			return (JSONArray) json.get("ips");
	}
	
	@SuppressWarnings("deprecation")
	@Command(hook = "firstseenP")
	public void firstseen(CommandSender sender, String person)
	{
		OfflinePlayer oPlayer = Bukkit.getPlayer(person);
		if (oPlayer == null)
			oPlayer = Bukkit.getServer().getOfflinePlayer(person);
		Long firstJoin = oPlayer.getFirstPlayed();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String disDate = format.format(new Date(firstJoin));
		if (disDate.equals("1970-01-01 00:00"))
		{
			getLogger().message(sender, true, "&3" + oPlayer.getName() + "&c has never joined.");
		}
		else
		{
			getLogger().message(sender, "&3" + oPlayer.getName() + " &efirst joined&a " + disDate + "&e.");
		}
	}
	
	@Command(hook = "firstseen")
	public void firstseen(CommandSender sender)
	{
		firstseen(sender, sender.getName());
	}
	
	@Command(hook = "playtimeDef")
	public boolean playtime(CommandSender sender)
	{
		return playtime(sender, sender.getName());
	}
	
	@Command(hook = "playtime")
	public boolean playtime(CommandSender sender, String name)
	{
		if (name == null)
			name = sender.getName();
		Player player = Bukkit.getPlayer(name);
		if (player == null)
		{
			getLogger().message(sender, true, "§e" + name
					+ "§7 couldn't be found! Hint: Currently, you can only check statistics of players that are online!");
			return true;
		}
		int ticks_lived = player.getStatistic(Statistic.PLAY_ONE_TICK);
		int days = ticks_lived / 1728000;
		int hours = (ticks_lived % 1728000) / 72000;
		int minutes = (ticks_lived % 72000) / 1200;
		getLogger().message(sender,
				"The player &e" + name + " &7has been online for "
						+ (days == 0 && hours == 0 && minutes == 0 ? "less than a minute."
								: ("a total of: &e" + (days != 0 ? (days + "d ") : "")
										+ ((hours != 0 || days != 0) ? (hours + "h ") : "")
										+ ((minutes != 0 || hours != 0 || days != 0) ? (minutes + "m") : ""))));
		return true;
	}
}
