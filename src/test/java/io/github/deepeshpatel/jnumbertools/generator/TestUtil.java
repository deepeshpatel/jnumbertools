package io.github.deepeshpatel.jnumbertools.generator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestUtil {

    public static <T> List<List<T>> collectEveryNthValue(Iterable<List<T>> iterable, int n) {

        List<List<T>> nthValues = new ArrayList<>();

        int i=0;
        for(List<T> l: iterable) {
            if(i%n==0) {
                nthValues.add(l);
            }
            i++;
        }
        return nthValues;
    }

    public static <T> List<List<T>> iteratorToList(Iterator<List<T>> iterator) {
        List<List<T>> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);
        return list;
    }
}