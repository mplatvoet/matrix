package nl.mplatvoet.collections.matrix;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Matrix<T> implements Iterable<Matrix.Row<T>> {
    private final IndexMap<Row<T>> rows = new IndexMap<>();

    private int maxRowIndex = -1;
    private int maxColumnIndex = -1;


    public void put(int row, int column, T value) {
        if (row < 0) {
            throw new IllegalArgumentException("row must be >= 0");
        }
        if (column < 0) {
            throw new IllegalArgumentException("column must be >= 0");
        }

        Row<T> r = rows.get(row);
        if (r == null) {
            r = new Row<>(this, row);
            rows.put(row, r);
            if (maxRowIndex < row) {
                maxRowIndex = row;
            }
        }
        r.put(column, value);
    }

    public void fillEmptyCells(CellValueFactory<? extends T> factory) {
        for (int rowIndex = 0; rowIndex <= maxRowIndex; ++rowIndex) {
            Row<T> row = rows.get(rowIndex);
            if (row == null) {
                row = new Row<>(this, rowIndex);
                rows.put(rowIndex, row);
            }
            row.fillEmptyCells(factory);
        }
    }

    public Cell get(int row, int column) {
        if (row < 0 || row > maxRowIndex) {
            throw new IndexOutOfBoundsException("Row must be >= 0 and <= " + maxRowIndex + ", but was: " + row);
        }
        Row<T> r = rows.get(row);
        return r == null ? null : r.get(column);
    }

    @Override
    public Iterator<Row<T>> iterator() {
        return new RowsIterator<>(this);
    }


    public static final class Row<T> implements Iterable<Cell<T>> {
        private final IndexMap<Cell<T>> cells = new IndexMap<>();
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

        private void put(int column, T value) {
            cells.put(column, new Cell<>(this, column, value));
            if (matrix.maxColumnIndex < column) {
                matrix.maxColumnIndex = column;
            }
        }

        public Cell<T> get(int column) {
            if (column < 0 || column > matrix.maxColumnIndex) {
                throw new IndexOutOfBoundsException("Column must be >= 0 and <= " + matrix.maxColumnIndex + ", but was: " + column);
            }
            return cells.get(column);
        }

        public void fillEmptyCells(CellValueFactory<? extends T> factory) {
            for (int columnIndex = 0; columnIndex <= matrix.maxColumnIndex; ++columnIndex) {
                Cell<T> cell = cells.get(columnIndex);
                if (cell == null) {
                    T value = factory.create(rowIndex, columnIndex);
                    cells.put(columnIndex, new Cell<>(this, columnIndex, value));
                }
            }
        }

        @Override
        public Iterator<Cell<T>> iterator() {
            return new CellsIterator<>(this);
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

        public boolean isEmpty() {
            return value == null;
        }
    }

    private static class RowsIterator<T> implements Iterator<Row<T>> {
        private final Matrix<T> matrix;
        private int index = -1;

        private RowsIterator(Matrix<T> matrix) {
            this.matrix = matrix;
        }

        @Override
        public boolean hasNext() {
            return index < matrix.maxRowIndex;
        }

        @Override
        public Row<T> next() {
            if (++index > matrix.maxRowIndex) {
                throw new NoSuchElementException();
            }
            Row<T> row = matrix.rows.get(index);
            return row == null ? new Row<>(matrix, index) : row;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class CellsIterator<T> implements Iterator<Cell<T>> {
        private final Row<T> row;
        private int index = -1;

        private CellsIterator(Row<T> row) {
            this.row = row;
        }

        @Override
        public boolean hasNext() {
            return index < row.matrix.maxColumnIndex;
        }

        @Override
        public Cell<T> next() {
            if (++index > row.matrix.maxColumnIndex) {
                throw new NoSuchElementException();
            }
            Cell<T> cell = row.get(index);
            return cell == null ? new Cell<>(row, index, null) : cell;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}


