package com.redstoner.modules.datamanager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
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
@Version(major = 3, minor = 0, revision = 3, compatible = 3)
public final class DataManager implements CoreModule, Listener
{
	private static final File dataFolder = new File(Main.plugin.getDataFolder(), "data");
	private static JSONObject data = new JSONObject();
	
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
			saveAndUnload(p);
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
		saveAndUnload(event.getPlayer());
	}
	
	@SuppressWarnings("unchecked")
	public static void loadData(UUID id)
	{
		JSONObject playerData = JsonManager.getObject(new File(dataFolder, id.toString() + ".json"));
		if (playerData == null)
			playerData = new JSONObject();
		data.put(id.toString(), playerData);
	}
	
	public static Object getData(CommandSender sender, String key)
	{
		return getData(sender, Utils.getCaller(DataManager.class), key);
	}
	
	public static Object getData(CommandSender sender, String module, String key)
	{
		String id;
		if (sender instanceof Player)
			id = ((Player) sender).getUniqueId().toString();
		else
			id = "CONSOLE";
		if (data.containsKey(id))
		{
			JSONObject moduleData = ((JSONObject) ((JSONObject) data.get(id)).get(module));
			if (moduleData == null)
				return null;
			return moduleData.get(key);
		}
		else
			return loadAndGet(id, module, key);
	}
	
	private static Object loadAndGet(String id, String module, String key)
	{
		JSONObject playerData = JsonManager.getObject(new File(dataFolder, id + ".json"));
		if (playerData == null)
			return null;
		return ((JSONObject) playerData.get(module)).get(key);
	}
	
	public static void setData(CommandSender sender, String key, Object value)
	{
		setData(sender, Utils.getCaller(DataManager.class), key, value);
	}
	
	@SuppressWarnings("unchecked")
	public static void setData(CommandSender sender, String module, String key, Object value)
	{
		String id;
		if (sender instanceof Player)
			id = ((Player) sender).getUniqueId().toString();
		else
			id = "CONSOLE";
		if (data.containsKey(id))
		{
			JSONObject moduleData = ((JSONObject) ((JSONObject) data.get(id)).get(module));
			if (moduleData == null)
			{
				moduleData = new JSONObject();
				((JSONObject) data.get(id)).put(module, moduleData);
			}
			moduleData.put(key, value);
			save(sender);
		}
		else
			loadAndSet(id, module, key, value);
	}
	
	@SuppressWarnings("unchecked")
	private static void loadAndSet(String id, String module, String key, Object value)
	{
		File dataFile = new File(dataFolder, id + ".json");
		JSONObject playerData = JsonManager.getObject(dataFile);
		if (playerData == null)
			playerData = new JSONObject();
		JSONObject moduleData = ((JSONObject) playerData.get(module));
		if (moduleData == null)
		{
			moduleData = new JSONObject();
			playerData.put(module, moduleData);
		}
		moduleData.put(key, value);
		JsonManager.save(playerData, dataFile);
	}
	
	public static void removeData(CommandSender sender, String key)
	{
		removeData(sender, Utils.getCaller(DataManager.class), key);
	}
	
	public static void removeData(CommandSender sender, String module, String key)
	{
		String id;
		if (sender instanceof Player)
			id = ((Player) sender).getUniqueId().toString();
		else
			id = "CONSOLE";
		if (data.containsKey(id.toString()))
		{
			JSONObject moduleData = ((JSONObject) ((JSONObject) data.get(id)).get(module));
			if (moduleData == null)
				return;
			moduleData.remove(key);
			save(sender);
		}
		else
			loadAndRemove(id, module, key);
	}
	
	private static void loadAndRemove(String id, String module, String key)
	{
		File dataFile = new File(dataFolder, id + ".json");
		JSONObject playerData = JsonManager.getObject(dataFile);
		if (playerData == null)
			return;
		JSONObject moduleData = ((JSONObject) playerData.get(module));
		if (moduleData == null)
			return;
		moduleData.remove(key);
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
			migrate(s.replace(".json", ""), oldName, newName);
		}
	}
	
	public static void migrate(String id, String oldName)
	{
		migrate(id, oldName, Utils.getCaller(DataManager.class));
	}
	
	@SuppressWarnings("unchecked")
	public static void migrate(String id, String oldName, String newName)
	{
		if (data.containsKey(id))
		{
			data.put(newName, data.get(oldName));
			data.remove(oldName);
			save(id);
		}
		else
			loadAndMigrate(id, oldName, newName);
	}
	
	@SuppressWarnings("unchecked")
	private static void loadAndMigrate(String id, String oldName, String newName)
	{
		File dataFile = new File(dataFolder, id + ".json");
		JSONObject data = JsonManager.getObject(dataFile);
		data.put(newName, data.get(oldName));
		data.remove(oldName);
		JsonManager.save(data, dataFile);
	}
	
	public static void save(CommandSender sender)
	{
		String id;
		if (sender instanceof Player)
			id = ((Player) sender).getUniqueId().toString();
		else
			id = "CONSOLE";
		save(id);
	}
	
	public static void save(String id)
	{
		Object raw = data.get(id);
		if (raw == null || ((JSONObject) raw).size() == 0)
			return;
		JsonManager.save((JSONObject) raw, new File(dataFolder, id + ".json"));
	}
	
	private static void saveAndUnload(CommandSender sender)
	{
		String id;
		if (sender instanceof Player)
			id = ((Player) sender).getUniqueId().toString();
		else
			id = "CONSOLE";
		saveAndUnload(id);
	}
	
	private static void saveAndUnload(String id)
	{
		save(id);
		data.remove(id);
	}
}
