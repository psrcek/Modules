package com.redstoner.modules.buildteam;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.modules.Module;

@Commands(CommandHolderType.File)
@Version(major = 4, minor = 0, revision = 0, compatible = 4)
public class BuildTeam implements Module
{
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
		getLogger().message(sender, "Teleported you to &e" + target.getDisplayName() + "&7!");
		return true;
	}
	
	@Command(hook = "team_add")
	public boolean add(CommandSender sender, String target_name)
	{
		if (!target_name.matches("^\\w{2,16}$"))
		{
			getLogger().message(sender, true, "This doesn't look like a valid playername!");
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
			getLogger().message(sender, true, "This doesn't look like a valid playername!");
			return true;
		}
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + target_name + " group remove +buildteam");
		return true;
	}
}
