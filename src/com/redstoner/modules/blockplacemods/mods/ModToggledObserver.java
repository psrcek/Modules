package com.redstoner.modules.blockplacemods.mods;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class ModToggledObserver extends ModToggledLogPlaceAbstract {
    
    protected ModToggledObserver() {
        super("observer", false);
    }
    
    @Override
    public String getDescription() {
        return "If active, observers face the block you place them against";
    }
    
    @Override
    protected boolean isApplicableToPlacedBlock(Block block) {
        return block.getType() == Material.OBSERVER;
    }
    
    @Override
    protected int getBlockDataForFacing(BlockFace direction) {
        switch (direction) {
            case UP:
                return 0;
            default:
            case DOWN:
                return 1;
            case SOUTH:
                return 2;
            case NORTH:
                return 3;
            case EAST:
                return 4;
            case WEST:
                return 5;
        }
    }
    
    
}
