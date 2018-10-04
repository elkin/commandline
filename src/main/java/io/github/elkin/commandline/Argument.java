package io.github.elkin.commandline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

abstract class Argument {

  private final String name;
  private final int position;
  private Consumer<String> consumer;
  private Predicate<String> checker;
  private boolean isRequired;
  private List<String> defaultValues;
  private String description;

  Argument(String name, boolean isRequired, int position) {
    assert name != null;
    assert !name.isEmpty();
    assert position >= 0;

    this.name = name;
    this.position = position;
    consumer = Util.empty();
    checker = value -> true;
    this.isRequired = isRequired;
    defaultValues = new ArrayList<>();
    description = "";
  }

  abstract void setValue(String value);

  abstract void setRemainder(Values remainder);

  String name() {
    return name;
  }

  Consumer<String> consumer() {
    return consumer;
  }

  Argument setConsumer(Consumer<String> consumer) {
    this.consumer = Objects.requireNonNull(consumer);
    return this;
  }

  Predicate<String> checker() {
    return checker;
  }

  Argument setChecker(Predicate<String> checker) {
    this.checker = Objects.requireNonNull(checker);
    return this;
  }

  int position() {
    return position;
  }

  boolean isRequired() {
    return isRequired;
  }

  List<String> defaultValues() {
    return Collections.unmodifiableList(defaultValues);
  }

  Argument addDefaultValue(String value) {
    defaultValues.add(Util.checkDefaultValue(value));
    return this;
  }

  String description() {
    return description;
  }

  Argument setDescription(String description) {
    this.description = Objects.requireNonNull(description);
    return this;
  }
}
