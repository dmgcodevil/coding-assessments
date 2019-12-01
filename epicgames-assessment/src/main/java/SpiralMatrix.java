import java.util.ArrayList;
import java.util.List;

public class SpiralMatrix {

    private SpiralMatrix() { }

    public static List<Integer> matrix(int[][] matrix) {
        if (matrix.length == 0) return new ArrayList<>();
        int m = matrix.length;

        int n = matrix[0].length;

        List<Integer> ans = new ArrayList<>(m * n);

        int colStart = 0;
        int colEnd = n - 1;
        int rowStart = 0;
        int rowEnd = m - 1;

        while (colStart <= colEnd && rowStart <= rowEnd) {
            // print top
            for (int j = colStart; j <= colEnd; j++) ans.add(matrix[rowStart][j]);
            // print right
            for (int i = rowStart + 1; i <= rowEnd; i++) ans.add(matrix[i][colEnd]);
            // check if there are four sides to this layer
            if (colStart < colEnd && rowStart < rowEnd) {
                // print bottom
                for (int j = colEnd - 1; j > colStart; j--) ans.add(matrix[rowEnd][j]);
                // print left
                for (int i = rowEnd; i > rowStart; i--) ans.add(matrix[i][colStart]);
            }

            rowStart++;
            rowEnd--;
            colStart++;
            colEnd--;
        }

        return ans;

    }
}
