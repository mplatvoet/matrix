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

    public void putAll(Matrix<? extends T> matrix) {
        putAll(matrix, 0, 0);
    }

    public void putAll(Matrix<? extends T> matrix, int rowOffset, int columnOffset) {
        if (matrix == null) {
            throw new IllegalArgumentException("matrix can not be null");
        }
        if (rowOffset < 0) {
            throw new IllegalArgumentException("rowOffset must be >= 0, but was: " + rowOffset);
        }
        if (columnOffset < 0) {
            throw new IllegalArgumentException("columnOffset must be >= 0, but was: " + columnOffset);
        }
        for (Row<? extends T> row : matrix.rows.values()) {
            for (Cell<? extends T> cell : row.cells.values()) {
                if (!cell.isBlank()) {
                    int rowIdx = cell.getRowIndex() + rowOffset;
                    int columnIdx = cell.getColumnIndex() + columnOffset;
                    put(rowIdx, columnIdx, cell.getValue());
                }
            }
        }
    }

    public Matrix<T> subMatrix(int rowBeginIdx, int rowEndIdx, int columnBeginIdx, int columnEndIdx) {
        if (rowBeginIdx < 0 || rowBeginIdx > maxRowIndex) {
            throw new IndexOutOfBoundsException("rowBeginIdx must be >= 0 and <= " + maxRowIndex + ", but was: " + rowBeginIdx);
        }
        if (rowEndIdx < 0 || rowEndIdx - 1 > maxRowIndex) {
            throw new IndexOutOfBoundsException("rowBeginIdx must be >= 0 and <= " + (maxRowIndex + 1) + ", but was: " + rowEndIdx);
        }
        if (columnBeginIdx < 0 || columnBeginIdx > maxColumnIndex) {
            throw new IndexOutOfBoundsException("columnBeginIdx must be >= 0 and <= " + maxColumnIndex + ", but was: " + columnBeginIdx);
        }
        if (columnEndIdx < 0 || columnEndIdx - 1 > maxColumnIndex) {
            throw new IndexOutOfBoundsException("columnEndIdx must be >= 0 and <= " + (maxColumnIndex + 1) + ", but was: " + columnEndIdx);
        }
        if (rowBeginIdx > rowEndIdx) {
            throw new IndexOutOfBoundsException("rowBeginIdx[" + rowBeginIdx + "] cannot be larger as rowEndIdx[" + rowEndIdx + "]");
        }
        if (columnBeginIdx > columnEndIdx) {
            throw new IndexOutOfBoundsException("columnBeginIdx[" + columnBeginIdx + "] cannot be larger as columnEndIdx[" + columnEndIdx + "]");
        }
        return subMatrixInternal(rowBeginIdx, rowEndIdx, columnBeginIdx, columnEndIdx);
    }

    private Matrix<T> subMatrixInternal(int rowBeginIdx, int rowEndIdx, int columnBeginIdx, int columnEndIdx) {
        Matrix<T> m = new Matrix<>();

        for (int rowIdx = rowBeginIdx; rowIdx < rowEndIdx; ++rowIdx) {
            Row<T> row = rows.get(rowIdx);
            if (row != null) {
                for (int columnIdx = columnBeginIdx; columnIdx < columnEndIdx; ++columnIdx) {
                    Cell<T> cell = row.cells.get(columnIdx);
                    if (cell != null && !cell.isBlank()) {
                        m.put(rowIdx - rowBeginIdx, columnIdx - columnBeginIdx, cell.getValue());
                    }
                }
            }
        }

        return m;
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

    public void clear() {
        for (Row<T> row : rows.values()) {
            row.clear();
        }
    }

    @Override
    public Iterator<Row<T>> iterator() {
        return new RowsIterator<>(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Matrix)) {
            return false;
        }
        Matrix other = (Matrix) obj;
        if (maxRowIndex != other.maxRowIndex || maxColumnIndex != other.maxColumnIndex) {
            return false;
        }

        if (rows.size() != other.rows.size()) {
            return false;
        }

        for (int rowIdx = 0; rowIdx <= maxRowIndex; ++rowIdx) {
            Row<T> row = rows.get(rowIdx);
            Row otherRow = (Row) other.rows.get(rowIdx);
            if (!rowsEqual(row, otherRow)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = ((Integer) maxRowIndex).hashCode() ^ ((Integer) maxColumnIndex).hashCode();
        for (int rowIdx = 0; rowIdx <= maxRowIndex; ++rowIdx) {
            Row<T> row = rows.get(rowIdx);
            if (row != null) {
                for (int columnIdx = 0; columnIdx <= maxColumnIndex; ++columnIdx) {
                    Cell<T> cell = row.cells.get(columnIdx);
                    if (cell != null && !cell.isBlank()) {
                        T value = cell.getValue();
                        h += value == null ? 0 : value.hashCode();
                    }
                }
            }
        }
        return h;
    }

    public Matrix<T> shallowCopy() {
        return subMatrixInternal(0, maxRowIndex + 1, 0, maxColumnIndex + 1);
    }

    private boolean rowsEqual(Row first, Row second) {
        if (first == null && second == null) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }

        for (int columnIdx = 0; columnIdx <= maxColumnIndex; ++columnIdx) {
            Cell firstCell = (Cell) first.cells.get(columnIdx);
            Cell secondCell = (Cell) second.cells.get(columnIdx);
            if (!cellsEqual(firstCell, secondCell)) {
                return false;
            }
        }
        return true;
    }

    private boolean cellsEqual(Cell first, Cell second) {
        if ((first == null || first.isBlank()) && (second == null || second.isBlank())) {
            return true;
        }
        if ((first == null || first.isBlank()) || (second == null || second.isBlank())) {
            return false;
        }

        Object firstValue = first.getValue();
        Object secondValue = second.getValue();
        if (firstValue == null && secondValue == null) {
            return true;
        }
        if (firstValue == null || secondValue == null) {
            return false;
        }
        return firstValue.equals(secondValue);
    }


    public static final class Column<T> implements Iterable<Cell<T>> {
        private final int columnIndex;
        private final Matrix<T> matrix;


        private Column(Matrix<T> matrix, int columnIndex) {
            this.columnIndex = columnIndex;
            this.matrix = matrix;
        }

        public Cell<T> getCell(int row) {
            if (row < 0 || row > matrix.maxRowIndex) {
                throw new IndexOutOfBoundsException("Row must be >= 0 and <= " + matrix.maxRowIndex + ", but was: " + row);
            }
            Row<T> r = matrix.getRow(row);
            return r.getCell(columnIndex);
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

        @Override
        public Iterator<Cell<T>> iterator() {
            return new ColumnIterator<>(this);
        }

        public void clear() {
            for (Row<T> row : matrix.rows.values()) {
                Cell<T> cell = row.cells.get(columnIndex);
                if (cell != null) {
                    cell.clear();
                }
            }
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

        public void clear() {
            for (Cell<T> cell : cells.values()) {
                cell.clear();
            }
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
            return new RowIterator<>(this);
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
            sb.append("(Cell ");
            sb.append(row.getRowIndex());
            sb.append(":");
            sb.append(columnIndex);
            if (!isBlank()) {
                sb.append(" [");
                sb.append(getValue());
                sb.append("]");
            }
            sb.append(")");
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


    private static class RowIterator<T> implements Iterator<Cell<T>> {
        private final Row<T> row;
        private int index = -1;

        private RowIterator(Row<T> row) {
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

    private static class ColumnIterator<T> implements Iterator<Cell<T>> {
        private final Column<T> column;
        private int index = -1;


        private ColumnIterator(Column<T> column) {
            this.column = column;
        }

        @Override
        public boolean hasNext() {
            return index < column.matrix.maxColumnIndex;
        }

        @Override
        public Cell<T> next() {
            if (++index > column.matrix.maxColumnIndex) {
                throw new NoSuchElementException();
            }
            return column.getCell(index);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}


