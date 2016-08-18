package nl.mplatvoet.collections.matrix;

import nl.mplatvoet.collections.matrix.fn.Function;

import java.util.Random;

public class ExampleUtil {
    public static MutableMatrix<String> generateMatrix(int rows, int columns) {
        MutableMatrix<String> m = new IndexMatrix<>(rows, columns);
        Random random = new Random();
        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < columns; ++column) {
                if (random.nextInt(3) == 0) {
                    m.put(row, column, "*");
                }
            }
        }
        return m;
    }

    public static <T> MutableMatrix<T> generateMatrix(int rows, int columns, Function<? super T, ? extends T> fn) {
        return new IndexMatrix<>(rows, columns, fn);
    }

    public static void printMatrix(Matrix<?> matrix) {
        for (Row<?> row : matrix.rows()) {
            for (Object value : row) {
                System.out.print(value == null ? " " : value);
                System.out.print(" ");
            }
            System.out.println();
        }
    }
}
