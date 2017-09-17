package com.redstoner.modules.adminnotes;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.misc.JsonManager;
import com.redstoner.misc.Main;
import com.redstoner.modules.Module;

import de.pepich.chestapi.CallbackHandler;
import de.pepich.chestapi.ClickableInventory;
import de.pepich.chestapi.DefaultSize;
import net.nemez.chatapi.click.ClickCallback;
import net.nemez.chatapi.click.Message;

@Commands(CommandHolderType.String)
@AutoRegisterListener
@Version(major = 4, minor = 0, revision = 0, compatible = 4)
public class AdminNotes implements Module, Listener
{
	JSONArray notes;
	File saveFile = new File(Main.plugin.getDataFolder(), "adminnotes.json");
	
	@Override
	public boolean onEnable()
	{
		notes = JsonManager.getArray(saveFile);
		if (notes == null)
			notes = new JSONArray();
		return true;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e)
	{
		if (e.getPlayer().hasPermission("utils.adminnotes"))
			if (notes.size() > 0)
			{
				Message m = new Message(e.getPlayer(), null);
				m.appendSendChat(getLogger().getPrefix(), "/an list");
				m.appendSendChat("&cThere are " + notes.size() + " open notes!", "/an list");
				m.send();
			}
	}
	
	@Override
	public void onDisable()
	{
		saveNotes();
	}
	
	@SuppressWarnings("unchecked")
	@Command(hook = "an_create")
	public void createNote(CommandSender sender, String note)
	{
		JSONArray temp = new JSONArray();
		temp.add(sender.getName());
		temp.add(note);
		temp.add((double) System.currentTimeMillis() / 1000);
		notes.add(temp);
		getLogger().message(sender, "&aNote added!");
		saveNotes();
	}
	
	@Command(hook = "an_del")
	public void delNote(CommandSender sender, int id)
	{
		if (id < notes.size() && id >= 0 && notes.get(id) != null)
		{
			notes.remove(id);
			getLogger().message(sender, "&aNote " + id + " has been removed!");
			saveNotes();
		}
		else
		{
			getLogger().message(sender, "&cThat note does not exist!");
		}
	}
	
	@Command(hook = "an_list")
	public void list(CommandSender sender)
	{
		Message m = new Message(sender, null);
		m.appendText(getLogger().getHeader());
		for (Object note : notes)
		{
			String string = ChatColor.YELLOW + "" + notes.indexOf(note) + ": ";
			string += "§a" + ((JSONArray) note).get(1);
			string += "\n§e - " + ((JSONArray) note).get(0) + ", §6";
			SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy HH:mm");
			string += format.format((double) ((JSONArray) note).get(2) * 1000);
			m.appendCallbackHover(string, new ClickCallback(true, true, null)
			{
				@Override
				public void run(CommandSender sender)
				{
					if (notes.contains(note))
						showMenu((Player) sender, notes.indexOf(note));
					else
						getLogger().message(sender, true, "That note no longer exists!");
				}
			}, "Click to show note options.");
			m.appendText("\n");
		}
		m.send();
	}
	
	public void showMenu(Player player, int index)
	{
		String note_text = ((String) ((JSONArray) notes.get(index)).get(1));
		if (note_text.length() > 15)
			note_text = note_text.substring(0, 15) + "...";
		ClickableInventory inv = new ClickableInventory("Note " + index + ": " + note_text, DefaultSize.FINAL_FIXED(9));
		
		ItemStack addItem = new ItemStack(Material.BOOK_AND_QUILL);
		ItemMeta addMeta = addItem.getItemMeta();
		addMeta.setDisplayName("§aAdd note!");
		addItem.setItemMeta(addMeta);
		inv.set(1, addItem, new CallbackHandler()
		{
			@Override
			public void run(Player player, ClickType type)
			{
				Message m = new Message(player, null);
				m.appendSuggest(getLogger().getPrefix(), "/an add ");
				m.appendSuggest("Click me to add a note!", "/an add ");
				m.send();
				player.closeInventory();
			}
		});
		
		ItemStack deleteItem = new ItemStack(Material.WOOL, 1, (short) 14);
		ItemMeta deleteMeta = deleteItem.getItemMeta();
		deleteMeta.setDisplayName("§cDelete note!");
		deleteMeta.setLore(Arrays.asList(new String[] {"Shift click to execute!"}));
		deleteItem.setItemMeta(deleteMeta);
		inv.set(3, deleteItem, new CallbackHandler()
		{
			@Override
			public void run(Player player, ClickType type)
			{
				if (type == ClickType.SHIFT_LEFT)
				{
					delNote(player, index);
					player.closeInventory();
				}
				else
					getLogger().message(player, true, "You need to shift click to execute this!");
			}
		});
		
		ItemStack closeItem = new ItemStack(Material.BARRIER);
		ItemMeta closeMeta = closeItem.getItemMeta();
		closeMeta.setDisplayName("§eClose menu");
		closeItem.setItemMeta(closeMeta);
		inv.set(9, closeItem, new CallbackHandler()
		{
			@Override
			public void run(Player player, ClickType type)
			{
				player.closeInventory();
			}
		});
		inv.show(player);
	}
	
	public void saveNotes()
	{
		JsonManager.save(notes, saveFile);
	}
	
	// @noformat
	@Override
	public String getCommandString()
	{
		return "command an {\n" + 
				"	\n" + 
				"	add [string:note...] {\n" + 
				"		help Creates a new admin note;\n" + 
				"		run an_create note;\n" + 
				"		perm utils.an;" +
				"	}\n" + 
				"	\n" + 
				"	del [int:id] {\n" + 
				"		help Deletes an admin note;\n" + 
				"		run an_del id;\n" + 
				"		perm utils.an;" +
				"	}\n" + 
				"	\n" + 
				"	list {\n" + 
				"		help Lists all notes;\n" + 
				"		run an_list;\n" + 
				"		perm utils.an;" +
				"	}\n" + 
				"}";
	}
	// @format
}
