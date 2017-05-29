package com.redstoner.modules.blockplacemods.mods;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

public class ModBooleanStep extends ModBooleanAbstract
{
	{
		getAliases().add("Slab");
	}
	
	@Override
	public String getName()
	{
		return "Step";
	}
	
	@Override
	public String getDescription()
	{
		return "turns steps upside-down upon placement";
	}
	
	@Override
	protected boolean enabledByDefault()
	{
		return true;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (isStep(event.getBlock().getType()) && !event.getPlayer().isSneaking() && hasEnabled(event.getPlayer()))
		{
			byte data = event.getBlock().getData();
			if (data != (data |= 0x8))
			{
				event.getBlock().setData(data);
			}
		}
	}
	
	private boolean isStep(Material block)
	{
		return block == Material.STEP || block == Material.STONE_SLAB2;
	}
	
	@Override
	public Object getDefault()
	{
		return true;
	}
}
