package com.redstoner.modules.signalstrength;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryType;
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
@Version(major = 4, minor = 0, revision = 2, compatible = 4)
public class SignalStrength implements Module
{
	
	private static final String namePrefix = ChatColor.GREEN.toString() + ChatColor.RESET + ChatColor.DARK_PURPLE
			+ "Signal Strength: " + ChatColor.RED + ChatColor.BOLD;
	
	private static String nameForSignalStrength(int strength)
	{
		return namePrefix + strength;
	}
	
	private static boolean isSignalStrengthNameOrEmpty(String name)
	{
		return name == null || name.isEmpty() || name.startsWith(namePrefix);
	}
	
	@Command(hook = "ss")
	public boolean ss(CommandSender sender, int strength)
	{
		return ssm(sender, strength, Material.REDSTONE.toString());
	}
	
	@Command(hook = "ssm")
	public boolean ssm(CommandSender sender, int strength, String material)
	{
		if (strength < 0 || strength > 15)
		{
			getLogger().message(sender, true, "The strength must be between 0 and 15!");
			return true;
		}
		
		Player player = (Player) sender;
		if (player.getGameMode() != GameMode.CREATIVE)
		{
			getLogger().message(sender, true, "You must be in creative mode to do that");
			return true;
		}
		
		Material itemType = Material.matchMaterial(material);
		if (itemType == null)
		{
			getLogger().message(sender, true, "The material " + material + " could not be recognized");
			return true;
		}
		
		// Empty set in the first argument would make it always return the first block, because no block types are
		// considered to be transparent. Only a value of null is treated as "air only".
		Block targetBlock = player.getTargetBlock((Set<Material>) null, 5);
		if (targetBlock == null)
		{
			getLogger().message(sender, true, "That command can only be used if a container is targeted!");
			return true;
		}
		Inventory inventory = getInventory(targetBlock);
		if (inventory == null)
		{
			getLogger().message(sender, true, "That command can only be used if a container is targeted!");
			return true;
		}
		
		// --------Get the stack size and required amount of items to achieve the desired signal strength---------
		int stackSize = itemType.getMaxStackSize();
		int slotCount = inventory.getSize();
		int itemCount = computeRequiredItemCount(strength, stackSize, slotCount);
		if (itemCount == -1)
		{
			getLogger().message(sender, true,
					"The desired signal strength could not be achieved with the requested item type");
			return true;
		}
		// #--------Add the other side of the chest if target is a double chest and check if player can build---------
		ArrayList<Block> containerBlocks = new ArrayList<>();
		containerBlocks.add(targetBlock);
		
		Material blockType = targetBlock.getType();
		if (inventory.getType() == InventoryType.CHEST)
		{
			Arrays.stream(new BlockFace[] {BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH})
					.map(targetBlock::getRelative).filter(b -> b.getType() == blockType).forEach(containerBlocks::add);
		}
		
		for (Block containerBlock : containerBlocks)
		{
			if (!canBuild(player, containerBlock))
			{
				getLogger().message(sender, true, "You can not build here!");
				return true;
			}
		}
		// #----------------Insert items-------------
		int fullStackCount = itemCount / stackSize;
		int remaining = itemCount % stackSize;
		for (Block containerBlock : containerBlocks)
		{
			// Below checks should evaluate to false, but let's be safe.
			BlockState blockState = containerBlock.getState();
			if (!(blockState instanceof InventoryHolder))
				continue;
			
			if (blockState instanceof Nameable && isSignalStrengthNameOrEmpty(((Nameable) blockState).getCustomName()))
			{
				((Nameable) blockState).setCustomName(nameForSignalStrength(strength));
				blockState.update();
			}
			
			Inventory inv = ((InventoryHolder) blockState).getInventory();
			if (inv == null)
				continue;
			
			inv.clear();
			for (int i = 0; i < fullStackCount; i++)
				inv.setItem(i, new ItemStack(itemType, stackSize));
			if (remaining > 0)
				inv.setItem(fullStackCount, new ItemStack(itemType, remaining));
			
		}
		getLogger().message(sender, "Comparators attached to this " + enumNameToHumanName(blockType.name())
				+ " will now put out a signal strength of " + strength);
		return true;
	}
	
	private static Inventory getInventory(Block b)
	{
		BlockState state = b.getState();
		if (state instanceof InventoryHolder)
			return ((InventoryHolder) state).getInventory();
		return null;
	}
	
	private static int computeRequiredItemCount(int strength, int stackSize, int slotCount)
	{
		int itemCount = -1;
		if (strength == 0)
			itemCount = 0;
		else if (strength == 1)
			itemCount = 1;
		else
			itemCount = (int) Math.ceil(slotCount * stackSize / 14.0 * (strength - 1));
		
		// Reverse engineer the calculation to verify
		int resultingStrength = itemCount == 0 ? 0 : (int) Math.floor(1 + 14.0 * itemCount / stackSize / slotCount);
		if (resultingStrength != strength)
		{
			return -1;
		}
		// Clarification on these formulas at https://minecraft.gamepedia.com/Redstone_Comparator#Containers
		return itemCount;
	}
	
	private static boolean canBuild(Player p, Block b)
	{
		BlockPlaceEvent event = new BlockPlaceEvent(b, b.getState(), b.getRelative(BlockFace.DOWN),
				p.getInventory().getItemInMainHand(), p, true, EquipmentSlot.HAND);
		Bukkit.getPluginManager().callEvent(event);
		return !event.isCancelled();
	}
	
	private static String enumNameToHumanName(String enumName)
	{
		return enumName.toLowerCase().replace('_', ' ');
	}
	
}
