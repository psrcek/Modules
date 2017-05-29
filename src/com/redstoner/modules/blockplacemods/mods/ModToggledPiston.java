package com.redstoner.modules.blockplacemods.mods;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

public class ModToggledPiston extends ModToggledAbstract
{
	
	public ModToggledPiston() {
		super("piston", false);
	}
	
	@Override
	public String getDescription()
	{
		return "makes pistons face the block you placed it against";
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		if (hasEnabled(player) && !player.isSneaking() && player.getGameMode() == GameMode.CREATIVE
				&& isPiston(event.getBlock().getType()))
		{
			Block block = event.getBlock();
			block.setData((byte) pistonDataForFace(block.getFace(event.getBlockAgainst())));
		}
	}
	
	private boolean isPiston(Material block)
	{
		return block == Material.PISTON_BASE || block == Material.PISTON_STICKY_BASE;
	}
	
	private int pistonDataForFace(BlockFace face)
	{
		switch (face)
		{
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
			default:
				return 0;
		}
	}
	
}
