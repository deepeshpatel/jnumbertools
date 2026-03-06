package io.github.deepeshpatel.jnumbertools.examples.realworld.testing;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;

import java.util.List;

/**
 * Generates pairwise combinations for combinatorial testing
 * Demonstrates covering all pairs of parameters efficiently
 */
public class PairwiseTesting {

    public static void main(String[] args) {
        System.out.println("=== Pairwise Test Generation ===\n");

        testBrowserConfigurations();
        testDatabaseConfigurations();
        testApiParameters();
    }

    private static void testBrowserConfigurations() {
        System.out.println("Browser compatibility test combinations:");

        var browsers = List.of("Chrome", "Firefox", "Safari", "Edge");
        var versions = List.of("v80", "v90", "v100", "v110");
        var os = List.of("Windows", "MacOS", "Linux", "iOS", "Android");
        var screenSizes = List.of("Desktop", "Tablet", "Mobile");

        var allCombos = JNumberTools.cartesianProduct()
                .simpleProductOf(browsers)
                .and(versions)
                .and(os)
                .and(screenSizes);
                //.lexOrder();

        System.out.println("Total combinations: " + allCombos.count());

        // Generate a subset that covers all pairs (simplified approach)
        System.out.println("\nReduced test set (every 100th combination):");
        allCombos.lexOrderMth(100, 0)
                .stream()
                .limit(10)
                .forEach(System.out::println);
    }

    private static void testDatabaseConfigurations() {
        System.out.println("\nDatabase configuration testing:");

        var dbTypes = List.of("MySQL", "PostgreSQL", "Oracle", "MongoDB");
        var isolationLevels = List.of("READ_UNCOMMITTED", "READ_COMMITTED",
                "REPEATABLE_READ", "SERIALIZABLE");
        var poolSizes = List.of(5, 10, 20, 50, 100);
        var sslModes = List.of("REQUIRED", "PREFERRED", "DISABLED");

        // Sample configurations for testing
        JNumberTools.cartesianProduct()
                .simpleProductOf(dbTypes)
                .and(isolationLevels)
                .and(poolSizes)
                .and(sslModes)
                .sample(15)  // Random sample of 15 configurations
                .stream()
                .forEach(System.out::println);
    }

    private static void testApiParameters() {
        System.out.println("\nAPI endpoint test combinations:");

        var endpoints = List.of("/users", "/orders", "/products", "/payments");
        var methods = List.of("GET", "POST", "PUT", "DELETE");
        var formats = List.of("JSON", "XML", "FORM");
        var authTypes = List.of("None", "Basic", "OAuth", "JWT");

        var testCases = JNumberTools.cartesianProduct()
                .simpleProductOf(endpoints)
                .and(methods)
                .and(formats)
                .and(authTypes);
                //.lexOrder();

        System.out.println("Total API test cases: " + testCases.count());
        System.out.println("\nFirst 5 test cases:");
        testCases.lexOrder().stream()
                .limit(5)
                .forEach(System.out::println);
    }
}
