package com.redstoner.modules.blockplacemods;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Version;
import com.redstoner.misc.Main;
import com.redstoner.misc.Utils;
import com.redstoner.modules.Module;
import com.redstoner.modules.blockplacemods.mods.Mod;
import com.redstoner.modules.blockplacemods.mods.ModAbstract;
import com.redstoner.modules.blockplacemods.util.CommandException;
import com.redstoner.modules.blockplacemods.util.CommandMap;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

@AutoRegisterListener
@Version(major = 3, minor = 2, revision = 0, compatible = 3)
public final class BlockPlaceMods implements Module, Listener
{
	public static String PREFIX = ChatColor.GRAY + "[" + ChatColor.DARK_GREEN + "BPM" + ChatColor.GRAY + "]"
			+ ChatColor.GREEN;
			
	@Override
	public boolean onEnable()
	{
		ModAbstract.registerAll();
		for (Mod mod : new ArrayList<>(ModAbstract.getMods().values()))
		{
			mod.registerListeners();
		}
		// CommandManager.registerCommand(getCommandString(), this, Main.plugin);
		// Sorry but this stuff isn't working for me. Not gonna spend more time on it.
		try
		{
			Map<String, org.bukkit.command.Command> commandMap = CommandMap.getCommandMap();
			String[] aliases = {"mod", Main.plugin.getName().toLowerCase() + ":mod"};
			org.bukkit.command.Command command = new org.bukkit.command.Command("mod")
			{
				@Override
				public boolean execute(CommandSender sender, String label, String[] args)
				{
					onModCommand(sender, String.join(" ", args));
					return true;
				}
			};
			for (String alias : aliases)
			{
				commandMap.put(alias, command);
			}
		}
		catch (ReflectiveOperationException ex)
		{
			throw new Error(ex);
		}
		return true;
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
			commandMap.remove("mod");
			commandMap.remove(Main.plugin.getName().toLowerCase() + ":mod");
		}
		catch (Exception ignored)
		{}
	}
	
	/* @Override
	 * public String getCommandString() {
	 * return "command mod {\n" +
	 * "perm utils.blockplacemods.command;\n" +
	 * "type player;\n" +
	 * "[empty] {\n" +
	 * "run mod_empty;\n" +
	 * "}\n" +
	 * "[string:args...] {\n" +
	 * "run mod args;\n" +
	 * "}\n" +
	 * "}\n";
	 * } */
	
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
		String prefix = PREFIX;
		String message;
		
		try {
			
			if (args.length > 0) {
				Mod target = ModAbstract.getMod(args[0].toLowerCase());
				if (target != null) {
					prefix += "&7[&2" + target.getName() + "&7]&a";
					if (!(sender instanceof Player)) {
						message = "&cYou must be a player to use any block place mod";
					} else {
						message = target.runCommand((Player) sender, Arrays.copyOfRange(args, 1, args.length));
					}
				} else if (args[0].equalsIgnoreCase("help")) {
					message = commandHelp(sender, args);
				} else {
					message = "&cThat argument could not be recognized";
				}
			} else {
				message = commandHelp(sender, args);
			}
			
		} catch (CommandException ex) {
			message = " &c" + ex.getMessage();
		} catch (Throwable t) {
			message = " &cAn unexpected error occurred while executing this command.";
			t.printStackTrace();
		}
		
		Utils.sendMessage(sender, prefix, message, '&');
	}
	
	private String commandHelp(CommandSender sender, String[] args)
	{
		StringBuilder result = new StringBuilder("§7BlockPlaceMods adds some redstone-centric utilities");
		result.append("\n").append(ChatColor.GRAY.toString()).append("Available mods:");
		for (Mod mod : ModAbstract.getMods().values())
		{
			result.append("\n").append(ChatColor.AQUA.toString()).append("/mod ").append(ChatColor.ITALIC.toString())
					.append(mod.getName().toLowerCase()).append(ChatColor.GRAY.toString()).append(" - ")
					.append(mod.getDescription());
		}
		return result.toString();
	}
	
}
