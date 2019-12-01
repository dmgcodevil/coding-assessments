package io.arena.assessment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static io.arena.assessment.Utils.newSet;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class IntersectTest {
    private final Set<Integer> s1;
    private final Set<Integer> s2;
    private final int upperBound;
    private final int expected;


    public IntersectTest(Set<Integer> s1,
                         Set<Integer> s2,
                         int upperBound,
                         int expected) {
        this.s1 = s1;
        this.s2 = s2;
        this.upperBound = upperBound;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // edge cases
                {newSet(), newSet(2, 4, 5), 2, 0},
                {newSet(2, 4, 5), newSet(), 2, 0},
                // normal cases
                {newSet(2), newSet(3), 2, 0},
                {newSet(2), newSet(2), 2, 1},
                {newSet(1, 2, 3, 4), newSet(2, 4, 5), 2, 2},
                {newSet(1, 2, 3, 4), newSet(4, 5), 2, 1},
                {newSet(1, 2, 3), newSet(1, 2, 3), 2, 2},
                {newSet(1, 2, 3), newSet(1, 2, 3), 3, 3},
                {newSet(1, 2, 3, 4), newSet(3), 3, 1},

        });
    }

    @Test
    public void testIntersect() {
        assertEquals(expected, Solution1.intersect(s1, s2, upperBound));
    }


}
