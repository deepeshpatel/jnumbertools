/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.api.examples.realworld.lottery;

import io.github.deepeshpatel.jnumbertools.api.JNumberTools;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Demonstrates lottery number generation using combinations.
 * Shows different lottery formats, odds calculation, random picks,
 * and rank-based / m-th generation for huge spaces.
 */
public class LotteryNumberGenerator {

    public static void main(String[] args) {
        System.out.println("=== Lottery Number Generator Examples ===\n");
        System.out.println("\nWarning: These are pseudo-random simulations only — never use for real gambling!");

        classicSixFromFortyNine();
        powerBallStyle();
        euroMillionsStyle();
        randomAndRankBasedDemo();
    }

    private static void classicSixFromFortyNine() {
        System.out.println("1. Classic 6/49 Lottery (e.g., many national lotteries)");
        int mainNumbers = 49;
        int pick = 6;

        BigInteger total = JNumberTools.combinations()
                .unique(mainNumbers, pick)
                .count();

        System.out.printf("   Total possible combinations: %,d (1 in %s odds)%n",
                total, total.toString());

        System.out.println("\n   First 3 combinations (lex order):");
        JNumberTools.combinations()
                .unique(mainNumbers, pick)
                .lexOrder()
                .stream()
                .limit(3)
                .map(LotteryNumberGenerator::formatTicket)
                .forEach(System.out::println);

        System.out.println("\n   One random ticket:");
        var randomTicket = JNumberTools.combinations()
                .unique(mainNumbers, pick)
                .choice(1,new Random())
                .stream()
                .findFirst()
                .map(LotteryNumberGenerator::formatTicket)
                .orElse("Error");
        System.out.println("   → " + randomTicket);
    }

    private static void powerBallStyle() {
        System.out.println("\n2. PowerBall-style (5 from 69 + 1 from 26)");

        int whiteBalls = 69;
        int whitePick = 5;
        int powerBalls = 26;
        int powerPick = 1;

        BigInteger whiteCombos = JNumberTools.combinations()
                .unique(whiteBalls, whitePick)
                .count();

        BigInteger powerCombos = JNumberTools.combinations()
                .unique(powerBalls, powerPick)
                .count();

        BigInteger total = whiteCombos.multiply(powerCombos);

        System.out.printf("   Total combinations: %,d (1 in %s odds)%n", total, total);

        System.out.println("\n   One random PowerBall ticket:");
        var ticket = JNumberTools.cartesianProduct()
                .constrainedProductOfDistinct(whitePick, range(1, whiteBalls + 1))
                .andDistinct(powerPick, range(1, powerBalls + 1))
                .choice(1,new Random())
                .stream()
                .findFirst()
                .map(LotteryNumberGenerator::formatPowerBall)   // ← Fix 2: method accepts List<?>
                .orElse("Error");

        System.out.println("   → " + ticket);
    }

    private static void euroMillionsStyle() {
        System.out.println("\n3. EuroMillions-style (5 from 50 + 2 Lucky Stars from 12)");

        int main = 50;
        int mainPick = 5;
        int stars = 12;
        int starsPick = 2;

        BigInteger mainCombos = JNumberTools.combinations().unique(main, mainPick).count();
        BigInteger starsCombos = JNumberTools.combinations().unique(stars, starsPick).count();
        BigInteger total = mainCombos.multiply(starsCombos);

        System.out.printf("   Total combinations: %,d (1 in %s odds)%n", total, total);

        System.out.println("\n   One random EuroMillions-style ticket:");
        var ticket = JNumberTools.cartesianProduct()
                .constrainedProductOfDistinct(mainPick, range(1, main + 1))
                .andDistinct(starsPick, range(1, stars + 1))
                .choice(1,new Random())
                .stream()
                .findFirst()
                .map(LotteryNumberGenerator::formatEuroMillions)   // ← Fix 2: method accepts List<?>
                .orElse("Error");

        System.out.println("   → " + ticket);
    }

    private static void randomAndRankBasedDemo() {
        System.out.println("\n4. Advanced: Random + Rank-based + Distant m-th tickets (6/49)");

        int n = 49;
        int r = 6;

        // Random ticket
        System.out.println("   Random ticket:");
        var random = JNumberTools.combinations()
                .unique(n, r)
                .choice(1,new Random())
                .stream()
                .findFirst()
                .map(LotteryNumberGenerator::formatTicket)
                .orElse("Error");
        System.out.println("   → " + random);

        // Ticket at a specific rank (e.g., 1 millionth)
        BigInteger rank = BigInteger.valueOf(1_000_000);
        System.out.println("\n   Ticket at rank 1,000,000:");
        var ranked = JNumberTools.combinations()
                .unique(n, r)
                .byRanks(List.of(rank))
                .stream()
                .findFirst()
                .map(LotteryNumberGenerator::formatTicket)
                .orElse("Error");
        System.out.println("   → " + ranked);

        // Very distant ticket (every 1 billionth)
        System.out.println("\n   Distant ticket (every 1 billionth, first one):");
        var distant = JNumberTools.combinations()
                .unique(n, r)
                .lexOrderMth(BigInteger.valueOf(1_000_000_000), BigInteger.ZERO)
                .stream()
                .findFirst()
                .map(LotteryNumberGenerator::formatTicket)
                .orElse("Error");
        System.out.println("   → " + distant);
    }

    // ─── Formatting Helpers ─────────────────────────────────────────────────────

    private static String formatTicket(List<Integer> numbers) {
        return numbers.stream()
                .map(n -> String.format("%02d", n + 1))  // 1-based
                .sorted()                                // sort for readability
                .collect(Collectors.joining(" - "));
    }

    private static String formatPowerBall(List<?> ticketObj) {
        @SuppressWarnings("unchecked")
        List<Integer> ticket = (List<Integer>) ticketObj;

        // Flat list: first 5 main, last 1 powerball
        List<Integer> main = ticket.subList(0, 5);
        int power = ticket.get(5);

        String mainStr = main.stream()
                .map(n -> String.format("%02d", n))
                .sorted()
                .collect(Collectors.joining(" - "));

        return mainStr + "   [PowerBall: " + String.format("%02d", power) + "]";
    }

    private static String formatEuroMillions(List<?> ticketObj) {
        @SuppressWarnings("unchecked")
        List<Integer> ticket = (List<Integer>) ticketObj;

        // Flat list: first 5 main, last 2 stars
        List<Integer> main = ticket.subList(0, 5);
        List<Integer> stars = ticket.subList(5, 7);

        String mainStr = main.stream()
                .map(n -> String.format("%02d", n))
                .sorted()
                .collect(Collectors.joining(" - "));

        String starsStr = stars.stream()
                .map(n -> String.format("%02d", n))
                .sorted()
                .collect(Collectors.joining(" - "));

        return mainStr + "   [Lucky Stars: " + starsStr + "]";
    }

    private static List<Integer> range(int startInclusive, int endExclusive) {
        return IntStream.range(startInclusive, endExclusive)
                .boxed()
                .toList();
    }
}
