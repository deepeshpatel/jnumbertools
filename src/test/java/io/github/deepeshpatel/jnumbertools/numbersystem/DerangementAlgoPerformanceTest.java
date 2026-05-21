package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.numbersystem.DerangadicAlgorithmsTest.isValidDerangement;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfSystemProperty(named = "performance.testing", matches = "true")
public class DerangementAlgoPerformanceTest {

    private static final DerangadicAlgorithms DERANGADIC = new DerangadicAlgorithms(new Calculator());

    @Test
    @DisplayName("Performance test - should generate 10,000 derangements quickly")
    void testPerformance() {
        int n = 8;
        int iterations = 10000;
        BigInteger total = DERANGADIC.derangementCount(n);
        assertTrue(total.longValue() >= iterations,
                String.format("D_%d should be at least %d", n, iterations));

        long startTime = System.currentTimeMillis();

        for (long m = 0; m < iterations; m++) {
            int[] derangement = DERANGADIC.unrank(BigInteger.valueOf(m), n);
            assertTrue(isValidDerangement(derangement),
                    String.format("Invalid derangement at rank %d", m));
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 500,
                String.format("Performance too slow: %d ms for %d derangements", duration, iterations));
        System.out.printf("Performance: %d ms for %d derangements (%.3f µs each)%n",
                duration, iterations, duration * 1000.0 / iterations);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    @DisplayName("Performance comparison with increasing n (fixed rank)")
    void testPerformanceFixedRank() {
        int rank = 7;  // Fixed small rank
        int[] nValues = {100, 200, 500, 1000, 2000, 5000, 10000, 20000};
        int iterations = 1000;

        System.out.println("\n=== Performance: Fixed Rank=" + rank + " ===");
        System.out.printf("%-10s %-12s %-14s %-12s %-10s%n", "n", "Array (ms)", "Fenwick (ms)", "Hybrid (ms)", "Routing Match?");
        System.out.println("-".repeat(65));

        for (int n : nValues) {
            int[] digits = DERANGADIC.toDerangadic(rank, n);

            // Warmup
            for (int i = 0; i < 100; i++) {
                DERANGADIC.toDerangementArray(digits, n);
                DERANGADIC.toDerangementFenwick(digits, n);
                DERANGADIC.toDerangement(digits, n);
            }

            // Test Array
            long startArray = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                DERANGADIC.toDerangementArray(digits, n);
            }
            long timeArray = System.currentTimeMillis() - startArray;

            // Test Fenwick
            long startFenwick = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                DERANGADIC.toDerangementFenwick(digits, n);
            }
            long timeFenwick = System.currentTimeMillis() - startFenwick;

            // Test Hybrid Auto
            long startHybrid = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                DERANGADIC.toDerangement(digits, n);
            }
            long timeHybrid = System.currentTimeMillis() - startHybrid;

            long bestEngTime = Math.min(timeArray, timeFenwick);
            // Verify if Hybrid picked the absolute best choice (allowing a 2ms margin for minor JVM noise)
            String matchStatus = (timeHybrid <= bestEngTime + 2) ? "OPTIMAL" : "SUBOPTIMAL";

