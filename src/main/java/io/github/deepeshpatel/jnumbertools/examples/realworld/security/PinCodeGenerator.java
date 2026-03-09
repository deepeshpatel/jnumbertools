package io.github.deepeshpatel.jnumbertools.examples.realworld.security;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;

import java.util.List;
import java.util.Random;

/**
 * PIN code generator with various constraints
 * Demonstrates repetitive permutations with custom validation
 */
public class PinCodeGenerator {

    private static final List<Integer> DIGITS = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

    public static void main(String[] args) {
        System.out.println("=== PIN Code Generator ===\n");

        generateAll4DigitPins();
        generatePinsWithoutRepeatingDigits();
        generatePinsWithCustomPattern();
        generateRandomPins();
    }

    private static void generateAll4DigitPins() {
        System.out.println("All possible 4-digit PINs:");
        var pins = JNumberTools.permutations()
                .repetitive(4, DIGITS);
                //.lexOrder();

        System.out.println("Total: " + pins.count());
        System.out.println("First 10 PINs:");
        pins.lexOrder().stream()
                .limit(10)
                .forEach(PinCodeGenerator::formatPin);
    }

    private static void generatePinsWithoutRepeatingDigits() {
        System.out.println("\n4-digit PINs with no repeating digits:");
        var pins = JNumberTools.permutations()
                .nPk(4, DIGITS);  // k-permutations = no repetitions
                //.lexOrder();

        System.out.println("Total: " + pins.count());
        System.out.println("First 10 PINs:");
        pins.lexOrder().stream()
                .limit(10)
                .forEach(PinCodeGenerator::formatPin);
    }

    private static void generatePinsWithCustomPattern() {
        System.out.println("\nPINs with pattern: odd-even-odd-even:");

        var odds = List.of(1, 3, 5, 7, 9);
        var evens = List.of(0, 2, 4, 6, 8);

        var pins = JNumberTools.cartesianProduct()
                .simpleProductOf(odds)
                .and(evens)
                .and(odds)
                .and(evens);

        System.out.println("Total: " + pins.count());
        System.out.println("First 10 PINs:");
        pins.lexOrder().stream()
                .limit(10)
                .forEach(tuple -> System.out.println(tuple));
    }

    private static void generateRandomPins() {
        System.out.println("\n10 random PINs:");

        JNumberTools.permutations()
                .repetitive(4, DIGITS)
                .choice(10,new Random())
                .stream()
                .forEach(PinCodeGenerator::formatPin);
    }

    private static void formatPin(List<Integer> pin) {
        pin.forEach(System.out::print);
        System.out.println();
    }
}
