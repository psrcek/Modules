package com.redstoner.modules.onlineplayers;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Version;
import com.redstoner.misc.JsonManager;
import com.redstoner.misc.Main;
import com.redstoner.modules.Module;
import com.redstoner.modules.datamanager.DataManager;

@AutoRegisterListener
@Version(major = 4, minor = 0, revision = 3, compatible = 4)
@SuppressWarnings("unchecked")
public class OnlinePlayers implements Module, Listener
{
	private File saveFile = null;
	private JSONObject output = null;
	private JSONArray players = null;
	
	@Override
	public void postEnable()
	{
		saveFile = new File(Main.plugin.getDataFolder(), "players.json");
		output = new JSONObject();
		players = new JSONArray();
		output.put("dataFormat", "v2");
		rescan();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		add(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		remove(event.getPlayer());
	}
	
	public void rescan()
	{
		players = new JSONArray();
		for (Player p : Bukkit.getOnlinePlayers())
			add(p);
		save();
	}
	
	public synchronized void add(Player player)
	{
		JSONObject jsonPlayer = new JSONObject();
		jsonPlayer.put("name", player.getName());
		jsonPlayer.put("UUID", player.getUniqueId().toString());
		jsonPlayer.put("joined", System.currentTimeMillis());
		jsonPlayer.put("vanished", DataManager.getState(player, "vanished"));
		jsonPlayer.put("afk", DataManager.getState(player, "afk"));
		players.add(jsonPlayer);
		save();
	}
	
	public synchronized void remove(Player player)
	{
		JSONArray toRemove = new JSONArray();
		for (Object obj : players)
		{
			JSONObject o = (JSONObject) obj;
			if (((String) o.get("UUID")).equals(player.getUniqueId().toString()))
				toRemove.add(obj);
		}
		players.removeAll(toRemove);
		save();
	}
	
	public synchronized void save()
	{
		output.put("players", players);
		output.put("amount", players.size());
		JsonManager.save((JSONObject) output.clone(), saveFile);
	}
}
