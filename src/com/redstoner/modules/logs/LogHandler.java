package com.redstoner.modules.logs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.GZIPInputStream;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.redstoner.misc.Utils;
import com.redstoner.modules.datamanager.DataManager;

public class LogHandler extends Thread
{
	private CommandSender sender;
	private String regex, fileName;
	private static ArrayList<CommandSender> stillSearching = new ArrayList<>();
	public int totalFiles = 0;
	public int filesSearched = 0;
	public int totalLines = 0;
	public int currentLine = 0;
	
	protected LogHandler(CommandSender sender, String regex, String fileName)
	{
		this.sender = sender;
		this.regex = regex;
		this.fileName = fileName;
	}
	
	public void doSearch()
	{
		if (stillSearching.contains(sender))
		{
			Logs.logger.message(sender, true, "§4 DO NOT EVER TRY TO QUERY TWO SEARCHES AT ONCE. Go die...!");
			return;
		}
		stillSearching.add(sender);
		this.start();
	}
	
	/** Searches the logs for a certain regex and forwards any matches to the sender.
	 * 
	 * @param sender the issuer of the search
	 * @param regex the regex to search for. Will be wrapped in "^.*" and ".*$" if it is missing line delimiters
	 * @param fileName the name of the files to search through. May contain wildcards. */
	private void search(CommandSender sender, String regex, String fileName)
	{
		long starttime = System.currentTimeMillis();
		int matches = 0;
		Logs.logger.message(sender, "Starting log search for &e" + regex + "&7 in &e" + fileName
				+ " &7now. &cPlease do not query any other searches until this one completes.");
		try
		{
			if (!regex.startsWith("^"))
				regex = "^.*" + regex;
			if (!regex.endsWith("$"))
				regex += ".*$";
			File logFolder = Logs.getLogsDir();
			Pattern fileNamePattern;
			try
			{
				fileNamePattern = Pattern.compile(fileName);
			}
			catch (PatternSyntaxException e)
			{
				Logs.logger.message(sender, true, "An error occured trying to compile the filename pattern!");
				stillSearching.remove(sender);
				return;
			}
			File[] files = logFolder.listFiles(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					return fileNamePattern.matcher(name).matches();
				}
			});
			totalFiles = files.length;
			if (totalFiles == 0)
			{
				Logs.logger.message(sender, true, "No files found!");
				stillSearching.remove(sender);
				return;
			}
			else
				Logs.logger.message(sender, "A total of &e" + totalFiles + "&7 files will be searched!");
			
			boolean progress = (boolean) DataManager.getOrDefault(Utils.getID(sender), "Logs", "progress", true);
			Pattern searchPattern;
			try
			{
				searchPattern = Pattern.compile(regex);
			}
			catch (PatternSyntaxException e)
			{
				Logs.logger.message(sender, true, "An error occured trying to compile the search pattern!");
				stillSearching.remove(sender);
				return;
			}
			for (File file : files)
			{
				if (file.getName().endsWith(".gz"))
				{
					
					BufferedReader inputReader = new BufferedReader(
							new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
					matches += searchStream(inputReader, searchPattern, sender, file.getName());
					inputReader.close();
				}
				else
				{
					BufferedReader inputReader = new BufferedReader(new FileReader(file));
					matches += searchStream(inputReader, searchPattern, sender, file.getName());
					inputReader.close();
				}
				filesSearched++;
				if (progress)
				{
					sender.sendMessage("§7So far, §e" + filesSearched + "§7/§e" + totalFiles + "§7 File(s) and §e"
							+ totalLines + "§7 Line(s) were searched.");
				}
			}
		}
		catch (Exception e)
		{
			Logs.logger.message(sender, true,
					"An unexpected error occured, please check your search parameters and try again!");
			stillSearching.remove(sender);
			return;
		}
		stillSearching.remove(sender);
		if ((boolean) DataManager.getOrDefault(Utils.getID(sender), "Logs", "summary", true))
		{
			String[] message = new String[2];
			message[0] = "§aYour search completed after " + (System.currentTimeMillis() - starttime) + "ms!";
			message[1] = "§7In total: §e" + filesSearched + "§7 File(s) and §e" + totalLines
					+ "§7 Line(s) were searched, §a" + matches + "§7 Match(es) were found!";
			Logs.logger.message(sender, message);
		}
		return;
	}
	
	/** This function searches through an InputStream to find a regex. If it finds a match, it will forward that match to the sender and increase the match counter.
	 * 
	 * @param inputReader the input reader containing the data
	 * @param regex the regex to search for
	 * @param sender the issuer of the search
	 * @param singleFile true if only a single file is being searched, false if the original filename contained wildcards.
	 * @param filename the name of the file that is currently being searched
	 * @return how many matches it found
	 * @throws IOException if something goes wrong */
	private int searchStream(BufferedReader inputReader, Pattern searchPattern, CommandSender sender, String filename)
			throws IOException
	{
		String format = (String) DataManager.getOrDefault(Utils.getID(sender), "Logs", "format", Logs.defaultFormat);
		boolean colors = (boolean) DataManager.getOrDefault(Utils.getID(sender), "Logs", "colors", true);
		Player p = null;
		if (sender instanceof Player)
			p = (Player) sender;
		int matches = 0;
		String line = "";
		currentLine = 0;
		while ((line = inputReader.readLine()) != null)
		{
			totalLines++;
			currentLine++;
			if (searchPattern.matcher(line).matches())
			{
				if (((p != null) && (!p.isOnline())))
				{
					stillSearching.remove(sender);
					throw new IOException("The player has left during the search. Aborting now.");
				}
				LogEntry entry = new LogEntry(filename, line, currentLine, totalLines);
				sender.sendMessage(entry.applyFormat(format, colors));
				matches++;
			}
		}
		return matches;
	}
	
	@Override
	public void run()
	{
		try
		{
			search(sender, regex, fileName);
		}
		catch (Exception e)
		{}
	}
}
