package com.redstoner.modules.blockplacemods.mods;

import org.bukkit.event.inventory.InventoryType;

public class ModInventoryHopper extends ModInventoryAbstract
{
	@Override
	public String getName()
	{
		return "Hopper";
	}
	
	@Override
	protected InventoryType getInventoryType()
	{
		return InventoryType.HOPPER;
	}
	
	@Override
	public Object getDefault()
	{
		return null;
	}
}
