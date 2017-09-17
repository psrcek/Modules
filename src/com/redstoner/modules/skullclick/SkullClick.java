package com.redstoner.modules.skullclick;

import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.modules.Module;

@Commands(CommandHolderType.None)
@Version(major = 4, minor = 0, revision = 0, compatible = 4)
@AutoRegisterListener
public class SkullClick implements Module, Listener
{
	private boolean seen = false;
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onClick(PlayerInteractEvent e)
	{
		// The event gets fired twice, once for mainhand and once for offhand. This fixes that.
		if (seen)
		{
			seen = false;
			return;
		}
		seen = true;
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && !e.isCancelled())
		{
			BlockState block = e.getClickedBlock().getState();
			if (block instanceof Skull)
			{
				Skull skull = (Skull) block;
				String owner = skull.getOwner();
				if (owner == null || owner.equals(""))
				{
					getLogger().message(e.getPlayer(), true, "That skull has no owner.");
				}
				else
				{
					getLogger().message(e.getPlayer(), "That's " + owner + ".");
				}
				if (!e.getPlayer().isSneaking())
				{
					e.setCancelled(true);
				}
			}
		}
	}
}
