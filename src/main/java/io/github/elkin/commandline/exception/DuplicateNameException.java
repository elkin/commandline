package io.github.elkin.commandline.exception;

@SuppressWarnings("serial")
public class DuplicateNameException extends CommandLineException {
    public DuplicateNameException(String message)
    {
        super(message);
    }
}
