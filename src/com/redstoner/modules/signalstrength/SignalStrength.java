package com.redstoner.modules.signalstrength;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.modules.Module;

@Commands(CommandHolderType.File)
@Version(major = 4, minor = 0, revision = 0, compatible = 4)
public class SignalStrength implements Module
{
	
	@Command(hook = "ss")
	public boolean ss(CommandSender sender, int strength)
	{
		return ssm(sender, strength, Material.REDSTONE_WIRE.toString());
	}
	
	@Command(hook = "ssm")
	public boolean ssm(CommandSender sender, int strength, String material)
	{
		Material item_type = Material.getMaterial(material);
		Player player = (Player) sender;
		Block target_block = player.getTargetBlock(new HashSet<Material>(), 5);
		if (target_block == null)
		{
			getLogger().message(sender, true, "That command can only be used if a container is targeted!");
			return true;
		}
		Inventory inventory = getInventory(target_block);
		if (inventory == null)
		{
			getLogger().message(sender, true, "That command can only be used if a container is targeted!");
			return true;
		}
		// --------Get the stack size and required amount of items to achieve the desired signal strength---------
		int stack_size = item_type.getMaxStackSize();
		int slot_count = inventory.getSize();
		int item_count = required_item_count(strength, stack_size, slot_count);
		if (item_count == -1)
		{
			getLogger().message(sender, true,
					"The desired signal strength could not be achieved with the requested item type");
			return true;
		}
		// #--------Add the other side of the chest if target is a double chest and check if player can build---------
		ArrayList<Block> container_blocks = getAllContainers(target_block);
		for (Block b : container_blocks)
		{
			if (!canBuild(player, b))
			{
				getLogger().message(sender, true, "You can not build here!");
				return true;
			}
		}
		// #----------------Insert items-------------
		int full_stack_count = item_count / stack_size;
		int remaining = item_count % stack_size;
		for (Block b : container_blocks)
		{
			Inventory inv = getInventory(b);
			inv.clear();
			for (int i = 0; i < full_stack_count; i++)
				inv.setItem(i, new ItemStack(item_type, stack_size));
			if (remaining > 0)
				inv.setItem(full_stack_count, new ItemStack(item_type, remaining));
		}
		getLogger().message(sender,
				"Comparators attached to this Inventory will now put out a signal strength of" + strength);
		return true;
	}
	
	private int required_item_count(int strength, int stack_size, int slot_count)
	{
		int item_count = -1;
		if (strength == 0)
			item_count = 0;
		else if (strength == 1)
			item_count = 1;
		else
			item_count = (int) Math.ceil(slot_count * stack_size / 14.0 * (strength - 1));
		int resulting_strength = item_count == 0 ? 0 : (int) Math.ceil(1 + 14.0 * item_count / stack_size / slot_count);
		// Clarification on these formulas at https://minecraft.gamepedia.com/Redstone_Comparator#Containers
		return resulting_strength == strength ? item_count : -1;
	}
	
	private Inventory getInventory(Block b)
	{
		BlockState state = b.getState();
		if (state instanceof InventoryHolder)
			return ((InventoryHolder) state).getInventory();
		return null;
	}
	
	private boolean canBuild(Player p, Block b)
	{
		BlockPlaceEvent e = new BlockPlaceEvent(b, b.getState(), b.getRelative(BlockFace.DOWN),
				p.getInventory().getItemInMainHand(), p, true, EquipmentSlot.HAND);
		return e.isCancelled();
	}
	
	private ArrayList<Block> getAllContainers(Block b)
	{
		ArrayList<Block> result = new ArrayList<Block>();
		result.add(b);
		for (BlockFace face : BlockFace.values())
		{
			Block b2 = b.getRelative(face);
			if (getInventory(b2) != null)
				result.add(b2);
		}
		return result;
	}
}
