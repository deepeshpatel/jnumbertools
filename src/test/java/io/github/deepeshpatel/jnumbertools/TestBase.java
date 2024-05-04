package io.github.deepeshpatel.jnumbertools;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;
import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;

public class TestBase {

    public static Calculator calculator = new Calculator();
    public static JNumberTools tools = new JNumberTools(calculator);

    static final int minN = 2000;
    static final int maxN = minN;

    static {
        calculator.memoizeAllCombinations(1,maxN);
    }


}
