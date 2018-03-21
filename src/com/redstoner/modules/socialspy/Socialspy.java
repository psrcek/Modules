package com.redstoner.modules.socialspy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.coremods.moduleLoader.ModuleLoader;
import com.redstoner.misc.BroadcastFilter;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.misc.Utils;
import com.redstoner.modules.CoreModule;
import com.redstoner.modules.Module;
import com.redstoner.modules.datamanager.DataManager;

import net.nemez.chatapi.click.Message;

@Commands(CommandHolderType.File)
@Version(major = 4, minor = 0, revision = 3, compatible = 4)
public class Socialspy implements CoreModule
{
	@Command(hook = "config_prefix_default")
	public boolean prefixDefault(CommandSender sender)
	{
		return prefix(sender, getDefaultPrefix());
	}
	
	@Command(hook = "config_prefix")
	public boolean prefix(CommandSender sender, String prefix)
	{
		getLogger().message(sender, "Set your prefix to: " + prefix);
		DataManager.setData(sender, "prefix", prefix);
		return true;
	}
	
	@Command(hook = "config_format_default")
	public boolean configFormatDefault(CommandSender sender)
	{
		return configFormat(sender, getDefaultFormat());
	}
	
	@Command(hook = "config_format_show")
	public boolean configFormatShow(CommandSender sender)
	{
		String format = (String) DataManager.getOrDefault(sender, "format", getDefaultFormat());
		getLogger().message(sender, "Your current format is: " + format.replaceAll("[&§]", "&&"));
		return true;
	}
	
	@Command(hook = "config_format")
	public boolean configFormat(CommandSender sender, String format)
	{
		getLogger().message(sender, "Set your format to: " + format);
		DataManager.setData(sender, "format", format);
		return true;
	}
	
	@Command(hook = "stripcolor_on")
	public boolean stripcolorOn(CommandSender sender)
	{
		getLogger().message(sender, "Enabled stripping colors!");
		DataManager.setData(sender, "stripcolor", "on");
		return true;
	}
	
	@Command(hook = "stripcolor_off")
	public boolean stripcolorOff(CommandSender sender)
	{
		getLogger().message(sender, "Disabled stripping colors!");
		DataManager.setData(sender, "stripcolor", "off");
		return true;
	}
	
	@Command(hook = "stripcolor_partial")
	public boolean stripcolor_partial(CommandSender sender)
	{
		getLogger().message(sender, "Now replacing colors with their colorcode equivalent!");
		DataManager.setData(sender, "stripcolor", "partial");
		return true;
	}
	
	@Command(hook = "stripcolor")
	public boolean stripcolor(CommandSender sender)
	{
		boolean b = DataManager.getOrDefault(sender, "stripcolor", "on").equals("on");
		getLogger().message(sender, (b ? "Disabled" : "Enabled") + " stripping colors!");
		DataManager.setData(sender, "stripcolor", !b);
		return true;
	}
	
	@Command(hook = "on")
	public boolean spyOn(CommandSender sender)
	{
		getLogger().message(sender, "Enabled socialspy!");
		DataManager.setData(sender, "enabled", true);
		return true;
	}
	
	@Command(hook = "off")
	public boolean spyOff(CommandSender sender)
	{
		getLogger().message(sender, "Disabled socialspy!");
		DataManager.setData(sender, "enabled", false);
		return true;
	}
	
	@Command(hook = "toggle")
	public boolean spyToggle(CommandSender sender)
	{
		boolean b = (boolean) DataManager.getOrDefault(sender, "enabled", false);
		getLogger().message(sender, (b ? "Disabled" : "Enabled") + " socialspy!");
		DataManager.setData(sender, "enabled", !b);
		return true;
	}
	
	@Command(hook = "format_help")
	public boolean formatInfo(CommandSender sender)
	{
		getLogger().message(sender,
				new String[] {" Format placeholders:", " &c%s&eender &7(display name) | &c%S&eender &7(real name)",
						" &c%t&earget &7(display name) | &c%T&earget &7(real name)",
						" &c%p&erefix &7(see prefix option)", " &c%m&eessage", " &c%c&eommand",
						" Any other text will be put as literal text. Use %% to escape any %.",
						" The default format is: §e" + getDefaultFormat().replaceAll("(?i)&([0-9a-fl-o])", "&&$1"),
						" The default prefix is: §e" + getDefaultPrefix().replaceAll("(?i)&([0-9a-fl-o])", "&&$1")});
		return true;
	}
	
