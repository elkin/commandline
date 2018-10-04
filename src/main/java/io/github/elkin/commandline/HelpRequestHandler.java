package io.github.elkin.commandline;

@FunctionalInterface
public interface HelpRequestHandler {

  void handleHelpRequest(CommandLineConfiguration commandLineConfiguration, String[] args);
}
