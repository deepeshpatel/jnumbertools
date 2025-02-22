package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MultisetCombinationMthTest {

    @Test
    void shouldGenerateEveryMthCombination() {
        int k=8;
        int m=1000;
        int start = 10;

        var options = Map.of(
                "A", 10, "B", 4, "C", 5, "D", 7, "E", 4,
                "F", 7, "G", 2, "H", 1, "I", 1, "J", 2
        );

        var result = JNumberTools.combinations().multiset(options, k)
                .lexOrderMth(m,start).stream().toList();

        var allItems = JNumberTools.combinations().multiset(options, k)
                .lexOrder().stream().toList();

        int i=start;
        for(var e: result) {
            assertEquals(allItems.get(i), e);
            i+=m;
        }
    }

}