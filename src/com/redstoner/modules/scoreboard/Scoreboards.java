package com.redstoner.modules.scoreboard;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.json.simple.JSONObject;

import com.nemez.cmdmgr.Command;
import com.nemez.cmdmgr.CommandManager;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Version;
import com.redstoner.exceptions.MissingVersionException;
import com.redstoner.misc.JsonManager;
import com.redstoner.misc.Main;
import com.redstoner.misc.Utils;
import com.redstoner.misc.VersionHelper;
import com.redstoner.modules.Module;

@AutoRegisterListener
@Version(major = 3, minor = 0, revision = 0, compatible = 3)
public class Scoreboards implements Module, Listener
{
	final File cmdFile = new File(
			new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile(),
			"Scoreboard.cmd");
	final File scoreboardsFolder = new File(Main.plugin.getDataFolder(), "scoreboards");
	HashMap<CommandSender, JSONObject> playerData = new HashMap<CommandSender, JSONObject>();
	
	@Override
	public boolean onEnable()
	{
		if (!scoreboardsFolder.exists())
			scoreboardsFolder.mkdirs();
		playerData.put(Bukkit.getConsoleSender(),
				JsonManager.getObject(new File(scoreboardsFolder, "scoreboards/players/console.json")));
		return true;
	}
	
	@Override
	public void postEnable()
	{
		CommandManager.registerCommand(cmdFile, this, Main.plugin);
	}
	
	@Override
	public void onDisable()
	{
		save();
		for (Player p : Bukkit.getOnlinePlayers())
		{
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		}
		playerData.remove(Bukkit.getConsoleSender());
	}
	
	private void save()
	{
		JsonManager.save(playerData.get(Bukkit.getConsoleSender()),
				new File(scoreboardsFolder, "scoreboards/players/console.json"));
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		JSONObject data = JsonManager.getObject(
				new File(scoreboardsFolder, "scoreboards/players/" + player.getUniqueId().toString() + ".json"));
		if (data == null)
			data = new JSONObject();
		playerData.put(player, data);
		Project currentProject = ProjectManager.getCurrentProject(player);
		if (currentProject == null)
			return;
		else
			player.setScoreboard(currentProject.getScoreboard());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		JsonManager.save(playerData.remove(player),
				new File(scoreboardsFolder, "scoreboards/players/" + player.getUniqueId().toString() + ".json"));
	}
	
	@Command(hook = "project")
	public boolean project(CommandSender sender) throws MissingVersionException
	{
		Utils.sendMessage(sender, null, "This server is running version" + VersionHelper.getVersion(this.getClass()));
		return true;
	}
	
	@Command(hook = "create")
	public boolean createProject(CommandSender sender, boolean sub, String name)
	{
		JSONObject data = playerData.get(sender);
		if (!sub)
		{
			int max = Integer.parseInt(getPermissionContent(sender, "utils.scoreboards.projects.amount."));
			int amount = 0;
			Object raw = data.get("amount");
			if (raw != null)
				amount = (int) raw;
			if (amount >= max)
			{
				Utils.sendErrorMessage(sender, null, "You have already reached the maximum amount of " + max
						+ " projects! Delete an old project before you create a new one!");
				return true;
			}
		}
		else
		{
			Project currentProject = ProjectManager.getCurrentProject(sender);
			int max = currentProject.getMaxSubprojects();
			int amount = currentProject.getSubProjects().size();
			Object raw = data.get("amount");
			if (raw != null)
				amount = (int) raw;
			if (amount >= max)
			{
				Utils.sendErrorMessage(sender, null, "You have already reached the maximum amount of " + max
						+ " projects! Delete an old project before you create a new one!");
				return true;
			}
		}
		return true;
	}
	
	private static String getPermissionContent(CommandSender sender, String permnode)
	{
		Set<PermissionAttachmentInfo> perms = sender.getEffectivePermissions();
		for (PermissionAttachmentInfo perm : perms)
			if (perm.getPermission().toString().startsWith(permnode))
				return perm.getPermission().replace(permnode, "");
		return null;
	}
}
