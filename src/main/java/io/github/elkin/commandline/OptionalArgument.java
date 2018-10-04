package io.github.elkin.commandline;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class OptionalArgument extends Argument {

  private Optional<String> value;
  private Values remainder;
  private Values values;

  OptionalArgument(String name, int position) {
    super(name, false, position);
    value = Optional.empty();
    remainder = ValuesImpl.empty();
    values = ValuesImpl.empty();
  }

  @Override
  public OptionalArgument addDefaultValue(String value) {
    super.addDefaultValue(value);
    return this;
  }

  @Override
  public OptionalArgument setChecker(Predicate<String> checker) {
    super.setChecker(checker);
    return this;
  }

  @Override
  public OptionalArgument setConsumer(Consumer<String> consumer) {
    super.setConsumer(consumer);
    return this;
  }

  @Override
  public OptionalArgument setDescription(String description) {
    super.setDescription(description);
    return this;
  }

  @Override
  void setValue(String value) {
    assert value != null;
    assert !value.isEmpty();

    this.value = Optional.of(value);
    values = new ValuesImpl(value);
  }

  @Override
  void setRemainder(Values remainder) {
    assert value.isPresent();
    assert remainder != null;
    assert !remainder.isEmpty();
    assert value.isPresent();

    this.remainder = remainder;

    values = new ValuesImpl(value.get(), this.remainder);
  }

  public Optional<String> value() {
    return value;
  }

  public Values remainder() {
    return remainder;
  }

  public Values values() {
    return values;
  }

  @Override
  public String toString() {
    return String.format(
        "Optional argument: %s, description: %s",
        name(),
        description());
  }
}
