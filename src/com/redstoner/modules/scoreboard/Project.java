package com.redstoner.modules.scoreboard;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.redstoner.misc.JsonManager;
import com.redstoner.misc.Main;
import com.redstoner.misc.Utils;

public class Project
{
	private int progress;
	private Project parent;
	private final String projectID;
	private String projectName;
	private String displayName;
	private UUID owner;
	private JSONArray admins, members;
	private int localID;
	private Location location;
	private Scoreboard scoreboard;
	ArrayList<Project> subs = new ArrayList<Project>();
	
	private Project(String projectID, Project parent, String name, String displayName, UUID owner, int localID,
			Location location, JSONArray admins, JSONArray members)
	{
		this.parent = parent;
		this.projectID = projectID;
		this.progress = 0;
		this.owner = owner;
		this.location = location;
		this.projectName = name;
		this.localID = localID;
		this.displayName = displayName;
		this.admins = admins;
		this.members = members;
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		updateScoreboard();
	}
	
	private Project(String projectID, Project parent, String name, String displayName, UUID owner, int localID,
			Location location)
	{
		this(projectID, null, name, displayName, owner, localID, location, new JSONArray(), new JSONArray());
	}
	
	private Project(Project parent, String name, String displayName, UUID owner, int localID, Location location)
	{
		this(ProjectManager.getNextID(name), null, name, displayName, owner, localID, location);
	}
	
	public Project(String name, String displayName, UUID owner, int localID, Location location)
	{
		this(null, name, displayName, owner, localID, location);
	}
	
	/** This method returns the progress of this project.
	 * 
	 * @return the progress in percent, as an integer between 0 and 100 (inclusive). */
	public int getProgress()
	{
		return progress;
	}
	
	/** This method sets the progress to the given parameter. The value must be between 0 and 100 (inclusive).</br>
	 * If this project is not a leaf, then only 0 and 100 can be set, recursively overriding the progress of all sub-projects.</br>
	 * This method will invoke updateProgress().
	 * 
	 * @param sender The CommandSender responsible for updating the progress.
	 * @param progress the new progress, 0 ≤ progress ≤ 100. */
	public void setProgress(CommandSender sender, int progress)
	{
		if (progress < 0 || progress > 100)
		{
			if (sender != null)
				Utils.sendErrorMessage(sender, null, "Progress must be between 0% and 100%!");
			return;
		}
		if (subs.size() == 0)
		{
			if (sender != null)
				Utils.sendMessage(sender, null, "Updated the project's progress to &e" + progress + "%&7.", '&');
			this.progress = progress;
			updateProgress();
		}
		else
		{
			if (progress == 100)
			{
				for (Project project : subs)
				{
					project.setProgress(sender, progress);
				}
				updateProgress();
				if (sender != null)
					Utils.sendMessage(sender, null, "Set the entire branch to done!");
			}
			if (progress == 0)
			{
				for (Project project : subs)
				{
					project.setProgress(null, progress);
				}
				updateProgress();
				if (sender != null)
					Utils.sendMessage(sender, null, "Set the entire branch to not done!");
			}
			else
			{
				if (sender != null)
				{
					Utils.sendErrorMessage(sender, null,
							"Can not set progress of a non-end project node to anything but 100 or 0!");
					Utils.sendErrorMessage(sender, null, "Please update the corresponding sub-project!");
				}
			}
		}
	}
	
	/** This method updates the progress on the project, calculating it from the sub-projects if exists and updating the parent as well.</br>
	 * This will also update the scoreboard. */
	public void updateProgress()
	{
		if (subs.size() != 0)
		{}
		if (parent != null)
		{
			parent.updateProgress();
		}
		updateScoreboard();
	}
	
	/** This returns the unique project identifier of this project, consisting of [name]#[ID].
	 * 
	 * @return the unique identifier. */
	public String getUUID()
	{
		return projectID;
	}
	
	/** This method allows access to the parent project.
	 * 
	 * @return the parent project, or null if this is the root project. */
	public Project getParent()
	{
		return parent;
	}
	
	/** Teleports a player to the location assigned with this project. If no location can be found, the parent project's location will be used.</br>
	 * If at the root Project no valid location was found, the player will not be teleported and an error message will be sent.
	 * 
	 * @param player the player to be teleported.
	 * @return true if the teleport was successfull. */
	public boolean tp(Player player)
	{
		if (location == null)
		{
			if (parent != null)
			{
				return parent.tp(player);
			}
			else
			{
				if (player.getUniqueId().equals(owner))
					Utils.sendErrorMessage(player, null,
							"No location was assigned with this project! Use &e/project option location set &7(or one of the interactive menus )to define a location!");
				else
					Utils.sendErrorMessage(player, null,
							"No location was assigned with this project! Ask a project administrator to define one!");
				return false;
			}
		}
		else
		{
			player.teleport(location);
			Utils.sendMessage(player, null, "Teleported you to the projects location!");
			return true;
		}
	}
	
