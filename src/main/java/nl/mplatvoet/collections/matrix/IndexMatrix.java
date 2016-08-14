package nl.mplatvoet.collections.matrix;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IndexMatrix<T> implements Matrix<T> {
    private final IndexMap<IndexRow<T>> rows;
    private final IndexMap<Column<T>> columns;

    private RowsIterable<T> rowsIterable = null;
    private ColumnsIterable<T> columnsIterable = null;

    private int maxRowIndex = -1;
    private int maxColumnIndex = -1;

    public IndexMatrix() {
        this(0, 0);
    }

    public IndexMatrix(int initialRows, int initialColumns) {
        if (initialRows < 0) {
            throw new IllegalArgumentException("initialRows must be >= 0");
        }
        if (initialColumns < 0) {
            throw new IllegalArgumentException("initialColumns must be >= 0");
        }
        maxRowIndex = initialRows - 1;
        maxColumnIndex = initialColumns - 1;
        rows = new IndexMap<>(initialRows);
        columns = new IndexMap<>(initialColumns);
    }


    @Override
    public T put(int row, int column, T value) {
        if (row < 0) {
            throw new IllegalArgumentException("row must be >= 0");
        }
        if (column < 0) {
            throw new IllegalArgumentException("row must be >= 0");
        }

        IndexRow<T> r = rows.get(row);
        if (r == null) {
            r = new IndexRow<>(this, row);
            rows.put(row, r);
            if (maxRowIndex < row) {
                maxRowIndex = row;
            }
        }
        return r.put(column, value);
    }

    @Override
    public void fillBlanks(CellValueFactory<? extends T> factory) {
        for (int rowIndex = 0; rowIndex <= maxRowIndex; ++rowIndex) {
            IndexRow<T> row = getRow(rowIndex);
            row.fillBlanks(factory);
        }
    }

    @Override
    public void putAll(Matrix<? extends T> matrix) {
        putAll(matrix, 0, 0);
    }

    @SuppressWarnings("unchecked")
    @Override
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
        if (matrix instanceof IndexMatrix) {
            putAllIndexMatrix((IndexMatrix<T>) matrix, rowOffset, columnOffset);
        } else {
            putAllGenericMatrix(matrix, rowOffset, columnOffset);
        }

    }

    private void putAllGenericMatrix(Matrix<? extends T> matrix, int rowOffset, int columnOffset) {
        for (Row<? extends T> row : matrix.rows()) {
            for (Cell<? extends T> cell : row.cells()) {
                if (!cell.isBlank()) {
                    int rowIdx = cell.getRowIndex() + rowOffset;
                    int columnIdx = cell.getColumnIndex() + columnOffset;
                    put(rowIdx, columnIdx, cell.getValue());
                }
            }
        }
    }

    private void putAllIndexMatrix(IndexMatrix<? extends T> matrix, int rowOffset, int columnOffset) {
        for (IndexRow<? extends T> row : matrix.rows.values()) {
            for (IndexCell<? extends T> cell : row.cells.values()) {
                if (!cell.isBlank()) {
                    int rowIdx = cell.getRowIndex() + rowOffset;
                    int columnIdx = cell.getColumnIndex() + columnOffset;
                    put(rowIdx, columnIdx, cell.getValue());
                }
            }
        }
    }

    @Override
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
        IndexMatrix<T> m = new IndexMatrix<>();

        for (int rowIdx = rowBeginIdx; rowIdx < rowEndIdx; ++rowIdx) {
            IndexRow<T> row = rows.get(rowIdx);
            if (row != null) {
                for (int columnIdx = columnBeginIdx; columnIdx < columnEndIdx; ++columnIdx) {
                    IndexCell<T> cell = row.cells.get(columnIdx);
                    if (cell != null && !cell.isBlank()) {
                        m.put(rowIdx - rowBeginIdx, columnIdx - columnBeginIdx, cell.getValue());
                    }
                }
            }
        }

        return m;
    }

    @Override
    public Cell<T> getCell(int row, int column) {
        return getRow(row).getCell(column);
    }

    @Override
    public T get(int row, int column) {
        return getCell(row, column).getValue();
    }

    @Override
    public IndexRow<T> getRow(int row) {
        if (row < 0 || row > maxRowIndex) {
            throw new IndexOutOfBoundsException("Row must be >= 0 and <= " + maxRowIndex + ", but was: " + row);
        }
        IndexRow<T> r = rows.get(row);
        if (r == null) {
            r = new IndexRow<>(this, row);
            rows.put(row, r);
        }
        return r;
    }

    @Override
    public Column<T> getColumn(int column) {
        if (column < 0 || column > maxColumnIndex) {
            throw new IndexOutOfBoundsException("Column must be >= 0 and <= " + maxColumnIndex + ", but was: " + column);
        }
        Column<T> c = columns.get(column);
        if (c == null) {
            c = new IndexColumn<>(this, column);
            columns.put(column, c);
        }
        return c;
    }

    @Override
    public void clear() {
        for (IndexRow<T> row : rows.values()) {
            row.clear();
        }
    }

    @Override
    public Iterable<Row<T>> rows() {
        if (rowsIterable == null) {
            rowsIterable = new RowsIterable<>(this);
        }
        return rowsIterable;
    }

    @Override
    public Iterable<Column<T>> columns() {
        if (columnsIterable == null) {
            columnsIterable = new ColumnsIterable<>(this);
        }
        return columnsIterable;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof IndexMatrix)) {
            return false;
        }
        IndexMatrix other = (IndexMatrix) obj;
        if (maxRowIndex != other.maxRowIndex || maxColumnIndex != other.maxColumnIndex) {
            return false;
        }

        if (rows.size() != other.rows.size()) {
            return false;
        }

        for (int rowIdx = 0; rowIdx <= maxRowIndex; ++rowIdx) {
            IndexRow<T> row = rows.get(rowIdx);
            IndexRow otherRow = (IndexRow) other.rows.get(rowIdx);
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
            IndexRow<T> row = rows.get(rowIdx);
            if (row != null) {
                for (int columnIdx = 0; columnIdx <= maxColumnIndex; ++columnIdx) {
                    IndexCell<T> cell = row.cells.get(columnIdx);
                    if (cell != null && !cell.isBlank()) {
                        T value = cell.getValue();
                        h += value == null ? 0 : value.hashCode();
                    }
                }
            }
        }
        return h;
    }

    @Override
    public Matrix<T> shallowCopy() {
        return subMatrixInternal(0, maxRowIndex + 1, 0, maxColumnIndex + 1);
    }

    private boolean rowsEqual(IndexRow first, IndexRow second) {
        if (first == null && second == null) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }

        for (int columnIdx = 0; columnIdx <= maxColumnIndex; ++columnIdx) {
            IndexCell firstCell = (IndexCell) first.cells.get(columnIdx);
            IndexCell secondCell = (IndexCell) second.cells.get(columnIdx);
            if (!cellsEqual(firstCell, secondCell)) {
                return false;
            }
        }
        return true;
    }

    private boolean cellsEqual(IndexCell first, IndexCell second) {
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
        //
        return firstValue.equals(secondValue);
    }


    private static final class IndexColumn<T> implements Column<T> {
        private final int columnIndex;
        private final IndexMatrix<T> matrix;
        private ColumnCellsIterable<T> cellsIterable = null;


        private IndexColumn(IndexMatrix<T> matrix, int columnIndex) {
            this.columnIndex = columnIndex;
            this.matrix = matrix;
        }

        @Override
        public T get(int row) {
            if (row < 0 || row > matrix.maxRowIndex) {
                throw new IndexOutOfBoundsException("Row must be >= 0 and <= " + matrix.maxRowIndex + ", but was: " + row);
            }
            IndexRow<T> r = matrix.rows.get(row);
            return r != null ? r.get(columnIndex) : null;
        }

        @Override
        public Cell<T> getCell(int row) {
            if (row < 0 || row > matrix.maxRowIndex) {
                throw new IndexOutOfBoundsException("Row must be >= 0 and <= " + matrix.maxRowIndex + ", but was: " + row);
            }
            IndexRow<T> r = matrix.getRow(row);
            return r.getCell(columnIndex);
        }

        @Override
        public T put(int row, T value) {
            return matrix.put(row, columnIndex, value);
        }

        @Override
        public int getColumnIndex() {
            return columnIndex;
        }

        @Override
        public Matrix<T> getMatrix() {
            return matrix;
        }

        @Override
        public Iterator<T> iterator() {
            return new ColumnIterator<>(this);
        }

        @Override
        public Iterable<Cell<T>> cells() {
            if (cellsIterable == null) {
                cellsIterable = new ColumnCellsIterable<>(this);
            }
            return null;
        }

        @Override
        public void fillBlanks(CellValueFactory<? extends T> factory) {
            for (int rowIndex = 0; rowIndex <= matrix.maxRowIndex; ++rowIndex) {
                Cell<T> cell = matrix.getRow(rowIndex).getCell(columnIndex);
                if (cell.isBlank()) {
                    T value = factory.create(rowIndex, columnIndex);
                    cell.setValue(value);
                }
            }
        }

        @Override
        public void clear() {
            for (IndexRow<T> row : matrix.rows.values()) {
                IndexCell<T> cell = row.cells.get(columnIndex);
                if (cell != null) {
                    cell.clear();
                }
            }
        }
    }


    private static final class IndexRow<T> implements Row<T> {
        private final IndexMap<IndexCell<T>> cells;
        private final IndexMatrix<T> matrix;
        private final int rowIndex;

        private RowCellsIterable<T> cellsIterable = null;

        private IndexRow(IndexMatrix<T> matrix, int rowIndex) {
            this.matrix = matrix;
            this.rowIndex = rowIndex;
            //prevents excess array resizing
            cells = new IndexMap<>(matrix.maxRowIndex + 1);
        }

        @Override
        public Matrix<T> getMatrix() {
            return matrix;
        }

        @Override
        public int getRowIndex() {
            return rowIndex;
        }

        public T put(int column, T value) {
            T previous = null;
            IndexCell<T> cell = cells.get(column);
            if (cell == null) {
                cell = new IndexCell<>(this, column);
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

        @Override
        public Cell<T> getCell(int column) {
            if (column < 0 || column > matrix.maxColumnIndex) {
                throw new IndexOutOfBoundsException("Column must be >= 0 and <= " + matrix.maxColumnIndex + ", but was: " + column);
            }
            IndexCell<T> cell = cells.get(column);
            if (cell == null) {
                cell = new IndexCell<>(this, column);
                cells.put(column, cell);
            }
            return cell;
        }

        @Override
        public T get(int column) {
            if (column < 0 || column > matrix.maxColumnIndex) {
                throw new IndexOutOfBoundsException("Column must be >= 0 and <= " + matrix.maxColumnIndex + ", but was: " + column);
            }
            IndexCell<T> cell = cells.get(column);
            return cell != null ? cell.getValue() : null;
        }

        @Override
        public void clear() {
            for (IndexCell<T> cell : cells.values()) {
                cell.clear();
            }
        }

        @Override
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
        public Iterator<T> iterator() {
            return new RowIterator<>(this);
        }

        @Override
        public Iterable<Cell<T>> cells() {
            if (cellsIterable == null) {
                cellsIterable = new RowCellsIterable<>(this);
            }
            return cellsIterable;
        }
    }


    private static final class IndexCell<T> implements Cell<T> {
        private static final Object BLANK = new Object();

        private final IndexRow<T> row;
        private final int columnIndex;
        private T value;

        private IndexCell(IndexRow<T> row, int columnIndex, T value) {
            this.row = row;
            this.columnIndex = columnIndex;
            this.value = value;
        }

        private IndexCell(IndexRow<T> row, int column) {
            this(row, column, IndexCell.<T>blank());
        }

        @SuppressWarnings("unchecked")
        private static <T> T blank() {
            return (T) BLANK;
        }

        @Override
        public Matrix<T> getMatrix() {
            return row.getMatrix();
        }

        @Override
        public Row<T> getRow() {
            return row;
        }

        @Override
        public Column<T> getColumn() {
            return row.matrix.getColumn(columnIndex);
        }

        @Override
        public T getValue() {
            return value == BLANK ? null : value;
        }

        @Override
        public void setValue(T value) {
            this.value = value;
        }

        @Override
        public void clear() {
            this.value = blank();
        }

        @Override
        public int getColumnIndex() {
            return columnIndex;
        }

        @Override
        public int getRowIndex() {
            return row.getRowIndex();
        }

        @Override
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
        private final IndexMatrix<T> matrix;
        private int index = -1;

        private RowsIterator(IndexMatrix<T> matrix) {
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
            return matrix.getRow(index);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


    private static class RowIterator<T> implements Iterator<T> {
        private final IndexRow<T> row;
        private int index = -1;

        private RowIterator(IndexRow<T> row) {
            this.row = row;
        }

        @Override
        public boolean hasNext() {
            return index < row.matrix.maxColumnIndex;
        }

        @Override
        public T next() {
            if (++index > row.matrix.maxColumnIndex) {
                throw new NoSuchElementException();
            }
            return row.get(index);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


    private static class ColumnsIterator<T> implements Iterator<Column<T>> {
        private final IndexMatrix<T> matrix;
        private int index = -1;


        private ColumnsIterator(IndexMatrix<T> matrix) {
            this.matrix = matrix;
        }

        @Override
        public boolean hasNext() {
            return index < matrix.maxColumnIndex;
        }

        @Override
        public Column<T> next() {
            if (++index > matrix.maxColumnIndex) {
                throw new NoSuchElementException();
            }
            return matrix.getColumn(index);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class ColumnIterator<T> implements Iterator<T> {
        private final IndexColumn<T> column;
        private int index = -1;


        private ColumnIterator(IndexColumn<T> column) {
            this.column = column;
        }

        @Override
        public boolean hasNext() {
            return index < column.matrix.maxRowIndex;
        }

        @Override
        public T next() {
            if (++index > column.matrix.maxRowIndex) {
                throw new NoSuchElementException();
            }
            return column.get(index);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class RowCellIterator<T> implements Iterator<Cell<T>> {
        private final IndexRow<T> row;
        private int index = -1;


        private RowCellIterator(IndexRow<T> row) {
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

    private static class ColumnCellIterator<T> implements Iterator<Cell<T>> {
        private final IndexColumn<T> column;
        private int index = -1;


        private ColumnCellIterator(IndexColumn<T> column) {
            this.column = column;
        }

        @Override
        public boolean hasNext() {
            return index < column.matrix.maxRowIndex;
        }

        @Override
        public Cell<T> next() {
            if (++index > column.matrix.maxRowIndex) {
                throw new NoSuchElementException();
            }
            return column.getCell(index);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class ColumnCellsIterable<T> implements Iterable<Cell<T>> {
        private final IndexColumn<T> column;

        private ColumnCellsIterable(IndexColumn<T> column) {
            this.column = column;
        }

        @Override
        public Iterator<Cell<T>> iterator() {
            return new ColumnCellIterator<>(column);
        }
    }

    private static class RowCellsIterable<T> implements Iterable<Cell<T>> {
        private final IndexRow<T> row;

        private RowCellsIterable(IndexRow<T> row) {
            this.row = row;
        }

        @Override
        public Iterator<Cell<T>> iterator() {
            return new RowCellIterator<>(row);
        }
    }

    private static class ColumnsIterable<T> implements Iterable<Column<T>> {
        private final IndexMatrix<T> matrix;

        private ColumnsIterable(IndexMatrix<T> matrix) {
            this.matrix = matrix;
        }

        @Override
        public Iterator<Column<T>> iterator() {
            return new ColumnsIterator<>(matrix);
        }
    }

    private static class RowsIterable<T> implements Iterable<Row<T>> {
        private final IndexMatrix<T> matrix;

        private RowsIterable(IndexMatrix<T> matrix) {
            this.matrix = matrix;
        }

        @Override
        public Iterator<Row<T>> iterator() {
            return new RowsIterator<>(matrix);
        }
    }
}


