package com.redstoner.modules.logs;

public class LogEntry
{
	public final int line;
	public final int global_line;
	public final String filename;
	public final String raw;
	
	public LogEntry(String raw, int line, int global_line)
	{
		this("Unkown", raw, line, global_line);
	}
	
	public LogEntry(String filename, String raw, int line, int global_line)
	{
		this.raw = resolveColors(raw);
		this.line = line;
		this.global_line = global_line;
		this.filename = filename;
	}
	
	public String applyFormat(String format, boolean colors)
	{
		// Replace escaped % with placeholder
		format = format.replace("%%", "§§");
		// Line numbers
		format = format.replace("%l", "" + line);
		format = format.replace("%L", "" + global_line);
		// Filename
		format = format.replace("%f", filename);
		// Strip colors
		if (!colors)
			format = format.replace("%r", raw.replaceAll("$.", ""));
		else
			format = format.replace("%r", raw);
		// Convert placeholder back
		format = format.replace("§§", "%");
		return format;
	}
	
	private String resolveColors(String message)
	{
		message = message.replace("[0;30;22m", "§0");
		message = message.replace("[0;34;22m", "§1");
		message = message.replace("[0;32;22m", "§2");
		message = message.replace("[0;36;22m", "§3");
		message = message.replace("[0;31;22m", "§4");
		message = message.replace("[0;35;22m", "§5");
		message = message.replace("[0;33;22m", "§6");
		message = message.replace("[0;37;22m", "§7");
		message = message.replace("[0;30;1m", "§8");
		message = message.replace("[0;34;1m", "§9");
		message = message.replace("[0;32;1m", "§a");
		message = message.replace("[0;36;1m", "§b");
		message = message.replace("[0;31;1m", "§c");
		message = message.replace("[0;35;1m", "§d");
		message = message.replace("[0;33;1m", "§e");
		message = message.replace("[0;37;1m", "§f");
		
		message = message.replace("[5m", "§k");
		message = message.replace("[21m", "§l");
		message = message.replace("[9m", "§m");
		message = message.replace("[4m", "§n");
		message = message.replace("[3m", "§o");
		
		message = message.replace("[m", "§r");
		
		return message;
	}
}
