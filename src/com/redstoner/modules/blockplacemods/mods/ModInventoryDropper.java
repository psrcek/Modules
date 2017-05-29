package com.redstoner.modules.blockplacemods.mods;

import org.bukkit.event.inventory.InventoryType;

public class ModInventoryDropper extends ModInventoryAbstract
{
	@Override
	public String getName()
	{
		return "Dropper";
	}
	
	@Override
	protected InventoryType getInventoryType()
	{
		return InventoryType.DROPPER;
	}
	
	@Override
	public Object getDefault()
	{
		return null;
	}
}
