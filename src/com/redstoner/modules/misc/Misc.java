package com.redstoner.modules.misc;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.misc.Utils;
import com.redstoner.modules.Module;
import net.nemez.chatapi.ChatAPI;
import net.nemez.chatapi.click.Message;

@Commands(CommandHolderType.File)
@AutoRegisterListener
@Version(major = 4, minor = 1, revision = 0, compatible = 4)
public class Misc implements Module, Listener
{
	private final String[] sudoBlacklist = new String[] {"(.*:)?e?sudo", "(.*:)?script.*", "(.*:)?stop",
			"(.*:)?modules", "(.*:)?sayn", "(.*:)?pex", "(.*:)?console_.*", "(.*:)?op", "(.*:)?login", "(.*:)?register",
			"(.*:)?.*pass"};
	JSONObject config;
	JSONArray unprotectedRegions;
	
	@EventHandler
	public void onFirstJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		if (!player.hasPlayedBefore())
		{
			Utils.broadcast("", "\n§a§lPlease welcome §f" + player.getDisplayName() + " §a§lto Redstoner!\n", null);
			String[] message = new String[] {" \n \n \n \n \n \n \n \n \n \n \n \n ",
					"  &4Welcome to the Redstoner Server!", "  &6Before you ask us things, take a quick",
					"  &6look at &a&nredstoner.com/info", "  \n&6thank you and happy playing ;)", " \n \n"};
			getLogger().message(player, message);
		}
		Material spawnBlock = player.getLocation().getBlock().getType();
		if (spawnBlock == Material.PORTAL || spawnBlock == Material.ENDER_PORTAL)
		{
			getLogger().message(player, "&4Looks like you spawned in a portal... Let me help you out");
			getLogger().message(player, "&6You can use /back if you &nreally&6 want to go back");
			player.teleport(player.getWorld().getSpawnLocation());
		}
	}
	
	// Disables spectator teleportation
	@EventHandler(priority = EventPriority.LOWEST)
	public void onTeleport(PlayerTeleportEvent event)
	{
		Player player = event.getPlayer();
		if (!event.isCancelled() && event.getCause() == TeleportCause.SPECTATE && !player.hasPermission("utils.tp"))
		{
			event.setCancelled(true);
			getLogger().message(event.getPlayer(), true, "Spectator teleportation is disabled!");
		}
	}
	
	// Disables water and lava breaking stuff
	@EventHandler
	public void onLiquidFlow(BlockFromToEvent event)
	{
		Material m = event.getToBlock().getType();
		switch (m)
		{
			case AIR:
			case WATER:
			case STATIONARY_WATER:
			case LAVA:
			case STATIONARY_LAVA:
				return;
			default:
			{
				event.setCancelled(true);
			}
		}
	}
	
	@Command(hook = "tempadddef")
	public boolean tempAddDef(CommandSender sender, String user, String group)
	{
		return tempAdd(sender, user, group, "604800");
	}
	
	@Command(hook = "tempadd")
	public boolean tempAdd(CommandSender sender, String user, String group, String duration)
	{
		// Use it to make a proper duration output later. Too lazy rn.
		@SuppressWarnings("unused")
		int i = 0;
		try
		{
			i = Integer.valueOf(duration);
		}
		catch (NumberFormatException e)
		{
			getLogger().message(sender, true, "That is not a valid number!");
			return true;
		}
		Bukkit.dispatchCommand(sender, "pex user " + user + " group add " + group + " * " + duration);
		getLogger().message(sender, "Added to group " + group + "for " + duration + " seconds.");
		return true;
	}
	
	@Command(hook = "echo")
	public boolean echo(CommandSender sender, String text)
	{
		sender.sendMessage(ChatAPI.colorify(null, text));
		return true;
	}
	
	@Command(hook = "ping")
	public boolean ping(CommandSender sender)
	{
		if (sender instanceof Player)
		{
			int ping = getPing((Player) sender);
			getLogger().message(sender, "Your ping is " + ping + "ms.");
		}
		else
		{
			sender.sendMessage("Pong!");
		}
		return true;
	}
	
	@Command(hook = "ping2")
	public boolean ping(CommandSender sender, String password)
	{
		if (password.equals("pong"))
			if (sender instanceof Player)
			{
				int ping = getPing((Player) sender);
				getLogger().message(sender, new String[] {"Your ping is " + ping + "ms.", ping < 20
						? "&aThat's gr8 m8 r8 8/8"
						: (ping < 50 ? "F&eair enough you cunt!"
								: (ping < 100 ? "&eShite, but not shite enough."
										: "&cLooks like the server is about two months ahead of you. GET A NEW FRIGGIN' ISP ALREADY"))});
			}
			else
				getLogger().message(sender, true,
						"M8 you shitty cunt are not supposed to run this shit it's for players only!!!");
		else
			getLogger().message(sender, true, "&4WRONG PASSWORD, 4/3 ATTEMPTS FAILED! BAN COMMENCING!");
		return true;
	}
	
	public int getPing(Player player)
	{
		return ((CraftPlayer) player).getHandle().ping;
	}
	
	@Command(hook = "sudo")
	public boolean sudo(CommandSender sender, String name, String command)
	{
		CommandSender target;
		if (name.equalsIgnoreCase("console"))
		{
			target = Bukkit.getConsoleSender();
		}
		else
			target = Bukkit.getPlayer(name);
		if (target == null)
		{
			getLogger().message(sender, false, "That player couldn't be found!");
			return true;
		}
		if (command.startsWith("/") || target.equals(Bukkit.getConsoleSender()))
		{
			String[] args = command.split(" ");
			for (String regex : sudoBlacklist)
			{
				if (args[0].matches("\\/" + regex))
				{
					getLogger().message(sender, true, "You can't sudo anyone into using that command!");
					return true;
				}
			}
			Bukkit.dispatchCommand(target, command.replaceFirst("/", ""));
			getLogger().message(sender, "Sudoed " + Utils.getName(target) + "&7 into running " + command);
		}
		else
		{
			((Player) target).chat(command);
			getLogger().message(sender, "Sudoed " + Utils.getName(target) + "&7 into saying " + command);
		}
		return true;
	}
	
	@Command(hook = "hasperm")
	public boolean hasPerm(CommandSender sender, boolean noformat, String name, String node)
	{
		Player p;
		if (name.contains("-"))
			try
			{
				p = Bukkit.getPlayer(UUID.fromString(name));
			}
			catch (Exception e)
			{
				if (noformat)
					sender.sendMessage("ERR: Invalid UUID");
				else
					getLogger().message(sender, "That UUID is not valid!");
				return true;
			}
		else
			p = Bukkit.getPlayer(name);
		if (p == null)
		{
			if (noformat)
			{
				Message m = new Message(sender, null);
				m.appendText("ERR: Invalid player");
				m.send();
			}
			else
			{
				getLogger().message(sender, "That player couldn't be found!");
			}
			return true;
		}
		
		if (noformat)
		{
			Message m = new Message(sender, null);
			m.appendText("" + p.hasPermission(node));
			m.send();
		}
		else
		{
			getLogger().message(sender, "" + p.hasPermission(node));
		}
		
		return true;
	}
	
	public boolean canBuild(Player player, Location location)
	{
		BlockBreakEvent event = new BlockBreakEvent(location.getBlock(), player);
		Bukkit.getPluginManager().callEvent(event);
		return event.isCancelled();
	}
	
	PotionEffect nightvision = new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false);
	
	@Command(hook = "illuminate")
	public void illuminate(CommandSender sender)
	{
		Player player = (Player) sender;
		if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION))
		{
			player.removePotionEffect(PotionEffectType.NIGHT_VISION);
			getLogger().message(sender, "Night Vision Disabled.");
		}
		else
		{
			player.addPotionEffect(nightvision, true);
			getLogger().message(sender, "Night Vision Enabled.");
		}
	}
}