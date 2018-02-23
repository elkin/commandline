package io.github.elkin.commandline.exception;

public class UnhandledArgumentException extends CommandLineException {
    public UnhandledArgumentException(String message)
    {
        super(message);
    }
}
