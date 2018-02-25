package io.github.elkin.commandline;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface Values extends Iterable<String> {
    int size();
    String getFirstValue();
    String getValue(int index);
    Stream<String> stream();
    boolean isEmpty();
    List<String> toList();
    List<String> toList(List<String> list);
    List<String> toList(Supplier<List<String>> supplier);
}
