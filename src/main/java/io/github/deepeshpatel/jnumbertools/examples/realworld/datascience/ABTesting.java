package io.github.deepeshpatel.jnumbertools.examples.realworld.datascience;

import io.github.deepeshpatel.jnumbertools.api.JNumberTools;

import java.util.List;
import java.util.Random;

/**
 * Demonstrates A/B testing experimental design
 * Shows combinatorial approaches to test design and analysis
 */
public class ABTesting {

    public static void main(String[] args) {
        System.out.println("=== A/B Testing Design Generator ===\n");

        generateTestVariations();
        generateAudienceSegments();
        generateTrafficAllocations();
        generateSuccessMetrics();
    }

    private static void generateTestVariations() {
        System.out.println("A/B Test Variations:");

        var testTypes = List.of("Headline", "Button Color", "Layout", "Pricing", "Image", "Copy");
        var variants = List.of("Control", "Variant A", "Variant B", "Variant C", "Variant D");
        var changeTypes = List.of("Minor", "Moderate", "Major", "Radical");
        var confidenceLevels = List.of("90%", "95%", "99%", "99.9%");

        var variations = JNumberTools.cartesianProduct()
                .simpleProductOf(testTypes)
                .and(variants)
                .and(changeTypes)
                .and(confidenceLevels);

        System.out.println("Total test variations: " + variations.count());
        System.out.println("\nSample A/B test variations:");
        variations.lexOrder()
                .stream()
                .limit(8)
                .forEach(ABTesting::formatTestVariation);
    }

    private static void generateAudienceSegments() {
        System.out.println("\nAudience Segmentation for Testing:");

        var userTypes = List.of("New", "Returning", "Premium", "Churned", "Inactive");
        var trafficSources = List.of("Organic", "Paid", "Direct", "Social", "Referral", "Email");
        var deviceTypes = List.of("Desktop", "Mobile", "Tablet");
        var geographicRegions = List.of("North America", "Europe", "Asia", "South America", "Africa");

        var segments = JNumberTools.cartesianProduct()
                .simpleProductOf(userTypes)
                .and(trafficSources)
                .and(deviceTypes)
                .and(geographicRegions);

        System.out.println("Total audience segments: " + segments.count());
        System.out.println("\nSample audience segments for targeting:");
        segments.lexOrderMth(500, 0)
                .stream()
                .limit(5)
                .forEach(ABTesting::formatAudienceSegment);
    }

    private static void generateTrafficAllocations() {
        System.out.println("\nTraffic Allocation Strategies:");

        var testGroups = List.of("A", "B", "C", "D", "E");
        var allocationMethods = List.of("Equal Split", "Weighted", "Adaptive", "Multi-Armed Bandit");
        var rolloutStrategies = List.of("Immediate", "Gradual", "Phased", "Winner-Takes-All");
        var sampleSizes = List.of(1000, 5000, 10000, 50000, 100000, 500000);

        var allocations = JNumberTools.cartesianProduct()
                .simpleProductOf(testGroups)
                .and(allocationMethods)
                .and(rolloutStrategies)
                .and(sampleSizes);

        System.out.println("Total allocation strategies: " + allocations.count());
        System.out.println("\nSample traffic allocation plans:");
        allocations.choice(6,new Random())
                .stream()
                .forEach(ABTesting::formatTrafficAllocation);
    }

    private static void generateSuccessMetrics() {
        System.out.println("\nSuccess Metrics Combinations:");

        var primaryMetrics = List.of("Conversion Rate", "Click-Through Rate", "Revenue", "Engagement", "Retention");
        var secondaryMetrics = List.of("Bounce Rate", "Time on Page", "Pages per Session", "Cart Abandonment", "Customer Satisfaction");
        var statisticalTests = List.of("t-test", "Chi-square", "Mann-Whitney", "Bootstrap", "Bayesian");
        var significanceThresholds = List.of("0.01", "0.05", "0.10", "0.15", "0.20");

        var metrics = JNumberTools.cartesianProduct()
                .simpleProductOf(primaryMetrics)
                .and(secondaryMetrics)
                .and(statisticalTests)
                .and(significanceThresholds);

        System.out.println("Total metric combinations: " + metrics.count());
        System.out.println("\nSample success metric frameworks:");
        metrics.lexOrder()
                .stream()
                .limit(6)
                .forEach(ABTesting::formatSuccessMetrics);
    }

    private static void formatTestVariation(List<Object> variation) {
        System.out.printf("  %s: %s variant, %s change, %s confidence%n",
                variation.get(0), variation.get(1), variation.get(2), variation.get(3));
    }

    private static void formatAudienceSegment(List<Object> segment) {
        System.out.printf("  %s users from %s on %s in %s%n",
                segment.get(0), segment.get(1), segment.get(2), segment.get(3));
    }

    private static void formatTrafficAllocation(List<Object> allocation) {
        System.out.printf("  Group %s: %s allocation, %s rollout, %,d sample size%n",
                allocation.get(0), allocation.get(1), allocation.get(2), allocation.get(3));
    }

    private static void formatSuccessMetrics(List<Object> metrics) {
        System.out.printf("  Primary: %s, Secondary: %s, Test: %s, α = %s%n",
                metrics.get(0), metrics.get(1), metrics.get(2), metrics.get(3));
    }
}
