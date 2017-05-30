package com.redstoner.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/** Save and load {@link ItemStack} in json format
 * Any additional NBT data not included by {@link ItemMeta} is discarded. */
public class ItemProperties
{
	private int id = 0;
	private byte data = 0;
	private int amount = 1;
	private Map<Enchantment, Integer> enchantments;
	private List<String> lore;
	private String displayName;
	private boolean unbreakable = false;
	
	public ItemProperties()
	{}
	
	@SuppressWarnings("deprecation")
	public ItemProperties(ItemStack item)
	{
		if (item == null)
			return;
		id = item.getTypeId();
		data = item.getData().getData();
		amount = item.getAmount();
		enchantments = new HashMap<>();
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
			return;
		if (meta.hasEnchants())
		{
			enchantments.putAll(meta.getEnchants());
		}
		if (meta.hasLore())
		{
			lore = meta.getLore();
		}
		if (meta.hasDisplayName())
		{
			displayName = meta.getDisplayName();
		}
		unbreakable = meta.isUnbreakable();
	}
	
	@SuppressWarnings("deprecation")
	public ItemStack toItemStack()
	{
		ItemStack result = new ItemStack(id, amount, data);
		ItemMeta meta = result.getItemMeta();
		if (meta == null)
			return result;
		if (enchantments != null)
		{
			enchantments.forEach(new BiConsumer<Enchantment, Integer>()
			{
				@Override
				public void accept(Enchantment ench, Integer level)
				{
					meta.addEnchant(ench, level, true);
				}
			});
		}
		if (lore != null)
		{
			meta.setLore(lore);
		}
		if (displayName != null)
		{
			meta.setDisplayName(displayName);
		}
		meta.setUnbreakable(unbreakable);
		result.setItemMeta(meta);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJSONObject()
	{
		JSONObject object = new JSONObject();
		object.put("id", id + "");
		object.put("data", data + "");
		object.put("amount", amount + "");
		if (displayName != null)
		{
			object.put("displayName", displayName);
		}
		if (enchantments != null)
		{
			Map<Enchantment, Integer> enchantments = this.enchantments;
			JSONObject stringKeys = new JSONObject();
			for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet())
			{
				stringKeys.put(entry.getKey().getName(), entry.getValue());
			}
			object.put("enchantments", stringKeys);
		}
		if (lore != null)
		{
			object.put("lore", JSONArray.toJSONString(lore));
		}
		if (unbreakable)
		{
			object.put("unbreakable", true);
		}
		return object;
	}
	
	@Override
	public String toString()
	{
		return toJSONObject().toString();
	}
	
	@SuppressWarnings("unchecked")
	public ItemProperties loadFrom(JSONObject object)
	{
		for (Object obj : object.entrySet())
		{
			Entry<String, Object> entry = (Entry<String, Object>) obj;
			final String key = entry.getKey();
			switch (key)
			{
				case "id":
					id = Integer.parseInt((String) entry.getValue());
					break;
				case "data":
					data = Byte.parseByte((String) entry.getValue());
					break;
				case "amount":
					amount = Integer.parseInt((String) entry.getValue());
					break;
				case "unbreakable":
					unbreakable = (boolean) entry.getValue();
					break;
				case "enchantments":
				{
					if (enchantments == null)
					{
						enchantments = new HashMap<>();
					}
					else if (!enchantments.isEmpty())
					{
						enchantments.clear();
					}
					JSONObject read = (JSONObject) entry.getValue();
					if (read != null)
					{
						for (Object obj2 : read.entrySet())
						{
							Entry<String, Integer> entry2 = (Entry<String, Integer>) obj2;
							Enchantment ench = Enchantment.getByName(entry2.getKey());
							if (ench != null)
							{
								enchantments.put(ench, entry2.getValue());
							}
						}
					}
					break;
				}
				case "lore":
					JSONParser parser = new JSONParser();
					Object rawObject;
					try
					{
						rawObject = parser.parse((String) entry.getValue());
					}
					catch (ParseException e)
					{
						rawObject = new JSONArray();
					}
					JSONArray jsonArray = (JSONArray) rawObject;
					lore = jsonArray;
					break;
				case "displayName":
					displayName = (String) entry.getValue();
				default:
			}
		}
		return this;
	}
	
	public int getId()
	{
		return id;
	}
	
	public byte getData()
	{
		return data;
	}
	
	public int getAmount()
	{
		return amount;
	}
	
	public Map<Enchantment, Integer> getEnchantments()
	{
		return enchantments;
	}
	
	public List<String> getLore()
	{
		return lore;
	}
	
	public String getDisplayName()
	{
		return displayName;
	}
	
	public boolean isUnbreakable()
	{
		return unbreakable;
	}
	
	public ItemProperties setId(int id)
	{
		this.id = id;
		return this;
	}
	
	public ItemProperties setData(byte data)
	{
		this.data = data;
		return this;
	}
	
	public ItemProperties setAmount(int amount)
	{
		this.amount = amount;
		return this;
	}
	
	public ItemProperties setEnchantments(Map<Enchantment, Integer> enchantments)
	{
		this.enchantments = enchantments;
		return this;
	}
	
	public ItemProperties setLore(List<String> lore)
	{
		this.lore = lore;
		return this;
	}
	
	public ItemProperties setDisplayName(String displayName)
	{
		this.displayName = displayName;
		return this;
	}
	
	public ItemProperties setUnbreakable(boolean unbreakable)
	{
		this.unbreakable = unbreakable;
		return this;
	}
}
