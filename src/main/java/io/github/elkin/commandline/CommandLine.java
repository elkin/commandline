package io.github.elkin.commandline;


import io.github.elkin.commandline.exception.CommandLineException;
import io.github.elkin.commandline.exception.UnknownNameException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class CommandLine {

  private static final HelpRequestHandler HELP_REQUEST_HANDLER =
      Util.makeHelpRequestHandler(0, System.out);

  private static final ExceptionHandler EXCEPTION_HANDLER =
      Util.makeExceptionHandler(1, System.out);

  private final Set<String> names;
  private final Set<String> flagNames;
  private final Map<String, Values> values;
  private final Set<String> flags;

  CommandLine(Set<String> names,
      Set<String> flagNames,
      Map<String, Values> values,
      Set<String> flags) {
    this.names = names;
    this.flagNames = flagNames;
    this.values = values;
    this.flags = flags;
  }

  private static boolean isHelpInfoNeeded(String[] args) {
    for (String arg : args) {
      if (arg.equals("-h") || arg.equals("--help")) {
        return true;
      }
    }

    return false;
  }

  public static CommandLine getCommandLine(CommandLineConfiguration commandLineConfiguration,
      String[] args) {
    return getCommandLine(
        commandLineConfiguration,
        args,
        HELP_REQUEST_HANDLER,
        EXCEPTION_HANDLER);
  }

  public static CommandLine getCommandLine(CommandLineConfiguration commandLineConfiguration,
      String[] args,
      HelpRequestHandler helpRequestHandler) {
    return getCommandLine(
        commandLineConfiguration,
        args,
        helpRequestHandler,
        EXCEPTION_HANDLER);
  }

  public static CommandLine getCommandLine(CommandLineConfiguration commandLineConfiguration,
      String[] args,
      ExceptionHandler exceptionHandler) {
    return getCommandLine(
        commandLineConfiguration,
        args,
        HELP_REQUEST_HANDLER,
        exceptionHandler);
  }

  public static CommandLine getCommandLine(CommandLineConfiguration commandLineConfiguration,
      String[] args,
      HelpRequestHandler helpRequestHandler,
      ExceptionHandler exceptionHandler) {
    Objects.requireNonNull(commandLineConfiguration);
    Objects.requireNonNull(args);
    Objects.requireNonNull(helpRequestHandler);
    Objects.requireNonNull(exceptionHandler);

    if (isHelpInfoNeeded(args)) {
      helpRequestHandler.handleHelpRequest(commandLineConfiguration, args);
    }

    try {
      CommandLineParser parser = new CommandLineParser(commandLineConfiguration, args);
      return parser.getCommandLine();
    } catch (CommandLineException e) {
      exceptionHandler.handleException(e, commandLineConfiguration, args);
    }

    assert false;
    // it's unreachable but javac doesn't believe me so let's trick him
    return null;
  }

  public static void parse(CommandLineConfiguration commandLineConfiguration,
      String[] args) {
    parse(commandLineConfiguration, args, HELP_REQUEST_HANDLER, EXCEPTION_HANDLER);
  }

  public static void parse(CommandLineConfiguration commandLineConfiguration,
      String[] args,
      HelpRequestHandler helpRequestHandler) {
    parse(commandLineConfiguration, args, helpRequestHandler, EXCEPTION_HANDLER);
  }

  public static void parse(CommandLineConfiguration commandLineConfiguration,
      String[] args,
      ExceptionHandler exceptionHandler) {
    parse(commandLineConfiguration, args, HELP_REQUEST_HANDLER, exceptionHandler);
  }

  public static void parse(CommandLineConfiguration commandLineConfiguration,
      String[] args,
      HelpRequestHandler helpRequestHandler,
      ExceptionHandler exceptionHandler) {
    Objects.requireNonNull(commandLineConfiguration);
    Objects.requireNonNull(args);
    Objects.requireNonNull(helpRequestHandler);
    Objects.requireNonNull(exceptionHandler);

    if (isHelpInfoNeeded(args)) {
      helpRequestHandler.handleHelpRequest(commandLineConfiguration, args);
    }

    try {
      CommandLineParser parser = new CommandLineParser(commandLineConfiguration, args);
      parser.parse();
    } catch (CommandLineException e) {
      exceptionHandler.handleException(e, commandLineConfiguration, args);
    }
  }

  public Set<String> names() {
    return Collections.unmodifiableSet(names);
  }

  public Set<String> flags() {
    return Collections.unmodifiableSet(flagNames);
  }

  public boolean isFlagSet(String name) {
    Util.checkName(name);

    if (!flagNames.contains(name)) {
      throw new UnknownNameException(String.format("Unknown flag <%s>", name));
    }
    return flags.contains(name);
  }

  public Values get(String name) {
    Util.checkName(name);

    if (!names.contains(name)) {
      throw new UnknownNameException(String.format("Unknown name <%s>", name));
    }

    Values result = this.values.getOrDefault(name, ValuesImpl.empty());
    assert result != null;
    return result;
  }
}
