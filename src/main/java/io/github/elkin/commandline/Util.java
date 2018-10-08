package io.github.elkin.commandline;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Util {

  static final int SHORT_OPTION_LENGTH = 2;
  private static final Pattern NATURAL_INTERER = Pattern.compile("\\d+");
  private static final Pattern POSITIVE_INTEGER = Pattern.compile("[1-9]\\d*");
  private static final Pattern INTEGER = Pattern.compile("-?\\d+");
  private static final Predicate<String> IS_NATURAL_NUMBER =
      value -> NATURAL_INTERER.matcher(value).matches();
  private static final Predicate<String> IS_POSITIVE_INTEGER =
      value -> POSITIVE_INTEGER.matcher(value).matches();
  private static final Predicate<String> IS_INTEGER =
      value -> INTEGER.matcher(value).matches();
  private static final ExceptionHandler RETHROW_EXCEPTION_HANDLER = (exception, configuration, args) -> {
    throw exception;
  };

  private Util() {
  }

  public static Predicate<String> isNaturalNumber() {
    return IS_NATURAL_NUMBER;
  }

  public static Predicate<String> isPositiveInteger() {
    return IS_POSITIVE_INTEGER;
  }

  public static Predicate<String> isInteger() {
    return IS_INTEGER;
  }

  public static Consumer<String> fromIntConsumer(Consumer<Integer> consumer) {
    // Please check if int can be parsed from string beforehand with a dedicated checker
    return value -> consumer.accept(Integer.parseInt(value));
  }

  public static Predicate<String> choice(String... values) {
    return value -> {
      // though it's a linear search but it works fast enough on small number of values
      for (String v : values) {
        if (v.equals(value)) {
          return true;
        }
      }

      return false;
    };
  }

  public static Predicate<String> choice(Collection<String> values) {
    return values::contains;
  }

  public static HelpRequestHandler makeHelpRequestHandler(int exitCode, OutputStream stream) {
    return (configuration, args) -> {
      try (PrintWriter writer = new PrintWriter(
          new OutputStreamWriter(stream, StandardCharsets.UTF_8))) {
        writer.println(configuration);
      }
      System.exit(exitCode);
    };
  }

  public static ExceptionHandler makeExceptionHandler(int exitCode, OutputStream stream) {
    return (e, configuration, args) -> {
      try (PrintWriter writer = new PrintWriter(
          new OutputStreamWriter(stream, StandardCharsets.UTF_8))) {
        writer.println(e.getMessage());
        writer.println(configuration);
        writer.flush();
      }
      System.exit(exitCode);
    };
  }

  public static ExceptionHandler reThrowExceptionHandler() {
    return RETHROW_EXCEPTION_HANDLER;
  }

  static <T> Consumer<T> empty() {
    return value -> {
    };
  }

  static boolean isOption(String prefix) {
    return prefix.startsWith("-");
  }

  static boolean isShortOption(String prefix) {
    return isOption(prefix) && !isLongOption(prefix);
  }

  static boolean isLongOption(String prefix) {
    return prefix.startsWith("--");
  }

  static String checkPrefix(String prefix) {
    if (prefix == null || !Util.isOption(prefix)) {
      throw new IllegalArgumentException(
          "Prefix mustn't be null and it must start with '-', e.g. '-a'");
    }
    return prefix;
  }

  static String checkName(String name) {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("Name mustn't be null or empty");
    }
    return name;
  }

  static String checkDefaultValue(String value) {
    if (value == null || value.isEmpty()) {
      throw new IllegalArgumentException("Default value mustn't be null or empty");
    }
    return value;
  }
}
