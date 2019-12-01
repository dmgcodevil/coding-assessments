import java.util.*;

public class BoundingBox {


    static int DEV = 0;
    static int PROD = 1;
    static int MODE = PROD;
    static boolean DEBUG = false;

    static int[][] MOVES = new int[][]{
            {0, -1}, // left
            {0,  1}, // right
            {-1, 0} // up
            {1,  0} // down
    };

    public static void main(String[] args) {
        if (MODE == PROD) prod();
        else dev();
    }

    static void prod() {
        Scanner scanner = new Scanner(System.in);
        List<String> lines = new ArrayList<>();
        while (true) {
            String line = scanner.nextLine();
            if (line.isEmpty()) break;
            lines.add(line);
        }

        int[][] grid = new int[lines.size()][lines.get(0).length()];

        for (int i = 0; i < lines.size(); i++) {
            for (int j = 0; j < lines.get(i).length(); j++) {
                grid[i][j] = lines.get(i).charAt(j) == '*' ? 1 : 0;
            }
        }

        for (Coordinates box : findBoxes(grid)) {
            System.out.println(box);
        }
    }

    static void dev() {
        runTests();
    }

    static List<Coordinates> findBoxes(int[][] grid) {

        List<Coordinates> allCoordinates = new ArrayList<>();
        List<Coordinates> answer = new ArrayList<>();

        // O(m*n)
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] == 1) {
                    Coordinates coordinates = extend(i, j, grid);
                    if (DEBUG) {
                        System.out.println(String.format("extend(%d, %d)", i, j));
                        System.out.println(coordinates);
                        print(grid);
                    }

                    allCoordinates.add(coordinates);
                }
            }
        }


        Set<Coordinates> overlapping = new HashSet<>();

        // O(N^2)
        for (int i = 0; i < allCoordinates.size(); i++) {
            for (int j = 0; j < allCoordinates.size(); j++) {
                if (i != j) {
                    if (allCoordinates.get(i).overlapping(allCoordinates.get(j))) {
                        overlapping.add(allCoordinates.get(j));
                    }
                }

            }
        }

        for (Coordinates coordinates : allCoordinates) {
            if (!overlapping.contains(coordinates)) {
                answer.add(coordinates);
            }
        }
        // TODO: Return the largest box, if there are more than one box of the same size, ruturn all
        return answer;
    }

    static void print(int[][] grid) {
        for (int[] row : grid) {
            System.out.println(Arrays.toString(row));
        }
    }

    // use dfs to find boundaries (left, right, down)
    static Coordinates extend(int i, int j, int[][] grid) {
        int leftMin = j;
        int rightMax = j;
        int bottomMax = i;
        Stack<int[]> stack = new Stack<>();
        stack.add(new int[]{i, j});
        grid[i][j] = 0;
        while (!stack.isEmpty()) {
            int[] node = stack.pop();
            leftMin = Math.min(leftMin, node[1]);
            rightMax = Math.max(rightMax, node[1]);
            bottomMax = Math.max(bottomMax, node[0]);

            for (int[] move : getMoves(node[0], node[1], grid)) {
                grid[move[0]][move[1]] = 0;
                stack.add(move);
            }
        }
        return Coordinates.of(i, leftMin, bottomMax, rightMax);
    }

    static List<int[]> getMoves(int i, int j, int[][] grid) {
        int m = grid.length;
        int n = grid[0].length;
        List<int[]> moves = new ArrayList<>();
        for (int[] move : MOVES) {
            int x = i + move[0];
            int y = j + move[1];
            if (x >= 0 && x < m && y >= 0 && y < n && grid[x][y] == 1) {
                moves.add(new int[]{x, y});
            }
        }
        return moves;
    }

    static void testOverlapping() {
        List<Pair<Coordinates, Coordinates>> cases = new ArrayList<>();
        /*
        BOTTOM-RIGHT overlaps with TOP-LEFT
         _____
        |     |
        |_____|___
        |___|_|   |
            |     |
            |_____|

         */
        cases.add(Pair.of(Coordinates.of(1, 1, 2, 2), Coordinates.of(2, 2, 3, 3)));
        cases.add(Pair.of(Coordinates.of(1, 1, 2, 2), Coordinates.of(1, 2, 3, 3)));
        cases.add(Pair.of(Coordinates.of(1, 1, 2, 2), Coordinates.of(2, 1, 3, 3)));
        cases.add(Pair.of(Coordinates.of(1, 1, 2, 2), Coordinates.of(0, 0, 3, 3)));
        cases.add(Pair.of(Coordinates.of(1, 1, 2, 2), Coordinates.of(0, 2, 3, 3)));
        cases.add(Pair.of(Coordinates.of(1, 1, 2, 2), Coordinates.of(1, 0, 3, 3)));
        cases.add(Pair.of(Coordinates.of(1, 1, 2, 2), Coordinates.of(1, 1, 3, 3)));
        cases.add(Pair.of(Coordinates.of(1, 1, 2, 2), Coordinates.of(2, 0, 3, 3)));


        /*
        TOP-RIGHT overlaps with BOTTOM-LEFT
   (1,1) _____
        |     |
(0,0) __|__   |
     |  |__|__|(3,3)
     |     |
     |_____|
         */

        cases.add(Pair.of(Coordinates.of(1, 1, 3, 3), Coordinates.of(0, 0, 1, 1)));
        cases.add(Pair.of(Coordinates.of(1, 1, 3, 3), Coordinates.of(1, 1, 3, 2)));
        cases.add(Pair.of(Coordinates.of(1, 1, 3, 3), Coordinates.of(1, 0, 3, 3)));
        cases.add(Pair.of(Coordinates.of(1, 1, 3, 3), Coordinates.of(3, 0, 4, 2)));
        cases.add(Pair.of(Coordinates.of(1, 1, 3, 3), Coordinates.of(0, 1, 1, 3)));
        cases.add(Pair.of(Coordinates.of(1, 1, 3, 3), Coordinates.of(0, 1, 1, 3)));
        cases.add(Pair.of(Coordinates.of(1, 1, 3, 3), Coordinates.of(1, 3, 3, 4)));
        cases.add(Pair.of(Coordinates.of(1, 1, 3, 3), Coordinates.of(3, 1, 4, 3)));
        cases.add(Pair.of(Coordinates.of(1, 1, 3, 3), Coordinates.of(1, 0, 3, 1)));
        cases.add(Pair.of(Coordinates.of(2, 0, 3, 2), Coordinates.of(2, 2, 3, 3)));

        // inside
        cases.add(Pair.of(Coordinates.of(0, 0, 3, 3), Coordinates.of(0, 0, 1, 1)));
        cases.add(Pair.of(Coordinates.of(0, 0, 3, 3), Coordinates.of(1, 1, 2, 2)));
        cases.add(Pair.of(Coordinates.of(0, 0, 3, 3), Coordinates.of(2, 2, 3, 3)));
        cases.add(Pair.of(Coordinates.of(0, 0, 3, 3), Coordinates.of(2, 1, 3, 2)));
        cases.add(Pair.of(Coordinates.of(0, 0, 3, 3), Coordinates.of(0, 1, 2, 2)));


        for (Pair<Coordinates, Coordinates> pair : cases) {
            testOverlapping(pair.first, pair.second);
        }

    }

    static void testOverlapping(Coordinates c1, Coordinates c2) {
        if (!c1.overlapping(c2) && !c2.overlapping(c1)) {
            throw new RuntimeException(String.format("%s should overlap with %s", c1, c2));
        }
    }

    static void testNonOverlapping(Coordinates c1, Coordinates c2) {
        if (c1.overlapping(c2) || c2.overlapping(c1)) {
            throw new RuntimeException(String.format("%s should not overlap with %s", c1, c2));
        }
    }

    static void testNonOverlapping() {
        List<Pair<Coordinates, Coordinates>> cases = new ArrayList<>();
        cases.add(Pair.of(Coordinates.of(2, 0, 3, 1), Coordinates.of(1, 2, 3, 3)));
        cases.add(Pair.of(Coordinates.of(0, 0, 1, 1), Coordinates.of(1, 2, 2, 3)));
        cases.add(Pair.of(Coordinates.of(0, 2, 0, 3), Coordinates.of(1, 2, 2, 3)));
        cases.add(Pair.of(Coordinates.of(0, 0, 1, 1), Coordinates.of(2, 2, 3, 3)));
        cases.add(Pair.of(Coordinates.of(2, 0, 3, 1), Coordinates.of(2, 2, 3, 3)));
        cases.add(Pair.of(Coordinates.of(2, 0, 3, 1), Coordinates.of(2, 2, 3, 3)));
        for (Pair<Coordinates, Coordinates> pair : cases) {
            testNonOverlapping(pair.first, pair.second);
        }
    }

    static void runTests() {
        testOverlapping();
        testNonOverlapping();
        testFindBoxes();
    }

    static void testFindBoxes() {
        List<Pair<int[][], Set<Coordinates>>> cases = new ArrayList<>();

        cases.add(
                Pair.of(new int[][]{
                        {0, 0, 0, 0},
                        {0, 1, 1, 0},
                        {0, 1, 1, 0},
                        {0, 0, 0, 0}
                }, asSet(Coordinates.of(1, 1, 2, 2)))
        );

        cases.add(
                Pair.of(new int[][]{
                        {1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1},
                        {0, 1, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0},
                        {0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1},
                        {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0},
                }, asSet(Coordinates.of(0, 0, 1, 1)))
        );

        for (Pair<int[][], Set<Coordinates>> pair : cases) {
            Set<Coordinates> actual = new HashSet<>(findBoxes(pair.first));
            if (!Objects.equals(pair.second, actual)) {
                throw new RuntimeException(String.format("test failed\ninput:\n%s   expected: %s; actual: %s",
                        asString(pair.first), pair.second, actual));
            }
        }

    }

    static String asString(int[][] grid) {
        StringBuilder builder = new StringBuilder();
        for (int[] row : grid) {
            builder.append(Arrays.toString(row)).append("\n");
        }
        return builder.toString();
    }

    static <T> Set<T> asSet(T... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    static class Coordinates {
        final Pair<Integer, Integer> topLeft;
        final Pair<Integer, Integer> bottomRight;


        Coordinates(Pair<Integer, Integer> topLeft, Pair<Integer, Integer> bottomRight) {
            this.topLeft = topLeft;
            this.bottomRight = bottomRight;
        }

        static Coordinates of(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY) {
            return new Coordinates(Pair.of(topLeftX, topLeftY), Pair.of(bottomRightX, bottomRightY));
        }

        @Override
        public String toString() {
            return String.format("(%d,%d),(%d,%d)",
                    topLeft.first + 1, topLeft.second + 1,
                    bottomRight.first + 1, bottomRight.second + 1);
        }

        boolean overlapping(Coordinates that) {

            // above / below
            if (bottomRight.first < that.topLeft.first  // this rectangle is above that
                    || that.bottomRight.first < topLeft.first //  this rectangle is below that
            ) {
                return false;
            }

            // right / left
            if (topLeft.second > that.bottomRight.second // this rectangle is on the right side of that
                    || bottomRight.second < that.topLeft.second // this rectangle is on the left side of that
            ) {
                return false;
            }

            return true;

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Coordinates that = (Coordinates) o;
            return topLeft.equals(that.topLeft) &&
                    bottomRight.equals(that.bottomRight);
        }

        @Override
        public int hashCode() {
            return Objects.hash(topLeft, bottomRight);
        }
    }

    static class Pair<T1, T2> {
        final T1 first;
        final T2 second;

        public Pair(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }

        static <T1, T2> Pair<T1, T2> of(T1 first, T2 second) {
            return new Pair<>(first, second);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(first, pair.first) &&
                    Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }


}
