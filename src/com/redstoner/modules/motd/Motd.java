package com.redstoner.modules.motd;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import com.nemez.cmdmgr.Command;
import com.nemez.cmdmgr.Command.AsyncType;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.modules.Module;

@Commands(CommandHolderType.String)
@AutoRegisterListener
@Version(major = 4, minor = 0, revision = 0, compatible = 4)
public class Motd implements Module, Listener
{
	private String default_motd, motd;
	
	@Command(hook = "setmotd", async = AsyncType.ALWAYS)
	public boolean setMotd(CommandSender sender, String motd)
	{
		if (motd.equals("--reset"))
			this.motd = default_motd;
		else
			this.motd = motd;
		getLogger().message(sender, "The new motd is:\n" + this.motd);
		return true;
	}
	
	@Command(hook = "getmotd", async = AsyncType.ALWAYS)
	public boolean getMotd(CommandSender sender)
	{
		getLogger().message(sender, motd == null ? default_motd : motd);
		return true;
	}
	
	@EventHandler
	public void onServerPing(ServerListPingEvent event)
	{
		event.setMotd(motd);
	}
	
	@Override
	public boolean onEnable()
	{
		default_motd = Bukkit.getMotd();
		if (default_motd == null)
		{
			default_motd = "§6Sample text\n§4FIX YOUR SERVER!";
		}
		motd = default_motd;
		return true;
	}
	
	// @noformat
	@Override
	public String getCommandString()
	{
		return "command setmotd {\n" + 
				"    [string:motd...] {\n" + 
				"        help Sets the motd. Use --reset to reset to default;\n" + 
				"        run setmotd motd;\n" + 
				"    	perm utils.setmotd;" +
				"    }\n" + 
				"}\n" + 
				"command getmotd {\n" + 
				"    [empty] {\n" + 
				"        help Returns the motd;\n" + 
				"        run getmotd;\n" + 
				"    	perm utils.getmotd;" +
				"    }\n" + 
				"}";
	}
	// @format
}
