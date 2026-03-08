package io.github.deepeshpatel.jnumbertools.base;

import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.errMsgNullInput;
import static org.junit.jupiter.api.Assertions.*;

class SubsetsTest {

    /*
    -------------------------------------------------------------------------------
    SUBSETS (Power Set)
    -------------------------------------------------------------------------------
    n (set size) | Range [from, to] | Mathematical     | Count | Iterator Returns
    -------------|-------------------|------------------|-------|------------------
    n = 0        | [0, 0]           | 2⁰ = 1           | 1     | [[]] (one empty subset)
    n = 0        | [0, m] where m>0 | Σ2⁰ = 1          | 1     | [[]] (one empty subset)
    n = 0        | [1, m] where m>0 | 0                | 0     | [] (empty iterator)
    n > 0        | [0, 0]           | 1                | 1     | [[]] (one empty subset)
    n > 0        | [0, n]           | 2ⁿ               | 2ⁿ    | all subsets
    n > 0        | [a, b] where 0 ≤ a ≤ b ≤ n | Σ ⁿCᵢ for i=a..b | Σ ⁿCᵢ | subsets in range
    n > 0        | a > n or b > n or a < 0 | invalid | -     | THROW IllegalArgumentException
    n > 0        | a > b             | invalid          | -     | THROW IllegalArgumentException
    null input   | any range         | invalid          | -     | THROW NullPointerException
    */

    private final Subsets subsets = new Subsets(calculator);
    private final List<String> elements = List.of("A", "B", "C");

    @Test
    void of() {
        // Test with List input
        var builder = subsets.of(elements);
        assertNotNull(builder);

        // Test null input - should throw NPE (outside rules)
        var nullExp = assertThrows(NullPointerException.class, () ->
                subsets.of((List<String>) null));
        assertTrue(nullExp.getMessage().startsWith(errMsgNullInput));

        // Note: empty list is allowed (tested in SubsetBuilderTest)
    }

    @Test
    void testOf() {
        // Test with varargs
        var builder = subsets.of("A", "B", "C");
        assertNotNull(builder);

        // Test single element
        var singleBuilder = subsets.of("X");
        assertNotNull(singleBuilder);

        // Test empty varargs (n=0) - allowed by rules
        var emptyBuilder = subsets.of(new String[0]);
        assertNotNull(emptyBuilder);

        // Test null varargs array
        String[] nullArray = null;
        assertThrows(NullPointerException.class, () ->
                subsets.of(nullArray));
    }

    @Test
    void testOf1() {
        // Test with integer range
        var builder = subsets.of(3);
        assertNotNull(builder);

        // Test n=0
        var zeroBuilder = subsets.of(0);
        assertNotNull(zeroBuilder);

        // Test negative n - should throw IAE (outside rules)
        var negativeNExp = assertThrows(IllegalArgumentException.class, () ->
                subsets.of(-1));
        assertEquals("dataSize should be ≥ 0 to generate subsets", negativeNExp.getMessage());

        // Note: n=0 is allowed (tested in SubsetBuilderTest)
    }
}