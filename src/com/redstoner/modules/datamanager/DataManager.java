package com.redstoner.modules.datamanager;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
import com.redstoner.modules.Module;

@AutoRegisterListener
@Version(major = 3, minor = 1, revision = 1, compatible = 3)
public final class DataManager implements CoreModule, Listener
{
	protected final File dataFolder = new File(Main.plugin.getDataFolder(), "data");
	protected JSONObject data = new JSONObject();
	
	@Override
	public void postEnable()
	{
		if (!dataFolder.exists())
			dataFolder.mkdirs();
		for (Player p : Bukkit.getOnlinePlayers())
		{
			loadData_(p.getUniqueId().toString());
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
		loadData_(event.getPlayer().getUniqueId().toString());
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		saveAndUnload(event.getPlayer());
	}
	
	public static void loadData(String id)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("loadData_", String.class);
			m.invoke(mod, id);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	@SuppressWarnings("unchecked")
	public void loadData_(String id)
	{
		JSONObject playerData = JsonManager.getObject(new File(dataFolder, id + ".json"));
		if (playerData == null)
			playerData = new JSONObject();
		data.put(id.toString(), playerData);
	}
	
	public static Object getOrDefault(CommandSender sender, String key, Object fallback)
	{
		return getOrDefault(sender, Utils.getCaller("DataManager"), key, fallback);
	}
	
	public static Object getOrDefault(CommandSender sender, String module, String key, Object fallback)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("getOrDefault_", CommandSender.class, String.class,
					String.class, Object.class);
			return m.invoke(mod, sender, module, key, fallback);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
		return fallback;
	}
	
	public Object getOrDefault_(CommandSender sender, String module, String key, Object fallback)
	{
		Object o = getData_(sender, module, key);
		return o == null ? fallback : o;
	}
	
	public static Object getData(CommandSender sender, String key)
	{
		return getData(sender, Utils.getCaller("DataManager"), key);
	}
	
	public static Object getData(CommandSender sender, String module, String key)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("getData_", CommandSender.class, String.class, String.class);
			return m.invoke(mod, sender, module, key);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
		return null;
	}
	
	public Object getData_(CommandSender sender, String module, String key)
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
	
	protected Object loadAndGet(String id, String module, String key)
	{
		JSONObject playerData = JsonManager.getObject(new File(dataFolder, id + ".json"));
		if (playerData == null)
			return null;
		return ((JSONObject) playerData.get(module)).get(key);
	}
	
	public static void setData(CommandSender sender, String key, Object value)
	{
		setData(sender, Utils.getCaller("DataManager"), key, value);
	}
	
	public static void setData(CommandSender sender, String module, String key, Object value)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("setData_", CommandSender.class, String.class, String.class,
					Object.class);
			m.invoke(mod, sender, module, key, value);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	@SuppressWarnings("unchecked")
	public void setData_(CommandSender sender, String module, String key, Object value)
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
			save_(sender);
		}
		else
			loadAndSet(id, module, key, value);
	}
	
	@SuppressWarnings("unchecked")
	protected void loadAndSet(String id, String module, String key, Object value)
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
		removeData(sender, Utils.getCaller("DataManager"), key);
	}
	
	public static void removeData(CommandSender sender, String module, String key)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("removeData_", CommandSender.class, String.class, String.class);
			m.invoke(mod, sender, module, key);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	public void removeData_(CommandSender sender, String module, String key)
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
			save_(sender);
		}
		else
			loadAndRemove(id, module, key);
	}
	
	protected void loadAndRemove(String id, String module, String key)
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
		migrateAll(oldName, Utils.getCaller("DataManager"));
	}
	
	public static void migrateAll(String oldName, String newName)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("migrateAll_", String.class, String.class);
			m.invoke(mod, oldName, newName);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	public void migrateAll_(String oldName, String newName)
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
			migrate_(s.replace(".json", ""), oldName, newName);
		}
	}
	
	public static void migrate(String id, String oldName)
	{
		migrate(id, oldName, Utils.getCaller("DataManager"));
	}
	
	public static void migrate(String id, String oldName, String newName)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("migrate_", String.class, String.class, String.class);
			m.invoke(mod, id, oldName, newName);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	@SuppressWarnings("unchecked")
	public void migrate_(String id, String oldName, String newName)
	{
		if (data.containsKey(id))
		{
			data.put(newName, data.get(oldName));
			data.remove(oldName);
			save_(id);
		}
		else
			loadAndMigrate(id, oldName, newName);
	}
	
	@SuppressWarnings("unchecked")
	protected void loadAndMigrate(String id, String oldName, String newName)
	{
		File dataFile = new File(dataFolder, id + ".json");
		JSONObject data = JsonManager.getObject(dataFile);
		data.put(newName, data.get(oldName));
		data.remove(oldName);
		JsonManager.save(data, dataFile);
	}
	
	public static void save(CommandSender sender)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("save_", CommandSender.class);
			m.invoke(mod, sender);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	public void save_(CommandSender sender)
	{
		String id;
		if (sender instanceof Player)
			id = ((Player) sender).getUniqueId().toString();
		else
			id = "CONSOLE";
		save_(id);
	}
	
	public static void save(String id)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("save_", String.class);
			m.invoke(mod, id);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	public void save_(String id)
	{
		Object raw = data.get(id);
		if (raw == null || ((JSONObject) raw).size() == 0)
			return;
		JsonManager.save((JSONObject) raw, new File(dataFolder, id + ".json"));
	}
	
	protected void saveAndUnload(CommandSender sender)
	{
		String id;
		if (sender instanceof Player)
			id = ((Player) sender).getUniqueId().toString();
		else
			id = "CONSOLE";
		saveAndUnload(id);
	}
	
	protected void saveAndUnload(String id)
	{
		save_(id);
		data.remove(id);
	}
}
