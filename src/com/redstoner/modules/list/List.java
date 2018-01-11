package com.redstoner.modules.list;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.misc.Utils;
import com.redstoner.modules.Module;
import com.redstoner.modules.datamanager.DataManager;

import net.nemez.chatapi.click.Message;

@Commands(CommandHolderType.File)
@Version(major = 4, minor = 0, revision = 5, compatible = 4)
public class List implements Module
{
	private HashMap<String, Integer> onConsole;
	
	@Override
	public void postEnable()
	{
		onConsole = new HashMap<>();
	}
	
	@Command(hook = "console_join")
	public boolean console_join(CommandSender sender, String name)
	{
		if (onConsole.containsKey(name))
			onConsole.put(name, onConsole.get(name) + 1);
		else
			onConsole.put(name, 1);
		return true;
	}
	
	@Command(hook = "console_leave")
	public boolean console_leave(CommandSender sender, String name)
	{
		if (onConsole.containsKey(name))
			if (onConsole.get(name) == 1)
				onConsole.remove(name);
			else
				onConsole.put(name, onConsole.get(name) - 1);
		return true;
	}
	
	@Command(hook = "staff")
	public boolean staff(CommandSender sender)
	{
		return listRank(sender, "staff");
	}
	
	@Command(hook = "list")
	public boolean list(CommandSender sender)
	{
		return (listRank(sender, "all"));
	}
	
	@Command(hook = "list_rank")
	public boolean listRank(CommandSender sender, String rank)
	{
		int onlinePlayers = Bukkit.getOnlinePlayers().size();
		getLogger().message(sender, "", "&7There are &e" + onlinePlayers + "&7 out of maximum &e"
				+ Bukkit.getMaxPlayers() + "&7 players online.");
		
		rank = rank.toLowerCase();
		boolean all = rank.equals("all");
		
		if (onlinePlayers == 0 && !rank.contains("console") && !all)
			return true;
		
		rank = rank.replace("staff", "mit mod admin lead");
		
		boolean shownAnything = false;
		
		if (rank.contains("visitor") || all)
			shownAnything |= show(sender, "&7Visitors", getPlayers(sender, "group.visitor", "group.member"), all);
		if (rank.contains("member") || all)
			shownAnything |= show(sender, "&fMembers", getPlayers(sender, "group.member", "group.builder"), all);
		if (rank.contains("builder") || all)
			shownAnything |= show(sender, "&aBuilders", getPlayers(sender, "group.builder", "group.trusted"), all);
		if (rank.contains("trusted") || all)
			shownAnything |= show(sender, "&3Trusteds", getPlayers(sender, "group.trusted", "group.trainingmod"), all);
		if (rank.contains("trainingmod") || rank.contains("mit") || all)
			shownAnything |= show(sender, "&cTrainingmod &e•", getPlayers(sender, "group.trainingmod", "group.mod"),
					all);
		if (rank.contains("mod") || all)
			shownAnything |= show(sender, "&cModerators", getPlayers(sender, "group.mod", "group.admin"), all);
		if (rank.contains("admin") || all)
			shownAnything |= show(sender, "&4Admins", getPlayers(sender, "group.admin", null), all);
		if (rank.contains("lead") || all)
			shownAnything |= show(sender, "&4Leads •", getPlayers(sender, "group.lead", null), all);
		if (rank.contains("console") || all)
		{
			if (sender.hasPermission("utils.list.console"))
			{
				StringBuilder sb = new StringBuilder();
				for (Entry<String, Integer> entry : onConsole.entrySet())
				{
					if (entry.getValue() > 0)
					{
						sb.append(entry.getKey());
						sb.append("&7, ");
					}
				}
				String players = sb.toString().replaceAll(", $", "");
				shownAnything |= show(sender, "&9Console" + afk(Bukkit.getConsoleSender()), players);
			}
			else
			{
				if (!all)
				{
					getLogger().message(sender, true, "You do not have permissions to see who's on console!");
					shownAnything = true;
				}
			}
		}
		if (!shownAnything)
		{
			getLogger().message(sender, new String[] {
					"Looks like I couldn't figure out what you meant. Try again with different parameters maybe?",
					"Possible parameters are: &eAll&7, &eStaff&7, &eVisitor&7, &eMember&7, &eBuilder&7, &eTrusted&7, &eMit&7, &eMod&7, &eAdmin&7 and &eLead",
					"You can also combine any of the parameters, like this: &eMember, Staff"});
		}
		return true;
	}
	
	public boolean show(CommandSender sender, String rank, String players)
	{
		return show(sender, rank, players, false);
	}
	
	public boolean show(CommandSender sender, String rank, String players, boolean all)
	{
		if (players.length() == 0)
		{
			if (!all)
			{
				players = "None";
			}
			else
				players = null;
		}
		if (players != null)
		{
			Message m = new Message(sender, null);
			m.appendText("&8[" + rank + "&8]&7: " + players);
			m.send();
			return true;
		}
		return false;
	}
	
	public String getPlayers(CommandSender sender, String positive, String negative)
	{
		Player player = null;
		if (sender instanceof Player)
			player = (Player) sender;
		StringBuilder sb = new StringBuilder();
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if (p.hasPermission(positive) && (negative == null || !p.hasPermission(negative))
					&& (positive.equals("group.lead") || !p.hasPermission("group.lead")))
			{
				if (player == null || player.canSee(p))
				{
					sb.append(Utils.getName(p) + afk(p) + vanish(p));
					sb.append(", ");
				}
			}
		}
		return sb.toString().replaceAll(", $", "");
	}
	
	public String afk(CommandSender sender)
	{
		return DataManager.getState(sender, "afk")
				? (String) DataManager.getConfigOrDefault("afk", "indicator", "&7[AFK]")
				: "";
	}
	
	public String vanish(Player player)
	{
		return DataManager.getState(player, "vanished")
				? (String) DataManager.getConfigOrDefault("vanish", "indicator", "&7[V]")
				: "";
	}
}
