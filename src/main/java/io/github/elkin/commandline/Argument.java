package io.github.elkin.commandline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class Argument {
    private final String d_name;
    private final int d_position;
    private Consumer<String> d_consumer;
    private Predicate<String> d_checker;
    private boolean d_isRequired;
    private List<String> d_defaultValues;
    private String d_description;

    Argument(String name, boolean isRequired, int position)
    {
        assert name != null;
        assert !name.isEmpty();
        assert position >= 0;

        d_name = name;
        d_position = position;
        d_consumer = Util.empty();
        d_checker = value ->  true;
        d_isRequired = isRequired;
        d_defaultValues = new ArrayList<>();
        d_description = "";
    }

    abstract void setValue(String value);

    abstract void setRemainder(Values remainder);

    public String name()
    {
        return d_name;
    }

    public Consumer<String> consumer()
    {
        return d_consumer;
    }

    public Argument setConsumer(Consumer<String> consumer)
    {
        d_consumer = Objects.requireNonNull(consumer);
        return this;
    }

    public Predicate<String> checker()
    {
        return d_checker;
    }

    public Argument setChecker(Predicate<String> checker)
    {
        d_checker = Objects.requireNonNull(checker);
        return this;
    }

    public int position()
    {
        return d_position;
    }

    public boolean isRequired()
    {
        return d_isRequired;
    }

    public List<String> defaultValues()
    {
        return Collections.unmodifiableList(d_defaultValues);
    }

    public Argument addDefaultValue(String value)
    {
        d_defaultValues.add(Util.checkDefaultValue(value));
        return this;
    }

    public String description()
    {
        return d_description;
    }

    public Argument setDescription(String description)
    {
        d_description = Objects.requireNonNull(description);
        return this;
    }
}
