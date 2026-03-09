package io.github.deepeshpatel.jnumbertools.examples.realworld.business;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;

import java.util.List;
import java.util.Random;

/**
 * Demonstrates inventory management scenarios
 * Shows combinatorial approaches to supply chain optimization
 */
public class InventoryManagement {

    record WarehouseItem(String product, String location, int quantity, double cost) {}

    public static void main(String[] args) {
        System.out.println("=== Inventory Management System ===\n");

        generateWarehouseAllocations();
        generateSupplierCombinations();
        generateDistributionPlans();
        generateReorderStrategies();
    }

    private static void generateWarehouseAllocations() {
        System.out.println("Warehouse Product Allocation:");

        var products = List.of("Laptop", "Monitor", "Keyboard", "Mouse", "Desk", "Chair");
        var warehouses = List.of("North", "South", "East", "West", "Central");
        var quantities = List.of(10, 25, 50, 100, 250, 500);

        var allocations = JNumberTools.cartesianProduct()
                .simpleProductOf(products)
                .and(warehouses)
                .and(quantities);

        System.out.println("Total possible allocations: " + allocations.count());
        System.out.println("\nSample product-warehouse allocations:");
        allocations.lexOrder()
                .stream()
                .limit(10)
                .forEach(InventoryManagement::formatAllocation);
    }

    private static void generateSupplierCombinations() {
        System.out.println("\nSupplier Selection Combinations:");

        var suppliers = List.of("SupplierA", "SupplierB", "SupplierC", "SupplierD", "SupplierE");
        var productCategories = List.of("Electronics", "Furniture", "Office Supplies", "Accessories");
        var qualityLevels = List.of("Premium", "Standard", "Economy");
        var deliveryTimes = List.of("1-2 days", "3-5 days", "1-2 weeks", "2-4 weeks");

        var combinations = JNumberTools.cartesianProduct()
                .simpleProductOf(suppliers)
                .and(productCategories)
                .and(qualityLevels)
                .and(deliveryTimes);

        System.out.println("Total supplier combinations: " + combinations.count());
        System.out.println("\nOptimal supplier combinations (every 500th):");
        combinations.lexOrderMth(500, 0)
                .stream()
                .limit(5)
                .forEach(InventoryManagement::formatSupplierCombo);
    }

    private static void generateDistributionPlans() {
        System.out.println("\nDistribution Network Planning:");

        var distributionCenters = List.of("DC-1", "DC-2", "DC-3");
        var retailStores = List.of("Store-A", "Store-B", "Store-C", "Store-D", "Store-E");
        var transportModes = List.of("Truck", "Rail", "Air", "Sea");
        var priorityLevels = List.of("Urgent", "Standard", "Economy");

        var plans = JNumberTools.cartesianProduct()
                .simpleProductOf(distributionCenters)
                .and(retailStores)
                .and(transportModes)
                .and(priorityLevels);

        System.out.println("Total distribution plans: " + plans.count());
        System.out.println("\nSample distribution plans:");
        plans.choice(8,new Random())
                .stream()
                .forEach(InventoryManagement::formatDistributionPlan);
    }

    private static void generateReorderStrategies() {
        System.out.println("\nReorder Strategy Analysis:");

        var products = List.of("CPU", "RAM", "SSD", "GPU", "Motherboard");
        var reorderPoints = List.of(10, 20, 50, 100, 200);
        var orderQuantities = List.of(25, 50, 100, 250, 500, 1000);
        var reviewPeriods = List.of("Daily", "Weekly", "Bi-weekly", "Monthly");

        var strategies = JNumberTools.cartesianProduct()
                .simpleProductOf(products)
                .and(reorderPoints)
                .and(orderQuantities)
                .and(reviewPeriods);

        System.out.println("Total reorder strategies: " + strategies.count());
        System.out.println("\nCost-effective reorder strategies:");
        strategies.lexOrder()
                .stream()
                .limit(6)
                .forEach(InventoryManagement::formatReorderStrategy);
    }

    private static void formatAllocation(List<Object> allocation) {
        System.out.printf("  %s allocated to %s warehouse: %d units%n",
                allocation.get(0), allocation.get(1), allocation.get(2));
    }

    private static void formatSupplierCombo(List<Object> combo) {
        System.out.printf("  %s for %s (%s quality, %s delivery)%n",
                combo.get(0), combo.get(1), combo.get(2), combo.get(3));
    }

    private static void formatDistributionPlan(List<Object> plan) {
        System.out.printf("  %s → %s via %s (%s priority)%n",
                plan.get(0), plan.get(1), plan.get(2), plan.get(3));
    }

    private static void formatReorderStrategy(List<Object> strategy) {
        System.out.printf("  %s: Reorder at %d, Order %d units, Review %s%n",
                strategy.get(0), strategy.get(1), strategy.get(2), strategy.get(3));
    }
}
