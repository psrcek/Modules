package com.redstoner.modules.blockplacemods.mods;

import org.bukkit.event.inventory.InventoryType;

public class ModInventoryFurnace extends ModInventoryAbstract
{
	@Override
	public String getName()
	{
		return "Furnace";
	}
	
	@Override
	protected InventoryType getInventoryType()
	{
		return InventoryType.FURNACE;
	}
	
	@Override
	public Object getDefault()
	{
		return null;
	}
}