	/** This method converts a JSONObject representation of a Project back into the original project.</br>
	 * It will also load all sub-projects recursively, and load complex types back from Strings.
	 * 
	 * @param project The JSONObject containing the Project.
	 * @return the project that was represented by the JSONObject. */
	public static Project getProject(JSONObject project)
	{
		ArrayList<Project> subs = new ArrayList<Project>();
		for (Object obj : (JSONArray) project.get("subs"))
		{
			subs.add(Project.getProject((JSONObject) obj));
		}
		String projectName = (String) project.get("projectName");
		String displayName = (String) project.get("displayName");
		String projectID = (String) project.get("projectID");
		UUID owner = UUID.fromString((String) project.get("owner"));
		JSONArray admins = (JSONArray) project.get("admins");
		JSONArray members = (JSONArray) project.get("members");
		int localID = (int) project.get("localID");
		int progress = (int) project.get("progress");
		Location location = getStringLocation((String) project.get("location"));
		Project result = new Project(projectID, null, projectName, displayName, owner, localID, location, admins,
				members);
		for (Project sub : subs)
			sub.parent = result;
		result.progress = progress;
		return result;
	}
	
	/** This method turns the project into a JSONObject for storing on the file system. It will recursively turn all sub-projects into JSONObjects</br>
	 * and it will also convert all complex types into Strings.
	 * 
	 * @return A JSONObject representing the project. */
	@SuppressWarnings("unchecked")
	public JSONObject toJSONObject()
	{
		JSONObject project = new JSONObject();
		JSONArray subProjects = new JSONArray();
		for (Project subProject : subs)
		{
			subProjects.add(subProject.toJSONObject());
		}
		project.put("subs", subProjects);
		project.put("projectName", this.projectName);
		project.put("displayName", this.displayName);
		project.put("projectID", this.projectID);
		project.put("owner", owner.toString());
		project.put("admins", admins);
		project.put("members", members);
		project.put("localID", localID);
		project.put("progress", progress);
		project.put("parent", parent.toString());
		project.put("location", getLocationString(location));
		return project;
	}
	
	/** This method converts a location into a String representation ready for storing on the file system.
	 * 
	 * @param location The location to be turned into a String.
	 * @return The String representation of the location. */
	public static String getLocationString(Location location)
	{
		UUID worldID = location.getWorld().getUID();
		return worldID + ";" + location.getX() + ";" + location.getY() + ";" + location.getZ() + ";" + location.getYaw()
				+ ";" + location.getPitch();
	}
	
	/** This method converts a String representation of a location back into its original location.
	 * 
	 * @param location The String representation of the location.
	 * @return the location described by the String given, or null if the world containing it is gone.
	 * @throws NumberFormatException when the floats given as coordinates are invalid.
	 * @throws NumberFormatException when the UUID given as world descriptor is invalid. */
	public static Location getStringLocation(String location) throws NumberFormatException, IllegalArgumentException
	{
		String[] params = location.split(";");
		return new Location(Bukkit.getWorld(UUID.fromString(params[0])), Float.parseFloat(params[1]),
				Float.parseFloat(params[2]), Float.parseFloat(params[3]), Float.parseFloat(params[4]),
				Float.parseFloat(params[5]));
	}
	
	/** This method refreshes the scoreboard assigned with the project and will be called internally when any value changes.</br>
	 * You can call this method at any time before displaying the scoreboard to make sure that the displayed values are accurate. */
	public void updateScoreboard()
	{
		Objective project = scoreboard.registerNewObjective("Project", "dummy");
		project.setDisplaySlot(DisplaySlot.SIDEBAR);
		if (displayName != null)
			project.setDisplayName(displayName);
		else
			project.setDisplayName(projectName);
		Score progress = project.getScore("§aProgress");
		progress.setScore(this.progress);
		save();
	}
	
	private void save()
	{
		if (parent == null)
			JsonManager.save(toJSONObject(),
					new File(Main.plugin.getDataFolder(), "scoreboards/projects/" + projectID + ".json"));
		else
			parent.save();
	}
	
	/** This method returns the UUID of the owner of this project.
	 * 
	 * @return the UUID of the owner, null if the owner is console. */
	public UUID getOwner()
	{
		return owner;
	}
	
	/** This method returns how many sub-projects can be created within this project, depending on the permissions of the project owner.</br>
	 * </br>
	 * Please note that as of Version 3.0.x, this value is always 5 and not respecting permissions.
	 * 
	 * @return The maximum amount of sub-projects this project can have. */
	public int getMaxSubprojects()
	{
		return 5;
	}
	
	/** This method returns an unmodifiable copy of the sub-projects. If you want to modify the list, use the corresponding add/delete/clear methods</br>
	 * of the project holding the list. You can still perform actions on all sub-projects as usual.
	 * 
	 * @return An unmodifiable copy of the sub-projects list. */
	public synchronized List<Project> getSubProjects()
	{
		return Collections.unmodifiableList(subs);
	}
	
	/** This method returns the scoreboard generated by the project status.
	 * 
	 * @return the scoreboard object. */
	public Scoreboard getScoreboard()
	{
		return scoreboard;
	}
}
