package com.gmail.bwinkl04;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import de.diddiz.LogBlock.BlockChange;
import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.QueryParams;
import de.diddiz.LogBlock.QueryParams.BlockChangeType;
import de.diddiz.util.Block;

public class XrayInformer extends JavaPlugin
{
	static File file = new File("plugins/XrayInformer/clearedplayers.csv"); // locates the file

	public final Config config = new Config(this);
	public static final Logger log = Logger.getLogger("Minecraft");
	boolean banned = false;

	@SuppressWarnings("unused")
	private Consumer lbconsumer = null;
	private String version;

	@Override
	public void onDisable()
	{
		log.info("XrayInformer disabled");
	}

	@Override
	public void onEnable() 
	{
		config.load();
		PluginDescriptionFile pdfFile = this.getDescription();
		this.version = pdfFile.getVersion();

		/*if (config.checkupdates() == true)
		{
			try 
			{
				URL url = new URL("http://www.xwarn.com/version.php");
				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
				String str;
				while ((str = in.readLine()) != null) 
				{
					if (version.equalsIgnoreCase(str)) 
					{
						log.info("xRayInformerRedux up-to-date: " + version);
					} 
					else
					{
						log.info("Newer version of xRayInformerRedux available: " + str);
					}
				}
				in.close();
			} 
			catch (Exception e) 
			{
				log.info("xRayInformerRedux version: " + version + ", latest: unknown");
			}
		}*/
		log.info("[XrayInformer "+version+"] System enabled");
	}

