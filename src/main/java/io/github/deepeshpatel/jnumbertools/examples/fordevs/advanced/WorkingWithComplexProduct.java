package io.github.deepeshpatel.jnumbertools.examples.fordevs.advanced;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Demonstrates advanced usage of constrained and simple Cartesian products
 * Shows real-world scenarios with complex constraints and large combinations
 */
public class WorkingWithComplexProduct {

    public static void main(String[] args) {
        System.out.println("=== Working with Complex Products ===\n");

        pizzaConfigurator();
        travelPackagePlanner();
        computerBuildOptimizer();
        teamFormationWithConstraints();
        productAnalysisAndFiltering();
        largeScaleProductProcessing();
    }

    private static void pizzaConfigurator() {
        System.out.println("1. PIZZA CONFIGURATOR WITH CONSTRAINTS\n");

        // Define ingredient pools
        var bases = List.of("Thin Crust", "Thick Crust", "Stuffed Crust", "Gluten-Free");
        var cheeses = List.of("Mozzarella", "Cheddar", "Parmesan", "Gouda", "Vegan");
        var sauces = List.of("Tomato", "Pesto", "BBQ", "Alfredo", "Garlic");
        var meats = List.of("Pepperoni", "Sausage", "Chicken", "Bacon", "Ham");
        var veggies = List.of("Mushrooms", "Onions", "Peppers", "Olives", "Spinach", "Corn", "Jalapenos");

        // Build pizza with constraints using ConstrainedProductBuilder
        var pizzaBuilder = JNumberTools.cartesianProduct()
                .constrainedProductOfDistinct(1, bases)                    // Choose 1 base
                .andDistinct(2, cheeses)                           // Choose 2 distinct cheeses
                .andMultiSelect(2, sauces)                         // Choose 2 sauces (can repeat)
                .andInRange(0, 3, meats)                           // 0-3 meats (optional)
                .andInRange(0, 4, veggies);                        // 0-4 veggies (optional)

        BigInteger totalPizzas = pizzaBuilder.count();
        System.out.println("   Total possible pizza combinations: " + totalPizzas);

        // Sample some random pizzas
        System.out.println("\n   Sample pizza configurations:");
        pizzaBuilder.choice(5,new Random())
                .stream()
                .forEach(pizza -> {
                    System.out.print("   Pizza: ");
                    System.out.print("Base=" + pizza.get(0) + ", ");
                    System.out.print("Cheeses=" + pizza.get(1) + ", " + pizza.get(2) + ", ");
                    System.out.print("Sauces=" + pizza.get(3) + ", " + pizza.get(4) + ", ");
                    System.out.print("Meats=" + (pizza.size() > 5 ? pizza.subList(5, Math.min(8, pizza.size())) : "none") + ", ");
                    System.out.print("Veggies=" + (pizza.size() > 8 ? pizza.subList(8, pizza.size()) : "none"));
                    System.out.println();
                });

        // Find specific pizza by rank
        BigInteger specialRank = new BigInteger("1000000");
        System.out.println("\n   Pizza at rank " + specialRank + ":");
        var specialPizza = pizzaBuilder.byRanks(List.of(specialRank))
                .stream()
                .findFirst()
                .orElseThrow();
        System.out.println("   " + specialPizza);
    }

