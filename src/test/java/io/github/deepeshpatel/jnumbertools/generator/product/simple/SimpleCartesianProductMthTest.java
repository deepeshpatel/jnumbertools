package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import io.github.deepeshpatel.jnumbertools.TestBase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class SimpleCartesianProductMthTest {

    @Test
    void shouldGenerateOutput_SimilarToRepetitivePerm_Mth_WhenProductIsWithSameListMultipleTimes() {
        var decimalDigits = List.of(0,1,2,3,4,5,6,7,8,9);
        for(int m = 2; m < 1000; m += 2) {

            var result = cartesianProduct.simpleProductOf(decimalDigits)
                    .and(decimalDigits)
                    .and(decimalDigits)
                    .lexOrderMth(m, 0).stream().toList();

            var expected = permutation.repetitive(3, decimalDigits)
                    .lexOrderMth(m, 0).stream().toList();

            assertEquals(expected, result);
        }
    }

    @Test
    void shouldGenerateOutput_SimilarToRepetitivePerm_When_M_equals_1_and_ProductIsWithSameListMultipleTimes() {

        var result = cartesianProduct.simpleProductOf(A_B_C)
                .and(A_B_C)
                .and(A_B_C)
                .lexOrderMth(1, 0).stream().toList();

        var expected = permutation.repetitive(3, A_B_C)
                .lexOrder().stream().toList();

        assertIterableEquals(expected, result);
    }

    @Test
    void shouldGenerateCorrect_Mth_Output_RelativeTo_listOfAll_CartesianValues() {


        int max = A_B_C.size() * num_1_2_3.size() * num_1_to_4.size();
        int start=3;

        for (int m = 2; m <= max / 2; m++) {
            var builder = cartesianProduct
                    .simpleProductOf(A_B_C)
                    .and(num_1_2_3)
                    .and(num_1_to_4);

            var all = builder.lexOrder().stream();
            var mth = builder.lexOrderMth(m, start).stream();
            assertEveryMthValue(all,mth, start, m);
        }
    }

    @Test
    void test_for_different_start_positions() {

        var builder = cartesianProduct.simpleProductOf(A_B_C).and(num_1_2_3);

        assertEquals("[[A, 1], [A, 3], [B, 2], [C, 1], [C, 3]]",
                builder.lexOrderMth(2, 0).stream().toList().toString());

        assertEquals("[[A, 2], [B, 1], [B, 3], [C, 2]]",
                builder.lexOrderMth(2, 1).stream().toList().toString());

        assertEquals("[[B, 1], [B, 3], [C, 2]]",
                builder.lexOrderMth(2, 3).stream().toList().toString());
    }

}
