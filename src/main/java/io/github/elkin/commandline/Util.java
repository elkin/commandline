package io.github.elkin.commandline;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Util {
    private Util() {}

    static final int SHORT_OPTION_LENGTH = 2;
    private static final Pattern s_naturalIntegerPattern = Pattern.compile("\\d+");
    private static final Pattern s_positiveIntegerPattern = Pattern.compile("[1-9]\\d*");

    private static final Predicate<String> s_isNaturalNumber =
            value -> s_naturalIntegerPattern.matcher(value).matches();

    private static final Predicate<String> s_isPositiveInteger =
            value -> s_positiveIntegerPattern.matcher(value).matches();

    private static final ExceptionHandler s_rethrowExceptionHandler = (exception, configuration, args) -> {
        throw exception;
    };

    public static Predicate<String> isNaturalNumber()
    {
        return s_isNaturalNumber;
    }

    public static Predicate<String> isPositiveInteger()
    {
        return s_isPositiveInteger;
    }

    public static Consumer<String> fromIntConsumer(Consumer<Integer> consumer) {
        // Please check if int can be parsed from string beforehand with a dedicated checker
        return value -> consumer.accept(Integer.parseInt(value));
    }

    public static Predicate<String> choice(String... values)
    {
        return value -> {
            for (String v : values) {
                if (v.equals(value)) {
                    return true;
                }
            }

            return false;
        };
    }

    public static HelpRequestHandler makeHelpRequestHandler(int exitCode, OutputStream stream)
    {
        return (configuration, args) -> {
            try (PrintWriter writer = new PrintWriter(stream)) {
                writer.println(configuration);
            }
            System.exit(exitCode);
        };
    }

    public static ExceptionHandler makeExceptionHandler(int exitCode, OutputStream stream)
    {
        return (e, configuration, args) -> {
            try (PrintWriter writer = new PrintWriter(stream)) {
                writer.println(e.getMessage());
                writer.println(configuration);
                writer.flush();
            }
            System.exit(exitCode);
        };
    }

    public static ExceptionHandler reThrowExceptionHandler()
    {
        return s_rethrowExceptionHandler;
    }

    static <T> Consumer<T> empty()
    {
        return value -> {};
    }

    static boolean isOption(String prefix)
    {
        return prefix.startsWith("-");
    }

    static boolean isShortOption(String prefix)
    {
        return isOption(prefix) && !isLongOption(prefix);
    }

    static boolean isLongOption(String prefix)
    {
        return prefix.startsWith("--");
    }

    static String checkPrefix(String prefix)
    {
        if (prefix == null || !Util.isOption(prefix)) {
            throw new IllegalArgumentException("Prefix mustn't be null and it must start with '-', e.g. '-a'");
        }
        return prefix;
    }

    static String checkName(String name)
    {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name mustn't be null or empty");
        }
        return name;
    }

    static String checkDefaultValue(String value)
    {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Default value mustn't be null or empty");
        }
        return value;
    }
}
