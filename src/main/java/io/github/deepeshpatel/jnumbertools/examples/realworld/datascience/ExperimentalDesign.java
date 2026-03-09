package io.github.deepeshpatel.jnumbertools.examples.realworld.datascience;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;

import java.util.List;
import java.util.Random;

/**
 * Demonstrates experimental design for scientific research
 * Shows combinatorial approaches to factorial designs and parameter optimization
 */
public class ExperimentalDesign {

    record Experiment(String name, List<String> factors, List<String> levels) {}

    public static void main(String[] args) {
        System.out.println("=== Experimental Design Generator ===\n");

        generateFactorialDesigns();
        generateParameterOptimization();
        generateTreatmentCombinations();
        generateBlockingDesigns();
    }

    private static void generateFactorialDesigns() {
        System.out.println("Full Factorial Experimental Designs:");

        var factors = List.of("Temperature", "Pressure", "pH", "Concentration", "Time");
        var levels = List.of("Low", "Medium", "High");
        var replications = List.of(1, 2, 3, 4, 5);

        var designs = JNumberTools.cartesianProduct()
                .simpleProductOf(factors)
                .and(levels)
                .and(replications);

        System.out.println("Total experimental conditions: " + designs.count());
        System.out.println("\nSample experimental conditions:");
        designs.lexOrder()
                .stream()
                .limit(10)
                .forEach(ExperimentalDesign::formatFactorialCondition);
    }

    private static void generateParameterOptimization() {
        System.out.println("\nMachine Learning Parameter Optimization:");

        var algorithms = List.of("Random Forest", "SVM", "Neural Network", "Gradient Boosting", "KNN");
        var hyperparameters = List.of("Learning Rate", "Tree Depth", "Regularization", "Batch Size", "Epochs");
        var parameterValues = List.of("0.001", "0.01", "0.1", "1.0", "10.0");
        var validationMethods = List.of("Cross-Validation", "Holdout", "Bootstrap", "Time Series Split");

        var optimization = JNumberTools.cartesianProduct()
                .simpleProductOf(algorithms)
                .and(hyperparameters)
                .and(parameterValues)
                .and(validationMethods);

        System.out.println("Total parameter combinations: " + optimization.count());
        System.out.println("\nSample ML parameter combinations:");
        optimization.lexOrderMth(500, 0)
                .stream()
                .limit(5)
                .forEach(ExperimentalDesign::formatParameterCombo);
    }

    private static void generateTreatmentCombinations() {
        System.out.println("\nMedical Treatment Combinations:");

        var treatments = List.of("Drug A", "Drug B", "Drug C", "Placebo", "Therapy");
        var dosages = List.of("Low", "Medium", "High");
        var frequencies = List.of("Daily", "Weekly", "Bi-weekly", "Monthly");
        var durations = List.of("2 weeks", "4 weeks", "8 weeks", "12 weeks");

        var combinations = JNumberTools.cartesianProduct()
                .simpleProductOf(treatments)
                .and(dosages)
                .and(frequencies)
                .and(durations);

        System.out.println("Total treatment combinations: " + combinations.count());
        System.out.println("\nSample treatment protocols:");
        combinations.choice(8,new Random())
                .stream()
                .forEach(ExperimentalDesign::formatTreatmentCombo);
    }

    private static void generateBlockingDesigns() {
        System.out.println("\nRandomized Block Designs:");

        var blocks = List.of("Block1", "Block2", "Block3", "Block4", "Block5");
        var treatments = List.of("Treatment A", "Treatment B", "Treatment C", "Control");
        var covariates = List.of("Age", "Gender", "BMI", "Baseline Score");
        var randomizationStrategies = List.of("Simple Random", "Stratified", "Cluster", "Systematic");

        var blockingDesigns = JNumberTools.cartesianProduct()
                .simpleProductOf(blocks)
                .and(treatments)
                .and(covariates)
                .and(randomizationStrategies);

        System.out.println("Total blocking designs: " + blockingDesigns.count());
        System.out.println("\nSample block designs:");
        blockingDesigns.lexOrder()
                .stream()
                .limit(6)
                .forEach(ExperimentalDesign::formatBlockingDesign);
    }

    private static void formatFactorialCondition(List<Object> condition) {
        System.out.printf("  %s at %s level, %d replications%n",
                condition.get(0), condition.get(1), condition.get(2));
    }

    private static void formatParameterCombo(List<Object> combo) {
        System.out.printf("  %s: %s = %s, validated by %s%n",
                combo.get(0), combo.get(1), combo.get(2), combo.get(3));
    }

    private static void formatTreatmentCombo(List<Object> combo) {
        System.out.printf("  %s (%s), %s, %s duration%n",
                combo.get(0), combo.get(1), combo.get(2), combo.get(3));
    }

    private static void formatBlockingDesign(List<Object> design) {
        System.out.printf("  %s: %s, controlling for %s (%s randomization)%n",
                design.get(0), design.get(1), design.get(2), design.get(3));
    }
}
