package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class MultisetCombinationMthTest {

    @Test
    void shouldGenerateEveryMthCombination() {
        int k=8;
        int m=1000;
        int start = 10;
        int[] frequencies = {10,4,5,7,4,7,2,1,1,2};
        var items = Arrays.asList("A","B","C","D","E","F","G","H","I","J");

        var result = JNumberTools.combinations().multiset(items, frequencies, k)
                .lexOrderMth(m,start).stream().toList();

        var allItems = JNumberTools.combinations().multiset(items, frequencies, k)
                .lexOrder().stream().toList();

        int i=start;
        for(var e: result) {
            assertEquals(allItems.get(i), e);
            i+=m;
        }
    }

}