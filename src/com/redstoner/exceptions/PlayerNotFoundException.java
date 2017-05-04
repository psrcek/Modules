package com.redstoner.exceptions;

public class PlayerNotFoundException extends Exception
{
	private static final long serialVersionUID = -7517266613348837760L;
	
	public PlayerNotFoundException()
	{
		super("That player could not be found!");
	}
}
