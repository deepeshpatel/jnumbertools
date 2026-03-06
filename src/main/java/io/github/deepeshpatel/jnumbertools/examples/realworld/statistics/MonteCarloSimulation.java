package io.github.deepeshpatel.jnumbertools.examples.realworld.statistics;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Demonstrates Monte Carlo simulations using combinatorial generators
 */
public class MonteCarloSimulation {

    public static void main(String[] args) {
        System.out.println("=== Monte Carlo Simulations ===\n");

        estimatePi();
        simulateDiceRolls();
        simulateCardDraws();
        simulateLotteryOdds();
    }

    private static void estimatePi() {
        System.out.println("Estimating π using Monte Carlo method:");

        int totalPoints = 10000;
        var random = new Random();

        // Generate random points in unit square
        int insideCircle = 0;
        for (int i = 0; i < totalPoints; i++) {
            double x = random.nextDouble();
            double y = random.nextDouble();
            if (x * x + y * y <= 1) {
                insideCircle++;
            }
        }

        double pi = 4.0 * insideCircle / totalPoints;
        System.out.println("Estimated π: " + pi);
        System.out.println("Actual π:    " + Math.PI);
        System.out.println("Error: " + Math.abs(pi - Math.PI));
    }

    private static void simulateDiceRolls() {
        System.out.println("\nDice roll probability simulation:");

        var dice = List.of(1, 2, 3, 4, 5, 6);
        int numRolls = 1000;

        // Count occurrences of each sum when rolling 2 dice
        var sumCounts = new int[13]; // indices 0-12, we'll use 2-12

        JNumberTools.permutations()
                .repetitive(2, dice)
                .choice(numRolls)
                .stream()
                .forEach(roll -> {
                    int sum = roll.get(0) + roll.get(1);
                    sumCounts[sum]++;
                });

        System.out.println("Distribution after " + numRolls + " rolls:");
        for (int sum = 2; sum <= 12; sum++) {
            double probability = sumCounts[sum] / (double) numRolls;
            System.out.printf("  Sum %2d: %5d (%.2f%%)%n",
                    sum, sumCounts[sum], probability * 100);
        }
    }

    private static void simulateCardDraws() {
        System.out.println("\nCard draw probabilities:");

        var suits = List.of("♠", "♥", "♦", "♣");
        var ranks = List.of("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K");

        // Generate all cards as strings
        var deck = JNumberTools.cartesianProduct()
                .simpleProductOf(ranks)
                .and(suits)
                .lexOrder()
                .stream()
                .map(tuple -> tuple.get(0).toString() + tuple.get(1).toString())
                .collect(Collectors.toList());

        System.out.println("Deck size: " + deck.size());

        // Simulate 5-card draws
        int numSimulations = 10000;
        int pairs = 0;

        for (int i = 0; i < numSimulations; i++) {
            var hand = JNumberTools.combinations()
                    .unique(5, deck)
                    .choice(1)
                    .stream()
                    .findFirst()
                    .orElseThrow();

            // Check for at least one pair
            var rankCounts = hand.stream()
                    .map(card -> ((String)card).substring(0, ((String)card).length() - 1))
                    .collect(Collectors.groupingBy(
                            r -> r, Collectors.counting()));

            if (rankCounts.values().stream().anyMatch(c -> c >= 2)) {
                pairs++;
            }
        }

        System.out.println("Probability of at least one pair in 5 cards: " +
                (pairs / (double) numSimulations));
    }

    private static void simulateLotteryOdds() {
        System.out.println("\nLottery odds simulation:");

        int totalTickets = 10000;
        var random = new Random();
        var comboBuilder = JNumberTools.combinations().unique(49,6);
        var totalCombinations = comboBuilder.count();
        System.out.println("Total possible combinations: " + totalCombinations);

        // Correct order: first total elements (49), then combination size (6)
        //var comboBuilder = JNumberTools.combinations().unique(49, 6);
        System.out.println("Builder count: " + comboBuilder.count());

        BigInteger winningRank;
        do {
            winningRank = new BigInteger(totalCombinations.bitLength(), random);
        } while (winningRank.compareTo(totalCombinations) >= 0);

        System.out.println("Requesting rank: " + winningRank);

        var winningTicket = comboBuilder
                .byRanks(List.of(winningRank))
                .stream()
                .findFirst()
                .orElseThrow();

        System.out.println("Winning ticket (first 3 numbers): " +
                winningTicket.subList(0, 3) + "...");

        int winners = 0;
        for (int i = 0; i < totalTickets; i++) {
            BigInteger rank;
            do {
                rank = new BigInteger(totalCombinations.bitLength(), random);
            } while (rank.compareTo(totalCombinations) >= 0);

            var ticket = comboBuilder
                    .byRanks(List.of(rank))
                    .stream()
                    .findFirst()
                    .orElseThrow();

            if (ticket.get(0).equals(winningTicket.get(0))) {
                winners++;
            }
        }

        System.out.println("Simulated " + totalTickets + " random tickets");
        System.out.println("Winners (matching first number): " + winners);
        System.out.println("Experimental probability: " +
                String.format("%.2f%%", (winners * 100.0 / totalTickets)));
        System.out.println("Theoretical probability (match first number): " +
                String.format("%.2f%%", (100.0 / 49)));
    }
}
