package io.github.elkin.commandline;

import io.github.elkin.commandline.CommandLineConfiguration.OptionPrefixHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Option {

  private final String name;
  private final SortedSet<String> prefixes;
  private final OptionPrefixHandler optionPrefixHandler;
  private Consumer<String> consumer;
  private Predicate<String> checker;
  private int maxNumberOfValues;
  private boolean isRequired;
  private List<String> defaultValues;
  private String description;
  private Optional<String> value;
  private Values values;

  Option(String name, String prefix, OptionPrefixHandler optionPrefixHandler) {
    assert name != null;
    assert !name.isEmpty();
    assert prefix != null;
    assert Util.isOption(prefix);

    this.name = name;
    prefixes = new TreeSet<>();
    prefixes.add(prefix);
    consumer = Util.empty();
    checker = value -> true;
    maxNumberOfValues = 1;
    defaultValues = new ArrayList<>();
    description = "";
    value = Optional.empty();
    values = ValuesImpl.empty();
    this.optionPrefixHandler = optionPrefixHandler;
  }

  void setValues(Values values) {
    assert values != null;
    if (!values.isEmpty()) {
      value = Optional.of(values.getFirstValue());
    }
    this.values = values;
  }

  public String name() {
    return name;
  }

  public Set<String> prefixes() {
    return Collections.unmodifiableSet(prefixes);
  }

  public Option addPrefix(String prefix) {
    Util.checkPrefix(prefix);
    optionPrefixHandler.handle(prefix, this);
    prefixes.add(prefix);
    return this;
  }

  public Consumer<String> consumer() {
    return consumer;
  }

  public Option setConsumer(Consumer<String> consumer) {
    this.consumer = Objects.requireNonNull(consumer);
    return this;
  }

  public Predicate<String> checker() {
    return checker;
  }

  public Option setChecker(Predicate<String> checker) {
    this.checker = Objects.requireNonNull(checker);
    return this;
  }

  public int maxNumberOfValues() {
    return maxNumberOfValues;
  }

  public Option setMaxNumberOfValues(int maxNumberOfValues) {
    if (maxNumberOfValues < 1) {
      throw new IllegalArgumentException("The value can't be less than 1");
    }
    this.maxNumberOfValues = maxNumberOfValues;
    return this;
  }

  public boolean isRequired() {
    return isRequired;
  }

  public Option require() {
    isRequired = true;
    return this;
  }

  public List<String> defaultValues() {
    return Collections.unmodifiableList(defaultValues);
  }

  public Option addDefaultValue(String value) {
    defaultValues.add(Util.checkDefaultValue(value));
    return this;
  }

  public String description() {
    return description;
  }

  public Option setDescription(String description) {
    this.description = Objects.requireNonNull(description);
    return this;
  }

  public Optional<String> value() {
    return value;
  }

  public Values values() {
    return values;
  }

  @Override
  public String toString() {
    return String.format(
        "Option: %s, prefixes: %s, description: %s",
        name,
        prefixes,
        description);
  }
}
