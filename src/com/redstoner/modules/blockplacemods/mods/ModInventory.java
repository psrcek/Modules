package com.redstoner.modules.blockplacemods.mods;

import com.redstoner.modules.datamanager.DataManager;
import com.redstoner.utils.CommandException;
import com.redstoner.utils.ItemProperties;
import org.bukkit.GameMode;
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

import java.util.Arrays;

public class ModInventory extends ModAbstract
{
	protected InventoryType inventoryType;
	
	public ModInventory(String name, InventoryType inventoryType)
	{
		super(name);
		this.inventoryType = inventoryType;
	}
	
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
		return "Controls " + inventoryType.name().toLowerCase() + " placement content";
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
				ItemStack item = sender.getInventory().getItemInMainHand();
				if (item == null || item.getType() == Material.AIR || item.getAmount() == 0)
				{
					// Remove the item.
					// Set item to null to ensure correct itemName below.
					item = null;
					if (present(sender))
					{
						set(sender, slot, null);
					}
				}
				else
				{
					set(sender, slot, item);// don't need to clone because the reference isn't kept
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
		message.append(" &a### &3Container Mod&a Help ###\n");
		message.append("&7").append(getDescription()).append('\n');
		message.append("&6/mod ").append(getName().toLowerCase())
				.append("&o <slot> &bsets the item in slot to your hand\n");
		message.append("&6/mod ").append(getName().toLowerCase()).append("&o clear &bclears the data\n");
		message.append("&6/mod ").append(getName().toLowerCase()).append("&o help &bshows this help page\n");
		return message.toString();
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (present(event.getPlayer()) && event.getPlayer().getGameMode() == GameMode.CREATIVE)
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
	
	protected ItemStack[] get(Player player)
	{
		Object obj = DataManager.getData(player.getUniqueId().toString(), "BlockPlaceMods", getName());
		if (obj == null)
			return getDefault();
		JSONArray array = (JSONArray) obj;
		ItemStack[] items = new ItemStack[Math.min(inventoryType.getDefaultSize(), array.size())];
		for (int i = 0, n = items.length; i < n; i++)
		{
			Object obj2 = array.get(i);
			if (obj2 instanceof JSONObject)
			{ // if null, items[i] remains null
				items[i] = new ItemProperties().loadFrom((JSONObject) obj2).toItemStack();
			}
		}
		return items;
	}
	
	protected void set(Player player, int index, ItemStack item)
	{
		ItemStack[] data = get(player);
		if (item == null)
		{
			if (index < data.length)
			{
				data[index] = null;
			}
		}
		else
		{
			if (index >= data.length)
			{
				data = Arrays.copyOf(data, index + 1);
			}
			data[index] = item;
		}
		set(player, data);
	}
	
	@SuppressWarnings("unchecked")
	protected void set(Player player, ItemStack[] data)
	{
		if (highestUsedIndex(data) == -1)
			reset(player);
		else
		{
			JSONArray array = new JSONArray();
			for (int i = 0, n = highestUsedIndex(data); i < n; i++)
			{
				ItemStack item = data[i];
				array.add(item == null ? null : new ItemProperties(item).toJSONObject());
			}
			DataManager.setData(player.getUniqueId().toString(), "BlockPlaceMods", getName(), array);
		}
	}
	
	protected boolean present(Player player)
	{
		return get(player) != null;
	}
	
	@Override
	public ItemStack[] getDefault()
	{
		return new ItemStack[0];
	}
}
