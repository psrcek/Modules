package com.redstoner.modules.firstseen;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.Version;
import com.redstoner.misc.Utils;
import com.redstoner.modules.Module;

@Version(major = 2, minor = 0, revision = 7, compatible = 2)
public class FirstSeen implements Module
{
	@SuppressWarnings("deprecation")
	@Command(hook = "firstseenP")
	public void firstseen(CommandSender sender, String person)
	{
		Utils.sendMessage(sender, "", "&7Please note that the data may not be fully accurate!", '&');
		OfflinePlayer oPlayer = Bukkit.getPlayer(person);
		if (oPlayer == null)
			oPlayer = Bukkit.getServer().getOfflinePlayer(person);
		Long firstJoin = oPlayer.getFirstPlayed();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String disDate = format.format(new Date(firstJoin));
		if (disDate.equals("1969-12-31 19:00"))
		{
			Utils.sendMessage(sender, null, "&3" + oPlayer.getName() + "&c has never joined.", '&');
		}
		else
		{
			Utils.sendMessage(sender, null, "&3" + oPlayer.getName() + " &efirst joined&a " + disDate + "&e.", '&');
		}
	}
	
	@Command(hook = "firstseen")
	public void firstseen(CommandSender sender)
	{
		firstseen(sender, sender.getName());
	}
	
	@Command(hook = "playtimeDef")
	public boolean playtime(CommandSender sender)
	{
		return playtime(sender, sender.getName());
	}
	
	@Command(hook = "playtime")
	public boolean playtime(CommandSender sender, String name)
	{
		if (name == null)
			name = sender.getName();
		Player player = Bukkit.getPlayer(name);
		if (player == null)
		{
			Utils.sendErrorMessage(sender, null,
					"That player couldn't be found! Hint: Currently, you can only check statistics of players that are online!");
			return true;
		}
		int ticks_lived = player.getStatistic(Statistic.PLAY_ONE_TICK);
		int days = ticks_lived / 1728000;
		int hours = (ticks_lived % 1728000) / 72000;
		int minutes = (ticks_lived % 72000) / 1200;
		Utils.sendMessage(sender, null,
				"The player &e" + name + " &7has been on for "
						+ (days == 0 && hours == 0 && minutes == 0 ? "less than a minute."
								: ("a total of: &e" + (days != 0 ? (days + "d ") : "")
										+ ((hours != 0 || days != 0) ? (hours + "h ") : "")
										+ ((minutes != 0 || hours != 0 || days != 0) ? (minutes + "m") : ""))),
				'&');
		return true;
	}
	
	// @noformat
	@Override
	public String getCommandString()
	{
		return    "command firstseen {\n"
				+ "	   [empty] {\n"
				+ "		   run firstseen;\n"
				+ "		   type player;\n"
				+ "		   help Gives the date and time they first joined;\n"
				+ "		   perm utils.firstseen;\n"
				+ "	   }\n"
				+ "	   [string:person] {\n"
				+ "		   run firstseenP person;\n"
				+ "		   help Gives the date and time when a player first joined;\n"
				+ "		   perm utils.firstseen.other;\n"
				+ "	   }\n"
				+ "}\n"
				+ "command playtime {\n"
				+ "    [empty] {\n"
				+ "        type player;\n"
				+ "        run playtimeDef;\n"
				+ "        perm utils.playtime;\n"
				+ "        help Displays your total playtime!;\n"
				+ "    }\n"
				+ "    [string:name] {\n"
				+ "        run playtime name;\n"
				+ "        perm utils.playtime.others;\n"
				+ "        help Displays the playtime of another player. The player must be online!;\n"
				+ "    }\n"
				+ "}";
	}
	// @format
}
