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
import com.redstoner.coremods.moduleLoader.ModuleLoader;
import com.redstoner.misc.JsonManager;
import com.redstoner.misc.Main;
import com.redstoner.misc.Utils;
import com.redstoner.modules.CoreModule;

@AutoRegisterListener
@Version(major = 3, minor = 0, revision = 7, compatible = 3)
public final class DataManager implements CoreModule, Listener
{
	private final File dataFolder = new File(Main.plugin.getDataFolder(), "data");
	private JSONObject data = new JSONObject();
	
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
	public void loadData(UUID id)
	{
		JSONObject playerData = JsonManager.getObject(new File(dataFolder, id.toString() + ".json"));
		if (playerData == null)
			playerData = new JSONObject();
		data.put(id.toString(), playerData);
	}
	
	public Object getOrDefault(CommandSender sender, String key, Object fallback)
	{
		Object o = getData(sender, Utils.getCaller("DataManager"), key);
		return o == null ? fallback : o;
	}
	
	public Object getOrDefault(CommandSender sender, String module, String key, Object fallback)
	{
		Object o = getData(sender, module, key);
		return o == null ? fallback : o;
	}
	
	public Object getData(CommandSender sender, String key)
	{
		return getData(sender, Utils.getCaller("DataManager"), key);
	}
	
	public Object getData(CommandSender sender, String module, String key)
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
	
	private Object loadAndGet(String id, String module, String key)
	{
		JSONObject playerData = JsonManager.getObject(new File(dataFolder, id + ".json"));
		if (playerData == null)
			return null;
		return ((JSONObject) playerData.get(module)).get(key);
	}
	
	public void setData(CommandSender sender, String key, Object value)
	{
		setData(sender, Utils.getCaller("DataManager"), key, value);
	}
	
	@SuppressWarnings("unchecked")
	public void setData(CommandSender sender, String module, String key, Object value)
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
	private void loadAndSet(String id, String module, String key, Object value)
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
	
	public void removeData(CommandSender sender, String key)
	{
		removeData(sender, Utils.getCaller("DataManager"), key);
	}
	
	public void removeData(CommandSender sender, String module, String key)
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
	
	private void loadAndRemove(String id, String module, String key)
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
	
	public void migrateAll(String oldName)
	{
		migrateAll(oldName, Utils.getCaller("DataManager"));
	}
	
	public void migrateAll(String oldName, String newName)
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
	
	public void migrate(String id, String oldName)
	{
		migrate(id, oldName, Utils.getCaller("DataManager"));
	}
	
	@SuppressWarnings("unchecked")
	public void migrate(String id, String oldName, String newName)
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
	private void loadAndMigrate(String id, String oldName, String newName)
	{
		File dataFile = new File(dataFolder, id + ".json");
		JSONObject data = JsonManager.getObject(dataFile);
		data.put(newName, data.get(oldName));
		data.remove(oldName);
		JsonManager.save(data, dataFile);
	}
	
	public void save(CommandSender sender)
	{
		String id;
		if (sender instanceof Player)
			id = ((Player) sender).getUniqueId().toString();
		else
			id = "CONSOLE";
		save(id);
	}
	
	public void save(String id)
	{
		Object raw = data.get(id);
		if (raw == null || ((JSONObject) raw).size() == 0)
			return;
		JsonManager.save((JSONObject) raw, new File(dataFolder, id + ".json"));
	}
	
	private void saveAndUnload(CommandSender sender)
	{
		String id;
		if (sender instanceof Player)
			id = ((Player) sender).getUniqueId().toString();
		else
			id = "CONSOLE";
		saveAndUnload(id);
	}
	
	private void saveAndUnload(String id)
	{
		save(id);
		data.remove(id);
	}
	
	public static DataManager getDataManager()
	{
		return (DataManager) ModuleLoader.getModule("DataManager");
	}
}
