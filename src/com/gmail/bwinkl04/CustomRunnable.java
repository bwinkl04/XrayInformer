package com.gmail.bwinkl04;

import org.bukkit.command.CommandSender;

public class CustomRunnable implements Runnable
{
	CommandSender sender;
	String world;
	int oreid;
	String bantype;
	float maxrate;
	boolean banned;
	int hours;

	public CustomRunnable(CommandSender sender, String world, int oreid, String bantype, float maxrate, boolean banned, int hours)
	{
		this.sender = sender;
		this.hours = hours;
		this.world = world;
		this.oreid = oreid;
		this.bantype = bantype;
		this.maxrate = maxrate;
		this.banned = banned;
	}
	@Override
	public void run()
	{
		// TODO Auto-generated method stub
	}
}