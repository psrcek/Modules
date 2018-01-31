package com.redstoner.modules.logs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.modules.Module;
import com.redstoner.modules.ModuleLogger;
import com.redstoner.modules.datamanager.DataManager;

@Commands(CommandHolderType.File)
@Version(major = 4, minor = 0, revision = 4, compatible = 4)
public class Logs implements Module
{
	public static final String defaultFormat = "§7 > %f: %r";
	private final LogEntry example_1 = new LogEntry("1970-01-01-2.log.gz",
			"[01:23:45] [Async Chat Thread - #1337/INFO]:  §aFooBar §7→ §4THIS SERVER SUCKS", 14, 73);
	private final LogEntry example_2 = new LogEntry("1970-01-01-2.log.gz",
			"[01:23:45] [Server thread/INFO]: admin issued server command: /ban FooBar Ab00se", 15, 74);
	protected static ModuleLogger logger;
	
	@Override
	public void firstLoad()
	{
		Module.super.firstLoad();
		DataManager.setConfig("logs.root", "/etc/minecraft/redstoner/logs");
	}
	
	@Override
	public boolean onEnable()
	{
		Module.super.onEnable();
		logger = getLogger();
		return true;
	}
	
	public static File getLogsDir()
	{
		return new File((String) DataManager.getConfigOrDefault("logs.root", "../logs"));
	}
	
	@Command(hook = "search_logs")
	public boolean search_logs(CommandSender sender, String files, String search)
	{
		LogHandler handler = new LogHandler(sender, search, files);
		handler.doSearch();
		return true;
	}
	
	// FORMATTING
	@Command(hook = "show_format")
	public boolean show_format(CommandSender sender)
	{
		showExample(sender);
		return true;
	}
	
	@Command(hook = "set_format")
	public boolean set_format(CommandSender sender, String format)
	{
		if (format.equals("--reset"))
			format = defaultFormat;
		format = format.replace("&", "§").replace("$$", "&");
		DataManager.setData(sender, "format", format);
		showExample(sender, format);
		return true;
	}
	
	private void showExample(CommandSender sender)
	{
		showExample(sender, (String) DataManager.getOrDefault(sender, "format", defaultFormat));
	}
	
	private void showExample(CommandSender sender, String format)
	{
		sender.sendMessage(getLogger().getHeader());
		sender.sendMessage("Your format is: " + format);
		sender.sendMessage("Here's an example of what it would look like in an actual log search:");
		boolean colors = (boolean) DataManager.getOrDefault(sender, "colors", true);
		if ((boolean) DataManager.getOrDefault(sender, "progress", true))
		{
			sender.sendMessage("§7So far, §e1§7/§e2§7 File(s) and §e68§7 Line(s) were searched.");
		}
		sender.sendMessage(example_1.applyFormat(format, colors));
		sender.sendMessage(example_2.applyFormat(format, colors));
		if ((boolean) DataManager.getOrDefault(sender, "summary", true))
		{
			sender.sendMessage("§aSearch completed after 39ms!");
			sender.sendMessage(
					"§7In total: §e2§7 File(s) and §e105§7 Line(s) were searched, §a2§7 Match(es) were found!");
		}
	}
	
	@Command(hook = "show_format_help")
	public boolean format_help(CommandSender sender)
	{
		//@noformat
		String[] format_help = new String[] {
				" &e%l&cine&7 -> Linenumber in the current file", 
				" &e%L&cine&7 -> Global linenumber (sum of all previous files + current line)", 
				" &e%f&cile&7 -> Complete filename", 
				" &e%r&caw&7 -> The raw line containing the text as it appears in the logs", 
				"",
				" &7Use %% to gain a literal %."};
		//@format
		getLogger().message(sender, format_help);
		return true;
	}
	
	// SEARCH OPTIONS
	@Command(hook = "show_option_help")
	public boolean show_options(CommandSender sender)
	{
		List<String> options = new ArrayList<>(Option.values().length + 1);
		options.add("Available options are:");
		for (Option o : Option.values())
			options.add(" - " + o.toString());
		getLogger().message(sender, options.toArray(new String[] {}));
		return true;
	}
	
	@Command(hook = "set_option")
	public boolean set_option(CommandSender sender, String option, boolean state)
	{
		option = option.toLowerCase();
		Option o = null;
		try
		{
			o = Option.valueOf(option);
		}
		catch (IllegalArgumentException e)
		{}
		if (o == null)
		{
			getLogger().message(sender, true,
					"Invalid option! To get a list of all available options, run &e/logs option_help");
			return true;
		}
		DataManager.setData(sender, option, state);
		getLogger().message(sender,
				"Successfully turned displaying of &e" + option + (state ? " &aon&7!" : " &coff&7!"));
		return true;
	}
}

enum Option
{
	summary,
	progress,
	colors
}