	@Command(hook = "commands_list")
	public boolean commands_list(CommandSender sender)
	{
		ArrayList<String> message = new ArrayList<>();
		JSONArray commands = (JSONArray) DataManager.getOrDefault(sender, "commands", getDefaultCommandList());
		if (commands == null || commands.size() == 0)
			message.add("You are not listening to any commands!");
		else
		{
			message.add("You are listening to the following " + commands.size() + " commands:");
			message.add(Arrays.toString(commands.toArray()).replace(", /", "&7, &e/").replace("[", "[&e").replace("]",
					"&7]"));
		}
		getLogger().message(sender, message.toArray(new String[] {}));
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private final JSONArray getDefaultCommandList()
	{
		JSONArray commands = new JSONArray();
		commands.add("/m");
		commands.add("/r");
		return commands;
	}
	
	@SuppressWarnings("unchecked")
	@Command(hook = "commands_add")
	public boolean commands_add(CommandSender sender, String command)
	{
		JSONArray commands = (JSONArray) DataManager.getOrDefault(sender, "commands", getDefaultCommandList());
		commands.add(command);
		DataManager.setData(sender, "commands", commands);
		getLogger().message(sender, "You are now spying on &e" + command);
		return true;
	}
	
	@Command(hook = "commands_del")
	public boolean commands_del(CommandSender sender, String command)
	{
		JSONArray commands = (JSONArray) DataManager.getOrDefault(sender, "commands", getDefaultCommandList());
		commands.remove(command);
		DataManager.setData(sender, "commands", commands);
		getLogger().message(sender, "You are no longer spying on &e" + command);
		return true;
	}
	
	public static void spyBroadcast(CommandSender sender, CommandSender target, String message, String command,
			BroadcastFilter filter)
	{
		try
		{
			Module mod = ModuleLoader.getModule("Socialspy");
			Method m = mod.getClass().getDeclaredMethod("spyBroadcast_", CommandSender.class, CommandSender.class,
					String.class, String.class, BroadcastFilter.class);
			m.invoke(mod, sender, target, message, command, filter);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	public void spyBroadcast_(CommandSender sender, CommandSender target, String message, String command,
			BroadcastFilter filter)
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if ((boolean) DataManager.getOrDefault(p, "enabled", false))
				if (p.hasPermission("utils.socialspy"))
				{
					if (((JSONArray) DataManager.getOrDefault(p, "commands", getDefaultCommandList()))
							.contains(command))
						if (filter == null || filter.sendTo(p))
						{
							Message m = new Message(p, null);
							m.appendText(formatMessage(p, sender, target, message, command));
							m.send();
						}
				}
				else
					DataManager.setData(sender, "enabled", false);
		}
		if (((JSONArray) DataManager.getOrDefault(Bukkit.getConsoleSender(), "commands", getDefaultCommandList()))
				.contains(command))
		{
			Message m = new Message(Bukkit.getConsoleSender(), null);
			m.appendText(formatMessage(Bukkit.getConsoleSender(), sender, target, message, command));
			m.send();
		}
	}
	
	public static void spyBroadcast(CommandSender sender, String target, String message, String command,
			BroadcastFilter filter)
	{
		try
		{
			Module mod = ModuleLoader.getModule("Socialspy");
			Method m = mod.getClass().getDeclaredMethod("spyBroadcast_", CommandSender.class, String.class,
					String.class, String.class, BroadcastFilter.class);
			m.invoke(mod, sender, target, message, command, filter);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
	}
	
	public void spyBroadcast_(CommandSender sender, String target, String message, String command,
			BroadcastFilter filter)
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if ((boolean) DataManager.getOrDefault(p, "enabled", false))
				if (p.hasPermission("utils.socialspy"))
				{
					if (((JSONArray) DataManager.getOrDefault(p, "commands", getDefaultCommandList()))
							.contains(command))
						if (filter == null || filter.sendTo(p))
						{
							Message m = new Message(p, null);
							m.appendText(formatMessage(p, sender, target, message, command));
							m.send();
						}
				}
				else
					DataManager.setData(sender, "enabled", false);
		}
		if (((JSONArray) DataManager.getOrDefault(Bukkit.getConsoleSender(), "commands", getDefaultCommandList()))
				.contains(command))
		{
			Message m = new Message(Bukkit.getConsoleSender(), null);
			m.appendText(formatMessage(Bukkit.getConsoleSender(), sender, target, message, command));
			m.send();
		}
	}
	
