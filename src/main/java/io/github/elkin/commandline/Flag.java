package io.github.elkin.commandline;

import io.github.elkin.commandline.CommandLineConfiguration.FlagPrefixHandler;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

public class Flag {
    private final String d_name;
    private final SortedSet<String> d_prefixes;
    private boolean d_isSet;
    private boolean d_isRequired;
    private int d_maxNumberOfValues;
    private int d_numberOfFlags;
    private Consumer<Flag> d_consumer;
    private String d_description;
    private final FlagPrefixHandler d_flagPrefixHandler;

    Flag(String name, String prefix, FlagPrefixHandler flagPrefixHandler)
    {
        assert name != null;
        assert !name.isEmpty();
        assert prefix != null;
        assert Util.isOption(prefix);

        d_name = name;
        d_prefixes = new TreeSet<>();
        d_prefixes.add(prefix);
        d_maxNumberOfValues = 1;
        d_numberOfFlags = 0;
        d_consumer = Util.empty();
        d_flagPrefixHandler = flagPrefixHandler;
    }

    void set()
    {
        d_isSet = true;
        d_numberOfFlags++;
        d_consumer.accept(this);
    }

    public String name()
    {
        return d_name;
    }

    public Set<String> prefixes()
    {
        return Collections.unmodifiableSet(d_prefixes);
    }

    public Flag addPrefix(String prefix)
    {
        Util.checkPrefix(prefix);
        d_flagPrefixHandler.handle(prefix, this);

        d_prefixes.add(prefix);
        return this;
    }

    public boolean isSet()
    {
        return d_isSet;
    }

    public Flag require()
    {
        d_isRequired = true;
        return this;
    }

    public boolean isRequired()
    {
        return d_isRequired;
    }

    public int maxNumberOfValues()
    {
        return d_maxNumberOfValues;
    }

    public Flag setMaxNumberOfValues(int maxNumberOfValues)
    {
        if (maxNumberOfValues < 1) {
            throw new IllegalArgumentException("The value can't be less than 1");
        }
        d_maxNumberOfValues = maxNumberOfValues;
        return this;
    }

    public int numberOfFlags()
    {
        return d_numberOfFlags;
    }

    public Flag setConsumer(Consumer<Flag> consumer)
    {
        d_consumer = Objects.requireNonNull(consumer);
        return this;
    }

    public Consumer<Flag> consumer()
    {
        return d_consumer;
    }

    public String description()
    {
        return d_description;
    }

    public Flag setDescription(String description)
    {
        d_description = Objects.requireNonNull(description);
        return this;
    }

    @Override
    public String toString()
    {
        return String.format(
                "Flag: %s, prefixes: %s, description: %s",
                d_name,
                d_prefixes,
                d_description);
    }
}
