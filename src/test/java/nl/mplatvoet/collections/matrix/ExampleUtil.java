package nl.mplatvoet.collections.matrix;

import nl.mplatvoet.collections.matrix.fn.Function;

import java.util.Random;

public class ExampleUtil {
    public static Matrix<String> randomMatrix(int rows, int columns) {
        return Matrices.of(rows, columns, new Function<Object, String>() {
            private final  Random random = new Random();
            @Override
            public String apply(int row, int column, Object value) {
                return random.nextInt(3) == 0 ? "*" : null;
            }
        });
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
