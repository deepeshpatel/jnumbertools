package io.github.deepeshpatel.jnumbertools.generator.product.constrained;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class ConstrainedCartesianProductMthTest {

    @Test
    void should_generate_correctly_for_different_m_values() {

        var pizzaBase = of("Small ","Medium","Large");
        var sauce = of( "Tomato Ketchup","White Sauce","Green Chutney");
        var cheese = of( "Ricotta","Mozzarella","Cheddar");
        var toppings = of("tomato","capsicum","onion","paneer","corn");

        var combProduct  = cartesianProduct
                .constrainedProductOf(1, pizzaBase)
                .andDistinct(2, cheese)
                .andMultiSelect(2, sauce)
                .andInRange(3, toppings.size(), toppings);

        for(int m = 2; m <= 10; m++){
            List expected = combProduct.lexOrder().stream().toList().get(m);
            List result = (List) combProduct.lexOrderMth(m, m).stream().toList().get(0);
            assertIterableEquals(expected, result);
        }
     }

    @Test
    void should_generate_correct_mth_value() {

        var pizzaBase = of("Small ","Medium","Large");
        var sauce = of( "Tomato Ketchup","White Sauce","Green Chutney");
        var cheese = of( "Ricotta","Mozzarella","Cheddar");
        var toppings = of("tomato","capsicum","onion","paneer","corn");

        var combProduct  = cartesianProduct
                .constrainedProductOf(1, pizzaBase)
                .andDistinct(2, cheese)
                .andMultiSelect(2, sauce)
                .andInRange(3, toppings.size(), toppings);

        int start = 800;
        int m = 50;

        var all = combProduct.lexOrder().stream().toList();
        var mth = combProduct.lexOrderMth(m, start).iterator();

        assertEquals(all.get(start), mth.next());
        assertEquals(all.get(start + m), mth.next());
    }

    @Test
    void should_generate_correct_combination_for_very_large_m() {

        var smallAlphabets = IntStream.rangeClosed('a', 'z').mapToObj(c -> (char) c).toList();
        var largeAlphabets = IntStream.rangeClosed('A', 'Z').mapToObj(c -> (char) c).toList();
        var numbers = IntStream.rangeClosed(0, 20).boxed().toList();

        ConstrainedProductBuilder productBuilder = cartesianProduct
                .constrainedProductOf(13, smallAlphabets)
                .andDistinct(13, largeAlphabets)
                .andInRange(0, 21, numbers);

        String[] every10ToThePower_8thRow = {
                "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, K, L, M]",
                "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, K, O, X, 0, 1, 2, 4, 6, 8, 9, 10, 12, 13, 18, 20]",
                "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, K, V, W, 0, 1, 8, 9, 12, 13, 15, 17, 18, 20]",
                "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, L, P, S, 0, 3, 8, 9, 10, 11, 19]",
                "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, L, W, X, 0, 3, 4, 5, 7, 8, 9, 11, 12, 16, 18, 20]",
                "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, M, R, S, 1, 2, 3, 6, 7, 10, 13, 16, 17, 19]",
                "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, N, P, R, 0, 1, 4, 5, 14, 15, 16, 19]",
                "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, N, V, Z, 2, 3, 4, 5, 7, 8, 9, 10, 12, 14, 18, 20]",
                "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, O, U, W, 2, 4, 5, 7, 11, 13, 14, 15, 16, 18]",
                "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, P, U, Z, 1, 6, 8, 11, 12, 17, 18, 19]"
        };

        testForEveryMth(100000000L, every10ToThePower_8thRow, productBuilder);
    }

    private void testForEveryMth(long m, String[] rows, ConstrainedProductBuilder builder) {
        long mm = -m;
        for (String row : rows) {
            mm += m;
            assertEquals(row, builder.lexOrderMth(mm, mm).iterator().next().toString());
            //assertEquals(row, builder.lexOrderMth(mm += m, 0).build().toString());
        }
    }
}
