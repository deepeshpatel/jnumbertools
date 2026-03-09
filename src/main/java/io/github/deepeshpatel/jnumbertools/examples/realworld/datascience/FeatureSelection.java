package io.github.deepeshpatel.jnumbertools.examples.realworld.datascience;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;

import java.util.List;
import java.util.Random;

/**
 * Demonstrates feature selection for machine learning
 * Shows combinatorial approaches to feature subset optimization
 */
public class FeatureSelection {

    record FeatureSet(String name, int size, double score) {}

    public static void main(String[] args) {
        System.out.println("=== Feature Selection Optimizer ===\n");

        generateFeatureCombinations();
        generateModelConfigurations();
        generateCrossValidationSplits();
        generateHyperparameterGrids();
    }

    private static void generateFeatureCombinations() {
        System.out.println("Feature Subset Combinations:");

        var featureCategories = List.of("Demographic", "Behavioral", "Transactional", "Geographic", "Temporal");
        var featureCounts = List.of(5, 10, 15, 20, 25, 50, 100);
        var selectionMethods = List.of("Correlation", "Mutual Information", "Chi-Square", "ANOVA", "RFE");
        var evaluationMetrics = List.of("Accuracy", "F1-Score", "AUC-ROC", "Precision", "Recall");

        var combinations = JNumberTools.cartesianProduct()
                .simpleProductOf(featureCategories)
                .and(featureCounts)
                .and(selectionMethods)
                .and(evaluationMetrics);

        System.out.println("Total feature combinations: " + combinations.count());
        System.out.println("\nSample feature selection strategies:");
        combinations.lexOrder()
                .stream()
                .limit(8)
                .forEach(FeatureSelection::formatFeatureCombo);
    }

    private static void generateModelConfigurations() {
        System.out.println("\nMachine Learning Model Configurations:");

        var algorithms = List.of("Logistic Regression", "Random Forest", "XGBoost", "Neural Network", "SVM");
        var preprocessingSteps = List.of("Standardization", "Normalization", "PCA", "Feature Scaling", "Encoding");
        var ensembleMethods = List.of("Bagging", "Boosting", "Stacking", "Voting", "Blending");
        var validationStrategies = List.of("K-Fold", "Stratified", "Time Series", "Group", "Leave-One-Out");

        var configurations = JNumberTools.cartesianProduct()
                .simpleProductOf(algorithms)
                .and(preprocessingSteps)
                .and(ensembleMethods)
                .and(validationStrategies);

        System.out.println("Total model configurations: " + configurations.count());
        System.out.println("\nSample ML configurations:");
        configurations.lexOrderMth(1000, 0)
                .stream()
                .limit(5)
                .forEach(FeatureSelection::formatModelConfig);
    }

    private static void generateCrossValidationSplits() {
        System.out.println("\nCross-Validation Split Strategies:");

        var datasetSizes = List.of(1000, 5000, 10000, 50000, 100000);
        var foldNumbers = List.of(3, 5, 7, 10, 15, 20);
        var splitRatios = List.of("70-30", "80-20", "90-10", "60-40", "75-25");
        var stratificationFactors = List.of("Class Balance", "Feature Distribution", "Temporal", "Geographic", "None");

        var splits = JNumberTools.cartesianProduct()
                .simpleProductOf(datasetSizes)
                .and(foldNumbers)
                .and(splitRatios)
                .and(stratificationFactors);

        System.out.println("Total CV split strategies: " + splits.count());
        System.out.println("\nSample cross-validation strategies:");
        splits.choice(6,new Random())
                .stream()
                .forEach(FeatureSelection::formatCVSplit);
    }

    private static void generateHyperparameterGrids() {
        System.out.println("\nHyperparameter Grid Search:");

        var modelTypes = List.of("Tree-based", "Neural Network", "Linear", "Kernel-based", "Ensemble");
        var parameterTypes = List.of("Learning Rate", "Regularization", "Depth", "Iterations", "Batch Size");
        var gridSizes = List.of("Coarse", "Medium", "Fine", "Adaptive", "Bayesian");
        var searchStrategies = List.of("Grid Search", "Random Search", "Bayesian", "Evolutionary", "Gradient-Based");

        var grids = JNumberTools.cartesianProduct()
                .simpleProductOf(modelTypes)
                .and(parameterTypes)
                .and(gridSizes)
                .and(searchStrategies);

        System.out.println("Total hyperparameter grids: " + grids.count());
        System.out.println("\nSample hyperparameter search configurations:");
        grids.lexOrder()
                .stream()
                .limit(6)
                .forEach(FeatureSelection::formatHyperparameterGrid);
    }

    private static void formatFeatureCombo(List<Object> combo) {
        System.out.printf("  %s features (%d selected) using %s, optimized for %s%n",
                combo.get(0), combo.get(1), combo.get(2), combo.get(3));
    }

    private static void formatModelConfig(List<Object> config) {
        System.out.printf("  %s with %s preprocessing, %s ensemble, %s validation%n",
                config.get(0), config.get(1), config.get(2), config.get(3));
    }

    private static void formatCVSplit(List<Object> split) {
        System.out.printf("  Dataset %,d: %d-fold, %s split, %s stratification%n",
                split.get(0), split.get(1), split.get(2), split.get(3));
    }

    private static void formatHyperparameterGrid(List<Object> grid) {
        System.out.printf("  %s model: %s optimization, %s grid, %s search%n",
                grid.get(0), grid.get(1), grid.get(2), grid.get(3));
    }
}
