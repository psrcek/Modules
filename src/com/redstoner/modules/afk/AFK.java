package com.redstoner.modules.afk;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;
import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.AutoRegisterListener;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.BroadcastFilter;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.misc.Main;
import com.redstoner.misc.Utils;
import com.redstoner.modules.Module;
import com.redstoner.modules.datamanager.DataManager;

@Commands(CommandHolderType.File)
@AutoRegisterListener
@Version(major = 4, minor = 0, revision = 5, compatible = 5)
public class AFK implements Module, Listener
{
	private CustomListener listener;
	boolean move = true, look = false;
	
	
	@Override
	public void firstLoad()
	{
		Module.super.firstLoad();
		DataManager.setConfig("indicator", "&7[AFK]");
		String[] choices = new String[] {"listen", "ignore"};
		DataManager.setConfig("move", "listen", choices);
		DataManager.setConfig("look", "ignore", choices);
		DataManager.setConfig("chat", "listen", choices);
		DataManager.setConfig("interact", "listen", choices);
		DataManager.setConfig("command", "ignore", choices);
	}
	
	@Override
	public void migrate(Version old)
	{
		Module.super.migrate(old);
		if ((old.major() == 4) && (old.minor() == 0) && (old.revision() == 3))
		{
			String[] choices = new String[] {"listen", "ignore"};
			DataManager.setConfig("look", "ignore", choices);
		}
	}
	
	@Override
	public void postEnable()
	{
		Module.super.postEnable();
		listener = new CustomListener();
		update_afk_listeners(Bukkit.getConsoleSender());
	}
	
	@Override
	public void onDisable()
	{
		Module.super.onDisable();
		HandlerList.unregisterAll(listener);
	}
	
	@Command(hook = "afk")
	public boolean afk(CommandSender sender, boolean silent)
	{
		return afk(sender, silent, "");
	}
	
	@Command(hook = "afk2")
	public boolean afk(CommandSender sender, boolean silent, String reason)
	{
		if (isafk(sender))
		{
			unafk(sender, silent);
		}
		else
		{
			DataManager.setData(sender, "afk_time", System.currentTimeMillis());
			DataManager.setData(sender, "afk_reason", reason);
			DataManager.setState(sender, "afk_silent", silent);
			DataManager.setState(sender, "afk", true);
			if (!silent)
				Utils.broadcast("§7 * ", Utils.getName(sender) + "§7 is now AFK", null);
		}
		return true;
	}
	
	public void unafk(CommandSender sender, boolean silent)
	{
		DataManager.setState(sender, "afk", false);
		if (!silent)
			Utils.broadcast("§7 * ", Utils.getName(sender) + "§7 is no longer AFK", null);
	}
	
	public boolean isafk(CommandSender sender)
	{
		return DataManager.getState(sender, "afk");
	}
	
	public boolean isVanished(Player player)
	{
		return DataManager.getState(player, "vanished");
	}
	
	@Command(hook = "update_afk_listeners")
	public boolean update_afk_listeners(CommandSender sender)
	{
		Utils.broadcast(null, "Updating afk listeners...", new BroadcastFilter()
		{
			@Override
			public boolean sendTo(CommandSender recipient)
			{
				return recipient.hasPermission("utils.afk.admin");
			}
		});
		move = DataManager.getConfigOrDefault("move", "listen").equals("listen");
		look = DataManager.getConfigOrDefault("look", "ignore").equals("listen");
		if (move || look)
			Bukkit.getPluginManager().registerEvent(PlayerMoveEvent.class, listener, EventPriority.MONITOR, listener,
					Main.plugin);
		else
			PlayerMoveEvent.getHandlerList().unregister(listener);
		if (DataManager.getConfigOrDefault("chat", "listen").equals("listen"))
			Bukkit.getPluginManager().registerEvent(PlayerInteractEvent.class, listener, EventPriority.MONITOR,
					listener, Main.plugin);
		else
			PlayerInteractEvent.getHandlerList().unregister(listener);
		if (DataManager.getConfigOrDefault("interact", "listen").equals("listen"))
			Bukkit.getPluginManager().registerEvent(AsyncPlayerChatEvent.class, listener, EventPriority.MONITOR,
					listener, Main.plugin);
		else
			AsyncPlayerChatEvent.getHandlerList().unregister(listener);
		if (DataManager.getConfigOrDefault("command", "ignore").equals("listen"))
			Bukkit.getPluginManager().registerEvent(PlayerCommandPreprocessEvent.class, listener, EventPriority.MONITOR,
					listener, Main.plugin);
		else
			PlayerCommandPreprocessEvent.getHandlerList().unregister(listener);
		return true;
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event)
	{
		DataManager.setState(event.getPlayer(), "afk", false);
	}
}

class CustomListener implements Listener, EventExecutor
{
	private boolean move = true, look = false;
	
	@Override
	public void execute(Listener listener, Event event) throws EventException
	{
		if (event instanceof PlayerEvent)
		{
			if (event instanceof PlayerMoveEvent)
			{
				PlayerMoveEvent pevent = (PlayerMoveEvent) event;
				double distance = pevent.getFrom().distance(pevent.getTo());
				boolean moved = distance > 0;
				boolean looked = (pevent.getFrom().getPitch() != pevent.getTo().getPitch())
						|| (pevent.getFrom().getYaw() != pevent.getTo().getYaw());
				if ((move && moved) || (look && looked))
				{
					Player player = pevent.getPlayer();
					if (isafk(player))
						if (!isVanished(player))
							unafk(player);
				}
			}
			else
			{
				PlayerEvent pevent = (PlayerEvent) event;
				Player player = pevent.getPlayer();
				if (isafk(player))
					if (!isVanished(player))
						unafk(player);
			}
		}
	}
	
	public void unafk(CommandSender sender)
	{
		DataManager.setState(sender, "afk", false);
		if ( !isSilent(sender) )
			Utils.broadcast("§7 * ", Utils.getName(sender) + "§7 is no longer AFK", null);
	}
	
	public boolean isafk(CommandSender sender)
	{
		return DataManager.getState(sender, "afk");
	}
	
	public boolean isSilent(CommandSender sender)
	{
		return DataManager.getState(sender, "afk_silent");
	}
	
	public boolean isVanished(Player player)
	{
		return DataManager.getState(player, "vanished");
	}
	
	public void listenMove(boolean move)
	{
		this.move = move;
	}
	
	public void listenLook(boolean look)
	{
		this.look = look;
	}
}
