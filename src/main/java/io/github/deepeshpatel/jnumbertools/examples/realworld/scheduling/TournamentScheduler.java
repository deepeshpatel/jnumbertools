package io.github.deepeshpatel.jnumbertools.examples.realworld.scheduling;

import io.github.deepeshpatel.jnumbertools.api.JNumberTools;

import java.util.List;
import java.util.Random;

/**
 * Generates tournament schedules and match pairings
 * Demonstrates combinations for round-robin tournaments
 */
public class TournamentScheduler {

    public static void main(String[] args) {
        System.out.println("=== Tournament Scheduler ===\n");

        var teams = List.of("Lions", "Tigers", "Bears", "Wolves", "Eagles", "Hawks");
        generateRoundRobinSchedule(teams);
        generateTournamentBracket(teams);
        generateRandomMatchOrder(teams);
    }

    private static void generateRoundRobinSchedule(List<String> teams) {
        System.out.println("Round-robin tournament schedule:");

        // All possible matches (combinations of 2 teams)
        var matches = JNumberTools.combinations()
                .unique(2, teams)
                .lexOrder()
                .stream()
                .toList();

        System.out.println("Total matches: " + matches.size());
        System.out.println("\nAll possible matchups:");
        matches.stream()
                .limit(10)
                .forEach(match -> System.out.println("  " + match.get(0) + " vs " + match.get(1)));

        // Schedule matches across rounds
        int teamsPerMatch = 2;
        int matchesPerRound = teams.size() / 2;
        int totalRounds = matches.size() / matchesPerRound;

        System.out.println("\nSchedule by round (simplified):");
        for (int round = 0; round < Math.min(3, totalRounds); round++) {
            System.out.println("Round " + (round + 1) + ":");
            for (int m = 0; m < matchesPerRound; m++) {
                int matchIndex = round * matchesPerRound + m;
                if (matchIndex < matches.size()) {
                    var match = matches.get(matchIndex);
                    System.out.println("  " + match.get(0) + " vs " + match.get(1));
                }
            }
        }
    }

    private static void generateTournamentBracket(List<String> teams) {
        System.out.println("\nSingle-elimination bracket possibilities:");

        // For 8 teams, first round pairings
        if (teams.size() == 8) {
            var firstRound = JNumberTools.combinations()
                    .unique(2, teams)
                    .lexOrderMth(7, 0)  // Sample some matchups
                    .stream()
                    .limit(4)
                    .toList();

            System.out.println("Sample first round pairings:");
            firstRound.forEach(match ->
                    System.out.println("  " + match.get(0) + " vs " + match.get(1)));
        }
    }

    private static void generateRandomMatchOrder(List<String> teams) {
        System.out.println("\nRandom match sequence for the season:");

        var matches = JNumberTools.combinations()
                .unique(2, teams)
                .lexOrder()
                .stream()
                .toList();

        // Generate random order of all matches
        var randomOrder = JNumberTools.permutations()
                .unique(matches)
                .choice(1,new Random())
                .stream()
                .findFirst()
                .orElseThrow();

        System.out.println("First 5 matches in random order:");
        randomOrder.stream()
                .limit(5)
                .forEach(match ->
                        System.out.println("  " + match.get(0) + " vs " + match.get(1)));
    }
}