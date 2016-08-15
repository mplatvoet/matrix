package nl.mplatvoet.collections.matrix;


import java.util.Iterator;
import java.util.Random;

public class MatrixExample {

    public static final MatrixFunction<String, String> PLUS_FACTORY = new MatrixFunction<String, String>() {
        @Override
        public String apply(int row, int column, String value) {
            return "+";
        }
    };

    public static void main(String[] args) throws Exception {
        Matrix<String> matrix = generateMatrix(10, 20);
        System.out.println("==Generated matrix==");
        printMatrix(matrix);
        System.out.println();

        System.out.println("==Sub matrix==");
        Matrix<String> subMatrix = matrix.map(0, 6, 0, 10);
        printMatrix(subMatrix);
        System.out.println();

        System.out.println("==Insert row==");
        subMatrix.insertRowBefore(3).fillBlanks(PLUS_FACTORY);
        printMatrix(subMatrix);
        System.out.println();

        System.out.println("==Delete row==");
        subMatrix.deleteRow(3);
        printMatrix(subMatrix);
        System.out.println();

        System.out.println("==Insert column==");
        subMatrix.insertColumnBefore(5).fillBlanks(PLUS_FACTORY);
        printMatrix(subMatrix);
        System.out.println();

        System.out.println("==Delete column==");
        subMatrix.deleteColumn(5);
        printMatrix(subMatrix);
        System.out.println();


        System.out.println("==Delete rows - iterator==");
        for (Iterator<Matrix.Row<String>> iter = subMatrix.rows().iterator(); iter.hasNext(); ) {
            Matrix.Row<String> row = iter.next();
            if (row.getRowIndex() > 3) iter.remove();
        }
        printMatrix(subMatrix);
        System.out.println();

        System.out.println("==Delete columns - iterator==");
        for (Iterator<Matrix.Column<String>> iter = subMatrix.columns().iterator(); iter.hasNext(); ) {
            Matrix.Column<String> column = iter.next();
            if (column.getColumnIndex() > 5) iter.remove();
        }
        printMatrix(subMatrix);
        System.out.println();

        System.out.println("==Fill function==");
        Matrix<Integer> numbers = generateMatrix(5, 5, new MatrixFunction<Integer, Integer>() {
            @Override
            public Integer apply(int row, int column, Integer value) {
                return ++row + column;
            }
        });
        printMatrix(numbers);
        System.out.println();

        System.out.println("==Map function==");
        Matrix<String> strings = numbers.map(new MatrixFunction<Integer, String>() {
            @Override
            public String apply(int row, int column, Integer value) {
                return "<" + value + ">";
            }
        });
        printMatrix(strings);
    }

    private static Matrix<String> generateMatrix(int rows, int columns) {
        Matrix<String> m = new IndexMatrix<>(rows, columns);
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

    private static <T> Matrix<T> generateMatrix(int rows, int columns, MatrixFunction<? super T, ? extends T> fn) {
        return new IndexMatrix<>(rows, columns, fn);
    }

    private static void printMatrix(Matrix<?> matrix) {
        for (Matrix.Row<?> row : matrix.rows()) {
            for (Object value : row) {
                System.out.print(value == null ? " " : value);
                System.out.print(" ");
            }
            System.out.println();
        }
    }
}
