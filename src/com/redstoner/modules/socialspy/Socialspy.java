package com.redstoner.modules.socialspy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;

import com.nemez.cmdmgr.Command;
import com.nemez.cmdmgr.CommandManager;
import com.redstoner.annotations.Version;
import com.redstoner.coremods.moduleLoader.ModuleLoader;
import com.redstoner.misc.BroadcastFilter;
import com.redstoner.misc.Main;
import com.redstoner.misc.Utils;
import com.redstoner.modules.CoreModule;
import com.redstoner.modules.datamanager.DataManager;

@Version(major = 3, minor = 1, revision = 6, compatible = 3)
public class Socialspy implements CoreModule
{
	@Override
	public void postEnable()
	{
		CommandManager.registerCommand(this.getClass().getResourceAsStream("Socialspy.cmd"), this, Main.plugin);
	}
	
	@Command(hook = "config_prefix_default")
	public boolean prefixDefault(CommandSender sender)
	{
		return prefix(sender, getDefaultPrefix());
	}
	
	@Command(hook = "config_prefix")
	public boolean prefix(CommandSender sender, String prefix)
	{
		Utils.sendMessage(sender, null, "Set your prefix to: " + prefix);
		DataManager.setData(sender, "prefix", prefix);
		return true;
	}
	
	@Command(hook = "config_format_default")
	public boolean configFormatDefault(CommandSender sender)
	{
		return configFormat(sender, getDefaultFormat());
	}
	
	@Command(hook = "config_format")
	public boolean configFormat(CommandSender sender, String format)
	{
		Utils.sendMessage(sender, null, "Set your format to: " + format);
		DataManager.setData(sender, "format", format);
		return true;
	}
	
	@Command(hook = "stripcolor_on")
	public boolean stripcolorOn(CommandSender sender)
	{
		Utils.sendMessage(sender, null, "Enabled stripping colors!");
		DataManager.setData(sender, "stripcolor", true);
		return true;
	}
	
	@Command(hook = "stripcolor_off")
	public boolean stripcolorOff(CommandSender sender)
	{
		Utils.sendMessage(sender, null, "Disabled stripping colors!");
		DataManager.setData(sender, "stripcolor", false);
		return true;
	}
	
	@Command(hook = "stripcolor")
	public boolean stripcolor(CommandSender sender)
	{
		boolean b = (boolean) DataManager.getOrDefault(sender, "stripcolor", true);
		Utils.sendMessage(sender, null, (b ? "Disabled" : "Enabled") + " stripping colors!");
		DataManager.setData(sender, "stripcolor", !b);
		return true;
	}
	
	@Command(hook = "on")
	public boolean spyOn(CommandSender sender)
	{
		Utils.sendMessage(sender, null, "Enabled socialspy!");
		DataManager.setData(sender, "enabled", true);
		return true;
	}
	
	@Command(hook = "off")
	public boolean spyOff(CommandSender sender)
	{
		Utils.sendMessage(sender, null, "Disabled socialspy!");
		DataManager.setData(sender, "enabled", false);
		return true;
	}
	
	@Command(hook = "toggle")
	public boolean spyToggle(CommandSender sender)
	{
		boolean b = (boolean) DataManager.getOrDefault(sender, "enabled", false);
		Utils.sendMessage(sender, null, (b ? "Disabled" : "Enabled") + " socialspy!");
		DataManager.setData(sender, "enabled", !b);
		return true;
	}
	
	@Command(hook = "format_help")
	public boolean formatInfo(CommandSender sender)
	{
		Utils.sendModuleHeader(sender);
		Utils.sendMessage(sender, "", " Format placeholders:");
		Utils.sendMessage(sender, "", " &c%s&eender &7(display name) | &c%S&eender &7(real name)", '&');
		Utils.sendMessage(sender, "", " &c%t&earget &7(display name) | &c%T&earget &7(real name)", '&');
		Utils.sendMessage(sender, "", " &c%p&erefix &7(see prefix option)", '&');
		Utils.sendMessage(sender, "", " &c%m&eessage", '&');
		Utils.sendMessage(sender, "", " &c%c&eommand", '&');
		Utils.sendMessage(sender, "", " Any other text will be put as literal text. Use %% to escape any %.", '&');
		Utils.sendMessage(sender, "", " The default format is: §e" + getDefaultFormat());
		Utils.sendMessage(sender, "", " The default prefix is: §e" + getDefaultPrefix());
		return true;
	}
	
