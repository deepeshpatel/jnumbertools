package io.github.deepeshpatel.jnumbertools.examples.realworld.operations;

import io.github.deepeshpatel.jnumbertools.api.JNumberTools;

import java.util.List;
import java.util.Random;

/**
 * Demonstrates resource optimization for operations management
 * Shows combinatorial approaches to scheduling and allocation problems
 */
public class ResourceOptimization {

    public static void main(String[] args) {
        System.out.println("=== Resource Optimization System ===\n");

        generateEmployeeScheduling();
        generateEquipmentAllocation();
        generateShiftPlanning();
        generateCapacityPlanning();
    }

    private static void generateEmployeeScheduling() {
        System.out.println("Employee Scheduling Combinations:");

        var employees = List.of("Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", "Grace", "Henry");
        var shifts = List.of("Morning", "Afternoon", "Evening", "Night", "Weekend");
        var departments = List.of("Sales", "Support", "Development", "Marketing", "Operations");
        var skillLevels = List.of("Junior", "Intermediate", "Senior", "Expert");

        var schedules = JNumberTools.cartesianProduct()
                .simpleProductOf(employees)
                .and(shifts)
                .and(departments)
                .and(skillLevels);

        System.out.println("Total possible assignments: " + schedules.count());
        System.out.println("\nSample employee assignments:");
        schedules.lexOrder()
                .stream()
                .limit(10)
                .forEach(ResourceOptimization::formatEmployeeSchedule);
    }

    private static void generateEquipmentAllocation() {
        System.out.println("\nEquipment Allocation Scenarios:");

        var equipment = List.of("Server", "Workstation", "Printer", "Scanner", "Projector", "Conference Phone");
        var locations = List.of("Floor 1", "Floor 2", "Floor 3", "Basement", "Remote");
        var priorities = List.of("Critical", "High", "Medium", "Low", "Optional");
        var timeSlots = List.of("8-10", "10-12", "1-3", "3-5", "5-7");

        var allocations = JNumberTools.cartesianProduct()
                .simpleProductOf(equipment)
                .and(locations)
                .and(priorities)
                .and(timeSlots);

        System.out.println("Total equipment allocations: " + allocations.count());
        System.out.println("\nSample equipment allocations:");
        allocations.lexOrderMth(800, 0)
                .stream()
                .limit(5)
                .forEach(ResourceOptimization::formatEquipmentAllocation);
    }

    private static void generateShiftPlanning() {
        System.out.println("\nShift Planning Combinations:");

        var shiftTypes = List.of("8-hour", "10-hour", "12-hour", "Rotating", "Flexible");
        var breakPatterns = List.of("Two 15-min", "One 30-min", "Three 10-min", "Custom");
        var coverageRequirements = List.of("Minimal", "Standard", "Enhanced", "24/7", "Peak Hours");
        var costLevels = List.of("Budget", "Standard", "Premium", "Overtime");

        var shiftPlans = JNumberTools.cartesianProduct()
                .simpleProductOf(shiftTypes)
                .and(breakPatterns)
                .and(coverageRequirements)
                .and(costLevels);

        System.out.println("Total shift plan combinations: " + shiftPlans.count());
        System.out.println("\nSample shift plans:");
        shiftPlans.choice(6,new Random())
                .stream()
                .forEach(ResourceOptimization::formatShiftPlan);
    }

    private static void generateCapacityPlanning() {
        System.out.println("\nCapacity Planning Scenarios:");

        var resourceTypes = List.of("CPU", "Memory", "Storage", "Network", "Power");
        var demandLevels = List.of("Low", "Medium", "High", "Peak", "Burst");
        var scalingStrategies = List.of("Manual", "Auto-Scale", "Predictive", "Hybrid");
        var timeHorizons = List.of("Daily", "Weekly", "Monthly", "Quarterly", "Yearly");

        var capacityPlans = JNumberTools.cartesianProduct()
                .simpleProductOf(resourceTypes)
                .and(demandLevels)
                .and(scalingStrategies)
                .and(timeHorizons);

        System.out.println("Total capacity planning scenarios: " + capacityPlans.count());
        System.out.println("\nSample capacity planning scenarios:");
        capacityPlans.lexOrder()
                .stream()
                .limit(6)
                .forEach(ResourceOptimization::formatCapacityPlan);
    }

    private static void formatEmployeeSchedule(List<Object> schedule) {
        System.out.printf("  %s assigned to %s shift in %s department (%s level)%n",
                schedule.get(0), schedule.get(1), schedule.get(2), schedule.get(3));
    }

    private static void formatEquipmentAllocation(List<Object> allocation) {
        System.out.printf("  %s at %s (%s priority, %s time slot)%n",
                allocation.get(0), allocation.get(1), allocation.get(2), allocation.get(3));
    }

    private static void formatShiftPlan(List<Object> plan) {
        System.out.printf("  %s shift with %s breaks, %s coverage, %s cost%n",
                plan.get(0), plan.get(1), plan.get(2), plan.get(3));
    }

    private static void formatCapacityPlan(List<Object> plan) {
        System.out.printf("  %s capacity for %s demand, %s scaling, %s horizon%n",
                plan.get(0), plan.get(1), plan.get(2), plan.get(3));
    }
}
