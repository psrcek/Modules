package com.redstoner.modules.eastereggs;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.misc.JsonManager;
import com.redstoner.misc.Main;
import com.redstoner.modules.Module;
import com.redstoner.modules.datamanager.DataManager;
import com.redstoner.utils.ItemProperties;

import net.nemez.chatapi.click.Message;

@Commands(CommandHolderType.File)
@AutoRegisterListener
@Version(major = 4, minor = 0, revision = 0, compatible = 4)
public class Eastereggs implements Module, Listener
{
	final File prefixFile = new File(Main.plugin.getDataFolder(), "eastereggs/prefix.json"),
			itemFile = new File(Main.plugin.getDataFolder(), "eastereggs/item.json"),
			modifierFile = new File(Main.plugin.getDataFolder(), "eastereggs/modifier.json"),
			suffixFile = new File(Main.plugin.getDataFolder(), "eastereggs/suffix.json");
	JSONArray prefix, item, modifier, suffix;
	
	@Override
	public boolean onEnable()
	{
		prefix = JsonManager.getArray(prefixFile);
		item = JsonManager.getArray(itemFile);
		modifier = JsonManager.getArray(modifierFile);
		suffix = JsonManager.getArray(suffixFile);
		return true;
	}
	
	@Command(hook = "rekmedaddy")
	public boolean rekmedaddy(CommandSender sender)
	{
		Message m = new Message(sender, null);
		m.appendTextHover("§c#BlameNyx", "§5No really. Blame Nyx.");
		m.send();
		return true;
	}
	
	@Command(hook = "remind")
	public boolean remindmedaddy(CommandSender sender)
	{
		Message m = new Message(sender, null);
		m.appendText("§cls = list");
		m.send();
		return true;
	}
	
	@Command(hook = "hidden")
	public boolean hidden(CommandSender sender)
	{
		Message m = new Message(sender, null);
		m.appendText("§dI am going to put cute stickers on your rotting corpse. <3");
		m.send();
		return true;
	}
	
	@Command(hook = "shear")
	public boolean shear(CommandSender sender)
	{
		Player p = (Player) sender;
		ItemStack item = p.getInventory().getItemInMainHand();
		if (item == null)
			return shear_clear(sender);
		ItemProperties itemProps = new ItemProperties(item);
		DataManager.setData(sender, "item", itemProps.toJSONObject());
		setPrefix("Shear");
		getLogger().message(sender, "Set ya shear item to whatever you have in ya hand m8!");
		return true;
	}
	
	@Command(hook = "shear_clear")
	public boolean shear_clear(CommandSender sender)
	{
		DataManager.removeData(sender, "item");
		getLogger().message(sender, "Cleared ya shear!");
		return true;
	}
	
