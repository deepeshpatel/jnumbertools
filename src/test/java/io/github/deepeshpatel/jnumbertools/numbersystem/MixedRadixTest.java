package io.github.deepeshpatel.jnumbertools.numbersystem;

import org.junit.Test;

public class MixedRadixTest {

    @Test
    public void shouldConvertFromDecimalToClock() {
        int[] bases = {Integer.MAX_VALUE, 24,60,60};
        var out = MixedRadix.toMixedRadix(60*60*24, bases);
        //System.out.println(Arrays.toString(out));
    }

}