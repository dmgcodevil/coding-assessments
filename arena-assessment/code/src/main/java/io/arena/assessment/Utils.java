package io.arena.assessment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Utils {

    static List<String> load(Path filePath) throws IOException {
        return Files.readAllLines(filePath);
    }

    static void saveAsCSV(Path filePath, List<Pair<String>> pairs) throws IOException {
        List<String> lines = pairs.stream()
                .map(pair -> pair.first + "," + pair.second)
                .collect(Collectors.toList());
        Files.write(filePath, lines);
    }

    static Set<Integer> newSet(int... arr) {
        Set<Integer> set = new HashSet<>();
        for (int n : arr) {
            set.add(n);
        }
        return set;
    }
}
