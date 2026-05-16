package io.github.deepeshpatel.jnumbertools.base;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

class DerangementsTest {

    /*
    -------------------------------------------------------------------------------
    DERANGEMENT (!n)
    -------------------------------------------------------------------------------
    n (set size) | Mathematical | Count | Iterator Returns
    -------------|--------------|-------|------------------
    n = 0        | !0 = 1       | 1     | [[]] (one empty derangement)
    n = 1        | !1 = 0       | 0     | [] (empty iterator)
    n ≥ 2        | !n           | !n    | derangements
    n < 0        | invalid      | -     | THROW IllegalArgumentException
    null input   | invalid      | -     | THROW NullPointerException
    */

    private final Derangements derangements = new Derangements(calculator);

    @Test
    void of_listVariant() {
        // Test null input - should throw NPE (outside rules)
        var nullExp = assertThrows(NullPointerException.class, () ->
                derangements.of((List<String>) null));
        assertTrue(nullExp.getMessage().startsWith(errMsgNullInput));

        // Empty list (n=0) is allowed, count = !0 = 1
        var emptyBuilder = derangements.of(List.of());
        assertEquals(BigInteger.ONE, emptyBuilder.count());

        // Single element (n=1) is allowed, count = !1 = 0
        var singletonBuilder = derangements.of(List.of("A"));
        assertEquals(BigInteger.ZERO, singletonBuilder.count());

        // n=4, count = !4 = 9
        var builder = derangements.of(List.of("A", "B", "C", "D"));
        assertEquals(BigInteger.valueOf(9), builder.count());
    }

    @Test
    void of_intVariant() {
        // Test negative n - should throw IAE (outside rules)
        var negativeNExp = assertThrows(IllegalArgumentException.class, () -> derangements.of(-1));
        assertEquals("n must be ≥ 0 for derangement generation", negativeNExp.getMessage());

        // Boundary counts: !0=1, !1=0, !2=1, !3=2, !4=9, !5=44
        assertEquals(BigInteger.ONE,           derangements.of(0).count());
        assertEquals(BigInteger.ZERO,          derangements.of(1).count());
        assertEquals(BigInteger.ONE,           derangements.of(2).count());
        assertEquals(BigInteger.valueOf(2),    derangements.of(3).count());
        assertEquals(BigInteger.valueOf(9),    derangements.of(4).count());
        assertEquals(BigInteger.valueOf(44),   derangements.of(5).count());
    }

    @Test
    void of_varargsVariant() {
        // Standard varargs path
        var builder = derangements.of("A", "B", "C", "D");
        assertNotNull(builder);
        assertEquals(BigInteger.valueOf(9), builder.count());

        // Empty varargs (n=0) - allowed by rules
        var emptyBuilder = derangements.of(new String[0]);
        assertEquals(BigInteger.ONE, emptyBuilder.count());
    }

    @Test
    void shouldBeWiredIntoJNumberToolsFacade() {
        // JNumberTools.derangements() must return a usable factory
        assertNotNull(JNumberTools.derangements());
        assertEquals(BigInteger.valueOf(9),
                JNumberTools.derangements().of(4).count());
    }
}

