package io.github.elkin.commandline;

import io.github.elkin.commandline.exception.CommandLineException;

@FunctionalInterface
public interface ExceptionHandler {

  void handleException(CommandLineException e,
      CommandLineConfiguration configuration,
      String[] args);
}
