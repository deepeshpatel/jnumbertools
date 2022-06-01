package io.github.deepeshpatel.jnumbertools.generator;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestUtil {

    public static <T> List<List<T>> collectEveryNthValue(Stream<List<T>> stream, int n) {
        final int[] j = {0};
        return stream.filter(e-> j[0]++%n==0).collect(Collectors.toList());
    }
}