package com.redstoner.modules.blockplacemods.mods;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class ModToggledPiston extends ModToggledLogPlaceAbstract
{
	public ModToggledPiston()
	{
		super("piston", false);
	}
	
	@Override
	public String getDescription()
	{
		return "If active, pistons face the block you place them against";
	}
	
	@Override
	protected boolean isApplicableToPlacedBlock(Block block) {
		Material type = block.getType();
		return type == Material.PISTON_BASE || type == Material.PISTON_STICKY_BASE;
	}
	
	@Override
	protected int getBlockDataForFacing(BlockFace direction) {
		switch (direction)
		{
			default:
			case DOWN:
				return 0;
			case UP:
				return 1;
			case NORTH:
				return 2;
			case SOUTH:
				return 3;
			case WEST:
				return 4;
			case EAST:
				return 5;
		}
	}
	
}
