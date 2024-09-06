package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import io.github.deepeshpatel.jnumbertools.TestBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;
import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;

public class SimpleCartesianProductMthTest {

    @Test
    public void shouldGenerateOutput_SimilarToRepetitivePerm_Mth_WhenProductIsWithSameListMultipleTimes() {
        var decimalDigits = List.of(0,1,2,3,4,5,6,7,8,9);
        for(int m = 2; m<1000; m+=2) {

            var result = cartesianProduct.simpleProductOf(decimalDigits)
                    .and(decimalDigits)
                    .and(decimalDigits)
                    .lexOrderMth(m, 0).stream().toList();

            var expected = permutation.repetitive(3,decimalDigits)
                    .lexOrderMth(m, 0).stream().toList();

            Assert.assertEquals(expected, result);

        }
    }

    @Test
    public void shouldGenerateOutput_SimilarToRepetitivePerm_When_M_equals_1_and_ProductIsWithSameListMultipleTimes() {
        var list = List.of("A","B","C");

        var result = cartesianProduct.simpleProductOf(list)
                .and(list)
                .and(list)
                .lexOrderMth(1, 0).stream().toList();

        var expected = permutation.repetitive(3,list)
                .lexOrder().stream().toList();

        Assert.assertEquals(expected, result);
    }


    @Test
    public void shouldGenerateCorrect_Mth_Output_RelativeTo_listOfAll_CartesianValues() {

        var list1 = List.of("A","B","C");
        var list2 = List.of(1,2,3);
        var list3 = List.of(0,1,2,3,4);

        int max = list1.size() * list2.size() * list3.size();

        for(int m=2; m<=max/2; m++) {
            var builder = cartesianProduct.simpleProductOf(list1)
                    .and(list2)
                    .and(list3);

            var result = builder.lexOrderMth(m, 0).stream().toList();
            var expected = TestBase.everyMthValue(builder.lexOrder().stream(), m);
            Assert.assertEquals(expected, result);
        }
    }

    @Test
    public void test_for_different_start_positions() {

        var builder = cartesianProduct.simpleProductOf("A","B","C").and(1,2,3);

        Assert.assertEquals("[[A, 1], [A, 3], [B, 2], [C, 1], [C, 3]]",
                builder.lexOrderMth(2,0).stream().toList().toString());

        Assert.assertEquals("[[A, 2], [B, 1], [B, 3], [C, 2]]",
                builder.lexOrderMth(2,1).stream().toList().toString());

        Assert.assertEquals("[[B, 1], [B, 3], [C, 2]]",
                builder.lexOrderMth(2,3).stream().toList().toString());
    }

}