package com.redstoner.modules.illumination;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nemez.cmdmgr.Command;
import com.redstoner.annotations.Commands;
import com.redstoner.annotations.Version;
import com.redstoner.misc.CommandHolderType;
import com.redstoner.modules.Module;

@Commands(CommandHolderType.String)
@Version(major = 4, minor = 0, revision = 0, compatible = 4)
public class Illumination implements Module
{
	PotionEffect effect = new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false);
	
	@Command(hook = "illuminate")
	public void illuminate(CommandSender sender)
	{
		Player player = (Player) sender;
		if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION))
		{
			player.removePotionEffect(PotionEffectType.NIGHT_VISION);
			getLogger().message(sender, "Night Vision Disabled.");
		}
		else
		{
			player.addPotionEffect(effect, true);
			getLogger().message(sender, "Night Vision Enabled.");
		}
	}
	
	// @noformat
	@Override
	public String getCommandString()
	{
		return "command nightvision {\n" + 
				"	[empty] {\n" + 
				"		run illuminate;\n" + 
				"		type player;\n" + 
				"		help Gives the player infinte night vision;\n" + 
				"		perm utils.illuminate;\n" + 
				"	}\n" + 
				"}";
	}
	// @format
}
