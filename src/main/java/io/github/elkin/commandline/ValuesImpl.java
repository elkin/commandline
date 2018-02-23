package io.github.elkin.commandline;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;


class ValuesImpl implements Values {
    private final List<String> d_values;

    private static final ValuesImpl s_empty = new ValuesImpl(Collections.emptyList());

    static ValuesImpl empty()
    {
        return s_empty;
    }

    ValuesImpl(List<String> values)
    {
        assert values != null;

        d_values = Collections.unmodifiableList(values);
    }

    @Override
    public String getFirstValue()
    {
        return d_values.get(0);
    }

    @Override
    public Iterator<String> iterator()
    {
        // iterator for an unmodifiable list can't remove elements
        return d_values.iterator();
    }

    @Override
    public String getValue(int index)
    {
        assert index >= 0;
        assert index < size();

        return d_values.get(index);
    }

    @Override
    public int size()
    {
        return d_values.size();
    }

    @Override
    public Stream<String> stream()
    {
        return d_values.stream();
    }

    @Override
    public boolean isEmpty()
    {
        return d_values.isEmpty();
    }
}
