package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import io.github.deepeshpatel.jnumbertools.generator.base.BuilderTestHelper;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@code cartesianProduct.conditionalProductOf(...)}. The valid tuples are cross-checked against
 * a plain filter over the full product, so the tests establish that counting, lexicographic order,
 * unranking and sampling over the rules agree with the brute-force definition.
 */
class ConditionalProductBuilderTest {

    private final List<String> os = new ArrayList<>(List.of("Windows", "macOS", "Linux"));
    private final List<String> browser = new ArrayList<>(List.of("Chrome", "Safari", "Firefox"));
    private final List<String> network = new ArrayList<>(List.of("WiFi", "4G"));

    // ── The worked example ───────────────────────────────────────────────────

    private List<List<Object>> expectedConfigs(boolean safariOnlyOnMac) {
        return cartesianProduct.simpleProductOf(os).and(browser).and(network).lexOrder().stream()
                .filter(t -> !(t.get(2).equals("4G") && t.get(0).equals("Linux")))
                .filter(t -> !(t.get(2).equals("4G") && t.get(1).equals("Chrome")))
                .filter(t -> !safariOnlyOnMac || !(t.get(1).equals("Safari") && !t.get(0).equals("macOS")))
                .toList();
    }

    private ConditionalProductBuilder twoRules() {
        return cartesianProduct.conditionalProductOf(os).and(browser).and(network)
                .when(network).is("4G").then(os).isNot("Linux")
                .when(network).is("4G").then(browser).isNot("Chrome");
    }

    @Test
    void twoRulesOnTheSameTrigger() {
        var builder = twoRules();
        assertEquals(BigInteger.valueOf(13), builder.count());
        assertEquals(expectedConfigs(false), builder.lexOrder().stream().toList());
    }

    @Test
    void threeRulesFormingAPath() {
        var builder = twoRules().when(browser).is("Safari").then(os).isOneOf("macOS");
        assertEquals(BigInteger.valueOf(10), builder.count());
        assertEquals(expectedConfigs(true), builder.lexOrder().stream().toList());
        assertFalse(builder.isEmpty());
    }

    @Test
    void ruleVocabularyIsEquivalent() {
        var a = cartesianProduct.conditionalProductOf(os).and(browser)
                .when(browser).is("Safari").then(os).isOneOf("macOS");
        var b = cartesianProduct.conditionalProductOf(os).and(browser)
                .when(browser).is("Safari").then(os).isNot("Windows")
                .when(browser).is("Safari").then(os).isNoneOf("Linux");
        var c = cartesianProduct.conditionalProductOf(os).and(browser)
                .when(browser).isOneOf("Safari").then(os).isNotIn("Windows", "Linux");
        var d = cartesianProduct.conditionalProductOf(os).and(browser)
                .when(browser).isNoneOf("Chrome", "Firefox").then(os).isOneOf(List.of("macOS"));
        var e = cartesianProduct.conditionalProductOf(os).and(browser)
                .when(browser).isNot("Chrome").then(os).is("macOS")
                .when(browser).is("Firefox").then(os).isOneOf(os); // all values: adds nothing to the Firefox rule above

        var expected = a.lexOrder().stream().toList();
        assertEquals(BigInteger.valueOf(7), a.count());
        for (var other : List.of(b, c, d)) {
            assertEquals(expected, other.lexOrder().stream().toList());
        }
        // e: Chrome with any os (3), Safari only with macOS (1), Firefox only with macOS (1)
        assertEquals(BigInteger.valueOf(5), e.count());
    }

    @Test
    void rulesCanBeFollowedByMoreDimensions() {
        var withRule = cartesianProduct.conditionalProductOf(os).and(browser)
                .when(browser).is("Safari").then(os).isOneOf("macOS");
        var extended = withRule.and(network);
        assertEquals(BigInteger.valueOf(7), withRule.count());
        assertEquals(BigInteger.valueOf(14), extended.count());
        var first = extended.lexOrder().iterator().next();
        assertEquals(List.of("Windows", "Chrome", "WiFi"), first);
    }

