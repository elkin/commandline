package io.github.elkin.commandline;

import java.util.function.Consumer;
import java.util.function.Predicate;

public final class RequiredArgument extends Argument {
    private String value;
    private Values remainder;
    private Values values;

    RequiredArgument(String name, int position) {
        super(name, true, position);
        remainder = ValuesImpl.empty();
        values = ValuesImpl.empty();
    }

    @Override
    public RequiredArgument addDefaultValue(String value)
    {
        super.addDefaultValue(value);
        return this;
    }

    @Override
    void setValue(String value)
    {
        assert value != null;
        assert !value.isEmpty();

        this.value = value;
        values = new ValuesImpl(value);
    }

    @Override
    void setRemainder(Values remainder)
    {
        assert remainder != null;
        assert !remainder.isEmpty();

        this.remainder = remainder;
        values = new ValuesImpl(value, this.remainder);
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
        return value;
    }

    public Values remainder()
    {
        return remainder;
    }

    public Values values()
    {
        return values;
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
