package com.redstoner.modules.blockplacemods.mods;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.redstoner.modules.blockplacemods.util.CommandException;
import com.redstoner.modules.blockplacemods.util.ItemProperties;
import com.redstoner.modules.datamanager.DataManager;

public abstract class ModInventoryAbstract extends ModAbstract<ItemStack[]>
{
	protected InventoryType inventoryType;
	
	@Override
	protected void preConstruction()
	{
		inventoryType = getInventoryType();
	}
	
	protected abstract InventoryType getInventoryType();
	
	private static int highestUsedIndex(ItemStack[] items)
	{
		for (int i = items.length - 1; i >= 0; i--)
		{
			if (items[i] != null)
			{
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public String getDescription()
	{
		return "Controls contents of " + inventoryType.name().toLowerCase() + "s upon placement";
	}
	
	@Override
	public String runCommand(Player sender, String[] args) throws CommandException
	{
		if (args.length > 0)
		{
			if (args[0].equalsIgnoreCase("clear"))
			{
				reset(sender);
				return "Reset data successfully";
			}
			try
			{
				int slot = Integer.parseInt(args[0]);
				if (slot >= inventoryType.getDefaultSize())
				{
					throw new CommandException(
							"Slot number " + slot + " is too high for " + inventoryType.toString().toLowerCase() + "s");
				}
				if (slot < 0)
				{
					throw new CommandException("Slot number " + slot + " is negative");
				}
				// Set the stored item to the item in the sender's hand
				ItemStack item = sender.getItemInHand();
				if (item == null || item.getType() == Material.AIR || item.getAmount() == 0)
				{
					// Remove the item.
					// Set item to null to ensure correct itemName below.
					item = null;
					if (present(sender))
					{
						ItemStack[] data = get(sender);
						data[slot] = null;
						set(sender, data);
					}
				}
				else
				{
					ItemStack[] data = get(sender);
					data[slot] = item.clone();
					set(sender, data);
				}
				String itemName = item == null ? "nothing"
						: item.getAmount() + " " + item.getType().toString().toLowerCase().replace("_", "");
				return "Set the item in slot " + slot + " to " + itemName;
			}
			catch (NumberFormatException ex)
			{
				if (!args[0].equalsIgnoreCase("help"))
				{
					throw new CommandException("Expected a number indicating the slot that you want to set");
				}
			}
		}
		StringBuilder message = new StringBuilder();
		message.append("&a### &3").append(getName()).append("&a Help ###\n");
		message.append("&8").append(getDescription()).append('\n');
		message.append("&6/mod ").append(getName().toLowerCase())
				.append("&o <slot> &bsets the item in slot to your hand\n");
		message.append("&6/mod ").append(getName().toLowerCase()).append("&o clear &bclears the data\n");
		message.append("&6/mod ").append(getName().toLowerCase()).append("&o help &bshows this help page\n");
		return message.toString();
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (present(event.getPlayer()))
		{
			BlockState state = event.getBlock().getState();
			if (state instanceof InventoryHolder)
			{
				Inventory inv = ((InventoryHolder) state).getInventory();
				if (inv.getType() == inventoryType)
				{
					ItemStack[] data = get(event.getPlayer());
					inv.setContents(data);
					state.update();
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected ItemStack[] get(Player player)
	{
		Object obj = DataManager.getData(player.getUniqueId().toString(), "BlockPlaceMods", getName());
		if (obj == null)
			return new ItemStack[inventoryType.getDefaultSize()];
		JSONArray array = (JSONArray) obj;
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		for (Object obj2 : array.toArray())
		{
			items.add((new ItemProperties()).loadFrom((JSONObject) obj2).toItemStack());
		}
		ItemStack[] itemArray = new ItemStack[array.size()];
		for (int i = 0; i < itemArray.length; i++)
		{
			itemArray[i] = items.get(i);
		}
		return itemArray;
	}
	
	protected void set(Player player, ItemStack[] data)
	{
		if (highestUsedIndex(data) == -1)
			reset(player);
		else
		{
			JSONArray array = new JSONArray();
			for (ItemStack stack : data)
			{
				array.add((new ItemProperties(stack)).toJSONObject());
			}
			DataManager.setData(player.getUniqueId().toString(), "BlockPlaceMods", getName(), array);
		}
	}
	
	protected boolean present(Player player)
	{
		return get(player) != null;
	}
}