	private String formatMessage(CommandSender formatHolder, CommandSender sender, CommandSender target, String message,
			String command)
	{
		String format = (String) DataManager.getOrDefault(formatHolder, "format", getDefaultFormat());
		// Replace escaped % with placeholder
		format = format.replace("%%", "§§");
		// Sender name
		format = format.replace("%s", Utils.getName(sender));
		format = format.replace("%S", sender.getName());
		// Target name
		format = format.replace("%t", Utils.getName(target));
		format = format.replace("%T", target.getName());
		// Prefix
		String prefix = (String) DataManager.getOrDefault(formatHolder, "prefix", getDefaultPrefix());
		format = format.replace("%p", prefix);
		// Apply colors to halfway replaced String
		format = ChatColor.translateAlternateColorCodes('&', format);
		// Insert command and message
		format = format.replace("%c", command);
		
		// Color stripping
		Object o = DataManager.getOrDefault(formatHolder, "stripcolor", "off");
		if (o instanceof Boolean)
		{
			boolean b = (boolean) o;
			if (b)
				o = "on";
			else
				o = "off";
			DataManager.setData(formatHolder, "stripcolor", o);
		}
		String s = (String) o;
		if (s.equals("on"))
			message = ChatColor.stripColor(message).replaceAll("(?i)[&$][0-9a-fk-o]", "");
		else if (s.equals("partial"))
			message = message.replaceAll("(?i)[§&]([0-9a-fk-o])", "&&$1");
		// Insert message
		format = format.replace("%m", message);
		
		// Convert placeholder back
		format = format.replace("§§", "%");
		return format;
	}
	
	private String formatMessage(CommandSender formatHolder, CommandSender sender, String target, String message,
			String command)
	{
		String format = (String) DataManager.getOrDefault(formatHolder, "format", getDefaultFormat());
		// Replace escaped % with placeholder
		format = format.replace("%%", "§§");
		// Sender name
		format = format.replace("%s", Utils.getName(sender));
		format = format.replace("%S", sender.getName());
		// Target name
		format = format.replace("%t", target);
		format = format.replace("%T", target);
		// Prefix
		String prefix = (String) DataManager.getOrDefault(formatHolder, "prefix", getDefaultPrefix());
		format = format.replace("%p", prefix);
		// Apply colors to halfway replaced String
		format = ChatColor.translateAlternateColorCodes('&', format);
		// Insert command
		format = format.replace("%c", command);
		
		// Color stripping
		Object o = DataManager.getOrDefault(formatHolder, "stripcolor", "off");
		if (o instanceof Boolean)
		{
			boolean b = (boolean) o;
			if (b)
				o = "on";
			else
				o = "off";
			DataManager.setData(formatHolder, "stripcolor", o);
		}
		String s = (String) o;
		if (s.equals("on"))
			message = ChatColor.stripColor(message).replaceAll("(?i)[&$][0-9a-fk-o]", "");
		else if (s.equals("partial"))
			message = message.replaceAll("(?i)[§&]([0-9a-fk-o])", "&&$1");
		// Insert message
		format = format.replace("%m", message);
		
		// Convert placeholder back
		format = format.replace("§§", "%");
		return format;
	}
	
	private final String getDefaultFormat()
	{
		return " %s &7to %t%p: %m";
	}
	
	private final String getDefaultPrefix()
	{
		return "&7";
	}
	
	@Command(hook = "migrate")
	public boolean migrate(CommandSender sender)
	{
		DataManager.migrateAll("Message");
		return true;
	}
}
