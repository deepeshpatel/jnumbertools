package io.github.deepeshpatel.jnumbertools.generator.subset;

import org.junit.jupiter.api.Test;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubsetGeneratorMthTest {

    @Test
    void shouldGenerate_AllSubsets_For_4Elements_and_M_Equals1() {

        var iter_expected = subsets.of(A_B_C_D).all().lexOrder().iterator();
        var iter_testResult = subsets.of(A_B_C_D).all().lexOrderMth(1, 0).iterator();
        while (iter_expected.hasNext()) {
            assertEquals(iter_expected.next(), iter_testResult.next());
        }
    }

    @Test
    void should_generate_all_subsets_for_6elements_and_different_m_for_range_3_6() {
        int start = 0;
        for (int from = 1; from < num_0_to_5.size(); from++) {
            for (int to = from; to < num_0_to_5.size(); to++) {
                for (int m = 1; m < 6; m++) {
                    var all = subsets.of(num_0_to_5).inRange(from, to).lexOrder().stream();
                    var mth = subsets.of(num_0_to_5).inRange(from, to).lexOrderMth(m, start).stream();
                    assertEveryMthValue(all, mth, start, m);
                }
            }
        }
    }

    @Test
    void should_generate_all_subsets_for_4elements_and_different_m() {
        int start = 3;
        for (int m = 1; m <= 15; m+=2) {
            var all = subsets.of(A_B_C_D).all().lexOrder().stream();
            var mth = subsets.of(A_B_C_D).all().lexOrderMth(m, start).stream();
            assertEveryMthValue(all, mth, start, m);
        }
    }

    @Test
    void test_start_parameter_greater_than_0() {
        var builder = subsets.of(A_B_C_D);

        var listOfAll = builder.all()
                .lexOrderMth(5, 3).stream().toList();
        assertEquals("[[C], [B, C], [A, C, D]]", listOfAll.toString());

        var listOfRange = builder.inRange(2, 3)
                .lexOrderMth(5, 3).stream().toList();
        assertEquals("[[B, C], [A, C, D]]", listOfRange.toString());
    }
}
