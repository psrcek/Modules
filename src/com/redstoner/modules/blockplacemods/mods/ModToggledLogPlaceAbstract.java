package com.redstoner.modules.blockplacemods.mods;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * A mod that makes placement of directional blocks act the way placement of logs does normally.
 * Quartz pillar placement works like this too.
 *
 * Placed blocks face the block you clicked to place them.
 */
public abstract class ModToggledLogPlaceAbstract extends ModToggledAbstract {
    
    protected ModToggledLogPlaceAbstract(String name, boolean enabledByDefault) {
        super(name, enabledByDefault);
    }
    
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Player player = event.getPlayer();
        Block block;
        if (hasEnabled(player) && !player.isSneaking() && player.getGameMode() == GameMode.CREATIVE
                && isApplicableToPlacedBlock(block = event.getBlock()))
        {
            block.setData((byte) getBlockDataForFacing(block.getFace(event.getBlockAgainst())));
        }
    }
    
    protected abstract int getBlockDataForFacing(BlockFace direction);
    
    protected abstract boolean isApplicableToPlacedBlock(Block block);
    
}
