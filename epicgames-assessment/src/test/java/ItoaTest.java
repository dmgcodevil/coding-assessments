import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ItoaTest {

    private final int number;
    private final int base;
    private final String expected;

    public ItoaTest(int number, int base, String expected) {
        this.number = number;
        this.base = base;
        this.expected = expected;
    }


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {0, 10, "0"},
                {123, 10, "123"},
                {-123, 10, "-123"},
                {123, 16, "7b"},
                {-123, 16, "-7b"},
                {123, 8, "173"},
                {-123, 8, "-173"},
                {123, 2, "1111011"},
                {-123, 2, "-1111011"},
        });
    }

    @Test
    public void test() {
        assertEquals(Integer.toString(number, base), expected); // validate expected
        assertEquals(expected, Itoa.itoa(number, base));
    }
}
