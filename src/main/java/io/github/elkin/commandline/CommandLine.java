package io.github.elkin.commandline;


import io.github.elkin.commandline.exception.CommandLineException;
import io.github.elkin.commandline.exception.UnknownNameException;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CommandLine {
    private static final HelpRequestHandler s_helpRequestHandler =
            Util.makeHelpRequestHandler(0, System.out);

    private static final ExceptionHandler s_exceptionHandler =
            Util.makeExceptionHandler(1, System.out);

    private final Set<String> d_names;
    private final Set<String> d_flagNames;
    private final Map<String, Values> d_values;
    private final Set<String> d_flags;

    private static boolean isHelpInfoNeeded(String[] args)
    {
        for (String arg : args) {
            if (arg.equals("-h") || arg.equals("--help")) {
                return true;
            }
        }

        return false;
    }

    CommandLine(Set<String> names,
                Set<String> flagNames,
                Map<String, Values> values,
                Set<String> flags)
    {
        d_names = names;
        d_flagNames = flagNames;
        d_values = values;
        d_flags = flags;
    }

    public Set<String> names()
    {
        return Collections.unmodifiableSet(d_names);
    }

    public Set<String> flags()
    {
        return Collections.unmodifiableSet(d_flagNames);
    }

    public boolean isFlagSet(String name)
    {
        Util.checkName(name);

        if (!d_flagNames.contains(name)) {
            throw new UnknownNameException(String.format("Unknown flag <%s>", name));
        }
        return d_flags.contains(name);
    }

    public Values get(String name)
    {
        Util.checkName(name);

        if (!d_names.contains(name)) {
            throw new UnknownNameException(String.format("Unknown name <%s>", name));
        }

        Values values = d_values.getOrDefault(name, ValuesImpl.empty());
        assert values != null;
        return values;
    }

    public static CommandLine getCommandLine(CommandLineConfiguration commandLineConfiguration,
                                             String[] args)
    {
        return getCommandLine(
                commandLineConfiguration,
                args,
                s_helpRequestHandler,
                s_exceptionHandler);
    }

    public static CommandLine getCommandLine(CommandLineConfiguration commandLineConfiguration,
                                             String[] args,
                                             HelpRequestHandler helpRequestHandler)
    {
        return getCommandLine(
                commandLineConfiguration,
                args,
                helpRequestHandler,
                s_exceptionHandler);
    }

    public static CommandLine getCommandLine(CommandLineConfiguration commandLineConfiguration,
                                             String[] args,
                                             ExceptionHandler exceptionHandler)
    {
        return getCommandLine(
                commandLineConfiguration,
                args,
                s_helpRequestHandler,
                exceptionHandler);
    }

    public static CommandLine getCommandLine(CommandLineConfiguration commandLineConfiguration,
                                             String[] args,
                                             HelpRequestHandler helpRequestHandler,
                                             ExceptionHandler exceptionHandler)
    {
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
                             String[] args)
    {
        parse(commandLineConfiguration, args, s_helpRequestHandler, s_exceptionHandler);
    }

    public static void parse(CommandLineConfiguration commandLineConfiguration,
                             String[] args,
                             HelpRequestHandler helpRequestHandler)
    {
        parse(commandLineConfiguration, args, helpRequestHandler, s_exceptionHandler);
    }

    public static void parse(CommandLineConfiguration commandLineConfiguration,
                             String[] args,
                             ExceptionHandler exceptionHandler)
    {
        parse(commandLineConfiguration, args, s_helpRequestHandler, exceptionHandler);
    }

    public static void parse(CommandLineConfiguration commandLineConfiguration,
                             String[] args,
                             HelpRequestHandler helpRequestHandler,
                             ExceptionHandler exceptionHandler)
    {
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
}
