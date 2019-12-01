package io.arena.assessment;

import org.junit.Test;

import java.util.*;

import static io.arena.assessment.Utils.newSet;
import static org.junit.Assert.assertEquals;

public class FindMatchedPairsTest {

    @Test
    public void findMatchedPairs() {
        int leastNumOfLines = 2;

        // a,b,c,d
        // e,b,c
        // a,d
        Map<String, Set<Integer>> words = new HashMap<>();
        words.put("a", newSet(0, 2));
        words.put("b", newSet(0, 1));
        words.put("c", newSet(0, 1));
        words.put("d", newSet(0, 2));
        words.put("e", newSet(1));

        List<Pair<String>> actual = Solution1.findMatchedPairs(words, leastNumOfLines);

        assertEquals(Arrays.asList(new Pair<>("a", "d"), new Pair<>("b", "c")), actual);
    }
}
