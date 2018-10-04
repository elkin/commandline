package io.github.elkin.commandline.exception;

@SuppressWarnings("serial")
public class UnhandledArgumentException extends CommandLineException {

  public UnhandledArgumentException(String message) {
    super(message);
  }
}
