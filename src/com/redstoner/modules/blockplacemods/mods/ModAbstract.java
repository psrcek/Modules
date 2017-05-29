package com.redstoner.modules.blockplacemods.mods;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.redstoner.misc.Main;
import com.redstoner.misc.Utils;
import com.redstoner.modules.datamanager.DataManager;

public abstract class ModAbstract<T> implements Mod<T>, Listener
{
	private static final Map<String, Mod> mods = new HashMap<>();
	
	public static Map<String, Mod> getMods()
	{
		return Collections.unmodifiableMap(mods);
	}
	
	public static Mod getMod(String name)
	{
		return mods.get(name);
	}
	
	public static void constructAll()
	{
		new ModBooleanCauldron();
		new ModBooleanPiston();
		new ModBooleanStep();
		new ModBooleanTorch();
		new ModInventoryDropper();
		new ModInventoryFurnace();
		new ModInventoryHopper();
	}
	
	private final Set<String> aliases;
	
	public ModAbstract()
	{
		preConstruction();
		Utils.info("Loaded mod " + getName());
		aliases = new HashSet<>();
		aliases.add(getName());
		mods.put(getName().toLowerCase(), this);
	}
	
	protected void preConstruction()
	{}
	
	@Override
	public void register()
	{
		for (String alias : aliases)
		{
			mods.putIfAbsent(alias.toLowerCase(), this);
		}
		Bukkit.getPluginManager().registerEvents(this, Main.plugin);
	}
	
	@Override
	public void unregister()
	{
		HandlerList.unregisterAll(this);
	}
	
	@Override
	public Set<String> getAliases()
	{
		return aliases;
	}
	
	protected void reset(Player player)
	{
		DataManager.removeData(player.getUniqueId().toString(), "BlockPlaceMods", getName());
	}
}