    @Test
    void ruleOnDimensionsInAnyDirection() {
        // rule written against the declaration order: later dimension triggers, earlier one is restricted
        var builder = cartesianProduct.conditionalProductOf(os).and(browser)
                .when(browser).is("Safari").then(os).isOneOf("macOS");
        var reversed = cartesianProduct.conditionalProductOf(os).and(browser)
                .when(os).isNoneOf("macOS").then(browser).isNot("Safari");
        assertEquals(builder.lexOrder().stream().toList(), reversed.lexOrder().stream().toList());
    }

    @Test
    void ruleThatForbidsEverythingMakesTheProductEmpty() {
        var builder = cartesianProduct.conditionalProductOf(os).and(browser)
                .when(browser).is("Safari").then(os).isNoneOf("Windows", "macOS", "Linux")
                .when(browser).isNoneOf("Safari").then(os).isNoneOf("Windows", "macOS", "Linux");
        BuilderTestHelper.testEmptyDomain(builder);
    }

    @Test
    void emptyDimensionMakesTheProductEmpty() {
        var builder = cartesianProduct.conditionalProductOf(os).and(Collections.emptyList());
        BuilderTestHelper.testEmptyDomain(builder);
    }

    // ── Shared builder rules ─────────────────────────────────────────────────

    @Test
    void sharedBuilderRules() {
        var builder = twoRules().when(browser).is("Safari").then(os).isOneOf("macOS");
        BuilderTestHelper.testLexOrderMth(builder);
        BuilderTestHelper.testByRanks(builder);
        BuilderTestHelper.testChoice(builder);
        BuilderTestHelper.testSample(builder);
        BuilderTestHelper.testLexOrder(builder);
        BuilderTestHelper.testIsEmpty(builder);
        BuilderTestHelper.testResultsAreImmutable(builder);
        BuilderTestHelper.testReIterable(builder);
        BuilderTestHelper.testByRanksIsLazy(builder);
        BuilderTestHelper.testChoiceMayExceedCountAndNullRandomIsRejected(builder);
    }

    @Test
    void immutability() {
        var base = cartesianProduct.conditionalProductOf(os).and(browser);
        var withRule = base.when(browser).is("Safari").then(os).isOneOf("macOS");
        assertNotSame(base, withRule);
        assertEquals(BigInteger.valueOf(9), base.count());
        assertEquals(BigInteger.valueOf(7), withRule.count());
        assertNotSame(base, base.and(network));
    }

    @Test
    void snapshotOfTheCallersLists() {
        var mutable = new ArrayList<>(List.of("a", "b"));
        var other = new ArrayList<>(List.of("x", "y"));
        var builder = cartesianProduct.conditionalProductOf(mutable).and(other);
        mutable.add("c");
        mutable.set(0, "changed");
        assertEquals(BigInteger.valueOf(4), builder.count());
        // the dimension is still recognised by its list instance, and its original values are used
        var ruled = builder.when(mutable).is("a").then(other).isOneOf("x");
        assertEquals(BigInteger.valueOf(3), ruled.count());
        assertEquals(List.of("a", "x"), ruled.lexOrder().iterator().next());
    }

    // ── Validation ───────────────────────────────────────────────────────────

    @Test
    void nullAndUnknownInput() {
        assertThrows(NullPointerException.class, () -> cartesianProduct.conditionalProductOf(null));
        assertThrows(NullPointerException.class,
                () -> cartesianProduct.conditionalProductOf(java.util.Arrays.asList("a", null)));
        var builder = cartesianProduct.conditionalProductOf(os).and(browser);
        assertThrows(NullPointerException.class, () -> builder.and(null));
        assertThrows(NullPointerException.class, () -> builder.when(null));
        assertThrows(IllegalArgumentException.class, () -> builder.when(List.of("Windows", "macOS", "Linux")),
                "an equal but different list instance is not a dimension");
        assertThrows(IllegalArgumentException.class, () -> builder.when(network));
        assertThrows(NullPointerException.class, () -> builder.when(os).is(null));
        assertThrows(NullPointerException.class, () -> builder.when(os).isOneOf((Object[]) null));
        assertThrows(NullPointerException.class, () -> builder.when(os).isOneOf("macOS", null));
    }

