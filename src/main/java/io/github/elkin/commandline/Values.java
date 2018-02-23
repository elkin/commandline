package io.github.elkin.commandline;

import java.util.Iterator;
import java.util.stream.Stream;

public interface Values {
    int size();
    String getFirstValue();
    String getValue(int index);

    Iterator<String> iterator();
    Stream<String> stream();
    boolean isEmpty();
}
