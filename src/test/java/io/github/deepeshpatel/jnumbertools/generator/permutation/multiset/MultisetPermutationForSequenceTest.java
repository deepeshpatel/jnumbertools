package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;


import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;
import static org.junit.jupiter.api.Assertions.*;

class MultisetPermutationForSequenceTest {

    @Nested
    class MultisetPermutationSampleTest {

        @Test
        void shouldGenerateSampledPermutationsWithinBounds() {
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1, "C", 1));
            int sampleSize = 3;
                var permBuilder = permutation.multiset(options);
                var sampled = permBuilder.sample(sampleSize).stream().toList();

                assertEquals(sampleSize, sampled.size(), "Sample size should match requested size for order: ");
                assertTrue(sampled.stream().allMatch(p -> p.size() == 4));
                assertEquals(sampled.stream().distinct().count(), sampled.size(), "Sampled permutations should be unique");

        }

        @Test
        void shouldThrowExceptionForInvalidSampleSize() {
            var options = new LinkedHashMap<>(Map.of("A", 1, "B", 1));
            var permBuilder = permutation.multiset(options);
            assertThrows(IllegalArgumentException.class, () -> permBuilder.sample(0), "Sample size of 0 should throw exception");
            assertThrows(IllegalArgumentException.class, () -> permBuilder.sample(3), "Sample size exceeding total permutations should throw exception");
        }
    }

    @Nested
    class MultisetPermutationChoiceTest {

        @Test
        void shouldGenerateChoicePermutationsWithPossibleDuplicates() {
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1));
            int choiceSize = 4;
                var permBuilder = permutation.multiset(options);
                var chosen = permBuilder.choice(choiceSize).stream().toList();

                //System.out.println(chosen);

                assertEquals(choiceSize, chosen.size(), "Choice size should match requested size for order: ");
                assertTrue(chosen.stream().allMatch(p -> p.size() == 3), "All permutations should have correct length for order: ");
                // With replacement, duplicates are allowed, so we don't check for uniqueness
        }

        @Test
        void shouldThrowExceptionForInvalidChoiceSize() {
            var options = new LinkedHashMap<>(Map.of("A", 1, "B", 1));
            var permBuilder = permutation.multiset(options);
            assertThrows(IllegalArgumentException.class, () -> permBuilder.choice(0), "Choice size of 0 should throw exception");
        }
    }
}