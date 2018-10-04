package io.github.elkin.commandline;

import io.github.elkin.commandline.exception.DuplicateNameException;
import io.github.elkin.commandline.exception.DuplicatePrefixException;
import io.github.elkin.commandline.exception.ValidationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

public class CommandLineConfiguration {

  private final HelpGenerator helpGenerator;
  private final List<Argument> arguments;
  private final List<Option> options;
  private final List<Flag> flags;
  private final SortedSet<String> prefixes;
  private final Map<String, Option> optionByPrefix;
  private final Map<String, Flag> flagByPrefix;
  private final Map<String, Type> names;
  private final OptionPrefixHandler optionPrefixHandler;
  private final FlagPrefixHandler flagPrefixHandler;
  private final List<Validator> checkers;
  private String description;
  private int position;
  private int maxLastArgumentSize;

  public CommandLineConfiguration(HelpGenerator helpGenerator) {
    description = "";
    this.helpGenerator = Objects.requireNonNull(helpGenerator);
    arguments = new ArrayList<>();
    options = new ArrayList<>();
    flags = new ArrayList<>();
    prefixes = new TreeSet<>();
    optionByPrefix = new HashMap<>();
    flagByPrefix = new HashMap<>();
    names = new HashMap<>();
    position = 0;
    maxLastArgumentSize = Integer.MAX_VALUE;
    optionPrefixHandler = this::checkOptionPrefix;
    flagPrefixHandler = this::checkFlagPrefix;
    checkers = new ArrayList<>();

    addFlag("help", "-h")
        .addPrefix("--help")
        .setDescription("print help information and exit");
  }
  public CommandLineConfiguration() {
    this(new DefaultHelpGenerator());
  }

  private void checkNameDuplicates(String name, Type nameType) {
    Type type = names.get(name);
    if (type != null) {
      String errorDescription = null;
      switch (type) {
        case ARGUMENT:
          errorDescription = "configuration already has agrument <%s>";
          break;

        case OPTION:
          errorDescription = "configuration already has option <%s>";
          break;

        case FLAG:
          errorDescription = "configuration already has flag <%s>";
          break;

        default:
          assert false;
      }
      throw new DuplicateNameException(String.format(errorDescription, name));
    }

    names.put(name, nameType);
  }

  private void checkPrefix(String prefix) {
    // check if prefix has already been registered
    if (!prefixes.add(prefix)) {
      throw new DuplicatePrefixException(
          String.format("configuration already has <%s> option/flag with prefix",
              prefix));
    }

    // check if a long option is a prefix of another long option
    if (Util.isLongOption(prefix)) {
      String lastOptionPrefix = " ";
      for (String p : prefixes) {
        if (p.startsWith(lastOptionPrefix)) {
          throw new DuplicatePrefixException(
              String.format("Option %s is a prefix of option %s", lastOptionPrefix, p));
        }
        lastOptionPrefix = p;
      }
    }
  }

  private void checkOptionPrefix(String optionPrefix, Option option) {
    checkPrefix(optionPrefix);
    optionByPrefix.put(optionPrefix, option);
  }

  private void checkFlagPrefix(String flagPrefix, Flag flag) {
    checkPrefix(flagPrefix);
    flagByPrefix.put(flagPrefix, flag);
  }

  private void checkIfLastArgumentHasFewDefaultValues() {
    if (!arguments.isEmpty() && arguments.get(position - 1).defaultValues().size() > 1) {
      throw new ValidationException("Only the last argument can have more than one default value");
    }
  }

  Option getOptionByPrefix(String prefix) {
    return optionByPrefix.get(prefix);
  }

  Flag getFlagByPrefix(String prefix) {
    return flagByPrefix.get(prefix);
  }

  boolean hasFlagWithPrefix(String prefix) {
    return flagByPrefix.containsKey(prefix);
  }

  boolean isPrefixRegistered(String prefix) {
    return optionByPrefix.containsKey(prefix)
        || flagByPrefix.containsKey(prefix);
  }

  public String description() {
    return description;
  }

  public CommandLineConfiguration setDescription(String description) {
    this.description = Objects.requireNonNull(description);
    return this;
  }

  public OptionalArgument addOptionalArgument(String name) {
    Util.checkName(name);
    checkNameDuplicates(name, Type.ARGUMENT);

    checkIfLastArgumentHasFewDefaultValues();

    OptionalArgument argument = new OptionalArgument(name, position++);
    arguments.add(argument);
    return argument;
  }

  public RequiredArgument addRequiredArgument(String name) {
    Util.checkName(name);
    checkNameDuplicates(name, Type.ARGUMENT);

    if (!arguments.isEmpty() && !arguments.get(position - 1).isRequired()) {
      throw new ValidationException(
          "It is not allowed to add required argument after an optional argument");
    }

    checkIfLastArgumentHasFewDefaultValues();

    RequiredArgument argument = new RequiredArgument(name, position++);
    arguments.add(argument);
    return argument;
  }

  public Option addOption(String name, String prefix) {
    Util.checkName(name);
    Util.checkPrefix(prefix);

    checkNameDuplicates(name, Type.OPTION);

    Option option = new Option(name, prefix, optionPrefixHandler);
    optionPrefixHandler.handle(prefix, option);
    options.add(option);
    return option;
  }

  public Flag addFlag(String name, String prefix) {
    Util.checkName(name);
    Util.checkPrefix(prefix);

    checkNameDuplicates(name, Type.FLAG);

    Flag flag = new Flag(name, prefix, flagPrefixHandler);
    flagPrefixHandler.handle(prefix, flag);
    flags.add(flag);
    return flag;
  }

  public int maxLastArgumentSize() {
    return maxLastArgumentSize;
  }

  public CommandLineConfiguration setMaxLastArgumentSize(int maxLastArgumentSize) {
    if (maxLastArgumentSize <= 0) {
      throw new IllegalArgumentException("Max last argument size musn't be less than 1");
    }
    this.maxLastArgumentSize = maxLastArgumentSize;
    return this;
  }

  public void addValidator(Validator checker) {
    checkers.add(Objects.requireNonNull(checker));
  }

  public List<Validator> checkers() {
    return Collections.unmodifiableList(checkers);
  }

  public List<Argument> arguments() {
    return Collections.unmodifiableList(arguments);
  }

  public List<Option> options() {
    return Collections.unmodifiableList(options);
  }

  public List<Flag> flags() {
    return Collections.unmodifiableList(flags);
  }

  @Override
  public String toString() {
    return helpGenerator.generateHelp(this);
  }

  private enum Type {
    ARGUMENT,
    OPTION,
    FLAG
  }

  @FunctionalInterface
  interface OptionPrefixHandler {

    void handle(String prefix, Option option);
  }

  @FunctionalInterface
  interface FlagPrefixHandler {

    void handle(String prefix, Flag flag);
  }
}
