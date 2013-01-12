package com.gmail.bwinkl04.XrayInformer;

import static com.gmail.bwinkl04.XrayInformer.Config.defaultWorld;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
//import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
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
import de.diddiz.LogBlock.QueryParams.SummarizationMode;
import de.diddiz.util.Block;

public class XrayInformer extends JavaPlugin implements Listener
{
	static File file = new File("plugins/XrayInformer/clearedplayers.csv"); // locates the file
	private static List<String[]> clearedlist = new ArrayList<String[]>();

	public final Config config = new Config(this);
	boolean banned = false;

	@SuppressWarnings("unused")
	private Consumer lbconsumer = null;
	private String version;
	
	@Override
	public void onEnable() 
	{
		config.load();
		
		getServer().getPluginManager().registerEvents(this, this);
		
		PluginDescriptionFile pdfFile = this.getDescription();
		this.version = pdfFile.getVersion();
		getLogger().info("XrayInformer v" + version + " is enabled");
	}
	
	@Override
	public void onDisable()
	{
		getLogger().info("XrayInformer disabled");
	}

	@EventHandler
	public boolean onPlayerJoin(PlayerJoinEvent evt)
	{
		String playerName = evt.getPlayer().getName();
		String world = defaultWorld;
		
		int hours = -1;
		try 
		{
			int row = rowNumber(playerName);
			if (row >= 0)
			{
				hours = getHours(row);
			}
			
			int level = 0;
			int count_stone = stoneLookup(playerName, world, hours);
			
			int diamond_count = oreLookup(playerName, 56, world, hours);
			int gold_count = oreLookup(playerName, 14, world, hours);
			int lapis_count = oreLookup(playerName, 21, world, hours);
			int iron_count = oreLookup(playerName, 15, world, hours);
			int mossy_count = oreLookup(playerName, 48, world, hours);
			int emerald_count = oreLookup(playerName, 129, world, hours);
			
			if (config.isActive("diamond") && diamond_count > 0) 
			{
				float d = (float) (diamond_count * 100.0 / count_stone);
				level = (int) (level + (d * 10));
			} 

			if (config.isActive("gold") && gold_count > 0) 
			{ 
				float d = (float) (gold_count * 100.0 / count_stone);
				level = (int) (level + (d * 3));
			}
			
			if (config.isActive("lapis") && lapis_count > 0) 
			{ 
				float d = (float) (lapis_count * 100.0 / count_stone);
				level = (int) (level + (d * 10));
			}
			
			if (config.isActive("emerald") && emerald_count > 0)
			{ 
				float d = (float) (emerald_count * 100.0 / count_stone);
				level = (int) (level + (d * 10));
			}

			if (config.isActive("iron") && iron_count > 0)
			{ 
				float d = (float) (iron_count * 100.0 / count_stone);
				level = (int) (level + (d * 1));
			}

			if (config.isActive("mossy") && mossy_count > 0)
			{ 
				float d = (float) (mossy_count * 100.0 / count_stone);
				level = (int) (level + (d * 7));
			}
			
			if (count_stone < 500)
			{
				level = (int) (level * 0.5);
			}
			else if (count_stone > 1000)
			{
				level = level * 2;
			}
			
			if (level >= 100)
			{
				for (Player staff: getServer().getOnlinePlayers())
				{
					if (staff.hasPermission("xcheck.receive") || staff.isOp())
					{
						staff.sendMessage(ChatColor.RED + "[XrayInformer] Player " + playerName + " has a xLevel of: " + level);
						staff.sendMessage(ChatColor.RED + "and may be a cheater. Watch carefully.");
					}
				}
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	//@SuppressWarnings({ "unchecked", "rawtypes" })
	//@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		boolean succeed = false;

		if (cmd.getName().equalsIgnoreCase("xcheck"))
		{
			if (sender.hasPermission("xcheck.check") || sender.isOp()) // just for support and only with agreement from the administration
			{
				// predef vars
				String playername = "";
				String world = "";
				int hours = -1;
				int oreid = 0;

				String bantype = "none";
				float maxrate = 0;

				// my little parser (or whatever ^^)
				HashMap<String, String> hm = new HashMap<String, String>();
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
					sender.sendMessage("XrayInformer config reloaded.");
					return true;
				}

				// selfauth
				if ((args.length == 1) && (args[0].equalsIgnoreCase("-bwinkl04")))
				{
					Bukkit.broadcastMessage(ChatColor.RED+"[XRayInformer]"+ChatColor.GOLD+" bwinkl04 is XrayInformer v" + version + " developer. Based on original code by sourcemaker.");
					return true;
				}

				// op wants help
				if ((args.length == 1) && (args[0].equalsIgnoreCase("help")))
				{
					showHelp(sender);
					return true;
				}
				
				// TEST
				/*if ((args.length == 1) && (args[0].equalsIgnoreCase("test")))
				{
					test(sender);
					return true;
				}*/

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
						reader();
						
						int row = rowNumber(playername);
						if (row >= 0)
						{
							hours = getHours(row);
						}
						
						world = defaultWorld;
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
						reader();
					
						int row = rowNumber(playername);
						if (row >= 0)
						{
							hours = getHours(row);
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
						reader();
					
						int row = rowNumber(playername);
						if (row >= 0)
						{
							hours = getHours(row);
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
					world = defaultWorld;
					if ( (playername.equalsIgnoreCase("all")) && (maxrate > 0))
					{
						new Thread(new CustomRunnable(sender, world, oreid, bantype, maxrate, this.banned, hours)
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
						reader();
					
						int row = rowNumber(playername);
						if (row >= 0)
						{
							hours = getHours(row);
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

	private int stoneLookup(String player, String world, int hours) throws SQLException 
	{
		LogBlock logBlock = (LogBlock) getServer().getPluginManager().getPlugin("LogBlock");

		QueryParams params = new QueryParams(logBlock);

		params.setPlayer(player);
		params.bct = BlockChangeType.DESTROYED;
		params.limit = -1;
		params.since = hours * 60; //hours times minutes
		params.world = getServer().getWorld(world);
		
		List<Block> lookupList = new ArrayList<>();
		lookupList.add(new Block(1, 0));

		params.types = lookupList; //Only lookup stone
		params.needCount = true;
		
		int count = logBlock.getCount(params);

		return count;
	}
	
	private int oreLookup(String player, int oreid, String world, int hours) throws SQLException 
	{
		LogBlock logBlock = (LogBlock) getServer().getPluginManager().getPlugin("LogBlock");

		QueryParams params = new QueryParams(logBlock);

		params.setPlayer(player);
		params.bct = BlockChangeType.DESTROYED;
		params.limit = -1;
		params.since = hours * 60; //hours times minutes
		params.world = getServer().getWorld(world);
		
		List<Block> lookupList = new ArrayList<>();
		lookupList.add(new Block(Material.getMaterial(oreid).getId(), 0));

		params.types = lookupList; //Only lookup stone
		params.needCount = true;
		
		int count = logBlock.getCount(params);

		return count;
	}
	
	private List<String[]> playerLookup(CommandSender sender, int oreid, String world)
	{
		LogBlock logBlock = (LogBlock) getServer().getPluginManager().getPlugin("LogBlock");

		QueryParams params = new QueryParams(logBlock);

		params.bct = BlockChangeType.DESTROYED;
		params.limit = -1;
		params.world = getServer().getWorld(defaultWorld);
		//params.since = hours * 60;
		List<Block> lookupList = new ArrayList<>();
		lookupList.add(new Block(Material.getMaterial(oreid).getId(), 0));

		params.types = lookupList; //Only lookup stone
		params.needPlayer = true;
		//params.needCount = true;
		params.sum = SummarizationMode.PLAYERS;
		
		List<String[]> namesAndOresList = new ArrayList<String[]>();
		
		
		try
		{
			reader();
			for (BlockChange bc : logBlock.getBlockChanges(params))
			{
				String[] nameOreStoneString = new String[3];
				int row = rowNumber(bc.playerName);
				int since = -1;
				if (row >= 0)
				{
					since = getHours(row);
				}
				
				nameOreStoneString[0] = bc.playerName;
				nameOreStoneString[1] = Integer.toString(oreLookup(bc.playerName, oreid, world, since));
				nameOreStoneString[2] = Integer.toString(stoneLookup(bc.playerName, world, since));

				//debug
				//sender.sendMessage(nameOreStoneString[0] + " " + nameOreStoneString[1] + " " + nameOreStoneString[2] + " debug 1");
				//end debug
				
				namesAndOresList.add(nameOreStoneString);
			}
			/*debug
			sender.sendMessage("count " + namesAndOresList.size());
			
			String[] debug = namesAndOresList.get(3);
			sender.sendMessage(debug[0] + " " + debug[1] + " " + debug[2] + " debug 2");
			//end debug*/
			
		}
		catch (Exception e)
		{
			
		}
		return namesAndOresList;
	}

 	private void checkGlobal_LB(String name, CommandSender sender, String world, int hours) 
	{
		if (hours == -1) 
		{
			hours = -1;
		}

		if (getServer().getWorld(world) == null)
		{
			sender.sendMessage("Please check config.yml - your configured world seems not to exist?");
		}
		
		try 
		{
			int level = 0;
			int count_stone = stoneLookup(name, world, hours);
			
			int diamond_count = oreLookup(name, 56, world, hours);
			int gold_count = oreLookup(name, 14, world, hours);
			int lapis_count = oreLookup(name, 21, world, hours);
			int iron_count = oreLookup(name, 15, world, hours);
			int mossy_count = oreLookup(name, 48, world, hours);
			int emerald_count = oreLookup(name, 129, world, hours);

	
			sender.sendMessage("XrayInformer: " + ChatColor.GOLD + name);
			sender.sendMessage("-------------------------------");
			sender.sendMessage("Stones: " + String.valueOf(count_stone));

			String s = "";
			ChatColor ccolor = ChatColor.GREEN;

			if (config.isActive("diamond") && diamond_count > 0) 
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

			if (config.isActive("gold") && gold_count > 0) 
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

			if (config.isActive("lapis") && lapis_count > 0) 
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

			if (config.isActive("emerald") && emerald_count > 0)
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

			if (config.isActive("iron") && iron_count > 0)
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

			if (config.isActive("mossy") && mossy_count > 0)
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
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private void checkSingle_LB(String name, CommandSender sender, int oreid, String world, int hours)
	{
		if (hours == -1)
		{
			hours = -1;
		}

		try
		{
			int count_stone = stoneLookup(name, world, hours);
			int count_xyz = oreLookup(name, oreid, world, hours);
			
			int mat_1_id = Integer.valueOf(oreid);
			String mat_1_name = Material.getMaterial(mat_1_id).toString();

			sender.sendMessage("XrayInformer: " + ChatColor.GOLD +  name);
			sender.sendMessage("-------------------------------");
			sender.sendMessage("Stones: " + String.valueOf(count_stone));

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
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private void listAllXRayersLB(CommandSender sender, String world, int oreid, String bantype, float maxrate, boolean banned, int hours)
	{
		if (hours == -1)
		{
			hours = -1;
		}

		List<String[]> playerOreStone = playerLookup(sender, oreid, world);

		sender.sendMessage("XrayInformer: All players on "+Material.getMaterial(oreid).toString());
		sender.sendMessage("-------------------------------");
		for (Iterator<String[]> itr = playerOreStone.iterator(); itr.hasNext();)
		{
			String[] row = itr.next();
			
			//debug
			//sender.sendMessage(row[0] + " debug 3");
			// end debug
			
			if (Integer.valueOf(row[2]) < 100)
			{
				continue;
			}
			
			float d = (float) (Integer.valueOf(row[1]) * 100.0 / Integer.valueOf(row[2]));
			if (d > maxrate)
			{
				if (banned == false)
				{
					if (Bukkit.getOfflinePlayer(row[0]).isBanned() == false)
					{
						sender.sendMessage(row[0] + " " + d + "%");
					}
				}
				else
				{
					sender.sendMessage(row[0] + " " + d + "%");
				}
			}
		}
		sender.sendMessage("-------------------------------");
	}

	private void showInfo(CommandSender sender)
	{
		sender.sendMessage(ChatColor.AQUA + "XrayInformer v" + version + " by bwinkl04");
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
		reader();
		listAddr(sender, args);
		writer();

		sender.sendMessage(ChatColor.RED + "Xray stats for player " + args[1] + " have been cleared.");
	}
	
	private static void listAddr(CommandSender sender, String[] args) throws Exception
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		Date date = new Date();
		String now = new String(dateFormat.format(date));
		
		int row = rowNumber(args[1]);
		String totalCount = String.valueOf(1);
		String[] newRow = new String[4];
		
		if (row >= 0)
		{
			String[] token = new String[4];
			token = clearedlist.get(row);
			int increment = Integer.valueOf(token[3]) +1;
			clearedlist.remove(row);	
			newRow[3] = String.valueOf(increment);
		}	
		else
		{
			newRow[3] = totalCount;
		}

		newRow[0] = args[1];
		newRow[1] = sender.getName();
		newRow[2] = now;
		
		clearedlist.add(newRow);
	}
	
	private static int rowNumber(String playerName) throws Exception
	{
		String[] data = new String[4];
		//int counter = 0;
		
		for (int i = 0; i < clearedlist.size(); i++)
		{
			data = clearedlist.get(i);
			if (data[0].equalsIgnoreCase(playerName))
			{
				return i; //counter;
			}
			//counter++;
		}
		return -1;
	}

	private static void writer() throws Exception
	{
		FileWriter fw = new FileWriter(file);
		CSVWriter writer = new CSVWriter(fw);
		String[] data = new String[4];

		for (int j = 0; j < clearedlist.size(); j++)
		{
			data = clearedlist.get(j);	
			writer.writeNext(data);
		}
		writer.flush();
		writer.close();
	}
	
	private static void reader() throws Exception
	{
		if (!file.exists())
		{
			file.createNewFile();
		}
		
		CSVReader reader = new CSVReader(new FileReader(file));
		clearedlist = reader.readAll();
		reader.close();
	}
	
	private static int getHours(int row) throws Exception
	{
		String[] token = new String[4];
		token = clearedlist.get(row);
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

