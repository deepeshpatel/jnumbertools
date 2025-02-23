package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.generator.combination.multiset.AbstractMultisetCombination.Order;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultisetCombinationMthTest {

    @Test
    void shouldGenerateCorrectCombinationsForAllOrdersAtRank10() {
        int k = 8;
        int start = 10;
        var options = new LinkedHashMap<>(Map.of("A", 10, "B", 4, "C", 5, "D", 7, "E", 4, "F", 7, "G", 2, "H", 1, "I", 1, "J", 2));
        Order[] orders = {Order.LEX, Order.REVERSE_LEX, Order.INPUT};

        for (Order order : orders) {
            var mth = new MultisetCombinationMth<>(options, k, start, 1, order).stream().toList();
            var all = new MultisetCombination<>(options, k, order).stream().toList();
            assertEquals(all.get(start), mth.get(0), "Order: " + order);
        }
    }

}