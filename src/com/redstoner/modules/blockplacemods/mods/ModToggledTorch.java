package com.redstoner.modules.blockplacemods.mods;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

import com.redstoner.misc.Main;

public class ModToggledTorch extends ModToggledAbstract
{
	private final Set<Block> torchesPlaced = new HashSet<>();
	
	public ModToggledTorch()
	{
		super("torch", true);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.plugin, this::updateTorches, 2, 2);
	}
	
	@Override
	public String getDescription()
	{
		return "If active, redstone torches placed on a redstone block disappear quickly";
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		final Player player = event.getPlayer();
		if (!player.isSneaking() && player.getGameMode() == GameMode.CREATIVE && hasEnabled(player)
				&& event.getBlock().getType() == Material.REDSTONE_TORCH_ON)
		{
			if (isAttachedToRedstoneBlock(event.getBlock()))
			{
				torchesPlaced.add(event.getBlock());
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private boolean isAttachedToRedstoneBlock(Block block)
	{
		BlockFace towardsAgainst = getFaceTowardsBlockAgainst(block.getData());
		return towardsAgainst != null && block.getRelative(towardsAgainst).getType() == Material.REDSTONE_BLOCK;
	}
	
	private BlockFace getFaceTowardsBlockAgainst(byte data)
	{
		switch (data)
		{
			case 1:
				return BlockFace.WEST;
			case 2:
				return BlockFace.EAST;
			case 3:
				return BlockFace.NORTH;
			case 4:
				return BlockFace.SOUTH;
			case 5:
				return BlockFace.DOWN;
			default:
				return null;
		}
	}
	
	private void updateTorches()
	{
		for (Iterator<Block> it = torchesPlaced.iterator(); it.hasNext();)
		{
			Block block = it.next();
			if (block.getType() == Material.REDSTONE_TORCH_OFF)
			{
				block.setType(Material.AIR);
				it.remove();
			}
			else if (block.getType() != Material.REDSTONE_TORCH_ON || !isAttachedToRedstoneBlock(block))
			{
				it.remove();
			}
		}
	}
}
