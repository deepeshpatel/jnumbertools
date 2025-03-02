package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;


import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.github.deepeshpatel.jnumbertools.TestBase.assertEveryMthValue;
import static io.github.deepeshpatel.jnumbertools.TestBase.combination;
import static org.junit.jupiter.api.Assertions.*;

class MultisetCombinationForSequenceTest {

    @Nested
    class MultisetCombinationMthTest {

        @Test
        void shouldGenerateCorrectCombinationsForAllOrdersAtRank10() {
            int k = 8;
            int start = 10;
            var options = new LinkedHashMap<>(Map.of("A", 10, "B", 4, "C", 5, "D", 7, "E", 4, "F", 7, "G", 2, "H", 1, "I", 1, "J", 2));
                var combBuilder = combination.multiset(options,k);
                var mth = combBuilder.lexOrderMth(1,start).stream().toList();
                var all = combBuilder.lexOrder().stream().toList();
                assertEquals(all.get(start), mth.get(0));
        }

        @Test
        void shouldGenerateMthMultisetCombinations() {
            int size = 4;
            int start = 3;
            var options = new LinkedHashMap<>(Map.of("A", 10, "B", 4, "C", 5, "D", 7, "E", 4, "F", 7, "G", 2, "H", 1, "I", 1, "J", 2));
            for (int m = 1; m <= 32; m += 2) {
                var builder = combination.multiset(options, size);
                var all = builder.lexOrder();
                var mth = builder.lexOrderMth(m, start);
                assertEveryMthValue(all.stream(), mth.stream(), start, m);
            }
        }

    }

    @Nested
    class MultisetCombinationSampleTest {
        //TODO:
    }

    @Nested
    class MultisetCombinationChoiceTest {
        //TODO:
    }

}