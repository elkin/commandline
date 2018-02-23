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
    private final String d_name;
    private final SortedSet<String> d_prefixes;
    private Consumer<String> d_consumer;
    private Predicate<String> d_checker;
    private int d_maxNumberOfValues;
    private boolean d_isRequired;
    private List<String> d_defaultValues;
    private String d_description;
    private Optional<String> d_value;
    private Values d_values;
    private final OptionPrefixHandler d_optionPrefixHandler;

    Option(String name, String prefix, OptionPrefixHandler optionPrefixHandler)
    {
        assert name != null;
        assert !name.isEmpty();
        assert prefix != null;
        assert Util.isOption(prefix);

        d_name = name;
        d_prefixes = new TreeSet<>();
        d_prefixes.add(prefix);
        d_consumer = Util.empty();
        d_checker = value -> true;
        d_maxNumberOfValues = 1;
        d_defaultValues = new ArrayList<>();
        d_description = "";
        d_value = Optional.empty();
        d_values = ValuesImpl.empty();
        d_optionPrefixHandler = optionPrefixHandler;
    }

    void setValues(Values values)
    {
        assert values != null;
        if (!values.isEmpty()) {
            d_value = Optional.of(values.getFirstValue());
        }
        d_values = values;
    }

    public String name()
    {
        return d_name;
    }

    public Set<String> prefixes()
    {
        return Collections.unmodifiableSet(d_prefixes);
    }

    public Option addPrefix(String prefix)
    {
        Util.checkPrefix(prefix);
        d_optionPrefixHandler.handle(prefix, this);
        d_prefixes.add(prefix);
        return this;
    }

    public Consumer<String> consumer()
    {
        return d_consumer;
    }

    public Option setConsumer(Consumer<String> consumer)
    {
        d_consumer = Objects.requireNonNull(consumer);
        return this;
    }

    public Predicate<String> checker()
    {
        return d_checker;
    }

    public Option setChecker(Predicate<String> checker)
    {
        d_checker = Objects.requireNonNull(checker);
        return this;
    }

    public int maxNumberOfValues()
    {
        return d_maxNumberOfValues;
    }

    public Option setMaxNumberOfValues(int maxNumberOfValues)
    {
        if (maxNumberOfValues < 1) {
            throw new IllegalArgumentException("The value can't be less than 1");
        }
        d_maxNumberOfValues = maxNumberOfValues;
        return this;
    }

    public boolean isRequired()
    {
        return d_isRequired;
    }

    public Option require()
    {
        d_isRequired = true;
        return this;
    }

    public List<String> defaultValues()
    {
        return Collections.unmodifiableList(d_defaultValues);
    }

    public Option addDefaultValue(String value)
    {
        d_defaultValues.add(Util.checkDefaultValue(value));
        return this;
    }

    public String description()
    {
        return d_description;
    }

    public Option setDescription(String description)
    {
        d_description = Objects.requireNonNull(description);
        return this;
    }

    public Optional<String> value()
    {
        return d_value;
    }

    public Values values()
    {
        return d_values;
    }

    @Override
    public String toString()
    {
        return String.format(
                "Option: %s, prefixes: %s, description: %s",
                d_name,
                d_prefixes,
                d_description);
    }
}
