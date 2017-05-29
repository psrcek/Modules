package com.redstoner.utils;

import java.util.Set;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import com.redstoner.annotations.Version;

@Version(major = 3, minor = 0, revision = 0, compatible = 3)
public class Susender implements CommandSender
{
	final CommandSender sender;
	CommandSender permission;
	CommandSender output;
	
	public Susender(CommandSender sender, CommandSender permissionHolder, CommandSender outputTarget)
	{
		this.sender = sender;
		this.permission = permissionHolder;
		this.output = outputTarget;
	}
	
	@Override
	public PermissionAttachment addAttachment(Plugin arg0)
	{
		return sender.addAttachment(arg0);
	}
	
	@Override
	public PermissionAttachment addAttachment(Plugin arg0, int arg1)
	{
		return sender.addAttachment(arg0, arg1);
	}
	
	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2)
	{
		return sender.addAttachment(arg0, arg1, arg2);
	}
	
	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2, int arg3)
	{
		return sender.addAttachment(arg0, arg1, arg2, arg3);
	}
	
	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions()
	{
		if (permission == null)
			return sender.getEffectivePermissions();
		return permission.getEffectivePermissions();
	}
	
	@Override
	public boolean hasPermission(String arg0)
	{
		if (permission == null)
			return sender.hasPermission(arg0);
		return permission.hasPermission(arg0);
	}
	
	@Override
	public boolean hasPermission(Permission arg0)
	{
		if (permission == null)
			return sender.hasPermission(arg0);
		return permission.hasPermission(arg0);
	}
	
	@Override
	public boolean isPermissionSet(String arg0)
	{
		if (permission == null)
			return sender.isPermissionSet(arg0);
		return permission.isPermissionSet(arg0);
	}
	
	@Override
	public boolean isPermissionSet(Permission arg0)
	{
		if (permission == null)
			return sender.isPermissionSet(arg0);
		return permission.isPermissionSet(arg0);
	}
	
	@Override
	public void recalculatePermissions()
	{
		if (permission == null)
			sender.recalculatePermissions();
		else
			permission.recalculatePermissions();
	}
	
	@Override
	public void removeAttachment(PermissionAttachment arg0)
	{
		sender.removeAttachment(arg0);
	}
	
	@Override
	public boolean isOp()
	{
		return permission.isOp();
	}
	
	@Override
	public void setOp(boolean arg0)
	{
		sender.setOp(arg0);
	}
	
	@Override
	public String getName()
	{
		return sender.getName();
	}
	
	@Override
	public Server getServer()
	{
		return sender.getServer();
	}
	
	@Override
	public void sendMessage(String arg0)
	{
		if (output == null)
			sender.sendMessage(arg0);
		else
			output.sendMessage(arg0);
	}
	
	@Override
	public void sendMessage(String[] arg0)
	{
		if (output == null)
			sender.sendMessage(arg0);
		else
			output.sendMessage(arg0);
	}
	
	public void setOutputTarget(CommandSender outputTarget)
	{
		output = outputTarget;
	}
	
	public void setPermissionHolder(CommandSender permissionHolder)
	{
		permission = permissionHolder;
	}
}
