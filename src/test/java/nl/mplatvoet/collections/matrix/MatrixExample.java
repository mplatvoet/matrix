package nl.mplatvoet.collections.matrix;

public class MatrixExample {
    public static void main(String[] args) {
        Matrix<Integer> m = new Matrix<>();
        printMatrix(m);

        m.put(1, 1, 1);
        m.put(2, 2, 1);
        m.put(3, 3, 1);
        m.put(4, 5, 1);
        m.put(5, 7, 1);
        m.put(6, 11, 1);
        m.put(7, 13, 1);
        m.put(8, 17, 1);
        printMatrix(m);
        printMatrixCells(m);

        m.fillBlanks(new CellValueFactory<Integer>() {
            @Override
            public Integer create(int row, int column) {
                return 0;
            }
        });
        printMatrix(m);
    }

    private static void printMatrix(Matrix<?> matrix) {
        for (Matrix.Row<?> row : matrix) {
            for (Matrix.Cell<?> cell : row) {
                System.out.print(cell.isBlank() ? " " : cell.getValue().toString());
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    private static void printMatrixCells(Matrix<?> matrix) {
        for (Matrix.Row<?> row : matrix) {
            for (Matrix.Cell<?> cell : row) {
                System.out.print(cell);
                System.out.print(" ");
            }
            System.out.println();
        }
    }
}