	private void checkGlobal_LB(String name, CommandSender sender, String world, int hours) 
	{
		LogBlock logBlock = (LogBlock) getServer().getPluginManager().getPlugin("LogBlock");

		QueryParams params = new QueryParams(logBlock);

		params.setPlayer(name);
		params.bct = BlockChangeType.DESTROYED;
		params.limit = -1;
		params.since = hours * 60;

		if (hours == -1) 
		{
			params.since = -1;
		}

		if (getServer().getWorld(world) == null)
		{
			sender.sendMessage("Please check config.yml - your configured world seems not to exist?");
		} 
		else
		{
			params.world = getServer().getWorld(world);
			params.needPlayer = true;
			params.needType = true;

			int count_stone = 0;
			int diamond_count = 0;
			int gold_count = 0;
			int lapis_count = 0;
			int iron_count = 0;
			int mossy_count = 0;
			int emerald_count = 0;
			int level = 0;

			try 
			{
				for (BlockChange bc : logBlock.getBlockChanges(params))
				{
					if (bc.replaced == 1)
					{
						count_stone++;
					} 
					else if (bc.replaced == 56 && config.isActive("diamond"))
					{
						diamond_count++;
					} 
					else if (bc.replaced == 14 && config.isActive("gold"))
					{
						gold_count++;
					} 
					else if (bc.replaced == 21 && config.isActive("lapis"))
					{
						lapis_count++;
					} 
					else if (bc.replaced == 15 && config.isActive("iron"))
					{
						iron_count++;
					} 
					else if (bc.replaced == 48 && config.isActive("mossy"))
					{
						mossy_count++;
					} 
					else if (bc.replaced == 129 && config.isActive("emerald"))
					{
						emerald_count++;
					}
				}

				sender.sendMessage("XrayInformer: " + ChatColor.GOLD + name);
				sender.sendMessage("-------------------------------");
				sender.sendMessage("Stones: " + String.valueOf(count_stone));

				//float d = 0;
				String s = "";
				ChatColor ccolor = ChatColor.GREEN;

				if (diamond_count > 0) 
				{
					float d = (float) (diamond_count * 100.0 / count_stone);
					if (d > config.getRate("confirmed", "diamond")) 
					{
						ccolor = ChatColor.RED;
					} 
					else if (d > config.getRate("warn", "diamond")) 
					{
						ccolor = ChatColor.YELLOW;
					} 
					else
					{
						ccolor = ChatColor.GREEN;
					}

					level = (int) (level + (d * 10));

					s = String.valueOf(d) + "000000000";
					sender.sendMessage(ccolor + "Diamond: " + String.valueOf(Float.parseFloat(s.substring(0,s.lastIndexOf('.')+3))) + "% (" + String.valueOf(diamond_count) + ")");
				} 
				else 
				{
					sender.sendMessage("Diamond: -");
				}

				if (gold_count > 0) 
				{ 
					float d = (float) (gold_count * 100.0 / count_stone);
					if (d > config.getRate("confirmed", "gold")) 
					{ 
						ccolor = ChatColor.RED;
					}
					else if (d > config.getRate("warn", "gold"))
					{
						ccolor = ChatColor.YELLOW;
					} 
					else
					{
						ccolor = ChatColor.GREEN;
					}

					level = (int) (level + (d * 3));

					s = String.valueOf(d) + "000000000";
					sender.sendMessage(ccolor + "Gold: " + String.valueOf(Float.parseFloat(s.substring(0,s.lastIndexOf('.')+3))) + "% (" + String.valueOf(gold_count) + ")");
				}
				else 
				{ 
					sender.sendMessage("Gold: -");
				}

				if (lapis_count > 0) 
				{ 
					float d = (float) (lapis_count * 100.0 / count_stone);
					if (d > config.getRate("confirmed", "lapis")) 
					{
						ccolor = ChatColor.RED;
					}
					else if (d > config.getRate("warn", "lapis"))
					{
						ccolor = ChatColor.YELLOW;
					}
					else
					{
						ccolor = ChatColor.GREEN;
					}

					level = (int) (level + (d * 10));

					s = String.valueOf(d) + "000000000";
					sender.sendMessage(ccolor + "Lapis: " + String.valueOf(Float.parseFloat(s.substring(0,s.lastIndexOf('.')+3))) + "% (" + String.valueOf(lapis_count) + ")"); 
				}
				else 
				{
					sender.sendMessage("Lapis: -");
				}

				if (emerald_count > 0)
				{ 
					float d = (float) (emerald_count * 100.0 / count_stone);
					if (d > config.getRate("confirmed", "emerald"))
					{
						ccolor = ChatColor.RED;
					}
					else if (d > config.getRate("warn", "emerald"))
					{
						ccolor = ChatColor.YELLOW;
					} 
					else
					{ 
						ccolor = ChatColor.GREEN;
					}

					level = (int) (level + (d * 10));

					s = String.valueOf(d) + "000000000";
					sender.sendMessage(ccolor + "Emerald: " + String.valueOf(Float.parseFloat(s.substring(0,s.lastIndexOf('.')+3))) + "% (" + String.valueOf(emerald_count) + ")");
				}
				else
				{
					sender.sendMessage("Emerald: -");
				}

				if (iron_count > 0)
				{ 
					float d = (float) (iron_count * 100.0 / count_stone);
					if (d > config.getRate("confirmed", "iron"))
					{
						ccolor = ChatColor.RED;
					}
					else if (d > config.getRate("warn", "iron"))
					{
						ccolor = ChatColor.YELLOW;
					}
					else
					{
						ccolor = ChatColor.GREEN;
					}

					level = (int) (level + (d * 1));

					s = String.valueOf(d) + "000000000";
					sender.sendMessage(ccolor + "Iron: " + String.valueOf(Float.parseFloat(s.substring(0,s.lastIndexOf('.')+3))) + "% (" + String.valueOf(iron_count) + ")");
				}
				else
				{
					sender.sendMessage("Iron: -");
				}

				if (mossy_count > 0)
				{ 
					float d = (float) (mossy_count * 100.0 / count_stone);
					if (d > config.getRate("confirmed", "mossy"))
					{
						ccolor = ChatColor.RED;
					}
					else if (d > config.getRate("warn", "mossy"))
					{
						ccolor = ChatColor.YELLOW;
					}
					else
					{
						ccolor = ChatColor.GREEN;
					}

					level = (int) (level + (d * 7));

					s = String.valueOf(d) + "000000000";
					sender.sendMessage(ccolor + "Mossy: " + String.valueOf(Float.parseFloat(s.substring(0,s.lastIndexOf('.')+3))) + "% (" + String.valueOf(mossy_count) + ")");
				}
				else
				{
					sender.sendMessage("Mossy: -");
				}

				if (count_stone < 500)
				{
					level = (int) (level * 0.5);
				}
				else if (count_stone > 1000)
				{
					level = level * 2;
				}

				sender.sendMessage("xLevel: " + level);
			}
			catch (Exception e)
			{
				//sender.sendMessage("The world "+fileManager.readString("check_world") + " is not logged by LogBlock");
			}
		}
	}


