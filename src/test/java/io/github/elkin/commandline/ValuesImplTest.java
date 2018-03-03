package io.github.elkin.commandline;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ValuesImplTest {
    @Test
    public void singleValue()
    {
        String value = "abc";
        ValuesImpl values = new ValuesImpl(value);
        
        assertEquals(values.size(), 1);
        assertEquals(values.getFirstValue(), value);
        assertEquals(values.toList(), Collections.singletonList(value));

        List<String> list = new ArrayList<>(2);
        list.add("0");
        values.toList(list);
        assertEquals(list.size(), 2);
        assertEquals(list, Arrays.asList("0", value));

        assertEquals(values.getValue(0), value);

        assertFalse(values.isEmpty());

        Iterator<String> iter = values.iterator();
        assertTrue(iter.hasNext());
        assertEquals(iter.next(), value);
        assertFalse(iter.hasNext());

        List<String> valuesList = new ArrayList<>();
        valuesList.add("1");
        values.toList(() -> valuesList);
        assertEquals(valuesList.size(), 2);
        assertEquals(valuesList, Arrays.asList("1", value));

        assertEquals(
                values.stream().collect(Collectors.toList()),
                Collections.singletonList(value));
    }
}