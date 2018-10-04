package io.github.elkin.commandline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;


class ValuesImpl implements Values {

  private static final ValuesImpl EMPTY = new ValuesImpl(Collections.emptyList());
  private List<String> values;

  ValuesImpl(String value) {
    assert value != null;
    assert !value.isEmpty();

    values = Collections.singletonList(value);
  }

  ValuesImpl(List<String> values) {
    assert values != null;

    this.values = Collections.unmodifiableList(values);
  }

  ValuesImpl(String firstValue, Values remainder) {
    assert firstValue != null;
    assert !firstValue.isEmpty();
    assert remainder != null;

    values = new ArrayList<>(1 + remainder.size());
    values.add(firstValue);
    remainder.toList(values);
    values = Collections.unmodifiableList(values);
  }

  static ValuesImpl empty() {
    return EMPTY;
  }

  @Override
  public String getFirstValue() {
    return values.get(0);
  }

  @Override
  public Iterator<String> iterator() {
    // iterator for an unmodifiable list can't remove elements
    return values.iterator();
  }

  @Override
  public String getValue(int index) {
    assert index >= 0;
    assert index < size();

    return values.get(index);
  }

  @Override
  public int size() {
    return values.size();
  }

  @Override
  public Stream<String> stream() {
    return values.stream();
  }

  @Override
  public boolean isEmpty() {
    return values.isEmpty();
  }

  @Override
  public List<String> toList() {
    return new ArrayList<>(values);
  }

  @Override
  public List<String> toList(List<String> list) {
    list.addAll(values);
    return list;
  }

  @Override
  public List<String> toList(Supplier<List<String>> supplier) {
    List<String> result = supplier.get();
    result.addAll(values);
    return result;
  }
}
