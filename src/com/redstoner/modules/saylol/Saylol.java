package com.redstoner.modules.saylol;

import java.io.File;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.BroadcastFilter;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.misc.JsonManager;
import com.redstoner.misc.Main;
import com.redstoner.misc.Utils;
import com.redstoner.modules.Module;

import net.nemez.chatapi.click.ClickCallback;
import net.nemez.chatapi.click.Message;

@Commands(CommandHolderType.String)
@Version(major = 4, minor = 0, revision = 0, compatible = 4)
public class Saylol implements Module
{
	private long lastLol = 0;
	private File lolLocation = new File(Main.plugin.getDataFolder(), "lol.json");
	private JSONArray lols, handlers;
	private final String LOL_PREFIX = "§8[§blol§8] ";
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean onEnable()
	{
		lols = JsonManager.getArray(lolLocation);
		if (lols == null)
			lols = new JSONArray();
		handlers = new JSONArray();
		for (int i = 0; i < lols.size(); i++)
			handlers.add(new ClickCallback(true, true, "")
			{
				@Override
				public void run(CommandSender sender)
				{
					if (handlers.contains(this))
						clickAction((Player) sender, handlers.indexOf(this));
					else
						getLogger().message(sender, true, "That lol no longer exists!");
				}
			});
		return true;
	}
	
	@Override
	public void onDisable()
	{
		saveLolsSync();
	}
	
	@SuppressWarnings("unchecked")
	@Command(hook = "addlol")
	public boolean addLol(CommandSender sender, String text)
	{
		if (lols.contains(text))
			getLogger().message(sender, true, "This lol already exists!");
		else
		{
			getLogger().message(sender, "Successfully added a new lol!");
			lols.add("&e" + text);
			handlers.add(new ClickCallback(true, true, "")
			{
				@Override
				public void run(CommandSender sender)
				{
					if (handlers.contains(this))
						clickAction((Player) sender, handlers.indexOf(this));
					else
						getLogger().message(sender, true, "That lol no longer exists!");
				}
			});
			saveLols();
		}
		return true;
	}
	
