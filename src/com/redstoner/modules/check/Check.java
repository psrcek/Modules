package com.redstoner.modules.check;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.nemez.cmdmgr.Command;
import com.nemez.cmdmgr.Command.AsyncType;
import com.nemez.cmdmgr.CommandManager;
import com.redstoner.annotations.Version;
import com.redstoner.coremods.moduleLoader.ModuleLoader;
import com.redstoner.misc.Main;
import com.redstoner.misc.mysql.JSONManager;
import com.redstoner.misc.mysql.MysqlHandler;
import com.redstoner.misc.mysql.elements.ConstraintOperator;
import com.redstoner.misc.mysql.elements.MysqlConstraint;
import com.redstoner.misc.mysql.elements.MysqlDatabase;
import com.redstoner.misc.mysql.elements.MysqlTable;
import com.redstoner.modules.Module;

import net.nemez.chatapi.click.Message;

@Version(major = 4, minor = 0, revision = 1, compatible = 4)
public class Check implements Module, Listener
{
	MysqlTable table;
	
	@Override
	public boolean onEnable()
	{
		Map<Serializable, Serializable> config = JSONManager.getConfiguration("check.json");
		if (config == null || !config.containsKey("database") || !config.containsKey("table"))
		{
			getLogger().error("Could not load the Check config file, disabling!");
			return false;
		}
		try
		{
			MysqlDatabase database = MysqlHandler.INSTANCE
					.getDatabase((String) config.get("database") + "?autoReconnect=true");
			table = database.getTable((String) config.get("table"));
		}
		catch (NullPointerException e)
		{
			getLogger().error("Could not use the Check config, disabling!");
			return false;
		}
		return true;
	}
	
	@Override
	public void postEnable()
	{
		CommandManager.registerCommand(getCommandString(), this, Main.plugin);
	}
	
	@SuppressWarnings("deprecation")
	@Command(hook = "checkCommand", async = AsyncType.ALWAYS)
	public void checkCommand(final CommandSender sender, final String player)
	{
		getLogger().message(sender, "&7Please note that the data may not be fully accurate!");
		OfflinePlayer oPlayer;
		oPlayer = Bukkit.getPlayer(player);
		if (oPlayer == null)
			oPlayer = Bukkit.getServer().getOfflinePlayer(player);
		sendData(sender, oPlayer);
		if (ModuleLoader.exists("Tag"))
			Bukkit.dispatchCommand(sender, "tag check " + player);
	}
	
	public String read(URL url)
	{
		String data = "";
		try
		{
			Scanner in = new Scanner(new InputStreamReader(url.openStream()));
			while (in.hasNextLine())
			{
				data += in.nextLine();
			}
			in.close();
			return data;
		}
		catch (IOException e)
		{}
		return null;
	}
	
	public JSONObject getIpInfo(OfflinePlayer player)
	{
		String ip = "";
		if (player.isOnline())
		{
			ip = player.getPlayer().getAddress().getHostString();
		}
		else
		{
			try
			{
				ip = (String) table.get("last_ip", new MysqlConstraint("uuid", ConstraintOperator.EQUAL,
						player.getUniqueId().toString().replace("-", "")))[0];
			}
			catch (Exception e)
			{
				return null;
			}
		}
		try
		{
			URL ipinfo = new URL("http://ipinfo.io/" + ip + "/json");
			String rawJson = read(ipinfo);
			return (JSONObject) new JSONParser().parse(rawJson);
		}
		catch (Exception e)
		{}
		return null;
	}
	
	public String getFirstJoin(OfflinePlayer player)
	{
		Long firstJoin = player.getFirstPlayed();
		Date date = new Date(firstJoin);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return format.format(date);
	}
	
	public String getLastSeen(OfflinePlayer player)
	{
		Long lastSeen = player.getLastPlayed();
		Date date = new Date(lastSeen);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return format.format(date);
	}
	
