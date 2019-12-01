import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class SpiralMatrixTest {

    private final int[][] input;
    private final List<Integer> expected;

    public SpiralMatrixTest(int[][] input, List<Integer> expected) {
        this.input = input;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new int[][]{}, Collections.emptyList()},
                {new int[][]{{1}}, Collections.singletonList(1)},
                {new int[][]{
                        {2, 3, 4, 8},
                        {5, 7, 9, 12},
                        {1, 0, 6, 10}

                }, Arrays.asList(2, 3, 4, 8, 12, 10, 6, 0, 1, 5, 7, 9)},
                {new int[][]{
                        {2, 3, 4},
                        {5, 7, 9},
                        {1, 0, 6}

                }, Arrays.asList(2, 3, 4, 9, 6, 0, 1, 5, 7)},
                {new int[][]{{2, 3, 4, 8},}, Arrays.asList(2, 3, 4, 8)},
                {new int[][]{
                        {2},
                        {5},
                        {1}

                }, Arrays.asList(2, 5, 1)},
        });
    }


    @Test
    public void test() {
        assertEquals(expected, SpiralMatrix.matrix(input));
    }

}
