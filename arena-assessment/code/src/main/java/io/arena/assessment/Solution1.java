package io.arena.assessment;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

// Time complexity: O(N^2 * M) where
// N - the number of unique words that appear in more than 50 lines
// M - the size of the shortest set of line numbers.
// Space complexity:
// Map[String, Set[Integer]]
// map size is O(N) where N is the number of unique words
// Set[Integer] is O(M) where M is the total number of lines
// Total space is (N+N*M)
public class Solution1 {

    final static int DEFAULT_LEAST_LINES_NUM = 50;

    public static void main(String[] args) throws IOException {

        int leastNumOfLines = DEFAULT_LEAST_LINES_NUM;
        if (args.length < 2) {
            throw new IllegalArgumentException("input and output files path are required");
        }


        if (args.length > 2) {
            leastNumOfLines = Integer.parseInt(args[2]);
        }


        List<String> lines = Utils.load(Paths.get(args[0]));
        long start = System.nanoTime();
        List<Pair<String>> res = solve(lines, leastNumOfLines);
        long end = System.nanoTime();
        Utils.saveAsCSV(Paths.get(args[1]), res);

        long totalTimeInMs = TimeUnit.MILLISECONDS.convert(end - start, TimeUnit.NANOSECONDS);
        System.out.println(String.format("Total time: %d ms", totalTimeInMs));
    }

    static List<Pair<String>> solve(List<String> lines, int leastNumOfLines) {
        Map<String, Set<Integer>> words = parse(lines);
        return findMatchedPairs(words, leastNumOfLines);
    }


    static List<Pair<String>> findMatchedPairs(Map<String, Set<Integer>> words, int leastNumOfLines) {
        List<Pair2<String, Set<Integer>>> candidates = new ArrayList<>();

        // remove any entries where number of lines is < leastNumOfLines
        // otherwise add to candidates

        Iterator<Map.Entry<String, Set<Integer>>> iterator = words.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Set<Integer>> entry = iterator.next();
            if (entry.getValue().size() < leastNumOfLines) {
                iterator.remove();
            } else {
                candidates.add(new Pair2<>(entry.getKey(), entry.getValue()));
            }
        }

        List<Pair<String>> result = new ArrayList<>(words.size() * words.size());

        for (int i = 0; i < candidates.size(); i++) {
            for (int j = i + 1; j < candidates.size(); j++) {
                int matchedLines = intersect(
                        candidates.get(i).second,
                        candidates.get(j).second,
                        leastNumOfLines);

                if (matchedLines >= leastNumOfLines) {
                    result.add(new Pair<>(candidates.get(i).first, candidates.get(j).first));
                }
            }
        }

        return result;
    }

    static Map<String, Set<Integer>> parse(List<String> lines) {
        Map<String, Set<Integer>> uniqueWords = new HashMap<>();

        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String[] words = lines.get(lineNumber).split(",");
            for (String word : words) {
                Set<Integer> lineNumbers = uniqueWords.get(word);
                // java's putIfAbsent is slow, don't use it for critical sections
                if (lineNumbers == null) {
                    lineNumbers = new HashSet<>();
                    uniqueWords.put(word, lineNumbers);
                }
                lineNumbers.add(lineNumber);
            }

        }
        return uniqueWords;
    }

    static int intersect(Set<Integer> s1, Set<Integer> s2, int upperBound) {
        if (s1.isEmpty() || s2.isEmpty()) return 0;
        int matchCount = 0;

        Set<Integer> shortest;
        Set<Integer> longest;

        if (s1.size() < s2.size()) {
            shortest = s1;
            longest = s2;
        } else {
            shortest = s2;
            longest = s1;
        }

        Iterator<Integer> iterator = shortest.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            if (longest.contains(iterator.next())) matchCount++;
            if (matchCount == upperBound || shortest.size() - i < upperBound - matchCount) break;
            i++;
        }
        return matchCount;
    }


}
