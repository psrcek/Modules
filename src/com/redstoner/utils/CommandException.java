package com.redstoner.utils;

public class CommandException extends Exception {
    
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CommandException(Throwable cause) {
        super(cause);
    }
    
    public CommandException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    public CommandException() {
    }
    
    public CommandException(String message) {
        super(message);
    }
}
