package io.github.elkin.commandline.exception;

@SuppressWarnings("serial")
public class ValidationException extends CommandLineException {

  public ValidationException(String message) {
    super(message);
  }
}
