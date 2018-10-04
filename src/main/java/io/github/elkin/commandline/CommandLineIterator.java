package io.github.elkin.commandline;

import io.github.elkin.commandline.exception.NoValueException;
import io.github.elkin.commandline.exception.UnhandledArgumentException;
import io.github.elkin.commandline.exception.UnknownPrefixException;
import java.util.Iterator;
import java.util.List;

class CommandLineIterator {

  private final ArgumentHandler argumentHandler;
  private final PrefixChecker prefixChecker;
  private final OptionHandler optionHandler;
  private final FlagHandler flagHandler;

  CommandLineIterator(ArgumentHandler argumentHandler,
      PrefixChecker prefixChecker,
      OptionHandler optionHandler,
      FlagHandler flagHandler) {
    this.argumentHandler = argumentHandler;
    this.prefixChecker = prefixChecker;
    this.optionHandler = optionHandler;
    this.flagHandler = flagHandler;
  }

  void iterate(List<String> args) {
    for (Iterator<String> iter = args.iterator(); iter.hasNext(); ) {
      String arg = iter.next();

      if (Util.isOption(arg)) {
        if (!prefixChecker.isPrefixedRegistered(arg)) {
          throw new UnknownPrefixException("Unknown prefix " + arg);
        }

        if (flagHandler.handle(arg)) {
          continue;
        }

        if (!iter.hasNext()) {
          throw new NoValueException(
              String.format("No value provided for option with prefix <%s>", arg));
        }

        String optionValue = iter.next();
        if (Util.isOption(optionValue)) {
          throw new NoValueException(
              String.format("No value provided for option with prefix <%s>", arg));
        }

        if (optionHandler.handle(arg, optionValue)) {
          continue;
        }

        throw new UnhandledArgumentException(String.format("Unhandled argument <%s>", arg));
      }

      if (!argumentHandler.handle(arg)) {
        throw new UnhandledArgumentException(String.format("Unhandled argument <%s>", arg));
      }
    }
  }

  @FunctionalInterface
  interface ArgumentHandler {

    boolean handle(String argument);
  }

  @FunctionalInterface
  interface PrefixChecker {

    boolean isPrefixedRegistered(String prefix);
  }

  @FunctionalInterface
  interface OptionHandler {

    boolean handle(String prefix, String value);
  }

  @FunctionalInterface
  interface FlagHandler {

    boolean handle(String prefix);
  }
}
