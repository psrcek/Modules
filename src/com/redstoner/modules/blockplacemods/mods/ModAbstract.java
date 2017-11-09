package com.redstoner.modules.blockplacemods.mods;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;

import com.redstoner.misc.Main;
import com.redstoner.modules.ModuleLogger;
import com.redstoner.modules.datamanager.DataManager;

public abstract class ModAbstract implements Mod, Listener
{
	private static final Map<String, Mod> mods = new HashMap<>();
	private final String name;
	private final Set<String> aliases;
	private static ModuleLogger logger;
	
	public static Map<String, Mod> getMods()
	{
		return Collections.unmodifiableMap(mods);
	}
	
	public static Mod getMod(String name)
	{
		return mods.get(name);
	}
	
	public static void registerMod(Mod mod)
	{
		mods.put(mod.getName(), mod);
		for (String alias : mod.getAliases())
		{
			mods.putIfAbsent(alias, mod);
		}
	}
	
	public static void registerAll(final ModuleLogger logger)
	{
		ModAbstract.logger = logger;
		registerMod(new ModToggledCauldron());
		registerMod(new ModToggledPiston());
		registerMod(new ModToggledObserver());
		registerMod(new ModToggledStep());
		registerMod(new ModToggledTorch());
		registerMod(new ModInventory("dropper", InventoryType.DROPPER));
		registerMod(new ModInventory("furnace", InventoryType.FURNACE));
		registerMod(new ModInventory("hopper", InventoryType.HOPPER));
	}
	
	public ModAbstract(String name)
	{
		this.name = Objects.requireNonNull(name);
		this.aliases = new HashSet<>(2);
		logger.info("Loaded mod " + name);
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public Set<String> getAliases()
	{
		return aliases;
	}
	
	@Override
	public void registerListeners()
	{
		Bukkit.getPluginManager().registerEvents(this, Main.plugin);
	}
	
	@Override
	public void unregisterListeners()
	{
		HandlerList.unregisterAll(this);
	}
	
	protected void reset(Player player)
	{
		DataManager.removeData(player.getUniqueId().toString(), "BlockPlaceMods", getName());
	}
}