            System.out.printf("%-10d %-12d %-14d %-12d %-10s%n", n, timeArray, timeFenwick, timeHybrid, matchStatus);
        }
    }

    @Test
    @DisplayName("Performance comparison: Finding the Crossover Point (n=10000)")
    void testPerformanceCrossover() {
        int n = 10000;

        List<NamedRank> testRanks = new ArrayList<>();
        testRanks.add(new NamedRank("Small (15 digits)", BigInteger.valueOf(1_000_000_000_000_000L)));
        testRanks.add(new NamedRank("Medium (100 digits)", BigInteger.TEN.pow(100)));
        testRanks.add(new NamedRank("Large (600 digits)", BigInteger.TEN.pow(600)));
        testRanks.add(new NamedRank("Large (700 digits)", BigInteger.TEN.pow(700)));
        testRanks.add(new NamedRank("Large (800 digits)", BigInteger.TEN.pow(800)));
        testRanks.add(new NamedRank("Heavy (900 digits)", BigInteger.TEN.pow(900)));
        testRanks.add(new NamedRank("Heavy (1000 digits)", BigInteger.TEN.pow(1000)));
        testRanks.add(new NamedRank("Heavy (1500 digits)", BigInteger.TEN.pow(1500)));
        testRanks.add(new NamedRank("Heavy (1600 digits)", BigInteger.TEN.pow(1600)));
        testRanks.add(new NamedRank("Heavy (1700 digits)", BigInteger.TEN.pow(1700)));

        int iterations = 100;

        System.out.println("\n=== Performance: Fixed n=" + n + " (Iterations: " + iterations + ") ===");
        System.out.printf("%-22s %-12s %-14s %-12s %-10s%n", "Rank Magnitude", "Array (ms)", "Fenwick (ms)", "Hybrid (ms)", "Routing Match?");
        System.out.println("-".repeat(78));

        for (NamedRank nr : testRanks) {
            int[] digits = DERANGADIC.toDerangadic(nr.value, n);

            // Warmup
            for (int i = 0; i < 5; i++) {
                DERANGADIC.toDerangementArray(digits, n);
                DERANGADIC.toDerangementFenwick(digits, n);
                DERANGADIC.toDerangement(digits, n);
            }

            long startArray = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                DERANGADIC.toDerangementArray(digits, n);
            }
            long timeArray = System.currentTimeMillis() - startArray;

            long startFenwick = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                DERANGADIC.toDerangementFenwick(digits, n);
            }
            long timeFenwick = System.currentTimeMillis() - startFenwick;

            long startHybrid = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                DERANGADIC.toDerangement(digits, n);
            }
            long timeHybrid = System.currentTimeMillis() - startHybrid;

            long bestEngTime = Math.min(timeArray, timeFenwick);
            String matchStatus = (timeHybrid <= bestEngTime + 2) ? "OPTIMAL" : "SUBOPTIMAL";

            System.out.printf("%-22s %-12d %-14d %-12d %-10s%n", nr.name, timeArray, timeFenwick, timeHybrid, matchStatus);
        }
    }

    @Test
    @DisplayName("Find N_THRESHOLD: Fixed High Rank vs Variable N")
    void testFindNThreshold() {
        BigInteger highRank = BigInteger.TEN.pow(150);

        int[] nValues = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1500, 2000, 3000, 4000};
        int iterations = 1000;

        System.out.println("\n=== N_THRESHOLD Search (Rank fixed at 10^150) ===");
        System.out.printf("%-10s %-12s %-14s %-12s %-10s%n", "N", "Array (ms)", "Fenwick (ms)", "Hybrid (ms)", "Routing Match?");
        System.out.println("-".repeat(65));

        for (int n : nValues) {
            int[] digits = DERANGADIC.toDerangadic(highRank, n);

            // Warmup
            for (int i = 0; i < 100; i++) {
                DERANGADIC.toDerangementArray(digits, n);
                DERANGADIC.toDerangementFenwick(digits, n);
                DERANGADIC.toDerangement(digits, n);
            }

            long startArray = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                DERANGADIC.toDerangementArray(digits, n);
            }
            long timeArray = System.currentTimeMillis() - startArray;

            long startFenwick = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                DERANGADIC.toDerangementFenwick(digits, n);
            }
            long timeFenwick = System.currentTimeMillis() - startFenwick;

            long startHybrid = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                DERANGADIC.toDerangement(digits, n);
            }
            long timeHybrid = System.currentTimeMillis() - startHybrid;

            long bestEngTime = Math.min(timeArray, timeFenwick);
            String matchStatus = (timeHybrid <= bestEngTime + 2) ? "OPTIMAL" : "SUBOPTIMAL";
            System.out.printf("%-10d %-12d %-14d %-12d %-10s%n", n, timeArray, timeFenwick, timeHybrid, matchStatus);
        }
    }

    private static class Scenario {
        String name;
        int n;
        BigInteger rank;

        Scenario(String name, int n, BigInteger rank) {
            this.name = name;
            this.n = n;
            this.rank = rank;
        }
    }

    private static class NamedRank {
        String name;
        BigInteger value;
        NamedRank(String name, BigInteger value) {
            this.name = name;
            this.value = value;
        }
    }

    @Test
    @DisplayName("Hybrid routing matrix: exhaustive N × digit-length combinations")
    void testRoutingMatrix() {

        int[] nValues = {50,100,200,500,1000, 2000,5000,10000};
        int[] digitLengths = {5,10,20,50,100, 200,300,500,800,1200};
        int iterations = 500;

        System.out.println("\n=== HYBRID ROUTING MATRIX ===");
        System.out.printf(
                "%-8s %-10s %-12s %-12s %-12s %-12s%n",
                "n","digits","Array","Fenwick","Hybrid","Choice");

        System.out.println("-".repeat(75));

        for(int n : nValues){
            for(int d : digitLengths){

                if(d>=n) continue;
                int[] digits=new int[d];

                for(int i=0;i<d;i++){
                    digits[i]=Math.min(i,2);
                }

                for(int i=0;i<20;i++){
                    DERANGADIC.toDerangementArray(digits,n);
                    DERANGADIC.toDerangementFenwick(digits,n);
                    DERANGADIC.toDerangement(digits,n);
                }

                long start=System.nanoTime();
                for(int i=0;i<iterations;i++){
                    DERANGADIC.toDerangementArray(digits,n);
                }
                long arrayTime=(System.nanoTime()-start)/1_000_000;

                start=System.nanoTime();
                for(int i=0;i<iterations;i++){
                    DERANGADIC.toDerangementFenwick(digits,n);
                }
                long fenwickTime=(System.nanoTime()-start)/1_000_000;

                start=System.nanoTime();
                for(int i=0;i<iterations;i++){
                    DERANGADIC.toDerangement(digits,n);
                }
                long hybridTime=(System.nanoTime()-start)/1_000_000;
                String best= arrayTime<=fenwickTime ?"Array" :"Fenwick";
                System.out.printf("%-8d %-10d %-12d %-12d %-12d %-12s%n", n,d,arrayTime,fenwickTime,hybridTime,best);
            }
        }
    }

    @Test
    @DisplayName("Find exact digit crossover for fixed N")
    void testDigitCrossoverSearch() {

        int n=10000;
        int iterations=200;

        System.out.println("\n=== DIGIT CROSSOVER SEARCH n="+n+" ===");
        System.out.printf("%-12s %-12s %-12s %-10s%n", "digits", "Array", "Fenwick", "Winner");

        System.out.println("-".repeat(50));

        for(int digitsLength=50; digitsLength<=1500; digitsLength+=50){

            int[] digits=new int[digitsLength];

            for(int i=0;i<digitsLength;i++){
                digits[i]=Math.min(i,2);
            }

            for(int i=0;i<20;i++){
                DERANGADIC.toDerangementArray(digits,n);
                DERANGADIC.toDerangementFenwick(digits,n);
            }

            long start=System.nanoTime();

            for(int i=0;i<iterations;i++){
                DERANGADIC.toDerangementArray(digits,n);
            }

            long arrayTime=(System.nanoTime()-start)/1_000_000;
            start=System.nanoTime();

            for(int i=0;i<iterations;i++){
                DERANGADIC.toDerangementFenwick(digits,n);
            }

            long fenwickTime=(System.nanoTime()-start)/1_000_000;
            String winner=arrayTime<fenwickTime ?"Array" :"Fenwick";
            System.out.printf("%-12d %-12d %-12d %-10s%n", digitsLength, arrayTime, fenwickTime, winner);
        }
    }

    @Test
    @DisplayName("Measure routing overhead only")
    void testRoutingOverhead() {

        int n=10000;
        int iterations=10000;
        int[] digits=DERANGADIC.toDerangadic(BigInteger.TEN.pow(100), n);

        for(int i=0;i<1000;i++){
            DERANGADIC.toDerangement(digits,n);
        }

        long start=System.nanoTime();

        for(int i=0;i<iterations;i++){
            DERANGADIC.toDerangement(digits,n);
        }

        long hybrid=System.nanoTime()-start;

        start=System.nanoTime();

        for(int i=0;i<iterations;i++){
            if (n >= 100 && digits.length >= 100) {
                long arrayComplexity= (long)n*digits.length;
                long fenwickComplexity= (long)digits.length* (long)(Math.log(n)/Math.log(2));
                boolean useArray= arrayComplexity<=fenwickComplexity;
            }
        }

        long routing=(System.nanoTime()-start);
        System.out.println("\nRouting overhead=" +(routing/1_000_000.0) +" ms");
        System.out.println("Hybrid total=" +(hybrid/1_000_000.0) +" ms");
    }

    @Test @Disabled
    @DisplayName("Pathological cases")
    void testPathologicalCases(){

        List<Scenario> scenarios=List.of(

                new Scenario(
                        "Huge N tiny digits",
                        100000,
                        BigInteger.valueOf(7)
                ),

                new Scenario(
                        "Huge N medium digits",
                        100000,
                        BigInteger.TEN.pow(10)
                ),

                new Scenario(
                        "Huge N massive digits",
                        100000,
                        BigInteger.TEN.pow(10)
                ),

                new Scenario(
                        "Tiny N huge rank",
                        120,
                        BigInteger.TEN.pow(10)
                )
        );

        int iterations=4;

        System.out.println("\n=== PATHOLOGICAL TESTS ===");

        for(Scenario s:scenarios){

            int[] digits=
                    DERANGADIC.toDerangadic(
                            s.rank,
                            s.n
                    );

            long start=System.nanoTime();

            for(int i=0;i<iterations;i++){
                DERANGADIC.toDerangement(
                        digits,
                        s.n
                );
            }

            long ms=
                    (System.nanoTime()-start)
                            /1_000_000;

            System.out.printf(
                    "%-30s n=%-8d digits=%-8d time=%dms%n",
                    s.name,
                    s.n,
                    digits.length,
                    ms
            );
        }
    }

}