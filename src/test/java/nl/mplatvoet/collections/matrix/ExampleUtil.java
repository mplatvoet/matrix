package nl.mplatvoet.collections.matrix;


import java.util.Random;

class ExampleUtil {
    private static final  Random RANDOM = new Random();
    static Matrix<String> randomMatrix(int rows, int columns) {
        return Matrices.of(rows, columns, cell -> {
            if (RANDOM.nextInt(3) == 0) {
                cell.setValue("*");
            }
            return null;
        });
    }

    static void printMatrix(Matrix<?> matrix) {
        for (Row<?> row : matrix.rows()) {
            for (Object value : row) {
                System.out.print(value == null ? " " : value);
                System.out.print(" ");
            }
            System.out.println();
        }
    }
}
