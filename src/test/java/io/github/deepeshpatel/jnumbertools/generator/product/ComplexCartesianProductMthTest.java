package io.github.deepeshpatel.jnumbertools.generator.product;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;

public class ComplexCartesianProductMthTest {

    @Test
    public void should_generate_correctly_for_differnt_m_values() {

        var pizzaBase = List.of("Small ","Medium","Large");
        var sauce = List.of( "Tomato Ketchup","White Sauce","Green Chutney");
        var cheese = List.of( "Ricotta","Mozzarella","Cheddar");
        var toppings = List.of("tomato","capsicum","onion","paneer","corn");

        var combProduct  = cartesianProduct
                .complexProductOf(1, pizzaBase)
                .andDistinct(2, cheese)
                .andMultiSelect(2, sauce)
                .andInRange(3, toppings.size(), toppings);

        for(int m=2; m<=10; m++){
            var expected = combProduct.lexOrder().stream().toList().get(m);
            var result = combProduct.lexOrderMth(m).build();
            Assert.assertEquals(expected, result);
        }
    }

    @Test
    public void should_generate_correct_combination_for_very_large_m() {

        var smallAlphabets = IntStream.rangeClosed('a', 'z').mapToObj(c -> (char) c).toList();
        var largeAlphabets = IntStream.rangeClosed('A', 'Z').mapToObj(c -> (char) c).toList();
        var numbers = IntStream.rangeClosed(0, 20).boxed().toList();

        ComplexProductBuilder productBuilder = cartesianProduct
                .complexProductOf(13, smallAlphabets)
                .andDistinct(13, largeAlphabets)
                .andInRange(0, 21, numbers);

        var row_0         = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, K, L, M]";
        var row_100000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, K, O, X, 0, 1, 2, 4, 6, 8, 9, 10, 12, 13, 18, 20]";
        var row_200000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, K, V, W, 0, 1, 8, 9, 12, 13, 15, 17, 18, 20]";
        var row_300000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, L, P, S, 0, 3, 8, 9, 10, 11, 19]";
        var row_400000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, L, W, X, 0, 3, 4, 5, 7, 8, 9, 11, 12, 16, 18, 20]";
        var row_500000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, M, R, S, 1, 2, 3, 6, 7, 10, 13, 16, 17, 19]";
        var row_600000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, N, P, R, 0, 1, 4, 5, 14, 15, 16, 19]";
        var row_700000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, N, V, Z, 2, 3, 4, 5, 7, 8, 9, 10, 12, 14, 18, 20]";
        var row_800000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, O, U, W, 2, 4, 5, 7, 11, 13, 14, 15, 16, 18]";
        var row_900000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, P, U, Z, 1, 6, 8, 11, 12, 17, 18, 19]";

        String[] every10ToThePower_8thRow = {row_0, row_100000000, row_200000000,row_300000000, row_400000000,
                row_500000000,row_600000000,row_700000000,row_800000000, row_900000000};

        var row_1000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, J, R, S, T, 0, 1, 3, 6, 7, 8, 9, 10, 13, 15, 17, 18, 20]";
        var row_2000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, K, R, W, X, 0, 1, 2, 3, 5, 6, 8, 9, 11, 12, 13, 19]";
        var row_3000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, M, N, U, V, 0, 1, 2, 4, 5, 10, 11, 13, 15, 16, 19]";
        var row_4000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, I, O, P, S, Y, 0, 1, 3, 4, 6, 7, 10, 13, 14, 15]";
        var row_5000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, J, K, L, M, R, 5, 6, 7, 9, 10, 13, 16, 19]";
        var row_6000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, J, L, M, P, T, 1, 3, 8, 9, 10, 14]";
        var row_7000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, J, M, P, S, T, 0, 3, 4, 5, 6, 7, 9, 10, 13, 15, 17, 19, 20]";
        var row_8000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, J, O, S, T, U, 0, 1, 3, 4, 8, 12, 14, 15, 16, 17, 18, 19]";
        var row_9000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, K, L, N, P, S, 0, 1, 4, 5, 7, 9, 11, 15, 17, 19, 20]";

        String[] every10ToThePower_9thRow = {row_0, row_1000000000, row_2000000000,row_3000000000,row_4000000000,
                row_5000000000, row_6000000000, row_7000000000, row_8000000000, row_9000000000};

        var row_10000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, H, K, M, R, S, V, 0, 2, 3, 4, 11, 12, 13, 15, 19, 20]";
        var row_20000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, I, J, M, P, T, Y, 0, 4, 5, 6, 7, 11, 13, 14, 15, 16, 17, 18]";
        var row_30000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, I, P, Q, R, T, Y, 0, 2, 6, 9, 14, 15, 17, 18]";
        var row_40000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, J, S, T, U, W, Y, 3, 5, 6, 8, 10, 11, 12, 15, 16, 19]";
        var row_50000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, G, L, P, S, X, Y, Z, 0, 2, 5, 6, 7, 9, 10, 11, 14, 16, 17, 18, 20]";
        var row_60000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, H, I, J, O, U, X, Z, 0, 2, 7, 8, 11, 12, 14, 17, 18]";
        var row_70000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, H, J, K, L, M, T, V, 1, 2, 4, 5, 6, 11, 14, 15, 16, 18, 20]";
        var row_80000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, H, K, L, N, S, U, X, 0, 1, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15, 18, 19, 20]";
        var row_90000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, H, M, N, P, R, V, Z, 0, 1, 2, 6, 7, 9, 11, 14, 16, 19]";

        String[] every10ToThePower_10thRow = {row_0, row_10000000000, row_20000000000, row_30000000000, row_40000000000,
                row_50000000000, row_60000000000, row_70000000000, row_80000000000, row_90000000000};

        var row_100000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, F, I, J, L, O, S, U, W, 0, 2, 3, 4, 6, 7, 9, 11, 13, 15, 17, 20]";
        var row_200000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, G, H, O, P, V, W, Y, Z, 1, 2, 7, 8, 10, 12, 14, 17, 19, 20]";
        var row_300000000000 = "[a, b, c, d, e, f, g, h, i, j, k, l, m, A, B, C, D, E, H, J, K, Q, R, S, W, Y, 1, 4, 5, 9, 11, 12, 15, 20]";


        String[] every10ToThePower_11thRow = {row_0, row_100000000000, row_200000000000, row_300000000000};

        testForEveryMth(100000000L,  every10ToThePower_8thRow,  productBuilder);
        testForEveryMth(1000000000L, every10ToThePower_9thRow, productBuilder);
        testForEveryMth(10000000000L, every10ToThePower_10thRow, productBuilder);
        testForEveryMth(100000000000L, every10ToThePower_11thRow, productBuilder);

//        var allValuesIterator = productBuilder.lexOrder().stream().iterator();
//        long c = 0;
//        System.out.println("Start..");
//
//        long skip = 1000000000L;
//
//        System.out.println(allValuesIterator.next());
//        for (long j = skip; j < totalCombinations; j+=skip) {
//            for(int i=1; i<skip; i++) {
//                allValuesIterator.next();
//            }
//            System.out.println(j + " " + allValuesIterator.next());
//        }
    }

    private void testForEveryMth(long m, String[] rows, ComplexProductBuilder builder) {
        long mm = -m;
        for (String row : rows) {
            Assert.assertEquals(row, builder.lexOrderMth(mm += m).build().toString());
        }
    }

}