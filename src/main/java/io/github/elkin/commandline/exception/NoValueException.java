package io.github.elkin.commandline.exception;

@SuppressWarnings("serial")
public class NoValueException extends CommandLineException {

  public NoValueException(String message) {
    super(message);
  }
}
