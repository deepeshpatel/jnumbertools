package io.github.deepeshpatel.jnumbertools.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Derangements Factory")
class DerangementsTest {

    /*
    ===============================================================================
    DERANGEMENT (!n)
    ===============================================================================
    n (set size) | Mathematical | Count | Iterator Returns
    -------------|--------------|-------|------------------
    n = 0        | !0 = 1       | 1     | [[]] (one empty derangement)
    n = 1        | !1 = 0       | 0     | [] (empty iterator)
    n ≥ 2        | !n           | !n    | derangements
    n < 0        | invalid      | -     | THROW IllegalArgumentException
    null input   | invalid      | -     | THROW NullPointerException
    */

    private final Derangements derangements = new Derangements(calculator);

    @Nested
    @DisplayName("of(List<T>)")
    class OfWithListTests {

        @Test
        @DisplayName("null input throws NPE")
        void nullInput() {
            var nullExp = assertThrows(NullPointerException.class, () ->
                    derangements.of((List<String>) null));
            assertTrue(nullExp.getMessage().startsWith(errMsgNullInput));
        }

        @Test
        @DisplayName("empty list (n=0): count = !0 = 1")
        void emptyList() {
            var emptyBuilder = derangements.of(List.of());
            assertEquals(BigInteger.ONE, emptyBuilder.count());
        }

        @Test
        @DisplayName("single element (n=1): count = !1 = 0")
        void singleElement() {
            var singletonBuilder = derangements.of(List.of("A"));
            assertEquals(BigInteger.ZERO, singletonBuilder.count());
        }

        @Test
        @DisplayName("n=4 elements: count = !4 = 9")
        void fourElements() {
            var builder = derangements.of(List.of("A", "B", "C", "D"));
            assertEquals(BigInteger.valueOf(9), builder.count());
        }
    }

    @Nested
    @DisplayName("of(int n)")
    class OfWithIntTests {

        @Test
        @DisplayName("negative n throws IAE")
        void negativeN() {
            var negativeNExp = assertThrows(IllegalArgumentException.class, () -> derangements.of(-1));
            assertEquals("n must be ≥ 0 for derangement generation", negativeNExp.getMessage());
        }

        @Test
        @DisplayName("derangement formula boundary values: !0=1, !1=0, !2=1, !3=2, !4=9, !5=44")
        void derangementFormula() {
            assertEquals(BigInteger.ONE,           derangements.of(0).count());
            assertEquals(BigInteger.ZERO,          derangements.of(1).count());
            assertEquals(BigInteger.ONE,           derangements.of(2).count());
            assertEquals(BigInteger.valueOf(2),    derangements.of(3).count());
            assertEquals(BigInteger.valueOf(9),    derangements.of(4).count());
            assertEquals(BigInteger.valueOf(44),   derangements.of(5).count());
        }
    }

    @Nested
    @DisplayName("of(T... elements)")
    class OfWithVarArgsTests {

        @Test
        @DisplayName("multiple elements returns builder with !4 = 9 count")
        void multipleElements() {
            var builder = derangements.of("A", "B", "C", "D");
            assertNotNull(builder);
            assertEquals(BigInteger.valueOf(9), builder.count());
        }

        @Test
        @DisplayName("empty varargs (n=0) returns builder with !0 = 1 count")
        void emptyVarArgs() {
            var emptyBuilder = derangements.of(new String[0]);
            assertEquals(BigInteger.ONE, emptyBuilder.count());
        }
    }

    @Nested
    @DisplayName("Integration with JNumberTools facade")
    class IntegrationTests {

        @Test
        @DisplayName("JNumberTools.derangements() is wired and functional")
        void shouldBeWiredIntoJNumberToolsFacade() {
            assertNotNull(JNumberTools.derangements());
            assertEquals(BigInteger.valueOf(9),
                    JNumberTools.derangements().of(4).count());
        }
    }
}