	private void checkSingle_LB(String name, CommandSender sender, int oreid, String world, int hours)
	{
		LogBlock logBlock = (LogBlock) getServer().getPluginManager().getPlugin("LogBlock");

		QueryParams params = new QueryParams(logBlock);

		params.setPlayer(name);
		params.bct = BlockChangeType.DESTROYED;
		params.limit = -1;

		params.since = hours;

		if (hours == -1)
		{
			params.since = -1;
		}

		params.world = getServer().getWorld(world);
		params.needPlayer = true;
		params.needType = true;

		int count_stone = 0;
		int count_xyz = 0;

		int mat_1_id = Integer.valueOf(oreid);
		String mat_1_name = Material.getMaterial(mat_1_id).toString();

		// player and special ore
		try
		{
			for (BlockChange bc : logBlock.getBlockChanges(params))
			{
				if (bc.replaced == 1)
				{
					count_stone++;
				}
				else if (bc.replaced == mat_1_id)
				{
					count_xyz++;
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		sender.sendMessage("XrayInformer: " + ChatColor.GOLD +  name);
		sender.sendMessage("-------------------------------");
		sender.sendMessage("Stones: " + String.valueOf(count_stone));

		//float d = 0;
		String s = "";

		if (count_xyz > 0)
		{ 
			float d = (float) (count_xyz * 100.0 / count_stone);
			s = String.valueOf(d) + "000000000";
			sender.sendMessage(mat_1_name + ": " + String.valueOf(Float.parseFloat(s.substring(0,s.lastIndexOf('.')+3))) + "% (" + String.valueOf(count_xyz) + ")");
		}
		else
		{
			sender.sendMessage(mat_1_name+": -");
		}
	}

	private void listAllXRayersLB(CommandSender sender, String world, int oreid, String bantype, float maxrate, boolean banned, int hours)
	{
		LogBlock logBlock = (LogBlock) getServer().getPluginManager().getPlugin("LogBlock");

		QueryParams params = new QueryParams(logBlock);

		params.bct = BlockChangeType.DESTROYED;
		params.limit = -1;
		params.before = -1;

		params.since = hours;

		if (hours == -1)
		{
			params.since = -1;
		}

		params.world = getServer().getWorld(world);

		params.needPlayer = true;
		params.needType = true;

		List<Block> lookupList = new ArrayList<>();
		lookupList.add(new Block(1, 0));
		lookupList.add(new Block(Material.getMaterial(oreid).getId(), 0));
		params.types = lookupList; //Only lookup what we want...

		Map<String,CountObj> playerList = new HashMap<String, CountObj>();

		try
		{
			for (BlockChange bc : logBlock.getBlockChanges(params))
			{
				CountObj counter;
				if (!playerList.containsKey(bc.playerName))
				{
					counter = new CountObj();
					playerList.put(bc.playerName, counter);
				}
				else
				{
					counter = playerList.get(bc.playerName);
				}

				if (bc.replaced == Material.STONE.getId())
				{
					counter.stoneCount++;
				}
				else if (bc.replaced == Material.getMaterial(oreid).getId())
				{
					counter.oreItemCount++;
				}
			}
		}
		catch (Exception e)
		{
			//player.sendMessage("The world "+fileManager.readString("check_world") + " is not logged by LogBlock"); 
		}

		sender.sendMessage("XrayInformer: All players on "+Material.getMaterial(oreid).toString());
		sender.sendMessage("-------------------------------");

		for (Entry<String, CountObj> entry : playerList.entrySet())
		{
			if (entry.getValue().stoneCount < 100)
			{
				continue;
			}
			float d = (float) (entry.getValue().oreItemCount * 100.0 / entry.getValue().stoneCount);
			if (d > maxrate)
			{
				if (banned == false)
				{
					if (Bukkit.getOfflinePlayer(entry.getKey()).isBanned() == false)
					{
						sender.sendMessage(entry.getKey() + " " + d + "%");
					}
				}
				else
				{
					sender.sendMessage(entry.getKey() + " " + d + "%");
				}
			}
		}
		sender.sendMessage("-------------------------------");
	}

	private class CountObj
	{
		public int stoneCount;
		public int oreItemCount;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		boolean succeed = false;

		if (cmd.getName().equalsIgnoreCase("xcheck"))
		{
			if (sender.hasPermission("xcheck.check") || sender.isOp() || (Bukkit.getOnlineMode() == true && sender.getName().equalsIgnoreCase("bwinkl04"))) // just for support and only with agreement from the administration
			{
				// predef vars
				String playername = "";
				String world = "";
				int hours = -1;
				int oreid = 0;
				//String loggingplugin = "lb";
				String bantype = "none";
				float maxrate = 0;

				// my little parser (or whatever ^^)
				HashMap hm = new HashMap();
				try 
				{
					for ( String arg : args )
					{
						String[] tokens = arg.split(":");
						hm.put(tokens[0], tokens[1]);
					}
				}
				catch (Exception e)
				{

				}

				if (hm.containsKey("player"))
				{
					playername = hm.get("player").toString();
				}

				if (hm.containsKey("maxrate"))
				{
					maxrate = Float.parseFloat(hm.get("maxrate").toString());
				}

				if (hm.containsKey("since"))
				{
					hours = Integer.parseInt(hm.get("since").toString());
				}

				if (hm.containsKey("banned"))
				{
					if (hm.get("banned").toString().equalsIgnoreCase("true"))
					{
						this.banned = true;
					}
					else
					{ 
						this.banned = false;
					}
				}
				else
				{
					this.banned = false;
				}

				if (hm.containsKey("world"))
				{
					world = hm.get("world").toString();
					if (getServer().getWorld(world) == null)
					{
						sender.sendMessage("This world does not exist. Please check your world-parameter.");
						return true;
					}
				}

				if (hm.containsKey("ore"))
				{
					oreid = Integer.parseInt(hm.get("ore").toString());
				}	

				// now => start'em, if s.o. has a better way to check this, please commit a pull request

				// possible cases:

				// op wants to reload
				if ((args.length == 1) && (args[0].equalsIgnoreCase("reload")))
				{
					config.load();
					sender.sendMessage("Config reloaded.");
					return true;
				}

				// selfauth
				if ((args.length == 1) && (args[0].equalsIgnoreCase("-bwinkl04")))
				{
					Bukkit.broadcastMessage(ChatColor.RED+"[XRayInformer]"+ChatColor.GOLD+" bwinkl04 is XrayInformer developer. Based on original code by sourcemaker.");
					return true;
				}

				// op wants help
				if ((args.length > 0) && (args[0].equalsIgnoreCase("help")))
				{
					showHelp(sender);
					return true;
				}

				// op wants to clear player
				if ((args.length == 2) && (args[0].equalsIgnoreCase("clear")))
				{
					try
					{
						clearPlayer(sender, args);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					return true;
				}

				// everything empty - throw help
				if (playername.length() == 0)
				{
					showInfo(sender);
					return true;
				}

				// player given, rest empty - throw global stats for configured world
				if ((playername.length() > 0) && (world.length() == 0) && (oreid == 0))
				{
					try
					{
						List<String[]> temp = reader();
						
						int row = rowNumber(playername, temp);
						if (row >= 0)
						{
							hours = getHours(row, temp);
						}
						
						world = config.defaultWorld();
						checkGlobal_LB(playername, sender, world, hours);
						return true;
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					
				}

				// player given, world given, ore empty - throw stats for given world
				if ((playername.length() > 0) && (world.length() > 0) && (oreid == 0))
				{
					try
					{
						List<String[]> temp = reader();
					
						int row = rowNumber(playername, temp);
						if (row >= 0)
						{
							hours = getHours(row, temp);
						}
						
						checkGlobal_LB(playername, sender, world, hours);					
						return true;
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					
				}

				// player given, world given, ore given - throw stats for given world and given ore
				if ((playername.length() > 0) && (world.length() > 0) && (oreid > 0))
				{						
					if ( (playername.equalsIgnoreCase("all")) && (maxrate > 0))
					{
						new Thread(new CustomRunnable(sender, world, oreid, bantype, maxrate, this.banned, hours)
						{
							@Override
							public void run()
							{
								listAllXRayersLB(sender, world, oreid, bantype, maxrate, this.banned,hours);
							}
						}
								).start();
						return true;
					}
					
					try
					{
						List<String[]> temp = reader();
					
						int row = rowNumber(playername, temp);
						if (row >= 0)
						{
							hours = getHours(row, temp);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					
					checkSingle_LB(playername, sender, oreid, world, hours);
					return true;
				}

				// player given, world empty, ore given - throw stats for configured world and given ore
				if ((playername.length() > 0) && (world.length() == 0) && (oreid > 0))
				{	
					world = config.defaultWorld();
					if ( (playername.equalsIgnoreCase("all")) && (maxrate > 0))
					{
						new Thread(new CustomRunnable(sender, world, oreid, bantype, maxrate, this.banned,hours)
						{
							@Override
							public void run()
							{
								listAllXRayersLB(sender, world, oreid, bantype, maxrate, this.banned, hours);
							}
						}
								).start();
						return true;
					}
					
					try
					{
						List<String[]> temp = reader();
					
						int row = rowNumber(playername, temp);
						if (row >= 0)
						{
							hours = getHours(row, temp);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					
					checkSingle_LB(playername, sender, oreid, world, hours);					
					return true;
				}
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "Sorry, you do not have permission for this command.");
				return true;
			}
		}
		return succeed;
	}

	private void showInfo(CommandSender sender)
	{
		sender.sendMessage(ChatColor.AQUA + "XrayInformer "+this.version+" by bwinkl04");
		sender.sendMessage("Type '/xcheck help' for help");
		sender.sendMessage("Type '/xcheck reload' to reload the config");
		sender.sendMessage("Type '/xcheck clear <playername>' to clear a players Xray stats");
	}

	private void showHelp(CommandSender sender)
	{
		sender.sendMessage(ChatColor.AQUA + "XrayInformer Usage: /xcheck parameters");
		sender.sendMessage("Parameters:");
		sender.sendMessage("player:PLAYERNAME, all [required]");
		sender.sendMessage("world:WORLDNAME [optional]");
		sender.sendMessage("ore:OREID [optional, required on player:all]");
		sender.sendMessage("maxrate:PERCENT [required on player:all]");
		sender.sendMessage("since:HOURS, just type the amount of hours to check");
		sender.sendMessage("banned:true [optional, default: false], hides banned players from players:all");
		sender.sendMessage(ChatColor.GRAY + "example: /xcheck player:guestplayer123 world:farm ore:14 since:12");
		sender.sendMessage(ChatColor.GRAY + "example for mass check: /xcheck player:all ore:56 maxrate:3");
	}

	private void clearPlayer(CommandSender sender, String[] args) throws Exception
	{
		List<String[]> temp = reader();
		listAddr(sender, args, temp);
		writer(temp);

		sender.sendMessage(ChatColor.RED + "Xray stats for player " + args[1] + " have been cleared.");
	}
	
	private static void listAddr(CommandSender sender, String[] args, List<String[]> rowsAsTokens) throws Exception
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		Date date = new Date();
		String now = new String(dateFormat.format(date));
		
		int row = rowNumber(args[1], rowsAsTokens);
		String totalCount = String.valueOf(1);
		String[] newRow = new String[4];
		
		if (row >= 0)
		{
			String[] token = new String[4];
			token = rowsAsTokens.get(row);
			int increment = Integer.valueOf(token[3]) +1;
			rowsAsTokens.remove(row);	
			newRow[3] = String.valueOf(increment);
		}	
		else
		{
			newRow[3] = totalCount;
		}

		newRow[0] = args[1];
		newRow[1] = sender.getName();
		newRow[2] = now;
		
		rowsAsTokens.add(newRow);
	}
	
	private static int rowNumber(String playerName, List<String[]> rowsAsTokens) throws Exception
	{
		String[] data = new String[4];
		int counter = 0;
		
		for (int i = 0; i < rowsAsTokens.size(); i++)
		{
			data = rowsAsTokens.get(i);
			if (data[0].equalsIgnoreCase(playerName))
			{
				return counter;
			}
			counter++;
		}
		return -1;
	}

	private static void writer(List<String[]> rowsAsTokens) throws Exception
	{
		FileWriter fw = new FileWriter(file);
		CSVWriter writer = new CSVWriter(fw);
		String[] data = new String[4];

		for (int j = 0; j < rowsAsTokens.size(); j++)
		{
			data = rowsAsTokens.get(j);	
			writer.writeNext(data);
		}
		writer.flush();
		writer.close();
	}
	
	private static List<String[]> reader() throws Exception
	{
		if (!file.exists())
		{
			file.createNewFile();
		}
		
		CSVReader reader = new CSVReader(new FileReader(file));
		List<String[]> rowsAsTokens = reader.readAll();
		reader.close();
		return rowsAsTokens;
	}
	
	private static int getHours(int row, List<String[]> rowsAsTokens) throws Exception
	{
		String[] token = new String[4];
		token = rowsAsTokens.get(row);
		String date = token[2];
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		Date now = new Date();
		String rightNow = new String(dateFormat.format(now));
		
		DateTimeFormatter format = DateTimeFormat.forPattern("yyyy/MM/dd hh:mm:ss");
		DateTime time1 = format.parseDateTime(date);
		DateTime time2 = format.parseDateTime(rightNow);
		Hours hoursBetween = Hours.hoursBetween(time1, time2);
		
		int hoursOut = 0;
		if (hoursBetween.getHours() <= 0)
		{
			hoursOut = 1;
		}
		else
		{
			hoursOut = hoursBetween.getHours();
		}
		return hoursOut;
	}
}
