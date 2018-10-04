package io.github.elkin.commandline;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

class DefaultHelpGenerator implements HelpGenerator {

  private static final int MAX_COLUMN_WIDTH = 80;
  private static final String DEFAULT_USAGE_LINE = "java -cp ${CLASSPATH} ${MAIN_CLASS} [OPTIONS] ";

  private final String usageLine;

  DefaultHelpGenerator(String usageLine) {
    assert usageLine != null;

    this.usageLine = usageLine.isEmpty() ? DEFAULT_USAGE_LINE : usageLine;
  }

  DefaultHelpGenerator() {
    this("");
  }

  private static void generateArgumentsCommandLine(
      StringBuilder builder,
      CommandLineConfiguration configuration) {
    List<Argument> arguments = configuration.arguments();
    for (int i = 0; i < arguments.size() - 1; ++i) {
      Argument argument = arguments.get(i);
      String argumentName = argument.name().toUpperCase();
      if (argument.isRequired()) {
        builder.append(argumentName).append(' ');
      } else {
        builder.append('[').append(argumentName).append("] ");
      }
    }

    if (!arguments.isEmpty()) {
      Argument lastArgument = arguments.get(arguments.size() - 1);
      String argumentName = lastArgument.name();
      if (lastArgument.isRequired()) {
        builder.append(argumentName).append(' ');
      } else {
        builder.append('[').append(argumentName).append("...] ");
      }

      if (configuration.maxLastArgumentSize() > 1) {
        builder.append(" [").append(argumentName).append("...]");
      }
    }
  }

  private static void generateArgumentsDescription(
      StringBuilder builder,
      CommandLineConfiguration configuration) {
    List<Argument> arguments = configuration.arguments();
    if (arguments.isEmpty()) {
      return;
    }

    builder.append("Positional arguments:\n");

    Optional<Argument> longestNameArgument = arguments.stream()
        .max(Comparator.comparing(arg -> arg.name().length()));
    assert longestNameArgument.isPresent();

    // 2 spaces + longest name length + 2 spaces
    int maxNameLength = longestNameArgument.get().name().length();
    String formatString = "  %-" + maxNameLength + "s  %s";

    for (Argument argument : arguments) {
      builder.append(String.format(formatString, argument.name(), argument.description()));

      if (argument.defaultValues().isEmpty()) {
        builder.append("\n");
        continue;
      }

      builder.append(" (default: ");
      builder.append(String.join(", ", argument.defaultValues()));
      builder.append(")\n");
    }
  }

  private static int getMaxColumnWidth(CommandLineConfiguration configuration) {
    List<Option> options = configuration.options();
    Optional<Integer> maxLengthOption = options.stream()
        .map(o -> DefaultHelpGenerator.getAllPrefixes(o).length())
        .max(Comparator.naturalOrder());
    int maxLength = maxLengthOption.orElse(0);

    List<Flag> flags = configuration.flags();

    Optional<Integer> longestFlag = flags.stream()
        .map(f -> DefaultHelpGenerator.getAllPrefixes(f).length())
        .max(Comparator.naturalOrder());

    return longestFlag.map(flagLength -> Math.max(maxLength, flagLength)).orElse(maxLength);
  }

  private static void generateOptionsDescription(StringBuilder builder,
      CommandLineConfiguration configuration) {
    List<Option> options = configuration.options();
    List<Flag> flags = configuration.flags();

    if (options.isEmpty() && flags.isEmpty()) {
      return;
    }

    builder.append("Prefixed arguments:\n");

    int columnWidth = getMaxColumnWidth(configuration);
    String formatString;
    if (columnWidth <= MAX_COLUMN_WIDTH) {
      formatString = "    %-" + columnWidth + "s  %s";
    } else {
      formatString = "    %s  %s\n";
    }

    Consumer<Option> optionConsumer = option -> {
      builder.append(
          String.format(
              formatString,
              DefaultHelpGenerator.getAllPrefixes(option),
              option.description()));

      if (option.defaultValues().isEmpty()) {
        builder.append('\n');
        return;
      }

      builder.append(" (default: ")
          .append(String.join(", ", option.defaultValues()))
          .append(")\n");
    };

    Consumer<Flag> flagConsumer = flag ->
        builder.append(
            String.format(
                formatString + '\n',
                DefaultHelpGenerator.getAllPrefixes(flag),
                flag.description()));

    if (options.stream().anyMatch(Option::isRequired)
        || flags.stream().anyMatch(Flag::isRequired)) {
      builder.append("  Required:\n");
      options.stream()
          .filter(Option::isRequired)
          .forEach(optionConsumer);

      flags.stream()
          .filter(Flag::isRequired)
          .forEach(flagConsumer);
    }

    if (options.stream().anyMatch(option -> !option.isRequired())
        || flags.stream().anyMatch(flag -> !flag.isRequired())) {
      builder.append("  Optional:\n");
      options.stream()
          .filter(option -> !option.isRequired())
          .forEach(optionConsumer);

      flags.stream()
          .filter(flag -> !flag.isRequired())
          .forEach(flagConsumer);
    }
  }

  private static String getAllPrefixes(Flag flag) {
    return String.join(", ", flag.prefixes());
  }

  private static String getAllPrefixes(Option option) {
    return String.join(", ", option.prefixes());
  }

  @Override
  public String generateHelp(CommandLineConfiguration configuration) {
    StringBuilder builder = new StringBuilder();

    builder.append("Usage: ").append(usageLine);
    generateArgumentsCommandLine(builder, configuration);

    builder.append('\n');

    if (!configuration.description().isEmpty()) {
      builder.append("\n");

      builder.append(configuration.description());
      builder.append("\n");
    }

    builder.append("\n");

    generateArgumentsDescription(builder, configuration);

    builder.append("\n");

    generateOptionsDescription(builder, configuration);

    return builder.toString();
  }
}
