package io.github.deepeshpatel.jnumbertools.api.examples.realworld.business;

import io.github.deepeshpatel.jnumbertools.api.JNumberTools;

import java.util.List;
import java.util.Random;

/**
 * Demonstrates marketing campaign optimization
 * Shows combinatorial approaches to marketing mix optimization
 */
public class MarketingCampaigns {

    public static void main(String[] args) {
        System.out.println("=== Marketing Campaign Optimizer ===\n");

        generateCampaignCombinations();
        generateAudienceTargeting();
        generateChannelMix();
        generateBudgetAllocation();
    }

    private static void generateCampaignCombinations() {
        System.out.println("Marketing Campaign Combinations:");

        var campaignTypes = List.of("Awareness", "Consideration", "Conversion", "Retention", "Advocacy");
        var channels = List.of("Email", "Social Media", "Search", "Display", "Video", "Podcast");
        var audienceSegments = List.of("New Users", "Active Users", "Lapsed Users", "Premium Users", "Enterprise");
        var contentTypes = List.of("Text", "Image", "Video", "Interactive", "Webinar");

        var campaigns = JNumberTools.cartesianProduct()
                .simpleProductOf(campaignTypes)
                .and(channels)
                .and(audienceSegments)
                .and(contentTypes);

        System.out.println("Total campaign combinations: " + campaigns.count());
        System.out.println("\nSample campaign combinations:");
        campaigns.lexOrder()
                .stream()
                .limit(8)
                .forEach(MarketingCampaigns::formatCampaign);
    }

    private static void generateAudienceTargeting() {
        System.out.println("\nAudience Targeting Strategies:");

        var demographics = List.of("18-24", "25-34", "35-44", "45-54", "55+");
        var interests = List.of("Technology", "Sports", "Fashion", "Travel", "Food", "Finance");
        var behaviors = List.of("Early Adopters", "Price Sensitive", "Brand Loyal", "Researchers", "Impulse Buyers");
        var locations = List.of("Urban", "Suburban", "Rural", "International");

        var targeting = JNumberTools.cartesianProduct()
                .simpleProductOf(demographics)
                .and(interests)
                .and(behaviors)
                .and(locations);

        System.out.println("Total targeting combinations: " + targeting.count());
        System.out.println("\nSample audience segments:");
        targeting.lexOrderMth(1000, 0)
                .stream()
                .limit(5)
                .forEach(MarketingCampaigns::formatTargeting);
    }

    private static void generateChannelMix() {
        System.out.println("\nMulti-Channel Marketing Mix:");

        var primaryChannels = List.of("Email", "Social", "Search", "Display");
        var secondaryChannels = List.of("Video", "Podcast", "Native", "Influencer");
        var tertiaryChannels = List.of("Direct Mail", "Events", "PR", "Affiliate");
        var budgets = List.of("Low", "Medium", "High", "Maximum");

        var channelMix = JNumberTools.cartesianProduct()
                .simpleProductOf(primaryChannels)
                .and(secondaryChannels)
                .and(tertiaryChannels)
                .and(budgets);

        System.out.println("Total channel mix combinations: " + channelMix.count());
        System.out.println("\nSample channel mixes:");
        channelMix.choice(6,new Random())
                .stream()
                .forEach(MarketingCampaigns::formatChannelMix);
    }

    private static void generateBudgetAllocation() {
        System.out.println("\nBudget Allocation Scenarios:");

        var totalBudgets = List.of(10000, 25000, 50000, 100000, 250000);
        var allocationStrategies = List.of("Equal Split", "Performance-Based", "Seasonal", "Geographic");
        var timePeriods = List.of("Monthly", "Quarterly", "Bi-annually", "Annually");
        var objectives = List.of("Brand Awareness", "Lead Generation", "Sales", "Retention");

        var allocations = JNumberTools.cartesianProduct()
                .simpleProductOf(totalBudgets)
                .and(allocationStrategies)
                .and(timePeriods)
                .and(objectives);

        System.out.println("Total allocation scenarios: " + allocations.count());
        System.out.println("\nSample budget allocations:");
        allocations.lexOrder()
                .stream()
                .limit(6)
                .forEach(MarketingCampaigns::formatBudgetAllocation);
    }

    private static void formatCampaign(List<Object> campaign) {
        System.out.printf("  %s campaign via %s targeting %s with %s content%n",
                campaign.get(0), campaign.get(1), campaign.get(2), campaign.get(3));
    }

    private static void formatTargeting(List<Object> targeting) {
        System.out.printf("  Target %s, %s, %s in %s areas%n",
                targeting.get(0), targeting.get(1), targeting.get(2), targeting.get(3));
    }

    private static void formatChannelMix(List<Object> mix) {
        System.out.printf("  %s + %s + %s channels (%s budget)%n",
                mix.get(0), mix.get(1), mix.get(2), mix.get(3));
    }

    private static void formatBudgetAllocation(List<Object> allocation) {
        System.out.printf("  $%,d budget, %s strategy, %s review, %s objective%n",
                allocation.get(0), allocation.get(1), allocation.get(2), allocation.get(3));
    }
}
