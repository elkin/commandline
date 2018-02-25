package io.github.elkin.commandline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;


class ValuesImpl implements Values {
    private List<String> d_values;

    private static final ValuesImpl s_empty = new ValuesImpl(Collections.emptyList());

    static ValuesImpl empty()
    {
        return s_empty;
    }

    ValuesImpl(String value)
    {
        assert value != null;

        d_values = Collections.singletonList(value);
    }

    ValuesImpl(List<String> values)
    {
        assert values != null;

        d_values = Collections.unmodifiableList(values);
    }

    ValuesImpl(String firstValue, Values remainder)
    {
        assert firstValue != null;
        assert  remainder != null;

        d_values = new ArrayList<>(1 + remainder.size());
        d_values.add(firstValue);
        for (String value : remainder) {
            d_values.add(value);
        }
        d_values = Collections.unmodifiableList(d_values);
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

    @Override
    public List<String> toList() {
        return new ArrayList<>(d_values);
    }

    @Override
    public List<String> toList(List<String> list) {
        list.addAll(d_values);
        return list;
    }

    @Override
    public List<String> toList(Supplier<List<String>> supplier) {
        List<String> result = supplier.get();
        result.addAll(d_values);
        return result;
    }
}
