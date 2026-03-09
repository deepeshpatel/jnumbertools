package io.github.deepeshpatel.jnumbertools.examples.realworld.business;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;

import java.util.List;
import java.util.Random;

/**
 * Demonstrates product configuration management for e-commerce
 * Shows how combinatorial generators handle real business scenarios
 */
public class ProductConfiguration {

    record ProductVariant(String sku, String name, double price) {}

    public static void main(String[] args) {
        System.out.println("=== Product Configuration Manager ===\n");

        generateLaptopConfigurations();
        generateCarConfigurations();
        generateFurnitureConfigurations();
        generatePricingStrategies();
    }

    private static void generateLaptopConfigurations() {
        System.out.println("Laptop Configuration Options:");

        var processors = List.of("i3", "i5", "i7", "i9");
        var memory = List.of("8GB", "16GB", "32GB", "64GB");
        var storage = List.of("256GB SSD", "512GB SSD", "1TB SSD", "2TB SSD");
        var displays = List.of("14\"", "15.6\"", "17\"");
        var colors = List.of("Silver", "Black", "White", "Blue");

        var configurations = JNumberTools.cartesianProduct()
                .simpleProductOf(processors)
                .and(memory)
                .and(storage)
                .and(displays)
                .and(colors);

        System.out.println("Total possible laptop configurations: " + configurations.count());
        System.out.println("\nFirst 10 configurations:");
        configurations.lexOrder()
                .stream()
                .limit(10)
                .forEach(ProductConfiguration::formatLaptopConfig);
    }

    private static void generateCarConfigurations() {
        System.out.println("\nCar Configuration Options:");

        var models = List.of("Sedan", "SUV", "Coupe", "Hatchback");
        var engines = List.of("2.0L", "2.5L", "3.0L", "Electric");
        var transmissions = List.of("Manual", "Automatic", "CVT");
        var colors = List.of("Red", "Blue", "Black", "White", "Gray", "Silver");
        var packages = List.of("Base", "Sport", "Luxury", "Premium");

        var configurations = JNumberTools.cartesianProduct()
                .simpleProductOf(models)
                .and(engines)
                .and(transmissions)
                .and(colors)
                .and(packages);

        System.out.println("Total possible car configurations: " + configurations.count());
        System.out.println("\nSample configurations (every 1000th):");
        configurations.lexOrderMth(1000, 0)
                .stream()
                .limit(5)
                .forEach(ProductConfiguration::formatCarConfig);
    }

    private static void generateFurnitureConfigurations() {
        System.out.println("\nOffice Furniture Configuration:");

        var desks = List.of("Standing", "Traditional", "Corner", "Executive");
        var chairs = List.of("Ergonomic", "Executive", "Guest", "Conference");
        var storage = List.of("Filing Cabinet", "Bookshelf", "Storage Unit", "None");
        var materials = List.of("Oak", "Mahogany", "Cherry", "Modern");

        var configurations = JNumberTools.cartesianProduct()
                .simpleProductOf(desks)
                .and(chairs)
                .and(storage)
                .and(materials);

        System.out.println("Total office furniture sets: " + configurations.count());
        System.out.println("\nRandom furniture sets for office design:");
        configurations.choice(5, new Random())
                .stream()
                .forEach(ProductConfiguration::formatFurnitureConfig);
    }

    private static void generatePricingStrategies() {
        System.out.println("\nPricing Strategy Combinations:");

        var basePrices = List.of(99.99, 149.99, 199.99, 299.99);
        var discounts = List.of(0.0, 0.1, 0.15, 0.2, 0.25); // 0%, 10%, 15%, 20%, 25%
        var shippingOptions = List.of("Free", "Standard", "Express", "Overnight");
        var customerSegments = List.of("Regular", "Premium", "VIP", "Wholesale");

        var pricing = JNumberTools.cartesianProduct()
                .simpleProductOf(basePrices)
                .and(discounts)
                .and(shippingOptions)
                .and(customerSegments);

        System.out.println("Total pricing combinations: " + pricing.count());
        System.out.println("\nSample pricing strategies:");
        pricing.lexOrder()
                .stream()
                .limit(8)
                .forEach(ProductConfiguration::formatPricingStrategy);
    }

    private static void formatLaptopConfig(List<Object> config) {
        System.out.printf("  Laptop: %s CPU, %s RAM, %s Storage, %s Display, %s Color%n",
                config.get(0), config.get(1), config.get(2), config.get(3), config.get(4));
    }

    private static void formatCarConfig(List<Object> config) {
        System.out.printf("  Car: %s, %s Engine, %s, %s, %s Package%n",
                config.get(0), config.get(1), config.get(2), config.get(3), config.get(4));
    }

    private static void formatFurnitureConfig(List<Object> config) {
        System.out.printf("  Office Set: %s Desk, %s Chair, %s Storage, %s Material%n",
                config.get(0), config.get(1), config.get(2), config.get(3));
    }

    private static void formatPricingStrategy(List<Object> config) {
        double basePrice = (Double) config.get(0);
        double discount = (Double) config.get(1);
        double finalPrice = basePrice * (1 - discount);
        
        System.out.printf("  Pricing: $%.2f -> $%.2f (%.0f%% off), %s Shipping, %s Customer%n",
                basePrice, finalPrice, discount * 100, config.get(2), config.get(3));
    }
}
