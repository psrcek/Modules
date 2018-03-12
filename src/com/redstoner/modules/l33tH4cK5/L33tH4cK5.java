package com.redstoner.modules.l33tH4cK5;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.modules.Module;

@AutoRegisterListener
@Commands(CommandHolderType.String)
@Version(major = 4, minor = 0, revision = 1, compatible = 4)
public class L33tH4cK5 implements Module, Listener
{
	private boolean oof = false;
	
	@Command(hook = "ööf")
	public boolean oof(CommandSender sender)
	{
		this.oof = !this.oof;
		getLogger().message(sender, oof ? "&aööf enabled!" : "&cNo longer ööf-ing!");
		return true;
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event)
	{
		if (oof)
			if (!event.getPlayer().hasPermission("chat.oof"))
				event.setMessage(event.getMessage().replace("oof", "ööf").replace("OOF", "ÖÖF")
						.replaceAll("[oO][oO][fF]", "ööf"));
	}
	
	@Override
	public String getCommandString()
	{
		return "command ööf {\n [empty] {\n run ööf;\n perm utils.ööf;\n}\n}";
	}
}
