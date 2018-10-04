package io.github.elkin.commandline.exception;

@SuppressWarnings("serial")
public class SizeViolationException extends CommandLineException {

  public SizeViolationException(String message) {
    super(message);
  }
}
