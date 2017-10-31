package com.redstoner.modules.blockplacemods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.misc.Main;
import com.redstoner.modules.Module;
import com.redstoner.modules.blockplacemods.mods.Mod;
import com.redstoner.modules.blockplacemods.mods.ModAbstract;
import com.redstoner.modules.blockplacemods.mods.ModToggledAbstract;
import com.redstoner.utils.CommandException;
import com.redstoner.utils.CommandMap;

@Commands(CommandHolderType.None)
@AutoRegisterListener
@Version(major = 4, minor = 1, revision = 1, compatible = 4)
public final class BlockPlaceMods implements Module, Listener
{
	@Override
	public boolean onEnable()
	{
		ModAbstract.registerAll(getLogger());
		for (Mod mod : new ArrayList<>(ModAbstract.getMods().values()))
		{
			mod.registerListeners();
		}
		try
		{
			Map<String, org.bukkit.command.Command> commandMap = CommandMap.getCommandMap();
			org.bukkit.command.Command command = new BlockPlaceModsCommand();
			for (String alias : getCommandAliases())
			{
				commandMap.put(alias, command);
			}
		}
		catch (ReflectiveOperationException ex)
		{
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	@Override
	public void postEnable()
	{
		setPrefix("BPM");
	}
	
	@Override
	public void onDisable()
	{
		for (Mod mod : ModAbstract.getMods().values())
		{
			mod.unregisterListeners();
		}
		try
		{
			Map<String, org.bukkit.command.Command> commandMap = CommandMap.getCommandMap();
			for (String alias : getCommandAliases())
			{
				org.bukkit.command.Command command = commandMap.get(alias);
				if (command != null && command.getClass() == BlockPlaceModsCommand.class)
				{
					commandMap.remove(alias);
				}
			}
		}
		catch (Exception ignored)
		{}
	}
	
	private static String[] getCommandAliases()
	{
		String pluginName = Main.plugin.getName().toLowerCase();
		// @noformat
		return new String[]{"mod", 		pluginName + ":mod",
				"set", 		pluginName + ":set",
				"toggle",	pluginName + ":toggle"
		};
		// @format
	}
	
	@Command(hook = "mod_empty")
	public void onModEmptyCommand(CommandSender sender)
	{
		onModCommand(sender, "");
	}
	
	@Command(hook = "mod")
	public void onModCommand(CommandSender sender, String input)
	{
		String[] args = new ArrayList<>(Arrays.asList(input.split(" "))).stream()
				.filter(x -> x != null && !x.trim().isEmpty()).toArray(String[]::new);
		String prefix = "";
		String message;
		try
		{
			if (args.length > 0)
			{
				Mod target = ModAbstract.getMod(args[0].toLowerCase());
				if (target != null)
				{
					prefix += "&7[&2" + capitalize(target.getName()) + "&7]:&a ";
					if (!(sender instanceof Player))
					{
						message = "&cYou must be a player to use any block place mod";
					}
					else
					{
						message = target.runCommand((Player) sender, Arrays.copyOfRange(args, 1, args.length));
					}
				}
				else if (args[0].equalsIgnoreCase("help"))
				{
					message = commandHelp(sender, args);
				}
				else
				{
					message = "&cThat argument could not be recognized";
				}
			}
			else
			{
				message = commandHelp(sender, args);
			}
		}
		catch (CommandException ex)
		{
			message = " &c" + ex.getMessage();
		}
		catch (Throwable t)
		{
			message = " &cAn unexpected error occurred while executing this command.";
			t.printStackTrace();
		}
		getLogger().message(sender, prefix + message);
	}
	
	private String commandHelp(CommandSender sender, String[] args)
	{
		StringBuilder result = new StringBuilder("§7BlockPlaceMods adds some redstone-centric utilities");
		result.append("\n").append(ChatColor.GRAY.toString()).append("Available mods:");
		List<Mod> mods = new ArrayList<>(new HashSet<>(ModAbstract.getMods().values()));
		mods.sort(Comparator.<Mod> comparingInt(m -> ModToggledAbstract.class.isInstance(m) ? 1 : -1)
				.thenComparing(Mod::getName));
		for (Mod mod : mods)
		{
			result.append("\n").append(ChatColor.AQUA.toString()).append("/mod ").append(ChatColor.ITALIC.toString())
					.append(mod.getName());
			for (String alias : mod.getAliases())
			{
				result.append('|').append(alias);
			}
			result.append(ChatColor.GRAY.toString()).append(" - ").append(mod.getDescription());
		}
		return result.toString();
	}
	
	private static String capitalize(String modName)
	{
		if (modName.isEmpty())
		{
			return modName;
		}
		char first = modName.charAt(0);
		if (first != (first = Character.toUpperCase(first)))
		{
			char[] result = modName.toCharArray();
			result[0] = first;
			return String.valueOf(result);
		}
		return modName;
	}
	
	private class BlockPlaceModsCommand extends org.bukkit.command.Command
	{
		public BlockPlaceModsCommand()
		{
			super("mod");
			String[] aliases = getCommandAliases();
			setAliases(Arrays.asList(Arrays.copyOfRange(aliases, 1, aliases.length)));
		}
		
		@Override
		public boolean execute(CommandSender sender, String label, String[] args)
		{
			onModCommand(sender, String.join(" ", args));
			return true;
		}
	}
}
