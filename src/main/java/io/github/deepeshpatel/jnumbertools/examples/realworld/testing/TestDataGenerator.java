package io.github.deepeshpatel.jnumbertools.examples.realworld.testing;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Generates test data for unit and integration tests
 * Demonstrates various data generation strategies
 */
public class TestDataGenerator {

    public static void main(String[] args) {
        System.out.println("=== Test Data Generator ===\n");

        generateUserProfiles();
        generateOrderCombinations();
        generateBoundaryTestData();
        generateRandomTestData();
    }

    private static void generateUserProfiles() {
        System.out.println("Test user profile combinations:");

        var roles = List.of("Admin", "User", "Guest", "Manager");
        var statuses = List.of("Active", "Inactive", "Pending", "Suspended");
        var regions = List.of("US", "EU", "APAC", "LATAM");

        var profiles = JNumberTools.cartesianProduct()
                .simpleProductOf(roles)
                .and(statuses)
                .and(regions);
                //.lexOrder();

        System.out.println("Total profile combinations: " + profiles.count());
        System.out.println("First 10 profiles:");
        profiles.lexOrder().stream()
                .limit(10)
                .forEach(tuple -> System.out.println("  Role=" + tuple.get(0) +
                        ", Status=" + tuple.get(1) +
                        ", Region=" + tuple.get(2)));
    }

    private static void generateOrderCombinations() {
        System.out.println("\nE-commerce order test combinations:");

        var paymentMethods = List.of("Credit Card", "PayPal", "Bank Transfer", "Cash");
        var shippingMethods = List.of("Standard", "Express", "Next Day");
        var quantities = List.of(1, 2, 3, 5, 10);

        var orders = JNumberTools.cartesianProduct()
                .simpleProductOf(paymentMethods)
                .and(shippingMethods)
                .and(quantities);
                //.lexOrder();

        System.out.println("Total order combinations: " + orders.count());
        orders.lexOrder().stream()
                .limit(5)
                .forEach(tuple -> System.out.println("  " + tuple));
    }

    private static void generateBoundaryTestData() {
        System.out.println("\nBoundary test values:");

        // Use ArrayList to build the list
        var values = new java.util.ArrayList<>();
        values.add(Integer.MIN_VALUE);
        values.add(-1);
        values.add(0);
        values.add(1);
        values.add(Integer.MAX_VALUE);
        values.add(Long.MIN_VALUE);
        values.add(-1L);
        values.add(0L);
        values.add(1L);
        values.add(Long.MAX_VALUE);
        values.add(Double.MIN_VALUE);
        values.add(-1.0);
        values.add(0.0);
        values.add(1.0);
        values.add(Double.MAX_VALUE);
        values.add("");           // empty string
        values.add(" ");          // space
        values.add("a");
        values.add("A");
        values.add("Z");
        values.add("test");

        System.out.println("Selecting every 1000th combination of 3 values:");
        JNumberTools.combinations()
                .unique(3, values)
                .lexOrderMth(1000, 0)
                .stream()
                .limit(5)
                .forEach(System.out::println);
    }

    private static void generateRandomTestData() {
        System.out.println("\n10 random test data sets:");

        var names = List.of("Alice", "Bob", "Charlie", "Diana", "Eve");
        var ages = IntStream.range(18, 100).boxed().toList();
        var scores = List.of(0, 50, 75, 90, 100);

        JNumberTools.cartesianProduct()
                .simpleProductOf(names)
                .and(ages)
                .and(scores)
                .choice(10)
                .stream()
                .forEach(tuple -> System.out.println("  Name=" + tuple.get(0) +
                        ", Age=" + tuple.get(1) +
                        ", Score=" + tuple.get(2)));
    }
}
