
package io.github.deepeshpatel.jnumbertools.benchmarks;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.ConditionalProductBuilder;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * JMH benchmark for conditional Cartesian product traversal.
 *
 * Compares four approaches using identical constraints and traversal limits:
 *
 * 1. ConditionalProductBuilder
 * 2. Naive Java Stream: generate full tuples, then filter()
 * 3. Pruned Java Stream: validate constraints during recursion
 * 4. Imperative recursive backtracking
 *
 * Correctness and ordering are verified during trial setup, outside
 * the timed benchmark methods.
 *
 * Each benchmark operation traverses up to the first 1,000 valid tuples.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 1, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class ConditionalProductBenchmark{

    private static final int DIMENSIONS = 3;
    private static final int FIRST_N = 1_000;
    private static final long MAX_PARTIAL_EXPANSIONS = 5_000_000L;

    // -------------------------------------------------------------------------
    // JMH state
    // -------------------------------------------------------------------------

    @State(Scope.Thread)
    public static class BenchmarkState {

        @Param({
                "25_LOW_SELECTIVITY",
                "50_LOW_SELECTIVITY",
                "100_LOW_SELECTIVITY",
                "150_LOW_SELECTIVITY",
                "200_LOW_SELECTIVITY",
                "250_LOW_SELECTIVITY",
                "25_HIGHER_SELECTIVITY",
                "50_HIGHER_SELECTIVITY",
                "100_HIGHER_SELECTIVITY",
                "150_HIGHER_SELECTIVITY",
                "200_HIGHER_SELECTIVITY",
                "250_HIGHER_SELECTIVITY"
        })
        public String scenarioName;

        Scenario scenario;
        ConditionalProductBuilder builder;

        @Setup(Level.Trial)
        public void setup() {

            String[] parts = scenarioName.split("_", 2);

            int listSize = Integer.parseInt(parts[0]);
            Profile profile = Profile.valueOf(parts[1]);

            scenario = createScenario(listSize, profile);
            builder = createBuilder(scenario);

            verifyFirstN(scenario, builder, FIRST_N);
        }
    }

    // -------------------------------------------------------------------------
    // 1. ConditionalProductBuilder
    // -------------------------------------------------------------------------

    @Benchmark
    public TraversalResult conditionalProductBuilder(BenchmarkState state) {

        Checksum checksum = new Checksum();

        state.builder.lexOrder().stream()
                .limit(FIRST_N)
                .forEach(checksum::accept);

        return checksum.result(true);
    }

    // -------------------------------------------------------------------------
    // 2. Naive Java Stream: Cartesian product followed by filter()
    // -------------------------------------------------------------------------

    @Benchmark
    public TraversalResult javaStreamFilter(BenchmarkState state) {

        Checksum checksum = new Checksum();
        WorkBudget budget = new WorkBudget();
        boolean completed = true;

        try {
            naiveStream(
                    state.scenario,
                    0,
                    List.of(),
                    budget
            )
                    .filter(tuple -> isValid(tuple, state.scenario))
                    .limit(FIRST_N)
                    .forEach(checksum::accept);

        } catch (WorkLimitExceeded ignored) {
            completed = false;
        }

        return checksum.result(completed);
    }

    // -------------------------------------------------------------------------
    // 3. Pruned recursive Java Stream
    // -------------------------------------------------------------------------

    @Benchmark
    public TraversalResult prunedStream(BenchmarkState state) {

        Checksum checksum = new Checksum();
        WorkBudget budget = new WorkBudget();
        boolean completed = true;

        try {
            prunedStream(
                    state.scenario,
                    0,
                    List.of(),
                    budget
            )
                    .limit(FIRST_N)
                    .forEach(checksum::accept);

        } catch (WorkLimitExceeded ignored) {
            completed = false;
        }

        return checksum.result(completed);
    }

    // -------------------------------------------------------------------------
    // 4. Imperative recursive backtracking
    // -------------------------------------------------------------------------

    @Benchmark
    public TraversalResult imperativeBacktracking(BenchmarkState state) {

        ChecksumSink sink = new ChecksumSink(FIRST_N);
        boolean completed = true;

        try {
            imperativeGenerate(
                    state.scenario,
                    0,
                    new int[DIMENSIONS],
                    new WorkBudget(),
                    sink
            );

        } catch (WorkLimitExceeded ignored) {
            completed = false;
        }

        return sink.checksum.result(completed);
    }

    // -------------------------------------------------------------------------
    // Scenario and builder creation
    // -------------------------------------------------------------------------

    private static Scenario createScenario(
            int listSize,
            Profile profile) {

        List<List<Integer>> lists = new ArrayList<>(DIMENSIONS);

        for (int i = 0; i < DIMENSIONS; i++) {

            List<Integer> values = new ArrayList<>(listSize);

            for (int value = 0; value < listSize; value++) {
                values.add(value);
            }

            lists.add(List.copyOf(values));
        }

        List<Integer> triggerValues = new ArrayList<>();

        if (profile == Profile.HIGHER_SELECTIVITY) {

            int triggerSize = triggerSize(listSize);

            for (int value = 0; value < triggerSize; value++) {
                triggerValues.add(value);
            }
        }

        return new Scenario(
                DIMENSIONS,
                listSize,
                profile,
                List.copyOf(lists),
                List.copyOf(triggerValues)
        );
    }

    private static ConditionalProductBuilder createBuilder(
            Scenario scenario) {

        ConditionalProductBuilder builder =
                JNumberTools.cartesianProduct()
                        .conditionalProductOf(scenario.lists.get(0));

        for (int d = 1; d < scenario.dimensions; d++) {
            builder = builder.and(scenario.lists.get(d));
        }

        for (int i = 0; i < scenario.dimensions - 1; i++) {

            List<Integer> current = scenario.lists.get(i);
            List<Integer> next = scenario.lists.get(i + 1);

            if (scenario.profile == Profile.LOW_SELECTIVITY) {

                builder = builder.when(current)
                        .is(0)
                        .then(next)
                        .isNot(0);

            } else {

                builder = builder.when(current)
                        .isOneOf(scenario.triggerValues)
                        .then(next)
                        .is(0);
            }
        }

        return builder;
    }

    private static int triggerSize(int listSize) {
        return (listSize + 9) / 10;
    }

    // -------------------------------------------------------------------------
    // Naive recursive Stream
    // -------------------------------------------------------------------------

    private static Stream<List<Integer>> naiveStream(
            Scenario scenario,
            int depth,
            List<Integer> prefix,
            WorkBudget budget) {

        if (depth == scenario.dimensions) {
            return Stream.of(List.copyOf(prefix));
        }

        return scenario.lists.get(depth).stream()
                .flatMap(value -> {

                    budget.expand();

                    List<Integer> nextPrefix = new ArrayList<>(prefix);
                    nextPrefix.add(value);

                    return naiveStream(
                            scenario,
                            depth + 1,
                            nextPrefix,
                            budget
                    );
                });
    }

    // -------------------------------------------------------------------------
    // Pruned recursive Stream
    // -------------------------------------------------------------------------

    private static Stream<List<Integer>> prunedStream(
            Scenario scenario,
            int depth,
            List<Integer> prefix,
            WorkBudget budget) {

        if (depth == scenario.dimensions) {
            return Stream.of(List.copyOf(prefix));
        }

        return scenario.lists.get(depth).stream()
                .flatMap(value -> {

                    budget.expand();

                    if (depth > 0) {

                        int previous = prefix.get(depth - 1);

                        if (!isValidPair(scenario, previous, value)) {
                            return Stream.empty();
                        }
                    }

                    List<Integer> nextPrefix = new ArrayList<>(prefix);
                    nextPrefix.add(value);

                    return prunedStream(
                            scenario,
                            depth + 1,
                            nextPrefix,
                            budget
                    );
                });
    }

    // -------------------------------------------------------------------------
    // Imperative recursive backtracking
    // -------------------------------------------------------------------------

    private static void imperativeGenerate(
            Scenario scenario,
            int depth,
            int[] tuple,
            WorkBudget budget,
            TupleSink sink) {

        if (sink.isFull()) {
            return;
        }

        if (depth == scenario.dimensions) {
            sink.accept(tuple, scenario.dimensions);
            return;
        }

        for (int value : scenario.lists.get(depth)) {

            if (sink.isFull()) {
                return;
            }

            budget.expand();

            if (depth > 0) {

                int previous = tuple[depth - 1];

                if (!isValidPair(scenario, previous, value)) {
                    continue;
                }
            }

            tuple[depth] = value;

            imperativeGenerate(
                    scenario,
                    depth + 1,
                    tuple,
                    budget,
                    sink
            );
        }
    }

    // -------------------------------------------------------------------------
    // Constraint validation
    // -------------------------------------------------------------------------

    private static boolean isValid(
            List<Integer> tuple,
            Scenario scenario) {

        for (int i = 0; i < tuple.size() - 1; i++) {

            if (!isValidPair(
                    scenario,
                    tuple.get(i),
                    tuple.get(i + 1))) {

                return false;
            }
        }

        return true;
    }

    private static boolean isValidPair(
            Scenario scenario,
            int current,
            int next) {

        if (scenario.profile == Profile.LOW_SELECTIVITY) {
            return current != 0 || next != 0;
        }

        return current >= triggerSize(scenario.listSize) || next == 0;
    }

    // -------------------------------------------------------------------------
    // Correctness and ordering verification
    // -------------------------------------------------------------------------

    private static void verifyFirstN(
            Scenario scenario,
            ConditionalProductBuilder builder,
            int limit) {

        List<List<Integer>> expected =
                materializeBuilder(builder, limit);

        MaterializationResult naive =
                materializeNaive(scenario, limit);

        MaterializationResult pruned =
                materializePruned(scenario, limit);

        MaterializationResult imperative =
                materializeImperative(scenario, limit);

        if (!naive.completed || !pruned.completed || !imperative.completed) {
            throw new IllegalStateException(
                    "Correctness verification exceeded the work budget: "
                            + scenario
            );
        }

        if (!expected.equals(naive.tuples)) {
            throw new IllegalStateException(
                    "Builder and Java Stream filter() results differ: "
                            + scenario
            );
        }

        if (!expected.equals(pruned.tuples)) {
            throw new IllegalStateException(
                    "Builder and pruned Stream results differ: "
                            + scenario
            );
        }

        if (!expected.equals(imperative.tuples)) {
            throw new IllegalStateException(
                    "Builder and imperative results differ: "
                            + scenario
            );
        }
    }

    private static List<List<Integer>> materializeBuilder(
            ConditionalProductBuilder builder,
            int limit) {

        return builder.lexOrder().stream()
                .limit(limit)
                .map(tuple -> {

                    List<Integer> values = new ArrayList<>(tuple.size());

                    for (Object value : tuple) {
                        values.add((Integer) value);
                    }

                    return List.copyOf(values);
                })
                .toList();
    }

    private static MaterializationResult materializeNaive(
            Scenario scenario,
            int limit) {

        List<List<Integer>> result = new ArrayList<>(limit);
        boolean completed = true;

        try {
            naiveStream(scenario, 0, List.of(), new WorkBudget())
                    .filter(tuple -> isValid(tuple, scenario))
                    .limit(limit)
                    .forEach(result::add);

        } catch (WorkLimitExceeded ignored) {
            completed = false;
        }

        return new MaterializationResult(result, completed);
    }

    private static MaterializationResult materializePruned(
            Scenario scenario,
            int limit) {

        List<List<Integer>> result = new ArrayList<>(limit);
        boolean completed = true;

        try {
            prunedStream(scenario, 0, List.of(), new WorkBudget())
                    .limit(limit)
                    .forEach(result::add);

        } catch (WorkLimitExceeded ignored) {
            completed = false;
        }

        return new MaterializationResult(result, completed);
    }

    private static MaterializationResult materializeImperative(
            Scenario scenario,
            int limit) {

        ListSink sink = new ListSink(limit);
        boolean completed = true;

        try {
            imperativeGenerate(
                    scenario,
                    0,
                    new int[scenario.dimensions],
                    new WorkBudget(),
                    sink
            );

        } catch (WorkLimitExceeded ignored) {
            completed = false;
        }

        return new MaterializationResult(sink.result, completed);
    }

    // -------------------------------------------------------------------------
    // Result and checksum types
    // -------------------------------------------------------------------------

    public record TraversalResult(
            long tupleCount,
            long checksum,
            boolean completed) {
    }

    private record MaterializationResult(
            List<List<Integer>> tuples,
            boolean completed) {
    }

    /**
     * Computes the same checksum directly from each tuple representation.
     * No intermediate tuple collection is created.
     */
    private static final class Checksum {

        private long tupleCount;
        private long hash = 1;

        void accept(List<?> tuple) {

            for (Object value : tuple) {
                acceptValue((Integer) value);
            }

            finishTuple();
        }

        void accept(int[] tuple, int length) {

            for (int i = 0; i < length; i++) {
                acceptValue(tuple[i]);
            }

            finishTuple();
        }

        private void acceptValue(int value) {
            hash = hash * 31 + value;
        }

        private void finishTuple() {
            hash = hash * 31 + 1;
            tupleCount++;
        }

        TraversalResult result(boolean completed) {
            return new TraversalResult(tupleCount, hash, completed);
        }
    }

    // -------------------------------------------------------------------------
    // Imperative traversal sinks
    // -------------------------------------------------------------------------

    private interface TupleSink {

        boolean isFull();

        void accept(int[] tuple, int length);
    }

    private static final class ChecksumSink implements TupleSink {

        private final int limit;
        private final Checksum checksum = new Checksum();

        private ChecksumSink(int limit) {
            this.limit = limit;
        }

        @Override
        public boolean isFull() {
            return checksum.tupleCount >= limit;
        }

        @Override
        public void accept(int[] tuple, int length) {
            checksum.accept(tuple, length);
        }
    }

    private static final class ListSink implements TupleSink {

        private final int limit;
        private final List<List<Integer>> result;

        private ListSink(int limit) {
            this.limit = limit;
            this.result = new ArrayList<>(limit);
        }

        @Override
        public boolean isFull() {
            return result.size() >= limit;
        }

        @Override
        public void accept(int[] tuple, int length) {

            List<Integer> values = new ArrayList<>(length);

            for (int i = 0; i < length; i++) {
                values.add(tuple[i]);
            }

            result.add(List.copyOf(values));
        }
    }

    // -------------------------------------------------------------------------
    // Work budget
    // -------------------------------------------------------------------------

    private static final class WorkBudget {

        private long expansions;

        void expand() {

            expansions++;

            if (expansions > MAX_PARTIAL_EXPANSIONS) {
                throw new WorkLimitExceeded();
            }
        }
    }

    private static final class WorkLimitExceeded
            extends RuntimeException {

        private WorkLimitExceeded() {
            super(null, null, false, false);
        }
    }

    // -------------------------------------------------------------------------
    // Scenario model
    // -------------------------------------------------------------------------

    private enum Profile {
        LOW_SELECTIVITY,
        HIGHER_SELECTIVITY
    }

    private record Scenario(
            int dimensions,
            int listSize,
            Profile profile,
            List<List<Integer>> lists,
            List<Integer> triggerValues) {
    }
}
