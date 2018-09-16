package io.github.elkin.commandline.exception;

@SuppressWarnings("serial")
public class CommandLineException extends RuntimeException  {
    public CommandLineException(String message) {
        super(message);
    }
    public CommandLineException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
