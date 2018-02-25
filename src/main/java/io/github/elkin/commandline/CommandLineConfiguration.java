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
    @FunctionalInterface
    interface OptionPrefixHandler {
        void handle(String prefix, Option option);
    }

    @FunctionalInterface
    interface FlagPrefixHandler {
        void handle(String prefix, Flag flag);
    }

    private enum Type {
        ARGUMENT,
        OPTION,
        FLAG
    }

    private String d_description;
    private final HelpGenerator d_helpGenerator;
    private final List<Argument> d_arguments;
    private final List<Option> d_options;
    private final List<Flag> d_flags;
    private final SortedSet<String> d_prefixes;
    private final Map<String, Option> d_optionByPrefix;
    private final Map<String, Flag> d_flagByPrefix;
    private final Map<String, Type> d_names;
    private final OptionPrefixHandler d_optionPrefixHandler;
    private final FlagPrefixHandler d_flagPrefixHandler;
    private int d_position;
    private int d_maxLastArgumentSize;
    private List<Validator> d_checkers;

    private void checkNameDuplicates(String name, Type nameType)
    {
        Type type = d_names.get(name);
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

        d_names.put(name, nameType);
    }

    private void checkPrefix(String prefix)
    {
        // check if prefix has already been registered
        if (!d_prefixes.add(prefix)) {
            throw new DuplicatePrefixException(
                    String.format("configuration already has <%s> option/flag with prefix",
                                  prefix));
        }

        // check if a long option is a prefix of another long option
        if (Util.isLongOption(prefix)) {
            String lastOptionPrefix = " ";
            for (String p : d_prefixes) {
                if (p.startsWith(lastOptionPrefix)) {
                    throw new DuplicatePrefixException(
                            String.format("Option %s is a prefix of option %s", lastOptionPrefix, p));
                }
                lastOptionPrefix = p;
            }
        }
    }

    private void checkOptionPrefix(String optionPrefix, Option option)
    {
        checkPrefix(optionPrefix);
        d_optionByPrefix.put(optionPrefix, option);
    }

    private void checkFlagPrefix(String flagPrefix, Flag flag)
    {
        checkPrefix(flagPrefix);
        d_flagByPrefix.put(flagPrefix, flag);
    }

    private void checkIfLastArgumentHasFewDefaultValues()
    {
        if (!d_arguments.isEmpty() && d_arguments.get(d_position - 1).defaultValues().size() > 1) {
            throw new ValidationException("Only the last argument can have more than one default value");
        }
    }

    Option getOptionByPrefix(String prefix)
    {
        return d_optionByPrefix.get(prefix);
    }

    Flag getFlagByPrefix(String prefix)
    {
        return d_flagByPrefix.get(prefix);
    }

    boolean hasFlagWithPrefix(String prefix)
    {
        return d_flagByPrefix.containsKey(prefix);
    }

    boolean isPrefixRegistered(String prefix)
    {
        return d_optionByPrefix.containsKey(prefix)
                || d_flagByPrefix.containsKey(prefix);
    }

    public CommandLineConfiguration(HelpGenerator helpGenerator)
    {
        d_helpGenerator = Objects.requireNonNull(helpGenerator);
        d_arguments = new ArrayList<>();
        d_options = new ArrayList<>();
        d_flags = new ArrayList<>();
        d_prefixes = new TreeSet<>();
        d_optionByPrefix = new HashMap<>();
        d_flagByPrefix = new HashMap<>();
        d_names = new HashMap<>();
        d_position = 0;
        d_maxLastArgumentSize = Integer.MAX_VALUE;
        d_optionPrefixHandler = this::checkOptionPrefix;
        d_flagPrefixHandler = this::checkFlagPrefix;
        d_checkers = new ArrayList<>();

        addFlag("help", "-h")
                .addPrefix("--help")
                .setDescription("print help information and exit");
    }

    public CommandLineConfiguration()
    {
        this(new DefaultHelpGenerator());
    }

    public String description()
    {
        return d_description;
    }

    public CommandLineConfiguration setDescription(String description)
    {
        d_description = Objects.requireNonNull(description);
        return this;
    }

    public OptionalArgument addOptionalArgument(String name)
    {
        Util.checkName(name);
        checkNameDuplicates(name, Type.ARGUMENT);

        checkIfLastArgumentHasFewDefaultValues();

        OptionalArgument argument = new OptionalArgument(name, d_position++);
        d_arguments.add(argument);
        return argument;
    }

    public RequiredArgument addRequiredArgument(String name)
    {
        Util.checkName(name);
        checkNameDuplicates(name, Type.ARGUMENT);

        if (!d_arguments.isEmpty() && !d_arguments.get(d_position - 1).isRequired()) {
            throw new ValidationException("It is not allowed to add required argument after an optional argument");
        }

        checkIfLastArgumentHasFewDefaultValues();

        RequiredArgument argument = new RequiredArgument(name, d_position++);
        d_arguments.add(argument);
        return argument;
    }

    public Option addOption(String name, String prefix)
    {
        Util.checkName(name);
        Util.checkPrefix(prefix);

        checkNameDuplicates(name, Type.OPTION);

        Option option = new Option(name, prefix, d_optionPrefixHandler);
        d_optionPrefixHandler.handle(prefix, option);
        d_options.add(option);
        return option;
    }

    public Flag addFlag(String name, String prefix)
    {
        Util.checkName(name);
        Util.checkPrefix(prefix);

        checkNameDuplicates(name, Type.FLAG);

        Flag flag = new Flag(name, prefix, d_flagPrefixHandler);
        d_flagPrefixHandler.handle(prefix, flag);
        d_flags.add(flag);
        return flag;
    }

    public int maxLastArgumentSize()
    {
        return d_maxLastArgumentSize;
    }

    public CommandLineConfiguration setMaxLastArgumentSize(int maxLastArgumentSize)
    {
        if (maxLastArgumentSize <= 0) {
            throw new IllegalArgumentException("Max last argument size musn't be less than 1");
        }
        d_maxLastArgumentSize = maxLastArgumentSize;
        return this;
    }

    public void addValidator(Validator checker)
    {
        d_checkers.add(Objects.requireNonNull(checker));
    }

    public List<Validator> checkers()
    {
        return Collections.unmodifiableList(d_checkers);
    }

    public List<Argument> arguments()
    {
        return Collections.unmodifiableList(d_arguments);
    }

    public List<Option> options()
    {
        return Collections.unmodifiableList(d_options);
    }

    public List<Flag> flags()
    {
        return Collections.unmodifiableList(d_flags);
    }

    @Override
    public String toString()
    {
        return d_helpGenerator.generateHelp(this);
    }
}