    @Test
    void ruleValuesMustBeValuesOfTheDimension() {
        var builder = cartesianProduct.conditionalProductOf(os).and(browser);
        assertThrows(IllegalArgumentException.class, () -> builder.when(browser).is("Safary"));
        assertThrows(IllegalArgumentException.class, () -> builder.when(browser).isNot("Safary"));
        assertThrows(IllegalArgumentException.class, () -> builder.when(browser).isOneOf("Safari", "Opera"));
        assertThrows(IllegalArgumentException.class,
                () -> builder.when(browser).is("Safari").then(os).isOneOf("macOS", "Solaris"));
        assertThrows(IllegalArgumentException.class, () -> builder.when(browser).isOneOf());
        assertThrows(IllegalArgumentException.class, () -> builder.when(browser).isNoneOf());
        assertThrows(IllegalArgumentException.class, () -> builder.when(browser).isNotIn(List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> builder.when(browser).is("Safari").then(os).isOneOf());
    }

    @Test
    void aRuleNeedsTwoDifferentDimensions() {
        var builder = cartesianProduct.conditionalProductOf(os).and(browser);
        assertThrows(IllegalArgumentException.class, () -> builder.when(os).is("Linux").then(os));
    }

    @Test
    void sameListInstanceForTwoDimensionsIsAmbiguous() {
        var die = new ArrayList<>(List.of(1, 2, 3));
        var builder = cartesianProduct.conditionalProductOf(die).and(die);
        assertEquals(BigInteger.valueOf(9), builder.count());
        assertThrows(IllegalArgumentException.class, () -> builder.when(die));
    }

    @Test
    void rulesFormingALoopAreSolvedTogether() {
        var builder = cartesianProduct.conditionalProductOf(os).and(browser).and(network)
                .when(browser).is("Safari").then(os).isOneOf("macOS")
                .when(network).is("4G").then(browser).isNot("Chrome")
                .when(network).is("4G").then(os).isNot("Linux");   // closes the loop os - browser - network
        assertEquals(BigInteger.valueOf(10), builder.count());
        assertEquals(expectedConfigs(true), builder.lexOrder().stream().toList());
    }

    @Test
    void aLoopOverTooManyCombinationsIsRejected() {
        var a = new ArrayList<Integer>();
        var b = new ArrayList<Integer>();
        var c = new ArrayList<Integer>();
        for (int i = 0; i < 1001; i++) { a.add(i); b.add(i); c.add(i); }
        var builder = cartesianProduct.conditionalProductOf(a).and(b).and(c)
                .when(a).is(0).then(b).is(0)
                .when(b).is(0).then(c).is(0);
        assertThrows(IllegalArgumentException.class, () -> builder.when(c).is(0).then(a).is(0));
    }

    // ── Random differential test against the plain definition ───────────────

    private record Rule(int a, Set<Integer> triggers, int b, Set<Integer> allowed) { }

    @Test
    void randomRuleSetsAgreeWithBruteForce() {
        Random random = new Random(20261113);
        int nonTrivial = 0;
        for (int round = 0; round < 400; round++) {
            nonTrivial += runRandomInstance(random) ? 1 : 0;
        }
        assertTrue(nonTrivial > 100, "the generator should mostly produce non-empty, rule-restricted products");
    }

    /** Returns true if the instance had rules and a non-empty valid set smaller than the full product. */
    private boolean runRandomInstance(Random random) {
        int n = 1 + random.nextInt(5);
        List<List<Integer>> lists = new ArrayList<>();
        for (int d = 0; d < n; d++) {
            int size = random.nextInt(10) == 0 ? 0 : 1 + random.nextInt(4);
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < size; i++) list.add(d * 100 + i);
            lists.add(list);
        }

        // random links between dimensions, in any direction; several links can close loops
        List<int[]> edges = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (random.nextInt(100) < 40) edges.add(random.nextBoolean() ? new int[]{i, j} : new int[]{j, i});
            }
        }

        ConditionalProductBuilder builder = cartesianProduct.conditionalProductOf(lists.get(0));
        for (int d = 1; d < n; d++) builder = builder.and(lists.get(d));

        List<Rule> rules = new ArrayList<>();
        for (int[] edge : edges) {
            int rulesOnEdge = 1 + random.nextInt(2);
            for (int r = 0; r < rulesOnEdge; r++) {
                int a = random.nextBoolean() ? edge[0] : edge[1];
                int b = a == edge[0] ? edge[1] : edge[0];
                if (lists.get(a).isEmpty() || lists.get(b).isEmpty()) continue;
                Set<Integer> triggers = randomSubset(lists.get(a).size(), random);
                Set<Integer> allowed = randomSubset(lists.get(b).size(), random);
                var trigger = whenIndexesTrigger(builder.when(lists.get(a)), lists.get(a), triggers, random);
                builder = thenIndexes(trigger.then(lists.get(b)), lists, b, allowed, random);
                rules.add(new Rule(a, triggers, b, allowed));
            }
        }

        // brute force: nested loops in declaration order give lexicographic order
        List<List<Object>> expected = new ArrayList<>();
        int[] idx = new int[n];
        boolean anyEmpty = lists.stream().anyMatch(List::isEmpty);
        if (!anyEmpty) {
            while (true) {
                if (valid(idx, rules)) {
                    List<Object> tuple = new ArrayList<>();
                    for (int d = 0; d < n; d++) tuple.add(lists.get(d).get(idx[d]));
                    expected.add(Collections.unmodifiableList(tuple));
                }
                int d = n - 1;
                while (d >= 0 && ++idx[d] == lists.get(d).size()) idx[d--] = 0;
                if (d < 0) break;
            }
        }

        final ConditionalProductBuilder product = builder;
        assertEquals(BigInteger.valueOf(expected.size()), product.count(), () -> product.toString());
        assertEquals(expected.isEmpty(), product.isEmpty());
        assertEquals(expected, product.lexOrder().stream().toList(), () -> product.toString());

        if (!expected.isEmpty()) {
            int size = expected.size();
            // every m-th from a start
            int m = 1 + random.nextInt(4);
            int start = random.nextInt(size);
            List<List<Object>> everyMth = new ArrayList<>();
            for (int r = start; r < size; r += m) everyMth.add(expected.get(r));
            assertEquals(everyMth, product.lexOrderMth(m, start).stream().toList());

            // arbitrary ranks, in any order, with repeats
            List<BigInteger> ranks = new ArrayList<>();
            List<List<Object>> byRank = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                int r = random.nextInt(size);
                ranks.add(BigInteger.valueOf(r));
                byRank.add(expected.get(r));
            }
            assertEquals(byRank, product.byRanks(ranks).stream().toList());

            // choice and sample only produce valid tuples; a full sample is every tuple once
            Set<List<Object>> valid = new HashSet<>(expected);
            for (List<Object> t : product.choice(10, random)) assertTrue(valid.contains(t));
            int sampleSize = Math.min(size, 1 + random.nextInt(4));
            List<List<Object>> sample = product.sample(sampleSize, random).stream().toList();
            assertEquals(sampleSize, sample.size());
            assertEquals(sampleSize, new HashSet<>(sample).size());
            assertTrue(valid.containsAll(sample));
            assertEquals(valid, new HashSet<>(product.sample(size, random).stream().toList()));

            // ranks at the boundary
            assertThrows(IllegalArgumentException.class,
                    () -> product.byRanks(List.of(BigInteger.valueOf(size))).stream().toList());
        }

        long full = lists.stream().mapToLong(List::size).reduce(1, (x, y) -> x * y);
        return !rules.isEmpty() && !expected.isEmpty() && expected.size() < full;
    }

    private static boolean valid(int[] idx, List<Rule> rules) {
        for (Rule rule : rules) {
            if (rule.triggers().contains(idx[rule.a()]) && !rule.allowed().contains(idx[rule.b()])) return false;
        }
        return true;
    }

    private static Set<Integer> randomSubset(int size, Random random) {
        Set<Integer> subset = new HashSet<>();
        for (int i = 0; i < size; i++) if (random.nextBoolean()) subset.add(i);
        return subset;
    }

    /** Expresses the index set with the positive or the negative vocabulary, whichever is possible. */
    private static ConditionalProductBuilder.Trigger whenIndexesTrigger(
            ConditionalProductBuilder.WhenDimension when, List<Integer> list, Set<Integer> set, Random random) {
        List<Integer> in = new ArrayList<>();
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) (set.contains(i) ? in : out).add(list.get(i));
        if (!in.isEmpty() && (random.nextBoolean() || out.isEmpty())) {
            return in.size() == 1 && random.nextBoolean() ? when.is(in.get(0)) : when.isOneOf(in);
        }
        return out.size() == 1 && random.nextBoolean() ? when.isNot(out.get(0)) : when.isNotIn(out);
    }

    private static ConditionalProductBuilder thenIndexes(
            ConditionalProductBuilder.ThenDimension then, List<List<Integer>> lists, int b, Set<Integer> allowed,
            Random random) {
        List<Integer> list = lists.get(b);
        List<Integer> in = new ArrayList<>();
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) (allowed.contains(i) ? in : out).add(list.get(i));
        if (!in.isEmpty() && (random.nextBoolean() || out.isEmpty())) {
            return in.size() == 1 && random.nextBoolean() ? then.is(in.get(0)) : then.isOneOf(in);
        }
        return out.size() == 1 && random.nextBoolean() ? then.isNot(out.get(0)) : then.isNoneOf(out);
    }

    // ── Primitive fast path and rejection sampling ───────────────────────────

    @Test
    void countsBeyondLongUseExactBigIntegerArithmetic() {
        List<List<Integer>> dims = new ArrayList<>();
        for (int d = 0; d < 11; d++) {
            List<Integer> values = new ArrayList<>();
            for (int i = 0; i < 100; i++) values.add(i);
            dims.add(values);
        }
        var builder = cartesianProduct.conditionalProductOf(dims.get(0));
        for (int d = 1; d < dims.size(); d++) builder = builder.and(dims.get(d));
        builder = builder.when(dims.get(0)).is(0).then(dims.get(1)).is(0);

        BigInteger hundred = BigInteger.valueOf(100);
        BigInteger expected = BigInteger.valueOf(99).multiply(hundred.pow(10)).add(hundred.pow(9));
        assertEquals(expected, builder.count());
        assertTrue(expected.bitLength() > 63);

        List<Object> first = builder.byRanks(List.of(BigInteger.ZERO)).stream().findFirst().orElseThrow();
        assertEquals(Collections.nCopies(11, 0), first);
        List<Object> lastWithZero = builder.byRanks(List.of(hundred.pow(9).subtract(BigInteger.ONE)))
                .stream().findFirst().orElseThrow();
        assertEquals(0, lastWithZero.get(0));
        assertEquals(0, lastWithZero.get(1));
        assertEquals(99, lastWithZero.get(10));
        List<Object> next = builder.byRanks(List.of(hundred.pow(9))).stream().findFirst().orElseThrow();
        assertEquals(1, next.get(0));
        assertEquals(0, next.get(1));
        List<Object> last = builder.byRanks(List.of(expected.subtract(BigInteger.ONE)))
                .stream().findFirst().orElseThrow();
        assertEquals(Collections.nCopies(11, 99), last);
    }

    @Test
    void choiceByRejectionOnlyReturnsValidTuplesAndCoversThem() {
        // 13 of 18 tuples are valid, so choice uses rejection sampling
        var builder = twoRules();
        Set<List<Object>> valid = new HashSet<>(builder.lexOrder().stream().toList());
        Set<List<Object>> seen = new HashSet<>();
        for (List<Object> t : builder.choice(2000, new Random(7))) {
            assertTrue(valid.contains(t));
            seen.add(t);
        }
        assertEquals(valid, seen);
        assertEquals(5, builder.choice(5, new Random(1)).stream().count());
    }

    @Test
    void choiceWithFewValidTuplesStillUsesRanks() {
        // exactly one valid tuple out of 3*3*2: far below a quarter, so the rank-based path is used
        var builder = cartesianProduct.conditionalProductOf(os).and(browser).and(network)
                .when(os).isNot("Windows").then(network).is("WiFi")
                .when(os).isNot("Windows").then(browser).is("Chrome")
                .when(network).is("WiFi").then(os).is("macOS")
                .when(network).is("4G").then(os).is("Windows");
        assertEquals(BigInteger.valueOf(builder.lexOrder().stream().count()), builder.count());
        Set<List<Object>> valid = new HashSet<>(builder.lexOrder().stream().toList());
        for (List<Object> t : builder.choice(50, new Random(3))) assertTrue(valid.contains(t));
    }
}
