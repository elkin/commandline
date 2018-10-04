package io.github.elkin.commandline.exception;

@SuppressWarnings("serial")
public class UnknownPrefixException extends CommandLineException {

  public UnknownPrefixException(String message) {
    super(message);
  }
}
