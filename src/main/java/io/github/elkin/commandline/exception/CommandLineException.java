package io.github.elkin.commandline.exception;

@SuppressWarnings("serial")
public class CommandLineException extends RuntimeException {

  CommandLineException(String message) {
    super(message);
  }

  CommandLineException(String message, Throwable cause) {
    super(message, cause);
  }
}