    private static void travelPackagePlanner() {
        System.out.println("\n2. TRAVEL PACKAGE PLANNER\n");

        var destinations = List.of("Paris", "London", "Tokyo", "New York", "Sydney", "Rome", "Barcelona");
        var hotels = List.of("Budget", "Standard", "Deluxe", "Luxury", "All-Inclusive");
        var transport = List.of("Flight", "Train", "Cruise", "Multi-city");
        var durations = List.of(3, 5, 7, 10, 14, 21);
        var activities = List.of("Tours", "Museums", "Adventure", "Relaxation", "Shopping", "Cultural");

        // For activities with repetition, we need multiple dimensions
        var packageBuilder = JNumberTools.cartesianProduct()
                .simpleProductOf(destinations)
                .and(hotels)
                .and(transport)
                .and(durations)
                .and(activities)  // First activity
                .and(activities)  // Second activity
                .and(activities); // Third activity

        System.out.println("   Total package combinations: " + packageBuilder.count());

        // Find packages with specific criteria using streaming
        System.out.println("\n   Luxury packages to Europe with 10+ days:");
        packageBuilder.lexOrder()
                .stream()
                .filter(pkg -> {
                    String dest = (String) pkg.get(0);
                    return dest.equals("Paris") || dest.equals("London") ||
                            dest.equals("Rome") || dest.equals("Barcelona");
                })
                .filter(pkg -> pkg.get(1).equals("Luxury") || pkg.get(1).equals("All-Inclusive"))
                .filter(pkg -> (Integer) pkg.get(3) >= 10)
                .limit(5)
                .forEach(pkg -> System.out.println("   " + pkg));

        // Analyze package distribution
        System.out.println("\n   Package count by destination:");
        var destCounts = packageBuilder.lexOrder()
                .stream()
                .limit(10000)  // Analyze first 10,000 packages
                .collect(Collectors.groupingBy(
                        pkg -> (String) pkg.get(0),
                        Collectors.counting()
                ));
        destCounts.forEach((dest, count) ->
                System.out.println("   " + dest + ": " + count));
    }

    private static void computerBuildOptimizer() {
        System.out.println("\n3. COMPUTER BUILD OPTIMIZER\n");

        var cpus = List.of("i5", "i7", "i9", "Ryzen 5", "Ryzen 7", "Ryzen 9");
        var gpus = List.of("RTX 3060", "RTX 3070", "RTX 3080", "RTX 4090", "RX 6800");
        var rams = List.of("16GB", "32GB", "64GB", "128GB");
        var storages = List.of("512GB SSD", "1TB SSD", "2TB SSD", "512GB+1TB HDD");
        var psus = List.of("550W", "650W", "750W", "850W", "1000W");

        // All possible configurations using simple product
        var allBuilds = JNumberTools.cartesianProduct()
                .simpleProductOf(cpus)
                .and(gpus)
                .and(rams)
                .and(storages)
                .and(psus);

        System.out.println("   Total possible builds: " + allBuilds.count());

        // Find high-end builds (i9/RTX 4090/64GB+)
        System.out.println("\n   High-end builds (first 5):");
        allBuilds.lexOrder()
                .stream()
                .filter(build -> build.get(0).equals("i9") || build.get(0).equals("Ryzen 9"))
                .filter(build -> build.get(1).equals("RTX 4090"))
                .filter(build -> ((String) build.get(2)).startsWith("64") ||
                        ((String) build.get(2)).startsWith("128"))
                .limit(5)
                .forEach(build -> System.out.println("   " + build));

        // Budget builds using m-th generation
        System.out.println("\n   Every 1000th budget build:");
        allBuilds.lexOrderMth(1000, 0)
                .stream()
                .limit(3)
                .forEach(build -> System.out.println("   " + build));
    }

    private static void teamFormationWithConstraints() {
        System.out.println("\n4. TEAM FORMATION WITH CONSTRAINTS\n");

        var roles = List.of("Frontend", "Backend", "Database", "DevOps", "UI/UX");
        var experience = List.of("Junior", "Mid", "Senior", "Lead");
        var locations = List.of("US", "UK", "India", "Remote");

        // For team formation, we need to assign developers to roles
        // This is a complex constraint that might need custom implementation
        // Here's a simplified version showing role-experience-location combinations
        var teamBuilder = JNumberTools.cartesianProduct()
                .simpleProductOf(roles)
                .and(experience)
                .and(locations);

        System.out.println("   Role assignments with experience and location: " +
                teamBuilder.count() + " combinations");

        // Find senior roles in remote locations
        System.out.println("\n   Senior roles in remote locations:");
        teamBuilder.lexOrder()
                .stream()
                .filter(assignment -> assignment.get(1).equals("Senior") ||
                        assignment.get(1).equals("Lead"))
                .filter(assignment -> assignment.get(2).equals("Remote"))
                .limit(5)
                .forEach(assignment ->
                        System.out.println("   " + assignment.get(0) + ": " +
                                assignment.get(1) + ", " + assignment.get(2)));
    }

