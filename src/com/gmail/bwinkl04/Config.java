package com.gmail.bwinkl04;

import org.bukkit.configuration.file.FileConfiguration;

public class Config
{
	private XrayInformer plugin;
	private FileConfiguration config;
	
	public Config(XrayInformer plugin)
	{
		this.plugin = plugin;
	}
	
	public void load()
	{
		plugin.reloadConfig();
		config = plugin.getConfig();
				
		config.addDefault("default_world", "world");
		//config.addDefault("check_updates", true);
		
		config.addDefault("diamond", true);
		config.addDefault("gold", true);
		config.addDefault("lapis", true);
		config.addDefault("iron", true);
		config.addDefault("mossy", true);
		config.addDefault("emerald", true);
		
		
		config.addDefault("diamond_warn", 3.2);
		config.addDefault("diamond_confirmed", 3.8);
		
		config.addDefault("gold_warn", 8.0);
		config.addDefault("gold_confirmed", 10.0);
		
		config.addDefault("emerald_warn", 0.3);
		config.addDefault("emerald_confirmed", 0.5);
		
		config.addDefault("lapis_warn", 3.2);
		config.addDefault("lapis_confirmed", 3.8);
		
		config.addDefault("iron_warn", 40.0);
		config.addDefault("iron_confirmed", 100.0);
		
		config.addDefault("mossy_warn", 40.0);
		config.addDefault("mossy_confirmed", 100.0);		
		
		config.options().copyDefaults(true);
		plugin.saveConfig();
	}
	
	public boolean isActive(String ore)
	{
		return config.getBoolean(ore);
	}
	
	public double getRate(String type, String ore)
	{
		return config.getDouble(ore + "_" + type);
	}
	
	public String defaultWorld()
	{
		return config.getString("default_world");
	}
	
	/*public boolean checkUpdates()
	{
		return config.getBoolean("check_updates");
	}*/
}
