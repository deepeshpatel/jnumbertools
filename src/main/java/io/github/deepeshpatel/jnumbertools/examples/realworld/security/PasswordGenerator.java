/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.examples.realworld.security;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple example showing how to generate passwords with constraints using Cartesian Product.
 */
public class PasswordGenerator {

    // Common character sets
    private static final List<Character> LOWERCASE = List.of('a','b','c','d','e','f','g','h','i','j','k','l','m',
            'n','o','p','q','r','s','t','u','v','w','x','y','z');
    private static final List<Character> UPPERCASE = List.of('A','B','C','D','E','F','G','H','I','J','K','L','M',
            'N','O','P','Q','R','S','T','U','V','W','X','Y','Z');
    private static final List<Character> DIGITS = List.of('0','1','2','3','4','5','6','7','8','9');
    private static final List<Character> SYMBOLS = List.of('!','@','#','$','%','^','&','*','(',')','-','+','=');

    public static void main(String[] args) {
        System.out.println("=== Password Generator Example ===\n");
        System.out.println("Warning: These are pseudo-random simulations only — never use for real security-critical passwords!\n");

        generateConstrainedPasswords();
    }

    private static void generateConstrainedPasswords() {
        System.out.println("Generating 5 random passwords with constraints:");
        System.out.println("  - At least 1 uppercase letter");
        System.out.println("  - At least 1 digit");
        System.out.println("  - At least 1 symbol");
        System.out.println("  - Rest from full charset\n");

        var builder = JNumberTools.cartesianProduct()
                .constrainedProductOfDistinct(1, UPPERCASE)          // at least 1 uppercase
                .andInRange(1, 3, DIGITS)   // at least 1 and at most 3 digits
                .andInRange(2, 3, SYMBOLS)  // at least 2 and at most 3 symbols
                .andDistinct (5, combine(LOWERCASE, UPPERCASE, DIGITS, SYMBOLS)); // rest to fill

        builder.choice(5)   // 5 random passwords (no duplicates)
                .stream()
                .map(PasswordGenerator::formatPassword)
                .forEach(pw -> System.out.println("   → " + pw + "   (length: " + pw.length() + ")"));
    }

    private static String formatPassword(List<?> charsObj) {
        @SuppressWarnings("unchecked")
        List<Character> chars = (List<Character>) charsObj;

        return chars.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    private static List<Character> combine(List<Character>... sets) {
        return Stream.of(sets)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
    }
}