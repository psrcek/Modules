package com.redstoner.modules.datamanager;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nemez.cmdmgr.Command;
import com.nemez.cmdmgr.CommandManager;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.coremods.moduleLoader.ModuleLoader;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.misc.JsonManager;
import com.redstoner.misc.Main;
import com.redstoner.misc.Utils;
import com.redstoner.modules.CoreModule;
import com.redstoner.modules.Module;

@Commands(CommandHolderType.Stream)
@AutoRegisterListener
@Version(major = 4, minor = 1, revision = 10, compatible = 4)
public final class DataManager implements CoreModule, Listener
{
	protected final File dataFolder = new File(Main.plugin.getDataFolder(), "data");
	protected JSONObject data = new JSONObject();
	protected JSONObject config_data;
	protected ArrayList<String> module_index;
	int old_hash = 0;
	protected HashMap<String, HashMap<String, Boolean>> states = new HashMap<>();
	private static DataManager previous_instance = null;
	protected ArrayList<String> subcommands;
	protected List<String> scheduled_saves = new ArrayList<>();
	int task_id;
	
	@Override
	public void postEnable()
	{
		if (!dataFolder.exists())
			dataFolder.mkdirs();
		for (Player p : Bukkit.getOnlinePlayers())
		{
			loadData_(p.getUniqueId().toString());
		}
		subcommands = new ArrayList<>();
		subcommands.add("list");
		subcommands.add("get");
		subcommands.add("set");
		subcommands.add("remove");
		if (previous_instance == null)
			states.put(getID(Bukkit.getConsoleSender()), new HashMap<String, Boolean>());
		else
		{
			this.states = previous_instance.states;
			previous_instance = null;
		}
		config_data = JsonManager.getObject(new File(dataFolder, "configs.json"));
		if (config_data == null)
			config_data = new JSONObject();
		fixJson();
		updateIndex();
		CommandManager.registerCommand(getClass().getResourceAsStream("DataManager.cmd"), this, Main.plugin);
		
		// Schedule save every ten seconds
		task_id = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.plugin, new Runnable()
		{
			@Override
			public void run()
			{
				for (String id : scheduled_saves)
				{
					Object raw = data.get(id);
					if (raw == null || ((JSONObject) raw).size() == 0)
						continue;
					JSONObject json = (JSONObject) raw;
					JsonManager.save(json, new File(dataFolder, id + ".json"));
				}
				scheduled_saves.clear();
			}
		}, 0, 20).getTaskId();
	}
	
	@Override
	public void onDisable()
	{
		previous_instance = this;
		for (Player p : Bukkit.getOnlinePlayers())
		{
			saveAndUnload(p);
		}
		JsonManager.save(config_data, new File(dataFolder, "configs.json"));
		Bukkit.getScheduler().cancelTask(task_id);
	}
	
	@Command(hook = "import_file")
	public boolean importFile(CommandSender sender, String file, String module)
	{
		try
		{
			JSONObject object = JsonManager.getObject(new File(file));
			importObject_(module, object);
		}
		catch (Exception e)
		{
			getLogger().error("Could not import data!");
		}
		return true;
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
	
	public static void loadData(CommandSender sender)
	{
		loadData(getID(sender));
	}
	
	public static void loadData(String id)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("loadData_", String.class);
			m.setAccessible(true);
			m.invoke(mod, id);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	@SuppressWarnings("unchecked")
	protected synchronized void loadData_(String id)
	{
		JSONObject playerData = JsonManager.getObject(new File(dataFolder, id + ".json"));
		if (playerData == null)
			playerData = new JSONObject();
		data.put(id.toString(), playerData);
		states.put(id.toString(), new HashMap<String, Boolean>());
	}
	
	public static Object getOrDefault(CommandSender sender, String key, Object fallback)
	{
		return getOrDefault(getID(sender), Utils.getCaller("DataManager"), key, fallback);
	}
	
	public static Object getOrDefault(String id, String key, Object fallback)
	{
		return getOrDefault(id, Utils.getCaller("DataManager"), key, fallback);
	}
	
	public static Object getOrDefault(String id, String module, String key, Object fallback)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("getOrDefault_", String.class, String.class, String.class,
					Object.class);
			m.setAccessible(true);
			return m.invoke(mod, id, module, key, fallback);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
		return fallback;
	}
	
	protected Object getOrDefault_(String id, String module, String key, Object fallback)
	{
		Object o = getData_(id, module, key);
		return o == null ? fallback : o;
	}
	
	public static Object getData(CommandSender sender, String key)
	{
		return getData(getID(sender), Utils.getCaller("DataManager"), key);
	}
	
	public static Object getData(String id, String key)
	{
		return getData(id, Utils.getCaller("DataManager"), key);
	}
	
	public static Object getData(String id, String module, String key)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("getData_", String.class, String.class, String.class);
			m.setAccessible(true);
			return m.invoke(mod, id, module, key);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
		return null;
	}
	
	protected Object getData_(String id, String module, String key)
	{
		if (data.containsKey(id))
		{
			JSONObject moduleData = ((JSONObject) ((JSONObject) data.get(id)).get(module));
			if (moduleData == null)
				return null;
			if (key == null)
				return moduleData;
			else
				return moduleData.get(key);
		}
		else
			return loadAndGet(id, module, key);
	}
	
	protected synchronized Object loadAndGet(String id, String module, String key)
	{
		JSONObject playerData = JsonManager.getObject(new File(dataFolder, id + ".json"));
		if (playerData == null)
			return null;
		if (key == null)
			return playerData.get(module);
		else
			return ((JSONObject) playerData.get(module)).get(key);
	}
	
	public static void setData(CommandSender sender, String key, Object value)
	{
		setData(getID(sender), Utils.getCaller("DataManager"), key, value);
	}
	
	public static void setData(String id, String key, Object value)
	{
		setData(id, Utils.getCaller("DataManager"), key, value);
	}
	
	public static void setData(String id, String module, String key, Object value)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("setData_", String.class, String.class, String.class,
					Object.class);
			m.setAccessible(true);
			m.invoke(mod, id, module, key, value);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	@SuppressWarnings("unchecked")
	protected void setData_(String id, String module, String key, Object value)
	{
		if (data.containsKey(id))
		{
			JSONObject moduleData = ((JSONObject) ((JSONObject) data.get(id)).get(module));
			if (moduleData == null)
			{
				moduleData = new JSONObject();
				((JSONObject) data.get(id)).put(module, moduleData);
			}
			if (key == null)
				setDirectly_(id, module, value);
			else
				moduleData.put(key, value);
			save_(id);
		}
		else
			loadAndSet(id, module, key, value);
	}
	
	public static void setDirectly(CommandSender sender, Object value)
	{
		setDirectly(getID(sender), Utils.getCaller("DataManager"), value);
	}
	
	public static void setDirectly(String id, Object value)
	{
		setDirectly(id, Utils.getCaller("DataManager"), value);
	}
	
	public static void setDirectly(String id, String module, Object value)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("setDirectly_", String.class, String.class, Object.class);
			m.setAccessible(true);
			m.invoke(mod, id, module, value);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	@SuppressWarnings("unchecked")
	protected void setDirectly_(String id, String module, Object value)
	{
		if (data.containsKey(id))
		{
			JSONObject playerdata = (JSONObject) data.get(id);
			playerdata.put(module, value);
			save_(id);
		}
		else
			loadAndSetDirectly(id, module, value);
	}
	
	@SuppressWarnings("unchecked")
	protected synchronized void loadAndSet(String id, String module, String key, Object value)
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
	
	@SuppressWarnings("unchecked")
	protected void loadAndSetDirectly(String id, String module, Object value)
	{
		File dataFile = new File(dataFolder, id + ".json");
		JSONObject playerData = JsonManager.getObject(dataFile);
		if (playerData == null)
			playerData = new JSONObject();
		playerData.put(module, value);
		JsonManager.save(playerData, dataFile);
	}
	
	public static void removeData(CommandSender sender, String key)
	{
		removeData(getID(sender), Utils.getCaller("DataManager"), key);
	}
	
	public static void removeData(String id, String key)
	{
		removeData(id, Utils.getCaller("DataManager"), key);
	}
	
	public static void removeData(String id, String module, String key)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("removeData_", String.class, String.class, String.class);
			m.setAccessible(true);
			m.invoke(mod, id, module, key);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	@SuppressWarnings("unchecked")
	protected void removeData_(String id, String module, String key)
	{
		if (data.containsKey(id))
		{
			JSONObject moduleData = ((JSONObject) ((JSONObject) data.get(id)).get(module));
			if (moduleData == null)
				return;
			moduleData.remove(key);
			data.put(module, data);
			save_(id);
		}
		else
			loadAndRemove(id, module, key);
	}
	
	protected synchronized void loadAndRemove(String id, String module, String key)
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
	
	public static void importObject(JSONObject object)
	{
		importObject(object, Utils.getCaller("DataManager"));
	}
	
	public static void importObject(JSONObject object, String module)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("importObject_", String.class, String.class, String.class);
			m.setAccessible(true);
			m.invoke(mod, module, object);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	protected void importObject_(String module, JSONObject object)
	{
		for (Object o : object.keySet())
		{
			String uid = null;
			if (o instanceof String)
				uid = (String) o;
			else if (o instanceof UUID)
				uid = ((UUID) o).toString();
			if (uid == null)
				continue;
			setDirectly_(uid, module, object.get(o));
		}
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
			m.setAccessible(true);
			m.invoke(mod, oldName, newName);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	protected void migrateAll_(String oldName, String newName)
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
			m.setAccessible(true);
			m.invoke(mod, id, oldName, newName);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	@SuppressWarnings("unchecked")
	protected void migrate_(String id, String oldName, String newName)
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
			m.setAccessible(true);
			m.invoke(mod, sender);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	protected void save_(CommandSender sender)
	{
		save_(getID(sender));
	}
	
	public static void save(String id)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("save_", String.class);
			m.setAccessible(true);
			m.invoke(mod, id);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	protected void save_(String id)
	{
		scheduled_saves.add(id);
	}
	
	protected void saveAndUnload(CommandSender sender)
	{
		saveAndUnload(getID(sender));
		states.remove(getID(sender));
	}
	
	protected void saveAndUnload(String id)
	{
		save_(id);
		data.remove(id);
	}
	
	private static String getID(CommandSender sender)
	{
		String id;
		if (sender instanceof Player)
			id = ((Player) sender).getUniqueId().toString();
		else
			id = "CONSOLE";
		return id;
	}
	
	public static void setState(CommandSender sender, String key, boolean value)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("setState_", CommandSender.class, String.class, boolean.class);
			m.setAccessible(true);
			m.invoke(mod, sender, key, value);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	protected void setState_(CommandSender sender, String key, boolean value)
	{
		String id = getID(sender);
		HashMap<String, Boolean> lstates = states.get(id);
		lstates.put(key, value);
		states.put(id, lstates);
	}
	
	public static boolean getState(CommandSender sender, String key)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("getState_", CommandSender.class, String.class);
			m.setAccessible(true);
			return (boolean) m.invoke(mod, sender, key);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
		return false;
	}
	
	protected boolean getState_(CommandSender sender, String key)
	{
		String id = getID(sender);
		HashMap<String, Boolean> lstates = states.get(id);
		if (lstates == null)
			return false;
		return lstates.containsKey(key) ? lstates.get(key) : false;
	}
	
	protected boolean hasConfigChanged()
	{
		return old_hash != config_data.hashCode();
	}
	
	protected void updateIndex()
	{
		if (!hasConfigChanged())
			return;
		old_hash = config_data.hashCode();
		module_index = new ArrayList<>();
		if (config_data.size() > 0)
		{
			for (Object key : config_data.keySet())
				module_index.add(key.toString());
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void fixJson()
	{
		for (Object key : config_data.keySet())
		{
			JSONObject json = (JSONObject) config_data.get(key);
			for (Object key2 : json.keySet())
			{
				Object o = json.get(key2);
				if (!(o instanceof ConfigEntry))
					json.put(key2, new ConfigEntry((JSONObject) o));
			}
			config_data.put(key, json);
		}
	}
	
	private List<String> subsetWhereStartsWith(List<String> list, String prefix)
	{
		ArrayList<String> subset = new ArrayList<>();
		if (prefix == null || prefix.equals(""))
			return list;
		for (String s : list)
			if (s.toLowerCase().startsWith(prefix.toLowerCase()))
				subset.add(s);
		return subset;
	}
	
	@SuppressWarnings("unchecked")
	@EventHandler
	public void onTabComplete(TabCompleteEvent event)
	{
		if (event.getBuffer().toLowerCase().matches("^/?settings? .*")
				|| event.getBuffer().toLowerCase().matches("^/?configs? .*"))
		{
			boolean argument_complete = event.getBuffer().endsWith(" ");
			String[] arguments = event.getBuffer().split(" ");
			event.setCompletions(new ArrayList<String>());
			if (arguments.length == 1 || (arguments.length == 2 && !argument_complete))
				event.setCompletions(subsetWhereStartsWith(subcommands, arguments.length >= 2 ? arguments[1] : ""));
			else if (arguments.length == 2 || (arguments.length == 3 && !argument_complete))
			{
				switch (arguments[1].toLowerCase())
				{
					case "list":
					case "get":
					case "set":
					{
						event.setCompletions(
								subsetWhereStartsWith(module_index, arguments.length == 3 ? arguments[2] : ""));
						break;
					}
				}
			}
			else if ((arguments.length == 3 && argument_complete) || (arguments.length == 4 && !argument_complete))
			{
				switch (arguments[1].toLowerCase())
				{
					case "get":
					case "set":
					{
						Object o = config_data.get(arguments[2]);
						if (o == null)
							break;
						event.setCompletions(subsetWhereStartsWith(new ArrayList<String>(((JSONObject) o).keySet()),
								arguments.length == 4 ? arguments[3] : ""));
						break;
					}
				}
			}
			else
			{
				if (arguments[1].toLowerCase().equals("set"))
				{
					Object o = config_data.get(arguments[2]);
					if (o == null)
						return;
					Object o2 = ((JSONObject) o).get(arguments[3]);
					if (o2 == null)
						return;
					event.setCompletions(subsetWhereStartsWith(Arrays.asList(((ConfigEntry) o2).getCompleteOptions()),
							arguments.length > 4 ? String.join(" ", Arrays.copyOfRange(arguments, 4, arguments.length))
									: ""));
				}
			}
		}
	}
	
	@Command(hook = "config_list")
	public boolean list(CommandSender sender)
	{
		getLogger().message(sender, Arrays.toString(module_index.toArray(new String[] {})));
		return true;
	}
	
	@Command(hook = "config_list2")
	public boolean list(CommandSender sender, String module)
	{
		Object o = config_data.get(module);
		if (o == null)
		{
			getLogger().message(sender, "This module has not registered any settings.");
		}
		else
		{
			ArrayList<String> entries = new ArrayList<>();
			JSONObject json = (JSONObject) o;
			for (Object key : json.keySet())
			{
				String entry = key.toString();
				entries.add("§e" + entry + "§7");
			}
			getLogger().message(sender, "The module §e" + module + "§7 has the following config settings: ",
					Arrays.toString(entries.toArray(new String[] {})));
		}
		return true;
	}
	
	@Command(hook = "config_get")
	public boolean get(CommandSender sender, String module, String key)
	{
		getLogger().message(sender, new String[] {"§e" + module + "." + key + "§7 currently holds the value:",
				getConfigOrDefault_(module, key, "<empty>").toString()});
		return true;
	}
	
	@Command(hook = "config_set")
	public boolean set(CommandSender sender, String module, String key, String value)
	{
		if (setConfig_(module, key, value))
		{
			getLogger().message(sender, "Successfully changed the value for §e" + module + "." + key);
		}
		else
		{
			getLogger().message(sender, true,
					"§7\"§e" + value + "§7\" is not a valid value for setting §e" + module + "." + key);
		}
		return true;
	}
	
	@Command(hook = "config_remove_all")
	public boolean remove_all(CommandSender sender, String module)
	{
		if (removeAllConfig_(module))
			getLogger().message(sender, "Successfully deleted all config entries for module §e" + module + "§7!");
		else
			getLogger().message(sender, true, "Could not delete all config entries for module §e" + module + "§7!");
		return true;
	}
	
	public static Object getConfigOrDefault(String key, Object fallback)
	{
		return getConfigOrDefault(Utils.getCaller("DataManager"), key, fallback);
	}
	
	public static Object getConfigOrDefault(String module, String key, Object fallback)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("getConfigOrDefault_", String.class, String.class,
					Object.class);
			m.setAccessible(true);
			return m.invoke(mod, module, key, fallback);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
		return fallback;
	}
	
	protected Object getConfigOrDefault_(String module, String key, Object fallback)
	{
		Object o = getConfigData_(module, key);
		return o == null ? fallback : o;
	}
	
	protected Object getConfigData_(String module, String key)
	{
		module = module.toLowerCase();
		Object o = config_data.get(module);
		if (o == null)
			return null;
		else
		{
			JSONObject json = (JSONObject) o;
			Object o2 = json.get(key);
			if (o2 == null)
				return null;
			return ((ConfigEntry) o2).getValue();
		}
	}
	
	protected ConfigEntry getConfigEntry_(String module, String key)
	{
		module = module.toLowerCase();
		Object o = config_data.get(module);
		if (o == null)
			return null;
		else
		{
			JSONObject json = (JSONObject) o;
			return (ConfigEntry) json.get(key);
		}
	}
	
	public static void setConfig(String key, String value)
	{
		setConfig(Utils.getCaller("DataManager"), key, value, null);
	}
	
	public static void setConfig(String key, String value, String[] complete_options)
	{
		setConfig(Utils.getCaller("DataManager"), key, value, complete_options);
	}
	
	public static void setConfig(String module, String key, String value, String[] complete_options)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("setConfig_", String.class, String.class, String.class,
					String[].class);
			m.setAccessible(true);
			m.invoke(mod, module, key, value, complete_options);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	@SuppressWarnings("unchecked")
	protected boolean setConfig_(String module, String key, String value)
	{
		module = module.toLowerCase();
		ConfigEntry entry = getConfigEntry_(module, key);
		if (entry == null)
			entry = new ConfigEntry(value, null);
		if (entry.attemptSet(value))
		{
			Object o = config_data.get(module);
			JSONObject json;
			if (o == null)
				json = new JSONObject();
			else
				json = (JSONObject) o;
			json.put(key, entry);
			config_data.put(module, json);
			updateIndex();
			return true;
		}
		else
			return false;
	}
	
	@SuppressWarnings("unchecked")
	protected void setConfig_(String module, String key, String value, String[] complete_options)
	{
		module = module.toLowerCase();
		ConfigEntry entry = new ConfigEntry(value, complete_options);
		Object o = config_data.get(module);
		JSONObject json;
		if (o == null)
			json = new JSONObject();
		else
			json = (JSONObject) o;
		json.put(key, entry);
		config_data.put(module, json);
		updateIndex();
	}
	
	public static boolean removeConfig(String key)
	{
		return removeConfig(Utils.getCaller("DataManager"), key);
	}
	
	public static boolean removeConfig(String module, String key)
	{
		try
		{
			Module mod = ModuleLoader.getModule("DataManager");
			Method m = mod.getClass().getDeclaredMethod("removeConfig_", String.class, String.class);
			m.setAccessible(true);
			return (boolean) m.invoke(mod, module, key);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	protected boolean removeConfig_(String module, String key)
	{
		module = module.toLowerCase();
		if (key == null)
			return removeAllConfig_(module);
		Object o = config_data.get(module);
		JSONObject json;
		if (o == null)
			return false;
		else
			json = (JSONObject) o;
		json.remove(key);
		if (json.size() == 0)
			config_data.remove(module);
		else
			config_data.put(module, json);
		updateIndex();
		return true;
	}
	
	protected boolean removeAllConfig_(String module)
	{
		module = module.toLowerCase();
		if (config_data.remove(module) == null)
			return false;
		updateIndex();
		return true;
	}
}

class ConfigEntry
{
	private String value;
	private String[] complete_options;
	
	public ConfigEntry(String value, String[] complete_options)
	{
		this.value = value;
		this.complete_options = complete_options;
	}
	
	@SuppressWarnings("unchecked")
	public ConfigEntry(JSONObject json)
	{
		this(json.get("value").toString(),
				(String[]) ((JSONArray) json.get("complete_options")).toArray(new String[] {}));
	}
	
	protected boolean attemptSet(String value)
	{
		if (complete_options == null || complete_options.length == 0)
		{
			this.value = value;
			return true;
		}
		else
		{
			for (String s : complete_options)
			{
				if (s.equals(value))
				{
					this.value = value;
					return true;
				}
			}
			return false;
		}
	}
	
	protected String[] getCompleteOptions()
	{
		return complete_options;
	}
	
	protected String getValue()
	{
		return value;
	}
	
	@Override
	public String toString()
	{
		return "{\"value\":\"" + value + "\",\"complete_options\":"
				+ (complete_options == null || complete_options.length == 0 ? "[]"
						: "[\"" + String.join("\",\"", complete_options) + "\"]")
				+ "}";
	}
}