	@Command(hook = "dellol")
	public boolean delLol(CommandSender sender, int id)
	{
		if (lols.size() == 0)
		{
			getLogger().message(sender, true, "There are no lols yet!");
			return true;
		}
		if (id < 0 || id >= lols.size())
		{
			getLogger().message(sender, true, "The ID must be at least 0 and at most " + (lols.size() - 1));
			return true;
		}
		getLogger().message(sender, "Successfully deleted the lol: " + lols.remove(id));
		handlers.remove(id);
		saveLols();
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Command(hook = "setlol")
	public boolean setLol(CommandSender sender, int id, String text)
	{
		if (lols.size() == 0)
		{
			getLogger().message(sender, true, "There are no lols yet!");
			return true;
		}
		if (id < 0 || id >= lols.size())
		{
			getLogger().message(sender, true, "The ID must be at least 0 and at most " + (lols.size() - 1));
			return true;
		}
		getLogger().message(sender, "Successfully changed the lol: &e" + lols.get(id) + " &7to: &e" + text);
		lols.set(id, text);
		saveLols();
		return true;
	}
	
	@Command(hook = "lolid")
	public boolean lolId(CommandSender sender, int id)
	{
		if (lols.size() == 0)
		{
			getLogger().message(sender, true, "There are no lols yet!");
			return true;
		}
		long time = System.currentTimeMillis();
		if (time - lastLol < 15000)
		{
			getLogger().message(sender, true,
					"You can't use saylol for another " + (14 - (int) Math.ceil((time - lastLol) / 1000)) + "s.");
			return true;
		}
		if (id < 0 || id >= lols.size())
		{
			getLogger().message(sender, true, "The ID must be at least 0 and at most " + (lols.size() - 1));
			return true;
		}
		String name;
		if (sender instanceof Player)
			name = ((Player) sender).getDisplayName();
		else
			name = "&9" + sender.getName();
		Utils.broadcast(LOL_PREFIX, name + "&8: &e" + lols.get(id), new BroadcastFilter()
		{
			@Override
			public boolean sendTo(CommandSender recipient)
			{
				return recipient.hasPermission("utils.lol.see");
			}
		});
		lastLol = time;
		return true;
	}
	
	@Command(hook = "saylol")
	public boolean saylol(CommandSender sender)
	{
		if (lols.size() == 0)
		{
			getLogger().message(sender, true, "There are no lols yet!");
			return true;
		}
		long time = System.currentTimeMillis();
		if (time - lastLol < 15000)
		{
			getLogger().message(sender, true,
					"You can't use saylol for another " + (14 - (int) Math.ceil((time - lastLol) / 1000)) + "s.");
			return true;
		}
		String name;
		if (sender instanceof Player)
			name = ((Player) sender).getDisplayName();
		else
			name = "&9" + sender.getName();
		Random random = new Random();
		int id = random.nextInt(lols.size());
		Utils.broadcast(LOL_PREFIX, name + "&8: &e" + lols.get(id), new BroadcastFilter()
		{
			@Override
			public boolean sendTo(CommandSender recipient)
			{
				return recipient.hasPermission("utils.lol.see");
			}
		});
		lastLol = time;
		return true;
	}
	
	@Command(hook = "listlols")
	public boolean listLols(CommandSender sender, int page)
	{
		if (lols.size() == 0)
		{
			getLogger().message(sender, true, "There are no lols yet!");
			return true;
		}
		page = page - 1;
		int start = page * 10;
		int end = start + 10;
		int pages = (int) Math.ceil(lols.size() / 10d);
		if (start < 0)
		{
			getLogger().message(sender, true, "Page number too small, must be at least 0!");
			return true;
		}
		if (start > lols.size())
		{
			getLogger().message(sender, true, "Page number too big, must be at most " + pages + "!");
			return true;
		}
		Message m = new Message(sender, null);
		m.appendText(getLogger().getHeader().replace("\n", ""));
		m.appendText("&ePage " + (page + 1) + "/" + pages + ":");
		for (int i = start; i < end && i < lols.size(); i++)
			m.appendCallback("\n&a" + i + "&8: &e" + lols.get(i), getCallback(i));
		m.send();
		return true;
	}
	
	@Command(hook = "listlolsdef")
	public boolean listLolsDefault(CommandSender sender)
	{
		return listLols(sender, 1);
	}
	
	@Command(hook = "searchlol")
	public boolean search(CommandSender sender, boolean sensitive, String text)
	{
		Message m = new Message(sender, null);
		m.appendText(getLogger().getHeader().replace("\n", ""));
		boolean found = false;
		if (!sensitive)
			text = text.toLowerCase();
		for (int i = 0; i < lols.size(); i++)
		{
			String lol = (String) lols.get(i);
			if ((sensitive ? lol : lol.toLowerCase()).contains(text))
			{
				m.appendCallback("\n&a" + i + "&8: &e" + lol, getCallback(i));
				found = true;
			}
		}
		if (!found)
			getLogger().message(sender, "&cCouldn't find any matching lols.");
		else
			m.send();
		return true;
	}
	
	@Command(hook = "matchlol")
	public boolean match(CommandSender sender, boolean sensitive, String regex)
	{
		Message m = new Message(sender, null);
		m.appendText(getLogger().getHeader().replace("\n", ""));
		boolean found = false;
		if (!sensitive)
			regex = regex.toLowerCase();
		for (int i = 0; i < lols.size(); i++)
		{
			String lol = (String) lols.get(i);
			if ((sensitive ? lol : lol.toLowerCase()).matches(regex))
			{
				m.appendCallback("\n&a" + i + "&8: &e" + lol, getCallback(i));
				found = true;
			}
		}
		if (!found)
			getLogger().message(sender, "&cCouldn't find any matching lols.");
		else
			m.send();
		return true;
	}
	
	public void saveLols()
	{
		JsonManager.save(lols, lolLocation);
	}
	
	public void saveLolsSync()
	{
		JsonManager.saveSync(lols, lolLocation);
	}
	
	public ClickCallback getCallback(int index)
	{
		return (ClickCallback) handlers.get(index);
	}
	
	public void clickAction(Player player, int index)
	{
		if (player.hasPermission("utils.lol.id"))
			Bukkit.dispatchCommand(player, "lol id " + index);
	}
	
	// @noformat
	@Override
	public String getCommandString()
	{
		return "command lol {\n" + 
				"    add [string:text...] {\n" + 
				"        help Lols a text.;\n" + 
				"        run addlol text;\n" + 
				"        perm utils.lol.admin;\n" + 
				"    }\n" + 
				"    del [int:id] {\n" + 
				"        help Unlols a lol.;\n" + 
				"        run dellol id;\n" + 
				"        perm utils.lol.admin;\n" + 
				"    }\n" + 
				"    set [int:id] [string:text...] {\n" + 
				"        help Relols a lol.;\n" + 
				"        run setlol id text;\n" + 
				"        perm utils.lol.admin;\n" + 
				"    }\n" + 
				"    id [int:id] {\n" + 
				"        help Lols specifically.;\n" + 
				"        run lolid id;\n" + 
				"        perm utils.lol.id;\n" + 
				"    }\n" + 
				"    list [int:page] {\n" + 
				"        help Shows lols.;\n" + 
				"        run listlols page;\n" + 
				"        perm utils.lol.list;\n" + 
				"    }\n" + 
				"    list {\n" + 
				"        help Shows lols.;\n" + 
				"        run listlolsdef;\n" + 
				"        perm utils.lol.list;\n" + 
				"    }\n" + 
				"    search [flag:-i] [string:text...] {\n" + 
				"        help Search lols.;\n" + 
				"        run searchlol -i text;\n" + 
				"        perm utils.lol.search;\n" + 
				"    }\n" + 
				"    match [flag:-i] [string:regex...] {\n" + 
				"        help Search lols. But better.;\n" + 
				"        run matchlol -i regex;\n" + 
				"        perm utils.lol.match;\n" + 
				"    }\n" + 
				"    [empty] {\n" + 
				"        help Lols.;\n" + 
				"        run saylol;\n" + 
				"        perm utils.lol;\n" + 
				"    }\n" + 
				"}";
	}
	// @format
}
