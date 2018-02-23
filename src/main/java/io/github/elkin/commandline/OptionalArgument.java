package io.github.elkin.commandline;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class OptionalArgument extends Argument {
    private Optional<String> d_value;
    private Values d_remainder;

    OptionalArgument(String name, int position) {
        super(name, false, position);
        d_value = Optional.empty();
        d_remainder = ValuesImpl.empty();
    }

    @Override
    public OptionalArgument setChecker(Predicate<String> checker)
    {
        super.setChecker(checker);
        return this;
    }

    @Override
    public OptionalArgument setConsumer(Consumer<String> consumer)
    {
        super.setConsumer(consumer);
        return this;
    }

    @Override
    public OptionalArgument setDescription(String description)
    {
        super.setDescription(description);
        return this;
    }

    @Override
    void setValue(String value)
    {
        assert value != null;
        assert !value.isEmpty();

        d_value = Optional.of(value);
    }

    @Override
    void setRemainder(Values remainder)
    {
        assert remainder != null;
        assert !remainder.isEmpty();

        d_remainder = remainder;
    }

    public Optional<String> value()
    {
        return d_value;
    }

    public Values remainder()
    {
        return d_remainder;
    }

    @Override
    public String toString()
    {
        return String.format(
                "Optional argument: %s, description: %s",
                name(),
                description());
    }
}