	@EventHandler
	public void onPlayerShear(PlayerInteractAtEntityEvent event)
	{
		if (event.getHand() == EquipmentSlot.OFF_HAND)
			return;
		Entity e = event.getRightClicked();
		if (!(e instanceof Player))
			return;
		if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.SHEARS)
			return;
		Player target = (Player) e;
		if (!target.hasPermission("utils.eastereggs.shear"))
			return;
		Player player = event.getPlayer();
		if (!(player.getGameMode() == GameMode.CREATIVE))
			return;
		ItemProperties itemProps = new ItemProperties();
		Object o = DataManager.getData(target, "item");
		ItemStack item;
		if (o == null)
			item = new ItemStack(Material.REDSTONE);
		else
			item = itemProps.loadFrom((JSONObject) o).toItemStack();
		player.playSound(player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1f, 1f);
		player.getInventory().addItem(item);
	}
	
	@Command(hook = "stick")
	public boolean stick(CommandSender sender)
	{
		setPrefix("Stick");
		Player player = (Player) sender;
		if (player.getGameMode() != GameMode.CREATIVE)
		{
			getLogger().message(sender, true, "§4The gods are angry with you, as you tried to cheat the system...");
			return true;
		}
		Random random = new Random();
		JSONObject prefx = (JSONObject) prefix.get(random.nextInt(prefix.size()));
		JSONObject itm = (JSONObject) item.get(random.nextInt(item.size()));
		JSONObject mod = (JSONObject) modifier.get(random.nextInt(modifier.size()));
		JSONObject sufx = (JSONObject) suffix.get(random.nextInt(suffix.size()));
		
		StringBuilder sb = new StringBuilder("§r");
		sb.append(prefx.get("name"));
		sb.append(" ");
		sb.append(itm.get("name"));
		sb.append(" of ");
		sb.append(mod.get("name"));
		sb.append(" ");
		sb.append(sufx.get("name"));
		
		String itemName = sb.toString().replace("  ", " ");
		
		Object rawMaterialName;
		if ((rawMaterialName = sufx.get("item")) == null)
			if ((rawMaterialName = mod.get("item")) == null)
				if ((rawMaterialName = itm.get("item")) == null)
					if ((rawMaterialName = prefx.get("item")) == null)
						rawMaterialName = "minecraft:stick";
						
		Material material = Material.valueOf(((String) rawMaterialName).toUpperCase().replaceFirst(".+?:", ""));
		if (material == null)
			material = Material.STICK;
			
		ItemStack resultingItem = new ItemStack(material);
		ItemMeta meta = resultingItem.getItemMeta();
		if (meta == null)
			meta = Bukkit.getServer().getItemFactory().getItemMeta(material);
		if (meta != null)
		{
			meta.setDisplayName(itemName);
			
			// Enchantments
			
			boolean override = false;
			int overrideLvl = 0;
			int totalMod = 0;
			
			Object rawMod;
			if ((rawMod = sufx.get("mod")) != null)
			{
				String stringMod = (String) rawMod;
				if (stringMod.startsWith("="))
				{
					overrideLvl = Integer.parseInt(stringMod.replaceFirst("=", ""));
					override = true;
				}
				else
				{
					totalMod += parseInt(stringMod);
				}
			}
			if ((rawMod = mod.get("mod")) != null)
			{
				String stringMod = (String) rawMod;
				if (stringMod.startsWith("="))
				{
					overrideLvl = Integer.parseInt(stringMod.replaceFirst("=", ""));
					override = true;
				}
				else
				{
					totalMod += parseInt(stringMod);
				}
			}
			if ((rawMod = itm.get("mod")) != null)
			{
				String stringMod = (String) rawMod;
				if (stringMod.startsWith("="))
				{
					overrideLvl = Integer.parseInt(stringMod.replaceFirst("=", ""));
					override = true;
				}
				else
				{
					totalMod += parseInt(stringMod);
				}
			}
			if ((rawMod = prefx.get("mod")) != null)
			{
				String stringMod = (String) rawMod;
				if (stringMod.startsWith("="))
				{
					overrideLvl = Integer.parseInt(stringMod.replaceFirst("=", ""));
					override = true;
				}
				else
				{
					totalMod += parseInt(stringMod);
				}
			}
			
			HashMap<Enchantment, Integer> enchants = parseEnchants(prefx, itm, mod, sufx);
			if (enchants != null)
				for (Entry<Enchantment, Integer> entry : enchants.entrySet())
				{
					Integer lvl = entry.getValue();
					if (lvl == null)
						lvl = 1;
					meta.addEnchant(entry.getKey() == null ? getRandomValue(Enchantment.values()) : entry.getKey(),
							override ? overrideLvl : (lvl + totalMod), true);
				}
				
			resultingItem.setItemMeta(meta);
		}
		
		getLogger().message(player, "§8You have received a gift from the gods!");
		try
		{
			player.getInventory().addItem(resultingItem);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return true;
	}
	
	public HashMap<Enchantment, Integer> parseEnchants(JSONObject prefx, JSONObject itm, JSONObject mod,
			JSONObject sufx)
	{
		HashMap<Enchantment, Integer> result = new HashMap<Enchantment, Integer>();
		Object raw;
		if ((raw = sufx.get("ench")) != null)
		{
			if (raw instanceof String)
				return null;
			JSONArray rawArray = (JSONArray) raw;
			for (Object o : rawArray)
			{
				JSONObject rawEnchant = (JSONObject) o;
				result.put(Enchantment.getByName((String) rawEnchant.get("id")),
						parseInt((String) rawEnchant.get("lvl")));
			}
		}
		
		if ((raw = mod.get("ench")) != null)
		{
			if (raw instanceof String)
				return null;
			JSONArray rawArray = (JSONArray) raw;
			for (Object o : rawArray)
			{
				JSONObject rawEnchant = (JSONObject) o;
				result.put(Enchantment.getByName((String) rawEnchant.get("id")),
						parseInt((String) rawEnchant.get("lvl")));
			}
		}
		
		if ((raw = itm.get("ench")) != null)
		{
			if (raw instanceof String)
				return null;
			JSONArray rawArray = (JSONArray) raw;
			for (Object o : rawArray)
			{
				JSONObject rawEnchant = (JSONObject) o;
				result.put(Enchantment.getByName((String) rawEnchant.get("id")),
						parseInt((String) rawEnchant.get("lvl")));
			}
		}
		
		if ((raw = prefx.get("ench")) != null)
		{
			if (raw instanceof String)
				return null;
			JSONArray rawArray = (JSONArray) raw;
			for (Object o : rawArray)
			{
				JSONObject rawEnchant = (JSONObject) o;
				result.put(Enchantment.getByName((String) rawEnchant.get("id")),
						parseInt((String) rawEnchant.get("lvl")));
			}
		}
		return result;
	}
	
	@Command(hook = "bush")
	public boolean bush(CommandSender sender)
	{
		Message m = new Message(sender, null);
		m.appendText("§8There is no /deadbush");
		m.send();
		return true;
	}
	
	private static Integer parseInt(String raw)
	{
		if (raw == null)
			return null;
		else if (raw.startsWith("["))
		{
			String[] rawArray = raw.replace("[", "").replace("]", "").split(";");
			if (rawArray.length != 2)
				return null;
			int lower = Integer.parseInt(rawArray[0]);
			int upper = Integer.parseInt(rawArray[1]);
			Random random = new Random();
			return random.nextInt(upper - lower) + lower;
		}
		else
			return Integer.valueOf(raw);
	}
	
	private static <T> T getRandomValue(T[] array)
	{
		Random random = new Random();
		return array[random.nextInt(array.length)];
	}
}
