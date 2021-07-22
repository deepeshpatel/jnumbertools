package io.github.deepeshpatel.jnumbertools.generator;

import java.util.ArrayList;
import java.util.List;

public class TestUtil {

    public static <T> List<List<T>> collectSkippedValues(Iterable<List<T>> iterable, int skip) {

        List<List<T>> skippedValues = new ArrayList<>();
        int i=0;
        for(List<T> l: iterable) {
            if(skip ==0 || i%skip==0) {
                skippedValues.add(l);
            }
            i++;
        }
        return skippedValues;
    }
}