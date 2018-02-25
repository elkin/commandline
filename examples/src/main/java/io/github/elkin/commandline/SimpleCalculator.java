package io.github.elkin.commandline;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class SimpleCalculator {
    public static void main(String[] args)
    {
        CommandLineConfiguration configuration = new CommandLineConfiguration();
        RequiredArgument argument = configuration.addRequiredArgument("numbers")
                .setDescription("Integer number")
                .setChecker(Util.isInteger())
                .addDefaultValue("0");

        Option option = configuration.addOption("operation", "-o")
                .addPrefix("--operation")
                .setDescription("Operation type")
                .addDefaultValue("sum")
                .setChecker(Util.choice("sum", "prod", "sub"));

        Flag flag = configuration.addFlag("verbose", "-v")
                .addPrefix("--verbose")
                .setDescription("Verbose mode");

        CommandLine.parse(configuration, args);

        // the option has default value so it's safe to use get here
        assert option.value().isPresent();
        String operation = option.value().get();

        Map<String, BinaryOperator<Integer>> operators = new HashMap<>();
        operators.put("sum", (lhs, rhs) -> lhs + rhs);
        operators.put("prod", (lhs, rhs) -> lhs * rhs);
        operators.put("sub", (lhs, rhs) -> lhs - rhs);

        StringBuilder output = new StringBuilder();
        if (flag.isSet()) {
            String sign = "";
            switch (operation) {
                case "sum":
                    sign = " + ";
                    break;
                case "prod":
                    sign = " * ";
                    break;

                case "sub":
                    sign = " - ";
                    break;
            }

            output.append(
                    argument.values().stream().collect(Collectors.joining(sign)));
            output.append(" = ");
        }

        Optional<Integer> result = argument.values()
                .stream()
                .map(value -> Integer.parseInt(value))
                .reduce(operators.get(operation));
        result.ifPresent(r -> output.append(r));
        System.out.println(output.toString());
    }
}
