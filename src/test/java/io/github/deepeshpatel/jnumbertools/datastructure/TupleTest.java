package io.github.deepeshpatel.jnumbertools.datastructure;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import static org.junit.jupiter.api.Assertions.*;

class TupleTest {

    private static Tuple<Object> tuple(Object... items) {
        return Tuple.wrap(items.clone());
    }

    @Test
    void behavesLikeAListWithTheSameElements() {
        var t = tuple("A", 1, "X");
        assertEquals(List.of("A", 1, "X"), t);
        assertEquals(t, List.of("A", 1, "X"));
        assertEquals(List.of("A", 1, "X").hashCode(), t.hashCode());
        assertEquals("[A, 1, X]", t.toString());
        assertEquals(3, t.size());
        assertEquals(1, t.get(1));
        assertEquals(1, t.indexOf("1".length()));
        assertTrue(t.contains("X"));
        assertFalse(t.isEmpty());
        assertTrue(t instanceof RandomAccess);
    }

    @Test
    void emptyTupleEqualsAnEmptyList() {
        var t = tuple();
        assertEquals(List.of(), t);
        assertTrue(t.isEmpty());
        assertEquals("[]", t.toString());
    }

    @Test
    void getOutOfRangeThrows() {
        var t = tuple("A");
        assertThrows(IndexOutOfBoundsException.class, () -> t.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> t.get(-1));
    }

    @Test
    void everyMutationIsRejected() {
        var t = tuple("B", "A");
        assertThrows(UnsupportedOperationException.class, () -> t.add("C"));
        assertThrows(UnsupportedOperationException.class, () -> t.add(0, "C"));
        assertThrows(UnsupportedOperationException.class, () -> t.set(0, "C"));
        assertThrows(UnsupportedOperationException.class, () -> t.remove(0));
        assertThrows(UnsupportedOperationException.class, () -> t.remove("A"));
        assertThrows(UnsupportedOperationException.class, t::clear);
        assertThrows(UnsupportedOperationException.class, () -> t.addAll(List.of("C")));
        assertThrows(UnsupportedOperationException.class, () -> t.removeIf(x -> true));
        assertThrows(UnsupportedOperationException.class, () -> t.replaceAll(x -> x));
        assertThrows(UnsupportedOperationException.class, () -> t.sort(null));
        assertThrows(UnsupportedOperationException.class, () -> t.subList(0, 1).set(0, "C"));
        Iterator<Object> it = t.iterator();
        it.next();
        assertThrows(UnsupportedOperationException.class, it::remove);
        assertEquals(List.of("B", "A"), t);
    }

    @Test
    void iteratorWalksTheElementsAndThenThrows() {
        var t = tuple("A", "B");
        Iterator<Object> it = t.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);

        List<Object> seen = new ArrayList<>();
        t.forEach(seen::add);
        assertEquals(List.of("A", "B"), seen);
        assertEquals(List.of("A", "B"), t.stream().toList());
    }

    @Test
    void toArrayReturnsACopy() {
        var t = tuple("A", "B");
        Object[] copy = t.toArray();
        copy[0] = "changed";
        assertEquals("A", t.get(0));
        assertArrayEquals(new Object[]{"A", "B"}, t.toArray());
    }

    @Test
    void wrapDoesNotCopyAndRejectsNull() {
        Object[] array = {"A", "B"};
        var t = Tuple.wrap(array);
        array[0] = "owner changed it";   // the caller handed the array over; this is what "no copy" means
        assertEquals("owner changed it", t.get(0));
        assertThrows(NullPointerException.class, () -> Tuple.wrap(null));
    }

    @Test
    void survivesSerialization() throws Exception {
        var t = tuple("A", 2);
        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) {
            out.writeObject(t);
        }
        try (var in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            assertEquals(t, in.readObject());
        }
    }
}
