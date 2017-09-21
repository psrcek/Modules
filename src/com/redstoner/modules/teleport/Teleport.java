package com.redstoner.modules.teleport;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.modules.Module;
import com.redstoner.modules.datamanager.DataManager;

import net.nemez.chatapi.click.Message;

@Commands(CommandHolderType.File)
@Version(major = 4, minor = 0, revision = 0, compatible = 4)
public class Teleport implements Module
{
	public static final String PERMISSION_TELEPORT = "utils.admin.teleport";
	
	public ArrayList<TPRequest> pending_requests;
	
	@Override
	public void postEnable()
	{
		pending_requests = new ArrayList<TPRequest>();
	}
	
	@Command(hook = "tp")
	public boolean teleport(CommandSender sender, String player)
	{
		if (!sender.hasPermission(PERMISSION_TELEPORT))
			return tpa(sender, player);
		return true;
	}
	
	@Command(hook = "tp2")
	public boolean teleport(CommandSender sender, String player, String player2)
	{
		if (!sender.hasPermission(PERMISSION_TELEPORT))
			if (player2.equals(((Player) sender).getName()))
				return tpahere(sender, player);
			else
			{
				getLogger().message(sender, "You do not have the required permissions to run that Command!");
				return true;
			}
		Player p1 = Bukkit.getPlayer(player);
		Player p2 = Bukkit.getPlayer(player2);
		if (p1 == null || p2 == null)
		{
			getLogger().message(sender, true, "The specified player couldn't be found!");
			return true;
		}
		p1.teleport(p2);
		getLogger().message(p1, "You have been teleported to: " + p2.getDisplayName());
		if (!sender.getName().equals(p1.getName()))
			getLogger().message(sender,
					p1.getDisplayName() + "&7 has been teleported to " + p2.getDisplayName() + "&7!");
		return true;
	}
	
	@Command(hook = "tpa")
	public boolean tpa(CommandSender sender, String player)
	{
		return true;
	}
	
	@Command(hook = "tpahere")
	public boolean tpahere(CommandSender sender, String player)
	{
		return true;
	}
	
	@Command(hook = "tpmenu")
	public boolean tpinventory(CommandSender sender)
	{
		
		return true;
	}
	
	protected void remove(TPRequest request)
	{
	
	}
}

class TPRequest implements Runnable
{
	private final Teleport holder;
	private final Player sender;
	private final Player target;
	private final Type type;
	private int index;
	
	Thread t;
	
	public TPRequest(Player sender, Player target, Type type, int index, Teleport holder)
	{
		this.sender = sender;
		this.target = target;
		this.type = type;
		this.index = 0;
		this.holder = holder;
	}
	
	public Player getSender()
	{
		return sender;
	}
	
	public Player getTarget()
	{
		return target;
	}
	
	public Type getType()
	{
		return type;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public void setIndex(int index)
	{
		this.index = index;
	}
	
	public void execute()
	{
		switch (type)
		{
			case tpa:
				sender.teleport(target);
				break;
			case tpahere:
				target.teleport(sender);
				break;
		}
	}
	
	public void abort()
	{
		t.interrupt();
	}
	
	@Override
	public void run()
	{
		t = Thread.currentThread();
		try
		{
			Thread.sleep(60000);
		}
		catch (InterruptedException e)
		{
			holder.remove(this);
			Message m = new Message(sender, null);
			if (DataManager.getState(sender, "AFK"))
			{
				m.appendText(target.getDisplayName() + " is AFK and might not respond. ");
				m.appendSendChat("Try again?", "/" + type.toString() + " " + target.getName());
			}
			if (DataManager.getState(sender, "BUSY"))
			{
				m.appendText(target.getDisplayName() + " is BUSY and might not respond. ");
				m.appendSendChat("Try again?", "/" + type.toString() + " " + target.getName());
			}
			return;
		}
		holder.remove(this);
	}
}

enum Type
{
	tpa,
	tpahere;
}