    private static void productAnalysisAndFiltering() {
        System.out.println("\n5. PRODUCT ANALYSIS AND FILTERING\n");

        var products = List.of("Laptop", "Phone", "Tablet", "Monitor", "Keyboard", "Mouse");
        var colors = List.of("Black", "Silver", "White", "Blue", "Red");
        var prices = List.of(499, 699, 899, 1099, 1299, 1499);
        var ratings = List.of(3.5, 4.0, 4.5, 5.0);

        var catalog = JNumberTools.cartesianProduct()
                .simpleProductOf(products)
                .and(colors)
                .and(prices)
                .and(ratings);

        System.out.println("   Total product variants: " + catalog.count());

        // Find best value products
        System.out.println("\n   Best value products (price < 1000, rating > 4.0):");
        catalog.lexOrder()
                .stream()
                .filter(p -> (Integer) p.get(2) < 1000)
                .filter(p -> (Double) p.get(3) > 4.0)
                .limit(5)
                .forEach(p -> System.out.println("   " + p));

        // Price distribution analysis
        System.out.println("\n   Price distribution for Laptops:");
        var laptopPrices = catalog.lexOrder()
                .stream()
                .filter(p -> p.get(0).equals("Laptop"))
                .map(p -> (Integer) p.get(2))
                .limit(100)
                .collect(Collectors.groupingBy(
                        price -> price,
                        Collectors.counting()
                ));
        laptopPrices.forEach((price, count) ->
                System.out.println("   $" + price + ": " + count + " variants"));

        // Find unique combinations using byRanks
        System.out.println("\n   Every 500th product in catalog:");
        catalog.lexOrderMth(500, 0)
                .stream()
                .limit(3)
                .forEach(p -> System.out.println("   " + p));
    }

    private static void largeScaleProductProcessing() {
        System.out.println("\n6. LARGE-SCALE PRODUCT PROCESSING\n");

        // Create a large product space
        var options1 = List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J");
        var options2 = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        var options3 = List.of("X", "Y", "Z", "W", "V", "U", "T", "S", "R", "Q");
        var options4 = List.of(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000);

        var largeProduct = JNumberTools.cartesianProduct()
                .simpleProductOf(options1)
                .and(options2)
                .and(options3)
                .and(options4);

        BigInteger total = largeProduct.count();
        System.out.println("   Total combinations: " + total);
        System.out.println("   (10×10×10×10 = 10,000 combinations)");

        // Process in chunks using m-th generation
        System.out.println("\n   Processing every 2,000th combination:");
        int chunkSize = 2000;
        BigInteger current = BigInteger.ZERO;
        int samples = 0;

        while (current.compareTo(total) < 0 && samples < 5) {
            var chunk = largeProduct.lexOrderMth(BigInteger.ONE, current)
                    .stream()
                    .limit(1)
                    .findFirst()
                    .orElseThrow();
            System.out.println("   Sample at rank " + current + ": " + chunk);
            current = current.add(BigInteger.valueOf(chunkSize));
            samples++;
        }

        // Parallel processing example
        System.out.println("\n   Parallel processing of first 10,000 combinations:");
        long count = largeProduct.lexOrder()
                .stream()
                .parallel()
                .limit(10000)
                .filter(p -> ((Integer) p.get(1)) % 2 == 0)  // Even numbers
                .filter(p -> ((String) p.get(2)).compareTo("M") > 0)  // After 'M'
                .count();
        System.out.println("   Matching combinations: " + count);
    }
}
