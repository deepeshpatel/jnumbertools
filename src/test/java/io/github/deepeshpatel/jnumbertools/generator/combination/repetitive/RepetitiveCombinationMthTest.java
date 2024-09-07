package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import org.junit.Assert;
import org.junit.Test;

import static io.github.deepeshpatel.jnumbertools.TestBase.combination;

public class RepetitiveCombinationMthTest  {

    @Test
    public void should_generate_correct_combinations_for_r_greater_then_n() {
        int n = 3;
        int r = 5;
        var expected = combination.repetitive(3,5).lexOrder().stream().toList();
        var output = combination.repetitive(3,5).lexOrderMth(1,0).stream().toList();
        Assert.assertEquals(expected, output);
    }

    //TODO: Add more tests

}