/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import io.github.deepeshpatel.jnumbertools.base.Product;

import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LambdaConditionalProductBuilderTest {
    @Test
    void oneDimensionPredicateFiltersAndRepeatedWhenUsesAnd() {
        List<Integer> values=List.of(1,2,3,4,5,6);
        var product=Product.conditional(values).withLambda(values).when(v->v%2==0).when(v->v<5);
        assertEquals(BigInteger.valueOf(2),product.count());
        assertEquals(List.of(List.of(2),List.of(4)),product.generate().stream().toList());
    }

    @Test
    void twoDimensionPredicateIsTypedAndSupportsOr() {
        List<Integer> cores=List.of(1,2,4,8), budgets=List.of(8,16,32);
        var product=Product.conditional(cores,budgets).withLambda(cores,budgets).when((core,budget)->core<=2||budget>=32);
        long expected=0;
        for(int c:cores) for(int b:budgets) if(c<=2||b>=32) expected++;
        assertEquals(BigInteger.valueOf(expected),product.count());
        assertEquals(expected,product.generate().stream().count());
    }

    @Test
    void threeDimensionPredicateSupportsNestedAndOr() {
        List<Integer> cores=List.of(1,2,4,8), memory=List.of(2,4,8), budget=List.of(8,16,32);
        var product=Product.conditional(cores,memory,budget).withLambda(cores,memory,budget)
                .when((core,memoryGb,maxBudget)->(core<=2&&memoryGb<=4)||(core>=8&&maxBudget>=16));
        long expected=0;
        for(int c:cores) for(int m:memory) for(int b:budget)
            if((c<=2&&m<=4)||(c>=8&&b>=16)) expected++;
        assertEquals(BigInteger.valueOf(expected),product.count());
        assertEquals(expected,product.generate().stream().count());
    }

    @Test
    void repeatedWhenCallsAreCombinedWithAnd() {
        List<Integer> a=List.of(1,2,3,4), b=List.of(1,2,3,4);
        var product=Product.conditional(a,b).withLambda(a,b).when((x,y)->x+y>=4).when((x,y)->x<=y);
        long expected=0;
        for(int x:a) for(int y:b) if(x+y>=4&&x<=y) expected++;
        assertEquals(BigInteger.valueOf(expected),product.count());
        assertEquals(expected,product.generate().stream().count());
    }

    @Test
    void fiveDimensionsUseStronglyTypedPredicate() {
        List<Integer> d=List.of(0,1);
        var product=Product.conditional(d,d,d,d,d).withLambda(d,d,d,d,d)
                .when((a,b,c,d4,e)->a+b+c+d4+e>=1);
        assertEquals(BigInteger.valueOf(31),product.count());
        assertEquals(31,product.generate().stream().count());
    }

    @Test
    void sixDimensionsUseArrayPredicate() {
        List<Integer> d=List.of(0,1);
        var product=Product.conditional(d,d,d,d,d,d).withLambda()
                .when(values->values[0].equals(values[5]));
        assertEquals(BigInteger.valueOf(32),product.count());
        assertEquals(32,product.generate().stream().count());
    }

    @Test
    void elevenDimensionsUseArrayPredicateFallback() {
        List<Integer> d=List.of(0,1);
        var product=Product.conditional(d,d,d,d,d,d,d,d,d,d,d).withLambda()
                .when(values->values[0].equals(values[10]));
        assertEquals(BigInteger.valueOf(1024),product.count());
        assertEquals(1024,product.generate().stream().count());
    }

    @Test
    void appendingDimensionsBeforeThePredicatePreservesTypedArity() {
        List<Integer> a=List.of(1,2), b=List.of(1,2), c=List.of(1,2);
        var product=Product.conditional(a,b,c).withLambda(a,b,c).when((x,y,z)->x+y+z<=4);
        assertEquals(BigInteger.valueOf(4),product.count());
        assertEquals(4,product.generate().stream().count());
    }

    @Test
    void alwaysTrueAndFalsePredicatesAreHandled() {
        List<Integer> a=List.of(1,2), b=List.of(1,2);
        var all=cartesianProduct.withLambda(a,b).when((x,y)->true);
        var none=cartesianProduct.withLambda(a,b).when((x,y)->false);
        assertEquals(BigInteger.valueOf(4),all.count());
        assertEquals(BigInteger.ZERO,none.count());
        assertTrue(none.isEmpty());
    }

    @Test
    void scopedTwoDimensionPredicateIgnoresUnrelatedDimensionsAndRemainsTyped() {
        List<Integer> a=java.util.stream.IntStream.range(0,10).boxed().toList();
        List<Integer> b=java.util.stream.IntStream.range(0,10).boxed().toList();
        List<Integer> c=java.util.stream.IntStream.range(0,10).boxed().toList();
        List<Integer> d=java.util.stream.IntStream.range(0,10).boxed().toList();
        List<Integer> e=java.util.stream.IntStream.range(0,10).boxed().toList();

        var product=Product.conditional(a,b,c,d,e).withLambda(a,b,c,d,e)
                .when(a,b).matches((x,y)->x<=y);

        assertEquals(BigInteger.valueOf(55_000),product.count());
        assertEquals(55_000,product.generate().stream().count());
    }

    @Test
    void scopedThreeDimensionPredicateReceivesOnlySelectedValues() {
        List<Integer> a=java.util.stream.IntStream.range(0,10).boxed().toList();
        List<Integer> b=java.util.stream.IntStream.range(0,10).boxed().toList();
        List<Integer> c=java.util.stream.IntStream.range(0,10).boxed().toList();
        List<Integer> d=java.util.stream.IntStream.range(0,10).boxed().toList();

        var product=Product.conditional(a,b,c,d).withLambda(a,b,c,d)
                .when(a,b,c).matches((x,y,z)->x+y<=z);

        long scopedCount=0;
        for(int x:a) for(int y:b) for(int z:c) if(x+y<=z) scopedCount++;
        assertEquals(BigInteger.valueOf(scopedCount*10),product.count());
        assertEquals(scopedCount*10,product.generate().stream().count());
    }

    @Test
    void scopedArrayPredicateUsesOnlyNamedDimensionsForProbeLimit() {
        List<Integer> a=java.util.stream.IntStream.range(0,100).boxed().toList();
        List<Integer> b=java.util.stream.IntStream.range(0,100).boxed().toList();
        List<Integer> unrelated=java.util.stream.IntStream.range(0,100).boxed().toList();

        var product=cartesianProduct.withLambda(a,b,unrelated,unrelated,unrelated)
                .when(new List<?>[]{a,b}).matches(values->(Integer)values[0]<(Integer)values[1]);

        assertEquals(BigInteger.valueOf(4_950L).multiply(BigInteger.valueOf(1_000_000L)),product.count());
    }

    // ── scoped rules keep the builder's type, so typed and scoped rules can be mixed in any order ──

    private static List<Integer> range(int size) {
        return java.util.stream.IntStream.range(0,size).boxed().toList();
    }

    @Test
    void aScopedRuleReturnsTheSameTypedBuilderAndTypedRulesCanFollow() {
        List<Integer> a=range(4), b=range(4), c=range(4), d=range(4), e=range(4);

        LambdaConditionalProductBuilder.Typed5<Integer,Integer,Integer,Integer,Integer> product =
                cartesianProduct.withLambda(a,b,c,d,e)
                        .when(a,b).matches((x,y)->x<=y)                         // scoped, still a Typed5
                        .when((v,w,x,y,z)->v+z>=2)                             // typed full-tuple rule after it
                        .when(c,d,e).matches((x,y,z)->x!=y||y!=z);            // scoped again

        long expected=0;
        for(int v:a) for(int w:b) for(int x:c) for(int y:d) for(int z:e)
            if(v<=w && v+z>=2 && (x!=y||y!=z)) expected++;
        assertEquals(BigInteger.valueOf(expected),product.count());
        assertEquals(expected,product.generate().stream().count());
        assertEquals(expected,product.generate().stream().parallel().count());
    }

    @Test
    void aScopedRuleOnAThreeDimensionBuilderCanStillAppendADimension() {
        List<Integer> a=range(3), b=range(3), c=range(3), d=range(3);

        var product=Product.conditional(a,b,c).withLambda(a,b,c)
                .when(a,b).matches((x,y)->x!=y)
                .and(d)                                                         // typed and(...) is still available
                .when((x,y,z,w)->x+w<=z+2);

        long expected=0;
        for(int x:a) for(int y:b) for(int z:c) for(int w:d)
            if(x!=y && x+w<=z+2) expected++;
        assertEquals(BigInteger.valueOf(expected),product.count());
    }

    @Test
    void scopedRulesMixedWithTypedRulesInAnyOrderGiveTheSameProduct() {
        List<Integer> a=range(5), b=range(5), c=range(5);

        var scopedFirst=cartesianProduct.withLambda(a,b,c)
                .when(a,c).matches((x,z)->x<z).when((x,y,z)->x+y!=z);
        var typedFirst=cartesianProduct.withLambda(a,b,c)
                .when((x,y,z)->x+y!=z).when(a,c).matches((x,z)->x<z);

        assertEquals(scopedFirst.count(),typedFirst.count());
        assertEquals(new java.util.HashSet<>(scopedFirst.generate().stream().toList()),
                new java.util.HashSet<>(typedFirst.generate().stream().toList()));
    }

    @Test
    void arrayScopeOnATypedBuilderReturnsTheTypedBuilder() {
        List<Integer> a=range(3), b=range(3), c=range(3), d=range(3), e=range(3), f=range(3);

        var product=Product.conditional(a,b,c,d,e,f).withLambda()                   // six dimensions: array-based
                .when(a,b,c,d,e,f).matches(values->(Integer)values[0]+(Integer)values[5]==2);

        long expected=0;
        for(int x:a) for(int y:f) if(x+y==2) expected++;
        assertEquals(BigInteger.valueOf(expected*81),product.count());
    }

    // ── a typed builder must be given exactly as many dimensions as its arity ──────────────────────

    @Test
    void typedBuildersRejectAProductWithADifferentNumberOfDimensions() {
        List<Integer> a=range(2), b=range(2);
        var twoDimensions=new ConditionalProductBuilder(a,b);

        var tooMany=assertThrows(IllegalArgumentException.class,
                ()->new LambdaConditionalProductBuilder.Typed3<Integer,Integer,Integer>(twoDimensions));
        assertTrue(tooMany.getMessage().contains("exactly 3 dimensions"));
        assertTrue(tooMany.getMessage().contains("has 2"));

        assertThrows(IllegalArgumentException.class,
                ()->new LambdaConditionalProductBuilder.Typed1<Integer>(twoDimensions));
        assertThrows(NullPointerException.class,
                ()->new LambdaConditionalProductBuilder.Typed2<Integer,Integer>(null));
        // the matching arity is accepted
        assertEquals(BigInteger.valueOf(4),
                new LambdaConditionalProductBuilder.Typed2<Integer,Integer>(twoDimensions).count());
    }

    // ── the probe limit ───────────────────────────────────────────────────────────────────────────

    @Test
    void aFullTupleRuleOverTooManyCombinationsPointsToTheScopedForm() {
        List<Integer> a=range(101), b=range(101), c=range(101);     // 101^3 = 1,030,301 probes > 1,000,000

        var typed=cartesianProduct.withLambda(a,b,c);
        var e=assertThrows(IllegalArgumentException.class,()->typed.when((x,y,z)->true));

        assertTrue(e.getMessage().contains("1030301"),e.getMessage());
        assertTrue(e.getMessage().contains("1000000"),e.getMessage());
        assertTrue(e.getMessage().contains("matches(predicate)"),e.getMessage());
    }

    @Test
    void aScopedRuleOverTheSameDimensionsFitsWhenItNamesFewerOfThem() {
        List<Integer> a=range(101), b=range(101), c=range(101);

        var product=cartesianProduct.withLambda(a,b,c).when(a,b).matches((x,y)->x<=y);
        assertEquals(BigInteger.valueOf(5151L*101),product.count());      // 5151 pairs with x <= y, times 101 values of c
    }

    // ── parallel streams ──────────────────────────────────────────────────────────────────────────

    @Test
    void parallelAndSequentialStreamsAgreeForLambdaRules() {
        List<Integer> a=range(6), b=range(6), c=range(6), d=range(6);
        var product=cartesianProduct.withLambda(a,b,c,d)
                .when(a,b).matches((x,y)->x!=y)
                .when((x,y,z,w)->(x+y+z+w)%3!=0);

        long expected=0;
        for(int x:a) for(int y:b) for(int z:c) for(int w:d) if(x!=y&&(x+y+z+w)%3!=0) expected++;
        assertEquals(expected,product.generate().stream().count());
        assertEquals(expected,product.generate().stream().parallel().count());
        assertEquals(BigInteger.valueOf(expected),product.count());
    }

    @Test
    void lambdaBuilderExposesLexicographicRanksChoiceAndSampling() {
        List<Integer> a = List.of(0, 1);
        List<Integer> b = List.of(0, 1);
        List<Integer> c = List.of(0, 1);

        var product = cartesianProduct.withLambda(a, b, c)
                .when(a, b).matches((x, y) -> x != 0 || y != 0);

        List<List<Object>> expected = List.of(
                List.of(0, 1, 0), List.of(0, 1, 1),
                List.of(1, 0, 0), List.of(1, 0, 1),
                List.of(1, 1, 0), List.of(1, 1, 1));

        assertEquals(expected, product.lexOrder().stream().toList());
        assertEquals(List.of(expected.get(0), expected.get(3), expected.get(5)),
                product.byRanks(List.of(BigInteger.ZERO, BigInteger.valueOf(3), BigInteger.valueOf(5))).stream().toList());
        assertEquals(expected, product.lexOrder(LexOrderStrategy.DP).stream().toList());
        assertEquals(expected, product.lexOrder(LexOrderStrategy.ADAPTIVE, 100).stream().toList());
        assertEquals(2, product.lexOrderMth(BigInteger.valueOf(3), BigInteger.ZERO).stream().count());
        assertEquals(4, product.choice(4, new java.util.Random(42)).stream().count());
        List<List<Object>> sample = product.sample(4, new java.util.Random(42)).stream().toList();
        assertEquals(4, sample.size());
        assertEquals(4, new java.util.HashSet<>(sample).size());
    }

}
