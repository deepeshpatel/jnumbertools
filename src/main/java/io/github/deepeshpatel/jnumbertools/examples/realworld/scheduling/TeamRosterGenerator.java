package io.github.deepeshpatel.jnumbertools.examples.realworld.scheduling;

import io.github.deepeshpatel.jnumbertools.api.JNumberTools;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Generates possible team rosters and lineups
 * Demonstrates k-permutations for team selection
 */
public class TeamRosterGenerator {

    record Player(String name, String position, int skill) {}

    public static void main(String[] args) {
        System.out.println("=== Team Roster Generator ===\n");

        var players = createPlayerPool();
        generateAllPossibleLineups(players);
        generateBalancedTeams(players);
        generateRandomTeam();
    }

    private static List<Player> createPlayerPool() {
        return List.of(
                new Player("Alice", "PG", 95),
                new Player("Bob", "SG", 88),
                new Player("Charlie", "SF", 92),
                new Player("Diana", "PF", 85),
                new Player("Eve", "C", 90),
                new Player("Frank", "PG", 82),
                new Player("Grace", "SG", 87),
                new Player("Henry", "SF", 79),
                new Player("Ivy", "PF", 84),
                new Player("Jack", "C", 78)
        );
    }

    private static void generateAllPossibleLineups(List<Player> players) {
        System.out.println("All possible 5-player lineups:");

        var lineups = JNumberTools.combinations()
                .unique(5, players);
                //.lexOrder();

        System.out.println("Total lineups: " + lineups.count());
        System.out.println("First 5 lineups:");
        lineups.lexOrder().stream()
                .limit(5)
                .forEach(lineup -> {
                    System.out.print("  ");
                    lineup.forEach(p -> System.out.print(p.name() + " "));
                    System.out.println();
                });
    }

    private static void generateBalancedTeams(List<Player> players) {
        System.out.println("\nLineups with at least one player from each position:");

        // Create indices for each position
        var pgIndices = IntStream.range(0, players.size())
                .filter(i -> players.get(i).position().equals("PG"))
                .boxed()
                .toList();

        // This is a simplified example - in practice you'd use constrained product
        System.out.println("(Simplified - actual implementation would use constraints)");
    }

    private static void generateRandomTeam() {
        System.out.println("\nRandom 5-player lineup:");

        var players = createPlayerPool();
        var lineup = JNumberTools.combinations()
                .unique(5, players)
                .choice(1,new Random())
                .stream()
                .findFirst()
                .orElseThrow();

        System.out.print("Selected team: ");
        lineup.forEach(p -> System.out.print(p.name() + " "));
        System.out.println();

        double avgSkill = lineup.stream()
                .mapToInt(Player::skill)
                .average()
                .orElse(0);

        System.out.println("Average skill: " + avgSkill);
    }
}
