package nl.mplatvoet.collections.matrix;

public class Matrix<T> {
    private int maxRowIndex = 0;
    private int maxColumnIndex = 0;


    public static final class Row<T> {
        private final Matrix<T> matrix;
        private final int rowIndex;

        private Row(Matrix<T> matrix, int rowIndex) {
            this.matrix = matrix;
            this.rowIndex = rowIndex;
        }

        public Matrix<T> getMatrix() {
            return matrix;
        }

        public int getRowIndex() {
            return rowIndex;
        }
    }


    public static final class Cell<T> {
        private final Row<T> row;
        private final int columnIndex;
        private final T value;

        private Cell(Row<T> row, int columnIndex, T value) {
            this.row = row;
            this.columnIndex = columnIndex;
            this.value = value;
        }

        public Matrix<T> getMatrix() {
            return row.getMatrix();
        }

        public Row<T> getRow() {
            return row;
        }

        public T getValue() {
            return value;
        }

        public int getColumnIndex() {
            return columnIndex;
        }

        public int getRowIndex() {
            return row.getRowIndex();
        }
    }
}


