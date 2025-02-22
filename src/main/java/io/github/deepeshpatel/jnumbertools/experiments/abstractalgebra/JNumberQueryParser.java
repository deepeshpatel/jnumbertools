package io.github.deepeshpatel.jnumbertools.experiments.abstractalgebra;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

public class JNumberQueryParser {
//    public static void executeQuery(String query) {
//        // Convert to uppercase for case-insensitive parsing
//        query = query.toUpperCase().trim();
//
//        // Extract main components
//        Pattern mainPattern = Pattern.compile(
//                "(COMBINATIONS|PERMUTATIONS|SUBSETS)\\s+(?:OF\\s+(\\d+)\\s+ITEMS\\s+)?FROM\\s+(.+?)(?:\\s+ORDER\\s+BY\\s+(LEX|COMBINATION))?(?:\\s+SKIP\\s+(\\d+)\\s+TAKE\\s+EVERY\\s+(\\d+))?"
//        );
//        Matcher matcher = mainPattern.matcher(query);
//
//        if (!matcher.matches()) {
//            throw new IllegalArgumentException("Invalid query syntax");
//        }
//
//        String operation = matcher.group(1);       // COMBINATIONS, PERMUTATIONS, SUBSETS
//        String kStr = matcher.group(2);            // Number of items (optional)
//        int k = kStr != null ? Integer.parseInt(kStr) : -1;
//        String sourcePart = matcher.group(3);      // MULTISET {...}, RANGE[n], etc.
//        String order = matcher.group(4);           // LEX or COMBINATION (optional)
//        String skipStr = matcher.group(5);         // Skip value (optional)
//        String takeStr = matcher.group(6);         // Take every value (optional)
//        int skip = skipStr != null ? Integer.parseInt(skipStr) : 0;
//        int take = takeStr != null ? Integer.parseInt(takeStr) : 1;
//
//        // Parse the source
//        if (sourcePart.startsWith("MULTISET")) {
//            handleMultiset(operation, k, sourcePart, order, skip, take);
//        } else if (sourcePart.startsWith("RANGE")) {
//            handleRange(operation, k, sourcePart, order, skip, take);
//        } else if (sourcePart.startsWith("SET")) {
//            handleSet(operation, k, sourcePart, order, skip, take);
//        } else {
//            throw new IllegalArgumentException("Unsupported source type");
//        }
//    }
//
//    private static void handleMultiset(String operation, int k, String source, String order, int skip, int take) {
//        // Extract elements and frequencies, e.g., {5 mangoes, 3 guavas, 2 bananas}
//        Pattern multisetPattern = Pattern.compile("\\{(.+?)\\}");
//        Matcher m = multisetPattern.matcher(source);
//        if (!m.find()) throw new IllegalArgumentException("Invalid multiset format");
//
//        String[] items = m.group(1).split(",");
//        List<String> elements = new ArrayList<>();
//        int[] frequencies = new int[items.length];
//
//        for (int i = 0; i < items.length; i++) {
//            String[] parts = items[i].trim().split("\\s+");
//            frequencies[i] = Integer.parseInt(parts[0]);
//            elements.add(parts[1]);
//        }
//
//        // Execute based on operation
//        List<?> result;
//        if ("COMBINATIONS".equals(operation)) {
//            if (order == null || "LEX".equals(order)) {
//                if (skip == 0 && take == 1) {
//                    result = JNumberTools.combinations()
//                            .multiset(elements, frequencies, k)
//                            .lexOrder().stream().toList();
//                } else {
//                    result = JNumberTools.combinations()
//                            .multiset(elements, frequencies, k)
//                            .lexOrderMth(take, skip).stream().toList();
//                }
//            } else {
//                throw new IllegalArgumentException("Unsupported order for multiset combinations");
//            }
//        } else if ("PERMUTATIONS".equals(operation)) {
//            if (order == null || "LEX".equals(order)) {
//                if (skip == 0 && take == 1) {
//                    result = JNumberTools.permutations()
//                            .multiset(elements, frequencies)
//                            .lexOrder().stream().toList();
//                } else {
//                    result = JNumberTools.permutations()
//                            .multiset(elements, frequencies)
//                            .lexOrderMth(take, skip).stream().toList();
//                }
//            } else {
//                throw new IllegalArgumentException("Unsupported order for multiset permutations");
//            }
//        } else {
//            throw new IllegalArgumentException("Subsets not supported for multisets yet");
//        }
//
//        System.out.println("Result: " + result);
//    }
//
//    private static void handleRange(String operation, int k, String source, String order, int skip, int take) {
//        Pattern rangePattern = Pattern.compile("RANGE\\[(\\d+)\\](?:\\s+WITH\\s+REPETITION)?");
//        Matcher m = rangePattern.matcher(source);
//        if (!m.matches()) throw new IllegalArgumentException("Invalid range format");
//
//        int n = Integer.parseInt(m.group(1));
//        boolean withRepetition = source.contains("WITH REPETITION");
//
//        List<?> result;
//        if ("COMBINATIONS".equals(operation)) {
//            if (withRepetition) {
//                result = JNumberTools.combinations()
//                        .repetitive(n, k)
//                        .lexOrderMth(take, skip).stream().toList();
//            } else {
//                result = JNumberTools.combinations()
//                        .unique(n, k)
//                        .lexOrderMth(take, skip).stream().toList();
//            }
//        } else if ("PERMUTATIONS".equals(operation)) {
//            if (withRepetition) {
//                result = JNumberTools.permutations()
//                        .repetitive(k, n)
//                        .lexOrderMth(take, skip).stream().toList();
//            } else {
//                result = JNumberTools.permutations()
//                        .nPk(n, k)
//                        .lexOrderMth(take, skip).stream().toList();
//            }
//        } else {
//            result = JNumberTools.subsets()
//                    .of(n)
//                    .all()
//                    .lexOrderMth(take, skip).stream().toList();
//        }
//
//        System.out.println("Result: " + result);
//    }
//
//    private static void handleSet(String operation, int k, String source, String order, int skip, int take) {
//        Pattern setPattern = Pattern.compile("\\{(.+?)\\}");
//        Matcher m = setPattern.matcher(source);
//        if (!m.find()) throw new IllegalArgumentException("Invalid set format");
//
//        String[] elements = m.group(1).split(",");
//        List<String> elementList = Arrays.stream(elements).map(String::trim).collect(Collectors.toList());
//
//        List<?> result;
//        if ("COMBINATIONS".equals(operation)) {
//            result = JNumberTools.combinations()
//                    .unique(k, elementList.toArray(new String[0]))
//                    .lexOrderMth(take, skip).stream().toList();
//        } else if ("PERMUTATIONS".equals(operation)) {
//            result = JNumberTools.permutations()
//                    .nPk(k, elementList.toArray(new String[0]))
//                    .lexOrderMth(take, skip).stream().toList();
//        } else {
//            result = JNumberTools.subsets()
//                    .of(elementList.toArray(new String[0]))
//                    .all()
//                    .lexOrderMth(take, skip).stream().toList();
//        }
//
//        System.out.println("Result: " + result);
//    }
//
//    public static void main(String[] args) {
//        // Test queries
//        String query1 = "COMBINATIONS OF 5 ITEMS FROM MULTISET {5 mangoes, 3 guavas, 2 bananas} ORDER BY LEX";
//        String query2 = "PERMUTATIONS OF 3 ITEMS FROM RANGE[5] WITH REPETITION ORDER BY LEX SKIP 5 TAKE EVERY 2";
//        String query3 = "SUBSETS FROM SET {Apple, Banana, Guava} ORDER BY LEX";
//
//        System.out.println("Executing: " + query1);
//        executeQuery(query1);
//        System.out.println("\nExecuting: " + query2);
//        executeQuery(query2);
//        System.out.println("\nExecuting: " + query3);
//        executeQuery(query3);
//    }
}