	@Command(hook = "commands_list")
	public boolean commands_list(CommandSender sender)
	{
		Utils.sendModuleHeader(sender);
		JSONArray commands = (JSONArray) DataManager.getOrDefault(sender, "commands", getDefaultCommandList());
		if (commands == null || commands.size() == 0)
			Utils.sendErrorMessage(sender, "", "You are not listening to any commands!");
		else
		{
			Utils.sendMessage(sender, "", "You are listening to the following " + commands.size() + " commands:");
			Utils.sendMessage(sender, "", Arrays.toString(commands.toArray()).replace(", /", "&7, &e/")
					.replace("[", "[&e").replace("]", "&7]"), '&');
		}
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
		Utils.sendMessage(sender, null, "You are now spying on &e" + command, '&');
		return true;
	}
	
	@Command(hook = "commands_del")
	public boolean commands_del(CommandSender sender, String command)
	{
		JSONArray commands = (JSONArray) DataManager.getOrDefault(sender, "commands", getDefaultCommandList());
		commands.remove(command);
		DataManager.setData(sender, "commands", commands);
		Utils.sendMessage(sender, null, "You are no longer spying on &e" + command, '&');
		return true;
	}
	
	public static void spyBroadcast(CommandSender sender, CommandSender target, String message, String command,
			BroadcastFilter filter)
	{
		try
		{
			Method m = Socialspy.class.getDeclaredMethod("spyBroadcast_", CommandSender.class, CommandSender.class,
					String.class, String.class, BroadcastFilter.class);
			m.invoke(ModuleLoader.getModule("Socialspy"), sender, target, message, command, filter);
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
							Utils.sendMessage(p, "", formatMessage(p, sender, target, message, command));
				}
				else
					DataManager.setData(sender, "enabled", false);
		}
	}
	
	public static void spyBroadcast(CommandSender sender, String target, String message, String command,
			BroadcastFilter filter)
	{
		try
		{
			Method m = Socialspy.class.getDeclaredMethod("spyBroadcast_", CommandSender.class, String.class,
					String.class, String.class, BroadcastFilter.class);
			m.invoke(ModuleLoader.getModule("Socialspy"), sender, target, message, command, filter);
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
							Utils.sendMessage(p, "", formatMessage(p, sender, target, message, command));
				}
				else
					DataManager.setData(sender, "enabled", false);
		}
	}
	
	private String formatMessage(CommandSender formatHolder, CommandSender sender, CommandSender target, String message,
			String command)
	{
		if ((boolean) DataManager.getOrDefault(formatHolder, "stripcolor", false))
			message = ChatColor.stripColor(message);
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
		format = format.replace("%m", message);
		// Convert placeholder back
		format = format.replace("§§", "%%");
		return format;
	}
	
	private String formatMessage(CommandSender formatHolder, CommandSender sender, String target, String message,
			String command)
	{
		if ((boolean) DataManager.getOrDefault(formatHolder, "stripcolor", false))
			message = ChatColor.stripColor(message);
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
		// Insert command and message
		format = format.replace("%c", command);
		format = format.replace("%m", message);
		// Convert placeholder back
		format = format.replace("§§", "%%");
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
	
	@Command(hook = "test")
	public boolean test(CommandSender sender) throws InterruptedException
	{
		DataManager.setData(sender, "test", "&aTest1");
		Thread.sleep(1);
		Utils.sendMessage(sender, null,
				"Wrote \"Test1\", got " + DataManager.getOrDefault(sender, "test", "&4fallback"), '&');
		DataManager.setData(sender, "test", "&aTest2");
		Thread.sleep(1);
		Utils.sendMessage(sender, null,
				"Wrote \"Test2\", got " + DataManager.getOrDefault(sender, "test", "&4fallback"), '&');
		DataManager.setData(sender, "test", "&4DATA");
		Thread.sleep(1);
		DataManager.removeData(sender, "test");
		Thread.sleep(1);
		Utils.sendMessage(sender, null, "Removed data, got " + DataManager.getOrDefault(sender, "test", "&anothing"),
				'&');
		return true;
	}
}
