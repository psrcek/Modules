package com.redstoner.modules.buildteam;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nemez.cmdmgr.Command;
import com.nemez.cmdmgr.CommandManager;
import com.redstoner.annotations.Version;
import com.redstoner.misc.Main;
import com.redstoner.misc.Utils;
import com.redstoner.modules.Module;

@Version(major = 3, minor = 0, revision = 0, compatible = 3)
public class BuildTeam implements Module
{
	@Override
	public void postEnable()
	{
		CommandManager.registerCommand(getClass().getResourceAsStream("BuildTeam.cmd"), this, Main.plugin);
	}
	
	@Command(hook = "teleport")
	public boolean teleport(CommandSender sender, String target_name)
	{
		final Player player = (Player) sender;
		final Player target = Bukkit.getPlayer(target_name);
		if (target == null || !player.hasPermission("utils.buildteam.teleport")
				|| !target.getLocation().getWorld().getName().equals("BuildTeam"))
		{
			player.performCommand("essentials:tp " + target_name);
			return true;
		}
		player.teleport(target);
		Utils.sendMessage(sender, null, "Teleported you to &e" + target.getDisplayName() + "&7!", '&');
		return true;
	}
	
	@Command(hook = "team_add")
	public boolean add(CommandSender sender, String target_name)
	{
		if (!target_name.matches("^\\w{2,16}$"))
		{
			Utils.sendErrorMessage(sender, null, "This doesn't look like a valid playername!");
			return true;
		}
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + target_name + " group add +buildteam");
		return true;
	}
	
	@Command(hook = "team_remove")
	public boolean remove(CommandSender sender, String target_name)
	{
		if (!target_name.matches("^\\w{2,16}$"))
		{
			Utils.sendErrorMessage(sender, null, "This doesn't look like a valid playername!");
			return true;
		}
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + target_name + " group remove +buildteam");
		return true;
	}
}
