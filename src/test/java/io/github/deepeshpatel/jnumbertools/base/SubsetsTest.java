package io.github.deepeshpatel.jnumbertools.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.errMsgNullInput;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Subsets Factory")
class SubsetsTest {

    /*
    ===============================================================================
    SUBSETS (Power Set)
    ===============================================================================
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

    @Nested
    @DisplayName("of(List<T>) | of(T... elements) | of(int n)")
    class OfTests {

        @Test
        @DisplayName("of(List<T>): valid list returns builder")
        void ofListValid() {
            var builder = subsets.of(List.of("A", "B", "C"));
            assertNotNull(builder);
        }

        @Test
        @DisplayName("of(List<T>): null input throws NPE")
        void ofListNullInput() {
            var nullExp = assertThrows(NullPointerException.class, () ->
                    subsets.of((List<String>) null));
            assertTrue(nullExp.getMessage().startsWith(errMsgNullInput));
        }

        @Test
        @DisplayName("of(T... elements): multiple elements returns builder")
        void ofVarArgsMultiple() {
            var builder = subsets.of("A", "B", "C");
            assertNotNull(builder);
        }

        @Test
        @DisplayName("of(T... elements): single element returns builder")
        void ofVarArgsSingle() {
            var singleBuilder = subsets.of("X");
            assertNotNull(singleBuilder);
        }

        @Test
        @DisplayName("of(T... elements): empty varargs returns builder")
        void ofVarArgsEmpty() {
            var emptyBuilder = subsets.of(new String[0]);
            assertNotNull(emptyBuilder);
        }

        @Test
        @DisplayName("of(T... elements): null varargs array throws NPE")
        void ofVarArgsNullArray() {
            String[] nullArray = null;
            assertThrows(NullPointerException.class, () ->
                    subsets.of(nullArray));
        }

        @Test
        @DisplayName("of(int n): positive n returns builder")
        void ofIntPositive() {
            var builder = subsets.of(3);
            assertNotNull(builder);
        }

        @Test
        @DisplayName("of(int n): n=0 returns builder")
        void ofIntZero() {
            var zeroBuilder = subsets.of(0);
            assertNotNull(zeroBuilder);
        }

        @Test
        @DisplayName("of(int n): negative n throws IAE")
        void ofIntNegative() {
            var negativeNExp = assertThrows(IllegalArgumentException.class, () ->
                    subsets.of(-1));
            assertEquals("dataSize should be ≥ 0 to generate subsets", negativeNExp.getMessage());
        }
    }
}