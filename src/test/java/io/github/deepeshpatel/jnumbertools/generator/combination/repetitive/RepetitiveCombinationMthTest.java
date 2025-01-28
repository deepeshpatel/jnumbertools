package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.combination;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class RepetitiveCombinationMthTest {

    @Test
    void should_generate_correct_combinations_for_r_greater_than_n() {
        int n = 3;
        int r = 5;
        var expected = combination.repetitive(n, r).lexOrder().stream().toList();
        var output = combination.repetitive(n, r).lexOrderMth(1, 0).stream().toList();
        assertIterableEquals(expected, output);
    }

    @Test
    void should_generate_correct_combination_for_r_equals_1() {
        int n = 4;
        int r = 1;
        var expected = combination.repetitive(n, r).lexOrder().stream().toList();
        var output = combination.repetitive(n, r).lexOrderMth(1, 0).stream().toList();
        assertIterableEquals(expected, output);
    }

    @Test
    void should_generate_correct_combination_for_n_equals_1() {
        int n = 1;
        int r = 5;
        var expected = combination.repetitive(n, r).lexOrder().stream().toList();
        var output = combination.repetitive(n, r).lexOrderMth(1, 0).stream().toList();
        assertIterableEquals(expected, output);
    }

    @Test
    void should_generate_correct_combination_for_mth_value() {
        int n = 3;
        int r = 3;
        var expected = List.of(
                of(0, 1, 1),
                of(1, 2, 2)
        );

        var output = combination.repetitive(n, r).lexOrderMth(5, 3).stream().toList();
        assertIterableEquals(expected, output);
    }

    @Test
    void should_return_empty_lists_for_m_out_of_bounds() {
        int n = 2;
        int r = 2;
        var list = combination.repetitive(n, r).lexOrderMth(8, 8).stream().toList();
        assertTrue(list.isEmpty());
    }

    @Test
    void should_return_empty_list_when_r_equals_0() {
        int n = 3;
        int r = 0;
        var expected = combination.repetitive(n, r).lexOrder().stream().toList();
        var output = combination.repetitive(n, r).lexOrderMth(1, 0).stream().toList();
        assertIterableEquals(expected, output);
    }

    @Test
    void should_throw_exception_for_negative_r_value() {
        int n = 3;
        int r = -2;
        assertThrows(IllegalArgumentException.class, () ->
                combination.repetitive(n, r).lexOrderMth(1, 0));
    }
}