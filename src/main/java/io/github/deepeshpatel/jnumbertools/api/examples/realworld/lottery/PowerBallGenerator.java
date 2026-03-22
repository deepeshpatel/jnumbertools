package io.github.deepeshpatel.jnumbertools.api.examples.realworld.lottery;

import io.github.deepeshpatel.jnumbertools.api.JNumberTools;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * PowerBall: Choose 5 numbers from 1-69 + PowerBall (1-26)
 * Demonstrates constrained product with different set sizes
 */
public class PowerBallGenerator {

    public static void main(String[] args) {
        System.out.println("=== PowerBall Generator ===\n");

        List<Integer> mainNumbers = range(1, 70);
        var powerBalls = range(1, 27);

        var tickets = JNumberTools.cartesianProduct()
                .constrainedProductOfDistinct(5, mainNumbers)  // Choose 5 main numbers (returns 5 individual numbers)
                .andDistinct(1, powerBalls);           // Choose 1 powerball (returns 1 number)

        System.out.println("Total possible PowerBall combinations: " + tickets.count());

        System.out.println("\nFirst 5 PowerBall tickets:");
        tickets.lexOrder()
                .stream()
                .limit(5)
                .forEach(ticket -> System.out.println(formatTicket(ticket)));

        System.out.println("\nRandom PowerBall ticket:");
        var random = tickets.choice(1 ,new Random()).stream().findFirst().orElseThrow();
        System.out.println(formatTicket(random));
    }

    private static List<Integer> range(int start, int endExclusive) {
        return java.util.stream.IntStream.range(start, endExclusive)
                .boxed()
                .toList();
    }

    private static String formatTicket(List<?> ticket) {
        // Ticket is a flat list: [num1, num2, num3, num4, num5, powerBall]
        List<Integer> mainNumbers = ticket.subList(0, 5).stream()
                .map(n -> (Integer) n)
                .toList();

        Integer powerBall = (Integer) ticket.get(5);

        String mainStr = mainNumbers.stream()
                .map(n -> String.format("%02d", n))
                .collect(Collectors.joining(" - "));

        return mainStr + "  [PowerBall: " + String.format("%02d", powerBall) + "]";
    }
}
