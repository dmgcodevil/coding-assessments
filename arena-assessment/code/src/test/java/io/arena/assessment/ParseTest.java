package io.arena.assessment;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class ParseTest {

    @Test
    public void testParse() {
        List<String> lines = Arrays.asList(
                "a,b,c,d",
                "e,f,b",
                "d"

        );
        Map<String, Set<Integer>> words = Solution1.parse(lines);
        assertEquals(new HashSet<>(Collections.singletonList(0)), words.get("a"));
        assertEquals(new HashSet<>(Arrays.asList(0, 1)), words.get("b"));
        assertEquals(new HashSet<>(Collections.singletonList(0)), words.get("c"));
        assertEquals(new HashSet<>(Arrays.asList(0, 2)), words.get("d"));
        assertEquals(new HashSet<>(Collections.singletonList(1)), words.get("e"));
        assertEquals(new HashSet<>(Collections.singletonList(1)), words.get("f"));
    }

}
