package com.redstoner.utils;

public class CommandException extends Exception
{
	private static final long serialVersionUID = -7176634557736106754L;
	
	public CommandException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public CommandException(Throwable cause)
	{
		super(cause);
	}
	
	public CommandException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public CommandException()
	{}
	
	public CommandException(String message)
	{
		super(message);
	}
}
