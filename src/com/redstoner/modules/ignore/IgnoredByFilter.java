package com.redstoner.modules.ignore;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;

import com.redstoner.misc.BroadcastFilter;

public class IgnoredByFilter implements BroadcastFilter{

	JSONArray isIgnoredBy;
	
	public IgnoredByFilter(JSONArray isIgnoredBy) {
		this.isIgnoredBy = isIgnoredBy;
	}
	@Override
	public boolean sendTo(CommandSender recipient) {
		if (recipient instanceof Player)
		{
			Player player = (Player) recipient;
			return !isIgnoredBy.contains( player.getUniqueId().toString() );
		}
		else
		{
			return true;
		}
	}

}
