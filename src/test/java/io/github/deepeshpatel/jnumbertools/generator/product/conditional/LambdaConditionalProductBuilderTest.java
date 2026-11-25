/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.util.List;
import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static org.junit.jupiter.api.Assertions.*;

class LambdaConditionalProductBuilderTest {
    @Test
    void oneDimensionPredicateFiltersAndRepeatedWhenUsesAnd() {
        List<Integer> values=List.of(1,2,3,4,5,6);
        var product=cartesianProduct.withLambda(values).when(v->v%2==0).when(v->v<5);
        assertEquals(BigInteger.valueOf(2),product.count());
        assertEquals(List.of(List.of(2),List.of(4)),product.generate().stream().toList());
    }

    @Test
    void twoDimensionPredicateIsTypedAndSupportsOr() {
        List<Integer> cores=List.of(1,2,4,8), budgets=List.of(8,16,32);
        var product=cartesianProduct.withLambda(cores,budgets).when((core,budget)->core<=2||budget>=32);
        long expected=0;
        for(int c:cores) for(int b:budgets) if(c<=2||b>=32) expected++;
        assertEquals(BigInteger.valueOf(expected),product.count());
        assertEquals(expected,product.generate().stream().count());
    }

    @Test
    void threeDimensionPredicateSupportsNestedAndOr() {
        List<Integer> cores=List.of(1,2,4,8), memory=List.of(2,4,8), budget=List.of(8,16,32);
        var product=cartesianProduct.withLambda(cores,memory,budget)
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
        var product=cartesianProduct.withLambda(a,b).when((x,y)->x+y>=4).when((x,y)->x<=y);
        long expected=0;
        for(int x:a) for(int y:b) if(x+y>=4&&x<=y) expected++;
        assertEquals(BigInteger.valueOf(expected),product.count());
        assertEquals(expected,product.generate().stream().count());
    }

    @Test
    void tenDimensionsUseStronglyTypedPredicate() {
        List<Integer> d=List.of(0,1);
        var product=cartesianProduct.withLambda(d,d,d,d,d,d,d,d,d,d)
                .when((a,b,c,d4,e,f,g,h,i,j)->a+b+c+d4+e+f+g+h+i+j>=1);
        assertEquals(BigInteger.valueOf(1023),product.count());
        assertEquals(1023,product.generate().stream().count());
    }

    @Test
    void elevenDimensionsUseArrayPredicateFallback() {
        List<Integer> d=List.of(0,1);
        var product=cartesianProduct.withLambda(d,d,d,d,d,d,d,d,d,d,d)
                .when(values->values[0].equals(values[10]));
        assertEquals(BigInteger.valueOf(1024),product.count());
        assertEquals(1024,product.generate().stream().count());
    }

    @Test
    void appendingDimensionsBeforeThePredicatePreservesTypedArity() {
        List<Integer> a=List.of(1,2), b=List.of(1,2), c=List.of(1,2);
        var product=cartesianProduct.withLambda(a).and(b).and(c).when((x,y,z)->x+y+z<=4);
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
}
