package io.github.elkin.commandline;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class RequiredArgument extends Argument {
    private String d_value;
    private Values d_remainder;

    RequiredArgument(String name, int position) {
        super(name, true, position);
        d_remainder = ValuesImpl.empty();
    }

    @Override
    void setValue(String value)
    {
        assert value != null;
        assert !value.isEmpty();

        d_value = value;
    }

    @Override
    void setRemainder(Values remainder)
    {
        assert remainder != null;
        assert !remainder.isEmpty();

        d_remainder = remainder;
    }

    @Override
    public RequiredArgument setChecker(Predicate<String> checker)
    {
        super.setChecker(checker);
        return this;
    }

    @Override
    public RequiredArgument setConsumer(Consumer<String> consumer)
    {
        super.setConsumer(consumer);
        return this;
    }

    @Override
    public RequiredArgument setDescription(String description)
    {
        super.setDescription(description);
        return this;
    }

    public String value()
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
                "Required argument: %s, description: %s",
                name(),
                description());
    }
}
