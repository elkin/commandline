package io.github.elkin.commandline.exception;

@SuppressWarnings("serial")
public class UnknownNameException extends CommandLineException {

  public UnknownNameException(String message) {
    super(message);
  }
}
