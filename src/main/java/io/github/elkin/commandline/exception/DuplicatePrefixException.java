package io.github.elkin.commandline.exception;

@SuppressWarnings("serial")
public class DuplicatePrefixException extends CommandLineException {

  public DuplicatePrefixException(String message) {
    super(message);
  }
}
