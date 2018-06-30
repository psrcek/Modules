package com.redstoner.modules.tilechunks;

import org.bukkit.Location;
import org.bukkit.World;

public class LaggyTileChunk {
	public final int	x, y, z, amount;
	public final World	world;
	
	public LaggyTileChunk(int x, int y, int z, World world, int amount) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.world = world;
		this.amount = amount;
	}
	
	public Location getLocation() {
		return new Location(world, x, y, z);
	}
}
