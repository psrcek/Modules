package com.redstoner.modules.buildchat;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.json.simple.JSONObject;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.BroadcastFilter;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.misc.JsonManager;
import com.redstoner.misc.Main;
import com.redstoner.misc.Utils;
import com.redstoner.modules.Module;

/** BuildTeamChat module. Allows the build team to chat privately using /bc \<message\> as well as a one char prefix or a toggle.
 * 
 * @author Pepich */
@Commands(CommandHolderType.String)
@AutoRegisterListener
@Version(major = 4, minor = 0, revision = 1, compatible = 4)
public class BuildChat implements Module, Listener
{
	private static final char defaultKey = ';';
	private static final File keysLocation = new File(Main.plugin.getDataFolder(), "buildchat_keys.json");
	private ArrayList<UUID> bctoggled;
	private static JSONObject keys;
	
	@Override
	public boolean onEnable()
	{
		keys = JsonManager.getObject(keysLocation);
		if (keys == null)
		{
			keys = new JSONObject();
			saveKeys();
		}
		bctoggled = new ArrayList<>();
		return true;
	}
	
	// @noformat
	@Override
	public String getCommandString()
	{
		return "command bc {\n" + 
				"	[string:message...] {\n" + 
				"		help Sends a message in BuildTeam Chat;\n" + 
				"		perm utils.bc;\n" + 
				"		run bc_msg message;\n" + 
				"	}\n" + 
				"}\n" + 
				"command bcn {\n" + 
				"	[string:name] [string:message...] {\n" + 
				"		help Sends a message in BuildTeam Chat;\n" + 
				"		perm utils.bc;\n" + 
				"		type console;\n" +
				"		run bcn_msg name message;\n" + 
				"	}\n" + 
				"}\n" + 
				"		\n" + 
				"command bckey {\n" + 
				"	[string:key] {\n" + 
				"		help Sets your BuildTeam Chat key;\n" + 
				"		perm utils.bc;\n" + 
				"		type player;\n" + 
				"		run setbckey key;\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"command bct {\n" + 
				"	on {\n" + 
				"		help Turns on bct;\n" + 
				"		perm utils.bc;\n" + 
				"		run bct_on;\n" + 
				"	}\n" + 
				"	off {\n" + 
				"		help Turns off bct;\n" + 
				"		perm utils.bc;\n" + 
				"		run bct_off;\n" + 
				"	}\n" + 
				"	[empty] {\n" + 
				"		help toggles BuildTeam Chat;\n" + 
				"		perm utils.bc;\n" + 
				"		run bct;\n" + 
				"	}\n" + 
				"}";
	}
	// @format
	
	@Command(hook = "bc_msg")
	public boolean bcSay(CommandSender sender, String message)
	{
		String name;
		if (sender instanceof Player)
			name = ((Player) sender).getDisplayName();
		else
			name = sender.getName();
		Utils.broadcast("§8[§cBC§8] §9" + name + "§8: §b", message, new BroadcastFilter()
		{
			@Override
			public boolean sendTo(CommandSender recipient)
			{
				return recipient.hasPermission("utils.bc");
			}
		});
		return true;
	}
	
	@Command(hook = "bcn_msg")
	public boolean bcnSay(CommandSender sender, String name, String message)
	{
		Utils.broadcast("§8[§cBC§8] §9" + name + "§8: §b", message, new BroadcastFilter()
		{
			@Override
			public boolean sendTo(CommandSender recipient)
			{
				return recipient.hasPermission("utils.bc");
			}
		});
		return true;
	}
	
	/** Let's a Player toggle their auto-cg status to allow for automatically sending chat messages to their chatgroup.
	 * 
	 * @param sender the issuer of the command.
	 * @param _void ignored.
	 * @return true. */
	@Command(hook = "bct")
	public boolean bcToggleCommand(CommandSender sender)
	{
		if (bctoggled.contains(((Player) sender).getUniqueId()))
		{
			bctoggled.remove(((Player) sender).getUniqueId());
			getLogger().message(sender, "BCT now §cdisabled");
		}
		else
		{
			bctoggled.add(((Player) sender).getUniqueId());
			getLogger().message(sender, "BCT now §aenabled");
		}
		return true;
	}
	
	/** Let's a Player toggle their auto-cg status to allow for automatically sending chat messages to their chatgroup.
	 * 
	 * @param sender the issuer of the command.
	 * @return true. */
	@Command(hook = "bct_on")
	public boolean bcToggleOnCommand(CommandSender sender)
	{
		if (!bctoggled.contains(((Player) sender).getUniqueId()))
		{
			bctoggled.add(((Player) sender).getUniqueId());
			getLogger().message(sender, "BCT now §aenabled");
		}
		else
			getLogger().message(sender, "BCT was already enabled");
		return true;
	}
	
	/** Let's a Player toggle their auto-cg status to allow for automatically sending chat messages to their chatgroup.
	 * 
	 * @param sender the issuer of the command.
	 * @return true. */
	@Command(hook = "bct_off")
	public boolean bcToggleOffCommand(CommandSender sender)
	{
		if (bctoggled.remove(((Player) sender).getUniqueId()))
			getLogger().message(sender, "BCT now §cdisabled");
		else
			getLogger().message(sender, "BCT was already disabled");
		return true;
	}
	
	/** Deals with chat events to allow for bckeys and bctoggle.
	 * 
	 * @param event the chat event containing the player and the message. */
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		if (!player.hasPermission("utils.bc"))
			return;
		if (event.getMessage().startsWith(getKey(player)))
		{
			event.setCancelled(true);
			bcSay(event.getPlayer(), event.getMessage().replaceFirst(Pattern.quote(getKey(player)), ""));
		}
		else if (bctoggled.contains(event.getPlayer().getUniqueId()))
		{
			event.setCancelled(true);
			bcSay(event.getPlayer(), event.getMessage());
		}
	}
	
	/** Sets the bckey of a Player.
	 * 
	 * @param sender the issuer of the command.
	 * @param key the key to be set. Set to NULL or "" to get your current key.
	 * @return true. */
	@SuppressWarnings("unchecked")
	@Command(hook = "setbckey")
	public boolean setBcKey(CommandSender sender, String key)
	{
		if (key.length() > 1)
		{
			getLogger().message(sender, true,
					"Could not set your key to §6" + key + " §7, it can be at most one char.");
			return true;
		}
		if (key == null || key.length() == 0)
		{
			getBcKey(sender);
			return true;
		}
		getLogger().message(sender, "Set your key to §6" + key);
		keys.put(((Player) sender).getUniqueId().toString(), key + "");
		saveKeys();
		return true;
	}
	
	/** This method will find the ChatgGroup key of any player.
	 * 
	 * @param player the player to get the key from.
	 * @return the key. */
	public static String getKey(Player player)
	{
		String key = (String) keys.get(player.getUniqueId().toString());
		return (key == null ? "" + defaultKey : key);
	}
	
	/** Prints a Players bckey to their chat.
	 * 
	 * @param sender the issuer of the command. */
	public void getBcKey(CommandSender sender)
	{
		getLogger().message(sender, "Your current bckey is §6" + getKey((Player) sender));
	}
	
	/** Saves the keys. */
	private void saveKeys()
	{
		JsonManager.save(keys, keysLocation);
	}
}
