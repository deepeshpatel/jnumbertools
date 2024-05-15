package io.github.deepeshpatel.jnumbertools;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;
import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;

public class TestBase {

    public static Calculator calculator = new Calculator();
    public static JNumberTools tools = new JNumberTools(calculator);

}
