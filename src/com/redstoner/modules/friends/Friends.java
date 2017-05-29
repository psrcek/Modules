package com.redstoner.modules.friends;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nemez.cmdmgr.Command;
import com.nemez.cmdmgr.CommandManager;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Version;
import com.redstoner.coremods.moduleLoader.ModuleLoader;
import com.redstoner.misc.Main;
import com.redstoner.misc.Utils;
import com.redstoner.modules.CoreModule;
import com.redstoner.modules.Module;
import com.redstoner.modules.datamanager.DataManager;

@AutoRegisterListener
@Version(major = 3, minor = 0, revision = 1, compatible = 3)
public class Friends implements CoreModule
{
	@Override
	public void postEnable()
	{
		CommandManager.registerCommand(getClass().getResourceAsStream("Friends.cmd"), this, Main.plugin);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		JSONArray friended_by = (JSONArray) DataManager.getOrDefault(e.getPlayer(), "friended_by", new JSONArray());
		for (Object obj : friended_by)
		{
			UUID uuid = UUID.fromString((String) obj);
			Player p = Bukkit.getPlayer(uuid);
			if (p != null && p.canSee(e.getPlayer()))
			{
				Utils.sendMessage(p, null, "Your friend &e" + e.getPlayer().getDisplayName() + "&7 just joined!", '&');
				p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
			}
		}
		JSONArray notifications = (JSONArray) DataManager.getOrDefault(e.getPlayer(), "scheduled_notifications",
				new JSONArray());
		for (Object obj : notifications)
			Utils.sendMessage(e.getPlayer(), null, (String) obj, '&');
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeave(PlayerQuitEvent e)
	{
		JSONArray friended_by = (JSONArray) DataManager.getOrDefault(e.getPlayer(), "friended_by", new JSONArray());
		for (Object obj : friended_by)
		{
			UUID uuid = UUID.fromString((String) obj);
			Player p = Bukkit.getPlayer(uuid);
			if (p != null && p.canSee(e.getPlayer()))
			{
				Utils.sendMessage(p, null, "Your friend &e" + e.getPlayer().getDisplayName() + "&7 just left!", '&');
				p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
			}
		}
	}
	
	@SuppressWarnings({"unchecked", "deprecation"})
	@Command(hook = "add")
	public boolean add(CommandSender sender, String target)
	{
		if (target.equalsIgnoreCase("CONSOLE"))
		{
			Utils.sendErrorMessage(sender, null, "You can't add console to your friends!");
			return true;
		}
		OfflinePlayer p = Bukkit.getPlayer(target);
		if (p == null)
			p = Bukkit.getOfflinePlayer(target);
		if (p == null)
		{
			Utils.sendErrorMessage(sender, null, "That player couldn't be found!");
			return true;
		}
		JSONArray friends = ((JSONArray) DataManager.getOrDefault(sender, "friends", new JSONArray()));
		if (friends.contains(p.getUniqueId().toString()))
		{
			Utils.sendErrorMessage(sender, null, "You are already friends with this person!");
			return true;
		}
		friends.add(p.getUniqueId().toString());
		DataManager.save(sender);
		JSONArray friended_by = ((JSONArray) DataManager.getOrDefault(p.getUniqueId().toString(), "friended_by",
				new JSONArray()));
		friended_by.add(getID(sender));
		DataManager.save(p.getUniqueId().toString());
		Utils.sendMessage(sender, null, "You are now friends with &e" + p.getName() + "&7!", '&');
		if (p instanceof Player)
		{
			Utils.sendMessage((Player) p, null, "&e" + Utils.getName(sender) + "&7 added you as a friend!", '&');
		}
		else
		{
			JSONArray notifications = (JSONArray) DataManager.getOrDefault(p.getUniqueId().toString(),
					"scheduled_notifications", new JSONArray());
			notifications.add("&e" + Utils.getName(sender) + "&7 added you as a friend!");
			notifications.remove("&e" + Utils.getName(sender) + "&7 removed you as a friend!");
			DataManager.setData(p.getUniqueId().toString(), "scheduled_notifications", notifications);
		}
		return true;
	}
	
	@SuppressWarnings({"deprecation", "unchecked"})
	@Command(hook = "add_grouped")
	public boolean add_grouped(CommandSender sender, String target, String group)
	{
		if (target.equalsIgnoreCase("CONSOLE"))
		{
			Utils.sendErrorMessage(sender, null, "You can't add console to your friends!");
			return true;
		}
		OfflinePlayer p = Bukkit.getPlayer(target);
		if (p == null)
			p = Bukkit.getOfflinePlayer(target);
		if (p == null)
		{
			Utils.sendErrorMessage(sender, null, "That player couldn't be found!");
			return true;
		}
		JSONArray friends = ((JSONArray) DataManager.getOrDefault(sender, "groups." + group, new JSONArray()));
		if (friends.contains(p.getUniqueId().toString()))
		{
			Utils.sendErrorMessage(sender, null, "This person already is part of that friendsgroup!");
			return true;
		}
		friends.add(p.getUniqueId().toString());
		DataManager.save(sender);
		Utils.sendMessage(sender, null, "&e" + p.getName() + "&7 is now part of the group &e" + group + "&7!", '&');
		if (p instanceof Player)
		{
			Utils.sendMessage((Player) p, null,
					"&e" + Utils.getName(sender) + " &7added you to their friendsgroup &e" + group + "&7!", '&');
		}
		else
		{
			JSONArray notifications = (JSONArray) DataManager.getOrDefault(p.getUniqueId().toString(),
					"scheduled_notifications", new JSONArray());
			notifications.add("&e" + Utils.getName(sender) + " &7added you to their friendsgroup &e" + group + "&7!");
			notifications
					.remove("&e" + Utils.getName(sender) + " &7removed you from their friendsgroup &e" + group + "&7!");
			DataManager.setData(p.getUniqueId().toString(), "scheduled_notifications", notifications);
		}
		return true;
	}
	
	@SuppressWarnings({"deprecation", "unchecked"})
	@Command(hook = "del")
	public boolean del(CommandSender sender, String target)
	{
		if (target.equalsIgnoreCase("CONSOLE"))
		{
			Utils.sendErrorMessage(sender, null, "You can't add console to your friends!");
			return true;
		}
		OfflinePlayer p = Bukkit.getPlayer(target);
		if (p == null)
			p = Bukkit.getOfflinePlayer(target);
		if (p == null)
		{
			Utils.sendErrorMessage(sender, null, "That player couldn't be found!");
			return true;
		}
		JSONArray friends = ((JSONArray) DataManager.getOrDefault(sender, "friends", new JSONArray()));
		if (friends.contains(p.getUniqueId().toString()))
		{
			Utils.sendErrorMessage(sender, null, "You are already friends with this person!");
			return true;
		}
		friends.remove(p.getUniqueId().toString());
		DataManager.save(sender);
		JSONArray friended_by = ((JSONArray) DataManager.getOrDefault(p.getUniqueId().toString(), "friended_by",
				new JSONArray()));
		friended_by.remove(getID(sender));
		DataManager.save(p.getUniqueId().toString());
		Utils.sendMessage(sender, null, "You are now friends with &e" + p.getName() + "&7!", '&');
		if (p instanceof Player)
		{
			Utils.sendMessage((Player) p, null, "&e" + Utils.getName(sender) + "&7 added you as a friend!", '&');
		}
		else
		{
			JSONArray notifications = (JSONArray) DataManager.getOrDefault(p.getUniqueId().toString(),
					"scheduled_notifications", new JSONArray());
			notifications.add("&e" + Utils.getName(sender) + "&7 removed you as a friend!");
			notifications.remove("&e" + Utils.getName(sender) + "&7 added you as a friend!");
			DataManager.setData(p.getUniqueId().toString(), "scheduled_notifications", notifications);
		}
		return true;
	}
	
	@SuppressWarnings({"deprecation", "unchecked"})
	@Command(hook = "del_grouped")
	public boolean del_grouped(CommandSender sender, String target, String group)
	{
		if (target.equalsIgnoreCase("CONSOLE"))
		{
			Utils.sendErrorMessage(sender, null, "You can't add console to your friends!");
			return true;
		}
		OfflinePlayer p = Bukkit.getPlayer(target);
		if (p == null)
			p = Bukkit.getOfflinePlayer(target);
		if (p == null)
		{
			Utils.sendErrorMessage(sender, null, "That player couldn't be found!");
			return true;
		}
		JSONArray friends = ((JSONArray) DataManager.getOrDefault(sender, "groups." + group, new JSONArray()));
		if (friends.contains(p.getUniqueId().toString()))
		{
			Utils.sendErrorMessage(sender, null, "This person already is part of that friendsgroup!");
			return true;
		}
		friends.add(p.getUniqueId().toString());
		DataManager.save(sender);
		Utils.sendMessage(sender, null, "&e" + p.getName() + "&7 is now part of the group &e" + group + "&7!", '&');
		if (p instanceof Player)
		{
			Utils.sendMessage((Player) p, null,
					"&e" + Utils.getName(sender) + " &7added you to their friendsgroup &e" + group + "&7!", '&');
		}
		else
		{
			JSONArray notifications = (JSONArray) DataManager.getOrDefault(p.getUniqueId().toString(),
					"scheduled_notifications", new JSONArray());
			notifications
					.add("&e" + Utils.getName(sender) + " &7removed you from their friendsgroup &e" + group + "&7!");
			notifications
					.remove("&e" + Utils.getName(sender) + " &7added you to their friendsgroup &e" + group + "&7!");
			DataManager.setData(p.getUniqueId().toString(), "scheduled_notifications", notifications);
		}
		return true;
	}
	
	@Command(hook = "list")
	public boolean list(CommandSender sender)
	{
		JSONArray friends = (JSONArray) DataManager.getOrDefault(sender, "friends", new JSONArray());
		if (friends.size() == 0)
		{
			Utils.sendErrorMessage(sender, null, "You didn't add anyone to your friendslist yet.");
		}
		else
		{
			Utils.sendModuleHeader(sender);
			Utils.sendMessage(sender, "", "You have a total of &e" + friends.size() + "&7 friends:", '&');
			StringBuilder sb = new StringBuilder();
			for (Object o : friends.toArray())
			{
				UUID id = UUID.fromString((String) o);
				Player p = Bukkit.getPlayer(id);
				if (p != null)
					sb.append(p.getDisplayName() + "&7, ");
				else
					sb.append("&9" + Bukkit.getOfflinePlayer(id).getName() + "&7, ");
			}
			String out = sb.toString().replaceAll(", $", "");
			Utils.sendMessage(sender, "", out, '&');
		}
		return true;
	}
	
	@Command(hook = "list_group")
	public boolean list_group(CommandSender sender, String group)
	{
		JSONArray friends = (JSONArray) DataManager.getOrDefault(sender, "group." + group, new JSONArray());
		if (friends.size() == 0)
		{
			Utils.sendErrorMessage(sender, null, "You didn't add anyone to this group yet.");
		}
		else
		{
			Utils.sendModuleHeader(sender);
			Utils.sendMessage(sender, "", "You have a total of &e" + friends.size() + "&7 friends added to this group:",
					'&');
			StringBuilder sb = new StringBuilder();
			for (Object o : friends.toArray())
			{
				UUID id = UUID.fromString((String) o);
				Player p = Bukkit.getPlayer(id);
				if (p != null)
					sb.append(p.getDisplayName() + "&7, ");
				else
					sb.append("&9" + Bukkit.getOfflinePlayer(id).getName() + "&7, ");
			}
			String out = sb.toString().replaceAll(", $", "");
			Utils.sendMessage(sender, "", out, '&');
		}
		return true;
	}
	
	@Command(hook = "list_groups")
	public boolean list_groups(CommandSender sender)
	{
		JSONObject raw = (JSONObject) DataManager.getOrDefault(sender, null, new JSONObject());
		Set<?> keys = raw.keySet();
		if (keys.size() == 0 || (keys.contains("friends") && keys.size() == 1))
		{
			Utils.sendErrorMessage(sender, null, "You don't have any custom groups made yet.");
			return true;
		}
		else
		{
			Utils.sendModuleHeader(sender);
			StringBuilder sb = new StringBuilder();
			for (Object o : keys)
			{
				sb.append("&e" + (String) o + "&7, ");
			}
			String out = sb.toString().replaceAll(", $", "");
			Utils.sendMessage(sender, "", out, '&');
		}
		return true;
	}
	
	public static boolean isFriend(CommandSender player, CommandSender friend)
	{
		return isFriend(player, friend, null);
	}
	
	public static boolean isFriend(CommandSender player, CommandSender friend, String condition)
	{
		try
		{
			Module mod = ModuleLoader.getModule("Friends");
			Method m = mod.getClass().getDeclaredMethod("isFriend_", String.class);
			return (boolean) m.invoke(mod, player, friend, condition);
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{}
		return false;
	}
	
	protected boolean isFriend_(CommandSender player, CommandSender friend, String group)
	{
		if (group == null)
			group = "friends";
		else if (!group.startsWith("group."))
			group = "group." + group;
		JSONArray array = (JSONArray) DataManager.getOrDefault(player, group, new JSONArray());
		return array.contains(getID(friend));
	}
	
	private final String getID(CommandSender sender)
	{
		if (sender instanceof Player)
			return ((Player) sender).getUniqueId().toString();
		else
			return sender.getName();
	}
}
