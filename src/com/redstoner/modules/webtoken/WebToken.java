package com.redstoner.modules.webtoken;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.parser.ParseException;

import com.nemez.cmdmgr.Command;
import com.nemez.cmdmgr.Command.AsyncType;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.misc.mysql.Config;
import com.redstoner.misc.mysql.MysqlHandler;
import com.redstoner.misc.mysql.elements.ConstraintOperator;
import com.redstoner.misc.mysql.elements.MysqlConstraint;
import com.redstoner.misc.mysql.elements.MysqlDatabase;
import com.redstoner.misc.mysql.elements.MysqlTable;
import com.redstoner.modules.Module;

@Commands(CommandHolderType.String)
@Version(major = 4, minor = 0, revision = 0, compatible = 4)
public class WebToken implements Module
{
	private static final int TOKEN_LENGTH = 6;
	private static final String CONSONANTS = "bcdfghjklmnpqrstvwxyz";
	private static final String VOWELS = "aeiou";
	private MysqlTable table;
	
	@Override
	public boolean onEnable()
	{
		Config config;
		try
		{
			config = Config.getConfig("WebToken.json");
		}
		catch (IOException | ParseException e1)
		{
			e1.printStackTrace();
			return false;
		}
		if (config == null || !config.containsKey("database") || !config.containsKey("table"))
		{
			getLogger().error("Could not load the WebToken config file, disabling!");
			config.put("database", "redstoner");
			config.put("table", "webtoken");
			return false;
		}
		try
		{
			MysqlDatabase database = MysqlHandler.INSTANCE.getDatabase(config.get("database") + "?autoReconnect=true");
			table = database.getTable(config.get("table"));
		}
		catch (NullPointerException e)
		{
			getLogger().error("Could not use the WebToken config, aborting!");
			return false;
		}
		return true;
	}
	
	private String getNextId() throws Exception
	{
		Object[] results = table.get("select id from register_tokens order by id desc limit 1;");
		if (results[0] instanceof Integer)
		{
			return ((int) results[0]) + 1 + "";
		}
		else if (results[0] instanceof String)
		{
			int id = Integer.valueOf((String) results[0]);
			return id + 1 + "";
		}
		else
		{
			throw new Exception("Token query returned invalid result!");
		}
	}
	
	private String query(String emailOrToken, UUID uuid) throws Exception
	{
		if (!(emailOrToken.equals("token") && emailOrToken.equals("email")))
		{
			throw new Exception("Invalid database query: " + emailOrToken);
		}
		Object[] results = table.get(emailOrToken,
				new MysqlConstraint("uuid", ConstraintOperator.EQUAL, uuid.toString().replaceAll("-", "")));
		if (results instanceof String[])
		{
			String[] tokenResults = (String[]) results;
			if (tokenResults.length == 1)
			{
				return tokenResults[0];
			}
			else
			{
				return null;
			}
		}
		else
		{
			throw new Exception("Token query returned invalid result!");
		}
	}
	
	private boolean match(String string, String regex)
	{
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(string);
		return matcher.find();
	}
	
	private void printToken(Player player, String email, String token)
	{
		String[] message = new String[] {"&aEmail: " + email, "&aToken: " + token,
				"&cIMPORTANT: never share the token with anyone!", "&cIt could be used to claim your website account!"};
		getLogger().message(player, message);
	}
	
	private String generateToken()
	{
		String token = "";
		Random random = new Random();
		int start = random.nextInt(2);
		for (int i = 0; i < TOKEN_LENGTH; i++)
		{
			if (i % 2 == start)
			{
				token += CONSONANTS.charAt(random.nextInt(21));
			}
			else
			{
				token += VOWELS.charAt(random.nextInt(5));
			}
		}
		return token;
	}
	
	@Command(hook = "token", async = AsyncType.ALWAYS)
	public void token(CommandSender sender)
	{
		Player player = (Player) sender;
		UUID uuid = player.getUniqueId();
		try
		{
			String token = query("token", uuid);
			if (token == null)
			{
				getLogger().message(player, true, "You don't have a token yet! Use &e/gettoken <email>&7 to get one.");
			}
			else
			{
				String email = query("email", uuid);
				printToken(player, email, token);
			}
		}
		catch (Exception e)
		{
			try
			{
				Thread.sleep(100);
				String token = query("token", uuid);
				if (token == null)
				{
					getLogger().message(player, true,
							"You don't have a token yet! Use &e/gettoken <email>&7 to get one.");
				}
				else
				{
					String email = query("email", uuid);
					printToken(player, email, token);
				}
			}
			catch (Exception e2)
			{
				getLogger().message(player, true, "Error getting your token, please contact an admin!");
				e2.printStackTrace();
			}
		}
	}
	
	@Command(hook = "gettoken", async = AsyncType.ALWAYS)
	public void token(CommandSender sender, String email)
	{
		Player player = (Player) sender;
		if (match(email, "^.+@(.+\\..{2,}|\\[[0-9a-fA-F:.]+\\])$"))
		{
			String uuid = player.getUniqueId().toString().replaceAll("-", "");
			String token = generateToken();
			try
			{
				String id = getNextId();
				table.delete(new MysqlConstraint("uuid", ConstraintOperator.EQUAL, uuid));
				table.insert(id, uuid, token, email);
				printToken(player, email, token);
			}
			catch (Exception e)
			{
				try
				{
					Thread.sleep(100);
					String id = getNextId();
					table.delete(new MysqlConstraint("uuid", ConstraintOperator.EQUAL, uuid));
					table.insert(id, uuid, token, email);
					printToken(player, email, token);
				}
				catch (Exception e2)
				{
					getLogger().message(player, true, "Error getting your token, please contact an admin!");
					e.printStackTrace();
				}
			}
		}
		else
		{
			getLogger().message(player, true, "Hmm... That doesn't look like a valid email!");
		}
	}
	
	// @noformat
	@Override
	public String getCommandString()
	{
		return "command token {\n" + 
				"	perm utils.webtoken;\n" + 
				"	\n" + 
				"	[empty] {\n" + 
				"		help Displays an already generated token;\n" + 
				"		type player;\n" + 
				"		perm utils.webtoken;\n" + 
				"		run token;\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"command gettoken {\n" + 
				"	perm utils.webtoken;\n" + 
				"	\n" + 
				"	[string:email...] {\n" + 
				"		help Generates a token used for website authentication;\n" + 
				"		type player;\n" + 
				"		perm utils.webtoken;\n" + 
				"		run gettoken email;\n" + 
				"	}\n" + 
				"}";
	}
	// @format
}