	public Object[] getWebsiteData(OfflinePlayer player)
	{
		MysqlConstraint constraint = new MysqlConstraint("uuid", ConstraintOperator.EQUAL,
				player.getUniqueId().toString().replace("-", ""));
		try
		{
			int id = (int) table.get("id", constraint)[0];
			String email = (String) table.get("email", constraint)[0];
			boolean confirmed = (boolean) table.get("confirmed", constraint)[0];
			return new Object[] {"https://redstoner.com/users/" + id, email, confirmed};
		}
		catch (Exception e)
		{
			try
			{
				int id = (int) table.get("id", constraint)[0];
				String email = (String) table.get("email", constraint)[0];
				boolean confirmed = (boolean) table.get("confirmed", constraint)[0];
				return new Object[] {"https://redstoner.com/users/" + id, email, confirmed};
			}
			catch (Exception e2)
			{}
			return new Object[] {null};
		}
	}
	
	public String getCountry(JSONObject data)
	{
		return (String) data.get("country");
	}
	
	public String getAllNames(OfflinePlayer player)
	{
		String uuid = player.getUniqueId().toString().replace("-", "");
		String nameString = "";
		try
		{
			String rawJson = read(new URL("https://api.mojang.com/user/profiles/" + uuid + "/names"));
			JSONArray names = (JSONArray) new JSONParser().parse(rawJson);
			for (Object obj : names)
			{
				nameString += "&e" + ((JSONObject) obj).get("name") + "&7, ";
			}
			nameString = nameString.substring(0, nameString.length() - 2);
			return nameString;
		}
		catch (Exception e)
		{}
		return null;
	}
	
	public void sendData(CommandSender sender, OfflinePlayer player)
	{
		try
		{
			JSONObject ipInfo = getIpInfo(player);
			// data
			String firstJoin = getFirstJoin(player);
			String lastSeen = getLastSeen(player);
			firstJoin = (firstJoin.equals("1970-01-01 01:00")) ? "&eNever" : "&7(yyyy-MM-dd hh:mm) &e" + firstJoin;
			lastSeen = (lastSeen.equals("1970-1-1 01:00")) ? "&eNever" : "&7(yyyy-MM-dd hh:mm) &e" + lastSeen;
			Object[] websiteData = getWebsiteData(player);
			String websiteUrl = (websiteData[0] == null) ? "None" : (String) websiteData[0];
			String email = (websiteData[0] == null) ? "Unknown" : (String) websiteData[1];
			boolean emailNotConfirmed = (websiteData[0] == null) ? false : !((boolean) websiteData[2]);
			String country = (ipInfo == null) ? "Unknown" : getCountry(ipInfo);
			String namesUsed = getAllNames(player);
			if (namesUsed == null)
				namesUsed = "None";
			// messages
			List<Message> messages = new ArrayList<>();
			messages.add(new Message(sender, null).appendText(getLogger().getHeader()));
			messages.add(new Message(sender, null).appendText("&7Data provided by redstoner:"));
			messages.add(new Message(sender, null).appendSuggestHover("&6> UUID: &e" + player.getUniqueId().toString(),
					player.getUniqueId().toString(), "Click to copy into chatbox!"));
			messages.add(new Message(sender, null).appendText("&6> First joined: &e" + firstJoin));
			messages.add(new Message(sender, null).appendText("&6> Last seen: &e" + lastSeen));
			messages.add(
					new Message(sender, null).appendText("&6> Website account: &e").appendLink(websiteUrl, websiteUrl));
			messages.add(new Message(sender, null).appendText("&6> Email: &e")
					.appendLink((emailNotConfirmed ? "&6> &4Email NOT Confirmed!" : "") + email, "mailto:" + email));
			messages.add(new Message(sender, null).appendText("&7Data provided by ipinfo:"));
			messages.add(new Message(sender, null).appendText("&6> Country: &e" + country));
			messages.add(new Message(sender, null).appendText("&6> "));
			messages.add(new Message(sender, null).appendText("&6> "));
			messages.add(new Message(sender, null).appendText("&7Data provided by mojang:"));
			messages.add(new Message(sender, null).appendText("&6> All ingame names used so far: &e" + namesUsed));
			for (Message m : messages)
				m.send();
		}
		catch (Exception e)
		{
			getLogger().message(sender, true, "Sorry, something went wrong while fetching data");
			e.printStackTrace();
		}
	}
	
	// @noformat
	@Override
	public String getCommandString()
	{
		return "command check {\n" + 
				"	perm utils.check;\n" + 
				"	\n" + 
				"	[string:player] {\n" + 
				"		run checkCommand player;\n" + 
				"		help Get info on a player;\n" + 
				"	}\n" + 
				"}";
	}
	// @format
}
