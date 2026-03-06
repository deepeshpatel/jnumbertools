package io.github.deepeshpatel.jnumbertools.examples.realworld.statistics;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Demonstrates statistical sampling techniques using combinatorial generators
 */
public class SurveySampling {

    record Respondent(int id, String ageGroup, String region, String income) {}

    public static void main(String[] args) {
        System.out.println("=== Survey Sampling Examples ===\n");

        var population = generatePopulation(1000);
        demonstrateRandomSampling(population);
        demonstrateStratifiedSampling(population);
        demonstrateSystematicSampling(population);
        demonstrateSampleSizeCalculation(population);
    }

    private static List<Respondent> generatePopulation(int size) {
        var ageGroups = List.of("18-25", "26-35", "36-50", "51+");
        var regions = List.of("North", "South", "East", "West", "Central");
        var incomes = List.of("Low", "Medium", "High");

        return IntStream.range(0, size)
                .mapToObj(i -> new Respondent(
                        i,
                        ageGroups.get(i % ageGroups.size()),
                        regions.get(i % regions.size()),
                        incomes.get(i % incomes.size())
                ))
                .toList();
    }

    private static void demonstrateRandomSampling(List<Respondent> population) {
        System.out.println("Simple Random Sampling:");
        System.out.println("Population size: " + population.size());

        // Select 50 random respondents
        var sample = JNumberTools.combinations()
                .unique(50, population)
                .choice(1)
                .stream()
                .findFirst()
                .orElseThrow();

        System.out.println("Sample size: " + sample.size());

        // Analyze sample composition
        var ageCounts = sample.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Respondent::ageGroup, java.util.stream.Collectors.counting()));

        System.out.println("Sample composition by age group:");
        ageCounts.forEach((age, count) ->
                System.out.println("  " + age + ": " + count));
    }

    private static void demonstrateStratifiedSampling(List<Respondent> population) {
        System.out.println("\nStratified Sampling by Region:");

        var byRegion = population.stream()
                .collect(java.util.stream.Collectors.groupingBy(Respondent::region));

        System.out.println("Population distribution by region:");
        byRegion.forEach((region, list) ->
                System.out.println("  " + region + ": " + list.size()));

        // Take 10% sample from each region
        System.out.println("\nStratified sample (10% from each region):");
        byRegion.forEach((region, list) -> {
            int sampleSize = list.size() / 10;
            var sample = JNumberTools.combinations()
                    .unique(sampleSize, list)
                    .choice(1)
                    .stream()
                    .findFirst()
                    .orElseThrow();
            System.out.println("  " + region + ": sampled " + sample.size());
        });
    }

    private static void demonstrateSystematicSampling(List<Respondent> population) {
        System.out.println("\nSystematic Sampling (every 20th respondent):");

        // Using every 20th combination as a proxy for systematic sampling
        var systematic = JNumberTools.combinations()
                .unique(1, population)
                .lexOrderMth(20, 0)
                .stream()
                .limit(10)
                .toList();

        System.out.println("First 10 systematically selected respondents:");
        systematic.forEach(list ->
                System.out.println("  Respondent " + list.get(0).id()));
    }

    private static void demonstrateSampleSizeCalculation(List<Respondent> population) {
        System.out.println("\nSample Size Analysis:");

        int populationSize = population.size();
        double marginOfError = 0.05;
        double confidenceLevel = 0.95;

        // Simplified sample size calculation
        int recommendedSize = (int) (populationSize / (1 + populationSize * marginOfError * marginOfError));

        System.out.println("Population: " + populationSize);
        System.out.println("Margin of error: " + (marginOfError * 100) + "%");
        System.out.println("Confidence level: " + (confidenceLevel * 100) + "%");
        System.out.println("Recommended sample size: " + recommendedSize);

        System.out.println("\nNumber of possible samples of size " + recommendedSize + ":");
        var possibleSamples = JNumberTools.combinations()
                .unique(recommendedSize, population)
                .count();
        System.out.println(possibleSamples + " (that's a lot!)");
    }
}
