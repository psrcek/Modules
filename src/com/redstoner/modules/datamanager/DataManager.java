package com.redstoner.modules.datamanager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONObject;

import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Version;
import com.redstoner.misc.JsonManager;
import com.redstoner.misc.Main;
import com.redstoner.misc.Utils;
import com.redstoner.modules.CoreModule;

@AutoRegisterListener
@Version(major = 3, minor = 0, revision = 0, compatible = 3)
public final class DataManager implements CoreModule, Listener
{
	private static final File dataFolder = new File(Main.plugin.getDataFolder(), "data");
	private static JSONObject data;
	
	@Override
	public void postEnable()
	{
		if (!dataFolder.exists())
			dataFolder.mkdirs();
		for (Player p : Bukkit.getOnlinePlayers())
		{
			loadData(p.getUniqueId());
		}
	}
	
	@Override
	public void onDisable()
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			saveAndUnload(p.getUniqueId());
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		loadData(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		saveAndUnload(event.getPlayer().getUniqueId());
	}
	
	@SuppressWarnings("unchecked")
	public static void loadData(UUID id)
	{
		JSONObject playerData = JsonManager.getObject(new File(dataFolder, id.toString() + ".json"));
		if (playerData == null)
			playerData = new JSONObject();
		data.put(id.toString(), playerData);
	}
	
	public static Object getData(UUID id, String key)
	{
		return getData(id, Utils.getCaller(DataManager.class), key);
	}
	
	public static Object getData(UUID id, String module, String key)
	{
		if (data.containsKey(id.toString()))
			return ((JSONObject) ((JSONObject) data.get(id.toString())).get(module)).get(key);
		else
			return loadAndGet(id, module, key);
	}
	
	private static Object loadAndGet(UUID id, String module, String key)
	{
		JSONObject playerData = JsonManager.getObject(new File(dataFolder, id.toString() + ".json"));
		if (playerData == null)
			return null;
		return ((JSONObject) playerData.get(module)).get(key);
	}
	
	public static void setData(UUID id, String key, Object value)
	{
		setData(id, Utils.getCaller(DataManager.class), key, value);
	}
	
	@SuppressWarnings("unchecked")
	public static void setData(UUID id, String module, String key, Object value)
	{
		if (data.containsKey(id.toString()))
		{
			((JSONObject) ((JSONObject) data.get(id.toString())).get(module)).put(key, value);
			save(id);
		}
		else
			loadAndSet(id, module, key, value);
	}
	
	@SuppressWarnings("unchecked")
	private static void loadAndSet(UUID id, String module, String key, Object value)
	{
		File dataFile = new File(dataFolder, id.toString() + ".json");
		JSONObject playerData = JsonManager.getObject(dataFile);
		if (playerData == null)
			playerData = new JSONObject();
		((JSONObject) playerData.get(module)).put(key, value);
		JsonManager.save(playerData, dataFile);
	}
	
	public static void removeData(UUID id, String key)
	{
		removeData(id, Utils.getCaller(DataManager.class), key);
	}
	
	public static void removeData(UUID id, String module, String key)
	{
		if (data.containsKey(id.toString()))
		{
			((JSONObject) ((JSONObject) data.get(module)).get(id.toString())).remove(key);
			save(id);
		}
		else
			loadAndRemove(id, module, key);
	}
	
	private static void loadAndRemove(UUID id, String module, String key)
	{
		File dataFile = new File(dataFolder, id.toString() + ".json");
		JSONObject playerData = JsonManager.getObject(dataFile);
		if (playerData == null)
			return;
		((JSONObject) playerData.get(module)).remove(key);
		JsonManager.save(playerData, dataFile);
	}
	
	public static void migrateAll(String oldName)
	{
		migrateAll(oldName, Utils.getCaller(DataManager.class));
	}
	
	public static void migrateAll(String oldName, String newName)
	{
		for (String s : dataFolder.list(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".json");
			}
		}))
		{
			migrate(UUID.fromString(s.replace(".json", "")), oldName, newName);
		}
	}
	
	public static void migrate(UUID id, String oldName)
	{
		migrate(id, oldName, Utils.getCaller(DataManager.class));
	}
	
	@SuppressWarnings("unchecked")
	public static void migrate(UUID id, String oldName, String newName)
	{
		if (data.containsKey(id.toString()))
		{
			data.put(newName, data.get(oldName));
			data.remove(oldName);
			save(id);
		}
		else
			loadAndMigrate(id, oldName, newName);
	}
	
	@SuppressWarnings("unchecked")
	private static void loadAndMigrate(UUID id, String oldName, String newName)
	{
		File dataFile = new File(dataFolder, id.toString() + ".json");
		JSONObject data = JsonManager.getObject(dataFile);
		data.put(newName, data.get(oldName));
		data.remove(oldName);
		JsonManager.save(data, dataFile);
	}
	
	public static void save(UUID id)
	{
		Object raw = data.get(id);
		if (raw == null || ((JSONObject) raw).size() == 0)
			return;
		JsonManager.save((JSONObject) raw, new File(dataFolder, id.toString() + ".json"));
	}
	
	private static void saveAndUnload(UUID id)
	{
		String key = id.toString();
		Object raw = data.containsKey(key);
		if (!(raw == null || ((JSONObject) raw).size() == 0))
			save(id);
		data.remove(key);
	}
}
