package com.redstoner.modules.iplock;

import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.json.simple.JSONObject;

import com.redstoner.annotations.Version;
import com.redstoner.modules.Module;

@Version(major = 3, minor = 0, revision = 0, compatible = 3)
public class IPLock implements Module, Listener
{
	JSONObject iplocks;
	
	@Override
	public void postEnable()
	{}
	
	public void onPlayerJoin(AsyncPlayerPreLoginEvent event)
	{
		event.getAddress().getHostAddress().toString();
		event.setKickMessage("");
		event.setLoginResult(Result.KICK_OTHER);
	}
}
