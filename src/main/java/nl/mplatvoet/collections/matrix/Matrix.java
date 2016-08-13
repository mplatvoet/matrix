package nl.mplatvoet.collections.matrix;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Matrix<T> implements Iterable<Matrix.Row<T>> {
    private final IndexMap<Row<T>> rows = new IndexMap<>();
    private final IndexMap<Column<T>> columns = new IndexMap<>();

    private int maxRowIndex = -1;
    private int maxColumnIndex = -1;


    public T put(int row, int column, T value) {
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
        return r.put(column, value);
    }

    public void fillBlanks(CellValueFactory<? extends T> factory) {
        for (int rowIndex = 0; rowIndex <= maxRowIndex; ++rowIndex) {
            Row<T> row = getRow(rowIndex);
            row.fillBlanks(factory);
        }
    }

    public Cell<T> getCell(int row, int column) {
        return getRow(row).getCell(column);
    }

    public T get(int row, int column) {
        return getCell(row, column).getValue();
    }

    public Row<T> getRow(int row) {
        if (row < 0 || row > maxRowIndex) {
            throw new IndexOutOfBoundsException("Row must be >= 0 and <= " + maxRowIndex + ", but was: " + row);
        }
        Row<T> r = rows.get(row);
        if (r == null) {
            r = new Row<>(this, row);
            rows.put(row, r);
        }
        return r;
    }

    public Column<T> getColumn(int column) {
        if (column < 0 || column > maxColumnIndex) {
            throw new IndexOutOfBoundsException("Column must be >= 0 and <= " + maxColumnIndex + ", but was: " + column);
        }
        Column<T> c = columns.get(column);
        if (c == null) {
            c = new Column<>(this, column);
            columns.put(column, c);
        }
        return c;
    }

    @Override
    public Iterator<Row<T>> iterator() {
        return new RowsIterator<>(this);
    }

    public static final class Column<T> {
        private final int columnIndex;
        private final Matrix<T> matrix;


        private Column(Matrix<T> matrix, int columnIndex) {
            this.columnIndex = columnIndex;
            this.matrix = matrix;
        }

        public Cell<T> get(int row) {
            if (row < 0 || row > matrix.maxRowIndex) {
                throw new IndexOutOfBoundsException("Row must be >= 0 and <= " + matrix.maxRowIndex + ", but was: " + row);
            }
            Row<T> r = matrix.rows.get(row);
            return r == null ? null : r.getCell(columnIndex);
        }

        public T put(int row, T value) {
            return matrix.put(row, columnIndex, value);
        }

        public int getColumnIndex() {
            return columnIndex;
        }

        public Matrix<T> getMatrix() {
            return matrix;
        }
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

        private T put(int column, T value) {
            T previous = null;
            Cell<T> cell = cells.get(column);
            if (cell == null) {
                cell = new Cell<T>(this, column);
                cells.put(column, cell);
            } else {
                previous = cell.getValue();
            }
            cell.setValue(value);

            if (matrix.maxColumnIndex < column) {
                matrix.maxColumnIndex = column;
            }
            return previous;
        }

        public Cell<T> getCell(int column) {
            if (column < 0 || column > matrix.maxColumnIndex) {
                throw new IndexOutOfBoundsException("Column must be >= 0 and <= " + matrix.maxColumnIndex + ", but was: " + column);
            }
            Cell<T> cell = cells.get(column);
            if (cell == null) {
                cell = new Cell<>(this, column);
                cells.put(column, cell);
            }
            return cell;
        }

        public void fillBlanks(CellValueFactory<? extends T> factory) {
            for (int columnIndex = 0; columnIndex <= matrix.maxColumnIndex; ++columnIndex) {
                Cell<T> cell = getCell(columnIndex);
                if (cell.isBlank()) {
                    T value = factory.create(rowIndex, columnIndex);
                    cell.setValue(value);
                }
            }
        }

        @Override
        public Iterator<Cell<T>> iterator() {
            return new CellsIterator<>(this);
        }
    }


    public static final class Cell<T> {
        private static final Object BLANK = new Object();

        private final Row<T> row;
        private final int columnIndex;
        private T value;

        private Cell(Row<T> row, int columnIndex, T value) {
            this.row = row;
            this.columnIndex = columnIndex;
            this.value = value;
        }

        private Cell(Row row, int column) {
            this(row, column, Cell.<T>blank());
        }

        @SuppressWarnings("unchecked")
        private static <T> T blank() {
            return (T) BLANK;
        }

        public Matrix<T> getMatrix() {
            return row.getMatrix();
        }

        public Row<T> getRow() {
            return row;
        }

        public Column<T> getColumn() {
            return row.matrix.getColumn(columnIndex);
        }

        public T getValue() {
            return value == BLANK ? null : value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public void clear() {
            this.value = blank();
        }

        public int getColumnIndex() {
            return columnIndex;
        }

        public int getRowIndex() {
            return row.getRowIndex();
        }

        public boolean isBlank() {
            return value == BLANK;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[Cell ");
            sb.append(row.getRowIndex());
            sb.append(":");
            sb.append(columnIndex);
            sb.append(" ");
            if (isBlank()) {
                sb.append("<empty>");
            } else {
                sb.append("[");
                sb.append(getValue());
                sb.append("]");
            }
            sb.append("]");
            return sb.toString();
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
            return row.getCell(index);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}


