package nl.mplatvoet.collections.matrix;

import java.util.Random;

public class MatrixExample {
    public static void main(String[] args) throws Exception {
        Matrix<String> matrix = generateMatrix(10, 20);
        System.out.println("==Generated matrix==");
        printMatrix(matrix);
        System.out.println();

        System.out.println("==Sub matrix==");
        Matrix<String> subMatrix = matrix.subMatrix(2, 8, 5, 15);
        printMatrix(subMatrix);
        System.out.println();

        System.out.println("==Copy matrix==");
        Matrix<String> copy = matrix.shallowCopy();
        System.out.println("original == copy:      " + (matrix == copy));
        System.out.println("original.equals(copy): " + (matrix.equals(copy)));
        System.out.println("orginal hashCode: " + matrix.hashCode());
        System.out.println("copy hashCode:    " + copy.hashCode());
    }

    private static Matrix<String> generateMatrix(int rows, int columns) {
        Matrix<String> m = new Matrix<>();
        Random random = new Random();
        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < columns; ++column) {
                if(random.nextInt(3) == 0){
                    m.put(row, column, "*");
                }
            }
        }
        return m;
    }

    private static void printMatrix(Matrix<?> matrix) {
        for (Matrix.Row<?> row : matrix) {
            for (Matrix.Cell<?> cell : row) {
                System.out.print(cell.isBlank() ? " " : cell.getValue());
                System.out.print(" ");
            }
            System.out.println();
        }
    }
}
