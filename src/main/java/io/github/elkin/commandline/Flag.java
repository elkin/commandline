package io.github.elkin.commandline;

import io.github.elkin.commandline.CommandLineConfiguration.FlagPrefixHandler;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

public final class Flag {

  private final String name;
  private final SortedSet<String> prefixes;
  private final FlagPrefixHandler flagPrefixHandler;
  private boolean isSet;
  private boolean isRequired;
  private int maxNumberOfValues;
  private int numberOfFlags;
  private Consumer<Flag> consumer;
  private String description;

  Flag(String name, String prefix, FlagPrefixHandler flagPrefixHandler) {
    assert name != null;
    assert !name.isEmpty();
    assert prefix != null;
    assert Util.isOption(prefix);

    this.name = name;
    prefixes = new TreeSet<>();
    prefixes.add(prefix);
    maxNumberOfValues = 1;
    numberOfFlags = 0;
    consumer = Util.empty();
    this.flagPrefixHandler = flagPrefixHandler;
  }

  void set() {
    isSet = true;
    numberOfFlags++;
    consumer.accept(this);
  }

  public String name() {
    return name;
  }

  public Set<String> prefixes() {
    return Collections.unmodifiableSet(prefixes);
  }

  public Flag addPrefix(String prefix) {
    Util.checkPrefix(prefix);
    flagPrefixHandler.handle(prefix, this);

    prefixes.add(prefix);
    return this;
  }

  public boolean isSet() {
    return isSet;
  }

  public Flag require() {
    isRequired = true;
    return this;
  }

  public boolean isRequired() {
    return isRequired;
  }

  public int maxNumberOfValues() {
    return maxNumberOfValues;
  }

  public Flag setMaxNumberOfValues(int maxNumberOfValues) {
    if (maxNumberOfValues < 1) {
      throw new IllegalArgumentException("The value can't be less than 1");
    }
    this.maxNumberOfValues = maxNumberOfValues;
    return this;
  }

  public int numberOfFlags() {
    return numberOfFlags;
  }

  public Flag setConsumer(Consumer<Flag> consumer) {
    this.consumer = Objects.requireNonNull(consumer);
    return this;
  }

  public Consumer<Flag> consumer() {
    return consumer;
  }

  public String description() {
    return description;
  }

  public Flag setDescription(String description) {
    this.description = Objects.requireNonNull(description);
    return this;
  }

  @Override
  public String toString() {
    return String.format(
        "Flag: %s, prefixes: %s, description: %s",
        name,
        prefixes,
        description);
  }
}
