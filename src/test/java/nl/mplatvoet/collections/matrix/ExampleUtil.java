package nl.mplatvoet.collections.matrix;

import nl.mplatvoet.collections.matrix.fn.CellFunction;

import java.util.Random;

public class ExampleUtil {
    public static Matrix<String> randomMatrix(int rows, int columns) {
        return Matrices.of(rows, columns, new CellFunction<String, MutableCell<String>>() {
            private final  Random random = new Random();
            @Override
            public void apply(MutableCell<String> cell) {
                if (random.nextInt(3) == 0) {
                    cell.setValue("*");
                }
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
