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
    private final CommandLineConfiguration d_configuration;
    private final List<String> d_args;
    private final Map<String, List<String>> d_values;
    private final Set<String> d_flags;
    private Iterator<Argument> d_argumentIterator;
    private Argument d_argument;
    private List<String> d_argumentRemainder;

    private List<String> preprocessArguments(String[] args)
    {
        List<String> result = new ArrayList<>(args.length);
        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (Util.isShortOption(arg) && arg.length() > Util.SHORT_OPTION_LENGTH) {
                String prefix = arg.substring(0, Util.SHORT_OPTION_LENGTH);
                result.add(prefix);

                String value = arg.substring(Util.SHORT_OPTION_LENGTH);
                if (d_configuration.hasFlagWithPrefix(prefix)) {
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

    private boolean handleFlag(String prefix)
    {
        Flag flag = d_configuration.getFlagByPrefix(prefix);

        if (flag == null) {
            return false;
        }

        d_flags.add(flag.name());
        flag.set();
        return true;
    }

    private boolean handleOption(String prefix, String value)
    {
        Option option = d_configuration.getOptionByPrefix(prefix);
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
        List<String> values = d_values.computeIfAbsent(
                option.name(),
                name -> new ArrayList<>());
        values.add(value);

        return true;
    }

    private boolean handleArgument(String arg)
    {
        List<Argument> arguments = d_configuration.arguments();
        if (arguments.isEmpty()) {
            return false;
        }

        boolean isRemainder = true;
        if (d_argumentIterator.hasNext()) {
            d_argument = d_argumentIterator.next();
            isRemainder = false;
        }

        String argName = d_argument.name();
        if (!d_argument.checker().test(arg)) {
            throw new CheckException(String.format("Argument <%s> can't have value <%s>", argName, arg));
        }
        d_argument.consumer().accept(arg);
        List<String> values = d_values.computeIfAbsent(d_argument.name(), name -> new ArrayList<>());
        values.add(arg);

        if (isRemainder) {
            d_argumentRemainder.add(arg);
        } else {
            d_argument.setValue(arg);
        }

        return true;
    }

    private void checkSizeConstraints()
    {
        List<Argument> arguments = d_configuration.arguments();
        for (Argument argument: arguments) {
            List<String> values = d_values.getOrDefault(
                    argument.name(),
                    Collections.emptyList());

            if (argument.isRequired() && values.isEmpty()) {
                throw new SizeViolationException(
                        String.format(
                                "Argument <%s> is required, please provide value for it",
                                argument.name()));
            }
        }

        if (!arguments.isEmpty()) {
            Argument lastArgument = arguments.get(arguments.size() - 1);
            int lastArgumentNumValues = d_values.getOrDefault(
                    lastArgument.name(),
                    Collections.emptyList()).size();

            if (lastArgumentNumValues > d_configuration.maxLastArgumentSize()) {
                throw new SizeViolationException(
                        String.format(
                                "%d is too many values(max number is %d) for the last argument <%s>",
                                lastArgumentNumValues,
                                d_configuration.maxLastArgumentSize(),
                                lastArgument.name()));
            }
        }

        for (Option option : d_configuration.options()) {
            List<String> values = d_values.getOrDefault(option.name(), Collections.emptyList());
            int size = values.size();

            if (values.isEmpty() && option.isRequired()) {
                throw new SizeViolationException(
                        String.format(
                                "Option <%s> is required, please provide value for it",
                                option.name()));
            }

            if (values.size() > option.maxNumberOfValues()) {
                throw new SizeViolationException(
                    String.format(
                            "%d is too many values(max number is %d) for option <%s>",
                            size,
                            option.maxNumberOfValues(),
                            option.name()));
            }
        }

        for (Flag flag : d_configuration.flags()) {
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

    private void addDefaultValues()
    {
        for (Option option : d_configuration.options()) {
            List<String> defaultValues = option.defaultValues();
            if (defaultValues.isEmpty()) {
                continue;
            }

            if (d_values.containsKey(option.name())) {
                continue;
            }

            String name = option.name();
            d_values.put(name, defaultValues);
            option.setValues(new ValuesImpl(defaultValues));
        }

        while (d_argumentIterator.hasNext()) {
            d_argument = d_argumentIterator.next();
            List<String> defaultValues = d_argument.defaultValues();

            if (!defaultValues.isEmpty() && !d_values.containsKey(d_argument.name())) {
                d_values.put(d_argument.name(), defaultValues);
                if (defaultValues.size() == 1) {
                    d_argument.setValue(defaultValues.get(0));
                } else {
                    d_argument.setValue(defaultValues.get(0));
                    d_argument.setRemainder(new ValuesImpl(defaultValues.subList(1, defaultValues.size())));
                }
            }
        }
    }

    CommandLineParser(CommandLineConfiguration configuration, String[] args)
    {
        d_configuration = configuration;
        d_args = preprocessArguments(args);
        d_values = new HashMap<>();
        d_flags = new HashSet<>();
        d_argumentRemainder = new ArrayList<>();
        d_argumentIterator = d_configuration.arguments().iterator();
    }

    CommandLine getCommandLine()
    {
        parse();

        Map<String, Values> commandLineValues = new HashMap<>();
        d_values.forEach((name, v) -> commandLineValues.put(name, new ValuesImpl(v)));

        Set<String> names = d_configuration.arguments()
                .stream()
                .map(Argument::name)
                .collect(Collectors.toSet());

        names.addAll(d_configuration.options()
                .stream()
                .map(Option::name)
                .collect(Collectors.toSet()));

        Set<String> flagNames = d_configuration.flags().stream()
                .map(Flag::name).collect(Collectors.toSet());

        return new CommandLine(
                names,
                flagNames,
                commandLineValues,
                d_flags);
    }

    void parse()
    {
        CommandLineHandler commandLineHandler = new CommandLineHandler(
                this::handleArgument,
                d_configuration::isPrefixRegistered,
                this::handleOption,
                this::handleFlag);

        commandLineHandler.iterate(d_args);

        d_configuration.options().forEach(option ->
            option.setValues(
                    new ValuesImpl(
                            d_values.getOrDefault(
                                    option.name(),
                                    Collections.emptyList())))
        );

        if (!d_argumentRemainder.isEmpty()) {
            d_argument.setRemainder(new ValuesImpl(d_argumentRemainder));
        }

        addDefaultValues();
        checkSizeConstraints();

        d_configuration.checkers().forEach(checker ->
                checker.validate(
                        d_configuration.arguments(),
                        d_configuration.options(),
                        d_configuration.flags()));
    }
}
