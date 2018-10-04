package io.github.elkin.commandline;

import io.github.elkin.commandline.exception.CheckException;
import io.github.elkin.commandline.exception.SizeViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class CommandLineParser {

  private final CommandLineConfiguration configuration;
  private final List<String> args;
  private final Map<String, List<String>> values;
  private final Set<String> flags;
  private final Iterator<Argument> argumentIterator;
  private final List<String> argumentRemainder;
  private Argument argument;

  CommandLineParser(CommandLineConfiguration configuration, String[] args) {
    this.configuration = configuration;
    this.args = preprocessArguments(args);
    values = new HashMap<>();
    flags = new HashSet<>();
    argumentRemainder = new ArrayList<>();
    argumentIterator = this.configuration.arguments().iterator();
  }

  private List<String> preprocessArguments(String[] args) {
    List<String> result = new ArrayList<>(args.length);
    for (String arg : args) {
      if (Util.isShortOption(arg) && arg.length() > Util.SHORT_OPTION_LENGTH) {
        String prefix = arg.substring(0, Util.SHORT_OPTION_LENGTH);
        result.add(prefix);

        String value = arg.substring(Util.SHORT_OPTION_LENGTH);
        if (configuration.hasFlagWithPrefix(prefix)) {
          for (int ch = 0; ch < value.length(); ++ch) {
            result.add("-" + value.charAt(ch));
          }
        } else {
          result.add(value);
        }

        continue;
      }

      if (Util.isLongOption(arg)) {
        int index = arg.indexOf('=');
        if (index >= 0) {
          result.add(arg.substring(0, index));

          if (index + 1 == arg.length()) {
            continue;
          }
          result.add(arg.substring(index + 1));

          continue;
        }
      }

      result.add(arg);
    }

    return result;
  }

  private boolean handleFlag(String prefix) {
    Flag flag = configuration.getFlagByPrefix(prefix);

    if (flag == null) {
      return false;
    }

    flags.add(flag.name());
    flag.set();
    return true;
  }

  private boolean handleOption(String prefix, String value) {
    Option option = configuration.getOptionByPrefix(prefix);
    if (option == null) {
      return false;
    }

    if (!option.checker().test(value)) {
      throw new CheckException(String.format(
          "Option <%s> can't have value <%s>",
          option.name(),
          value));
    }

    option.consumer().accept(value);
    List<String> vals = this.values.computeIfAbsent(
        option.name(),
        name -> new ArrayList<>());
    vals.add(value);

    return true;
  }

  private boolean handleArgument(String arg) {
    List<Argument> arguments = configuration.arguments();
    if (arguments.isEmpty()) {
      return false;
    }

    boolean isRemainder = true;
    if (argumentIterator.hasNext()) {
      argument = argumentIterator.next();
      isRemainder = false;
    }

    String argName = argument.name();
    if (!argument.checker().test(arg)) {
      throw new CheckException(String.format("Argument <%s> can't have value <%s>", argName, arg));
    }
    argument.consumer().accept(arg);
    List<String> vals = this.values.computeIfAbsent(argument.name(), name -> new ArrayList<>());
    vals.add(arg);

    if (isRemainder) {
      argumentRemainder.add(arg);
    } else {
      argument.setValue(arg);
    }

    return true;
  }

  private void checkSizeConstraints() {
    List<Argument> arguments = configuration.arguments();
    for (Argument arg : arguments) {
      List<String> vals = this.values.getOrDefault(
          arg.name(),
          Collections.emptyList());

      if (arg.isRequired() && vals.isEmpty()) {
        throw new SizeViolationException(
            String.format(
                "Argument <%s> is required, please provide value for it",
                arg.name()));
      }
    }

    if (!arguments.isEmpty()) {
      Argument lastArgument = arguments.get(arguments.size() - 1);
      int lastArgumentNumValues = values.getOrDefault(
          lastArgument.name(),
          Collections.emptyList()).size();

      if (lastArgumentNumValues > configuration.maxLastArgumentSize()) {
        throw new SizeViolationException(
            String.format(
                "%d is too many values(max number is %d) for the last argument <%s>",
                lastArgumentNumValues,
                configuration.maxLastArgumentSize(),
                lastArgument.name()));
      }
    }

    for (Option option : configuration.options()) {
      List<String> vals = values.getOrDefault(option.name(), Collections.emptyList());
      int size = vals.size();

      if (vals.isEmpty() && option.isRequired()) {
        throw new SizeViolationException(
            String.format(
                "Option <%s> is required, please provide value for it",
                option.name()));
      }

      if (vals.size() > option.maxNumberOfValues()) {
        throw new SizeViolationException(
            String.format(
                "%d is too many values(max number is %d) for option <%s>",
                size,
                option.maxNumberOfValues(),
                option.name()));
      }
    }

    for (Flag flag : configuration.flags()) {
      if (flag.isRequired() && !flag.isSet()) {
        throw new SizeViolationException(
            String.format("Flag <%s> is required", flag.name()));
      }

      if (flag.numberOfFlags() > flag.maxNumberOfValues()) {
        throw new SizeViolationException(
            String.format(
                "There're too many flags <%s> - %d, max number of values is %d",
                flag.name(),
                flag.numberOfFlags(),
                flag.maxNumberOfValues()));
      }
    }
  }

  private void addDefaultValues() {
    for (Option option : configuration.options()) {
      List<String> defaultValues = option.defaultValues();
      if (defaultValues.isEmpty()) {
        continue;
      }

      if (values.containsKey(option.name())) {
        continue;
      }

      String name = option.name();
      values.put(name, defaultValues);
      option.setValues(new ValuesImpl(defaultValues));
    }

    while (argumentIterator.hasNext()) {
      argument = argumentIterator.next();
      List<String> defaultValues = argument.defaultValues();

      if (!defaultValues.isEmpty() && !values.containsKey(argument.name())) {
        values.put(argument.name(), defaultValues);
        if (defaultValues.size() == 1) {
          argument.setValue(defaultValues.get(0));
        } else {
          argument.setValue(defaultValues.get(0));
          argument.setRemainder(new ValuesImpl(defaultValues.subList(1, defaultValues.size())));
        }
      }
    }
  }

  CommandLine getCommandLine() {
    parse();

    Map<String, Values> commandLineValues = new HashMap<>();
    values.forEach((name, v) -> commandLineValues.put(name, new ValuesImpl(v)));

    Set<String> allNames = configuration.arguments()
        .stream()
        .map(Argument::name)
        .collect(Collectors.toSet());

    allNames.addAll(configuration.options()
        .stream()
        .map(Option::name)
        .collect(Collectors.toSet()));

    Set<String> flagNames = configuration.flags().stream()
        .map(Flag::name).collect(Collectors.toSet());

    return new CommandLine(
        allNames,
        flagNames,
        commandLineValues,
        flags);
  }

  void parse() {
    CommandLineIterator commandLineIterator = new CommandLineIterator(
        this::handleArgument,
        configuration::isPrefixRegistered,
        this::handleOption,
        this::handleFlag);

    commandLineIterator.iterate(args);

    configuration.options().forEach(option ->
        option.setValues(
            new ValuesImpl(
                values.getOrDefault(
                    option.name(),
                    Collections.emptyList())))
    );

    if (!argumentRemainder.isEmpty()) {
      argument.setRemainder(new ValuesImpl(argumentRemainder));
    }

    addDefaultValues();
    checkSizeConstraints();

    configuration.checkers().forEach(checker ->
        checker.validate(
            configuration.arguments(),
            configuration.options(),
            configuration.flags()));
  }
}
