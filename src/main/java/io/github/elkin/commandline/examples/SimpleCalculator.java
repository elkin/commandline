package io.github.elkin.commandline.examples;

import io.github.elkin.commandline.CommandLine;
import io.github.elkin.commandline.CommandLineConfiguration;
import io.github.elkin.commandline.Flag;
import io.github.elkin.commandline.Option;
import io.github.elkin.commandline.RequiredArgument;
import io.github.elkin.commandline.Util;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class SimpleCalculator {

  private static final String SUM = "sum";
  private static final String PROD = "prod";
  private static final String SUB = "sub";

  public static void main(String[] args) {
    CommandLineConfiguration configuration = new CommandLineConfiguration();
    RequiredArgument argument = configuration.addRequiredArgument("numbers")
        .setDescription("Integer number")
        .setChecker(Util.isInteger())
        .addDefaultValue("0");

    Option option = configuration.addOption("operation", "-o")
        .addPrefix("--operation")
        .setDescription("Operation type")
        .addDefaultValue(SUM)
        .setChecker(Util.choice(SUM, PROD, SUB));

    Flag flag = configuration.addFlag("verbose", "-v")
        .addPrefix("--verbose")
        .setDescription("Verbose mode");

    CommandLine.parse(configuration, args);

    // the option has default value so it's safe to use get here
    assert option.value().isPresent();
    String operation = option.value().get();

    Map<String, BinaryOperator<Integer>> operators = new HashMap<>();
    operators.put(SUM, (lhs, rhs) -> lhs + rhs);
    operators.put(PROD, (lhs, rhs) -> lhs * rhs);
    operators.put(SUB, (lhs, rhs) -> lhs - rhs);

    StringBuilder output = new StringBuilder();
    if (flag.isSet()) {
      String sign = "";
      switch (operation) {
        case SUM:
          sign = " + ";
          break;
        case PROD:
          sign = " * ";
          break;

        case SUB:
          sign = " - ";
          break;
      }

      output.append(
          argument.values().stream().collect(Collectors.joining(sign)));
      output.append(" = ");
    }

    Optional<Integer> result = argument.values()
        .stream()
        .map(Integer::parseInt)
        .reduce(operators.get(operation));
    result.ifPresent(output::append);
    System.out.println(output.toString());
  }
}
