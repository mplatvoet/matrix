package nl.mplatvoet.collections.matrix;

import nl.mplatvoet.collections.matrix.fn.Function;
import nl.mplatvoet.collections.matrix.fn.Functions;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IndexMatrix<T> implements MutableMatrix<T> {
    private final IndexMap<IndexRow<T>> rows;
    private final IndexMap<IndexColumn<T>> columns;

    private RowsIterable<T> rowsIterable = null;
    private ColumnsIterable<T> columnsIterable = null;
    private MutableRowsIterable<T> mutableRowsIterable = null;
    private MutableColumnsIterable<T> mutableColumnsIterable = null;

    private int maxRowIndex = -1;
    private int maxColumnIndex = -1;

    public IndexMatrix() {
        this(0, 0, null);
    }

    public IndexMatrix(int initialRows, int initialColumns) {
        this(initialRows, initialColumns, null);
    }

    public IndexMatrix(int initialRows, int initialColumns, Function<? super T, ? extends T> function) {
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

        if (function != null) {
            fill(function);
        }
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
    public void fillBlanks(Function<? super T, ? extends T> function) {
        if (function == null) {
            throw new IllegalArgumentException("function cannot be null");
        }
        for (int rowIndex = 0; rowIndex <= maxRowIndex; ++rowIndex) {
            IndexRow<T> row = getRow(rowIndex);
            row.fillBlanks(function);
        }
    }

    @Override
    public void fill(Function<? super T, ? extends T> function) {
        if (function == null) {
            throw new IllegalArgumentException("function cannot be null");
        }
        for (int rowIndex = 0; rowIndex <= maxRowIndex; ++rowIndex) {
            IndexRow<T> row = getRow(rowIndex);
            row.fill(function);
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
    public MutableRow<T> insertRowBefore(int row) {
        if (row < 0) {
            throw new IllegalArgumentException("row must be >= 0");
        }
        if (row <= maxRowIndex) {
            for (int idx = maxRowIndex; idx >= row; --idx) {
                moveRow(idx, idx + 1);
            }
        }
        maxRowIndex = Math.max(maxRowIndex, row);
        return getRow(row);
    }

    @Override
    public MutableRow<T> insertRowAfter(int row) {
        return insertRowBefore(++row);
    }

    @Override
    public MutableRow<T> insertRowBefore(Row<T> row) {
        validateRow(row);
        return insertRowBefore(row.getRowIndex());
    }

    @Override
    public MutableRow<T> insertRowAfter(Row<T> row) {
        validateRow(row);
        return insertRowAfter(row.getRowIndex());
    }

    @Override
    public MutableColumn<T> insertColumnBefore(int column) {
        if (column < 0) {
            throw new IllegalArgumentException("column must be >= 0");
        }
        if (column <= maxColumnIndex) {
            for (int idx = maxColumnIndex; idx >= column; --idx) {
                moveColumn(idx, idx + 1);
            }
        }
        maxColumnIndex = Math.max(maxColumnIndex, column);
        return getColumn(column);
    }

    @Override
    public MutableColumn<T> insertColumnAfter(int column) {
        return insertColumnBefore(++column);
    }

    @Override
    public MutableColumn<T> insertColumnBefore(Column<T> column) {
        validateColumn(column);
        return insertColumnBefore(column.getColumnIndex());
    }

    @Override
    public MutableColumn<T> insertColumnAfter(Column<T> column) {
        validateColumn(column);
        return insertColumnAfter(column.getColumnIndex());
    }

    @Override
    public void deleteRow(int row) {
        if (row < 0 || row > maxRowIndex) {
            throw new IndexOutOfBoundsException("row must be >= 0 and <= " + maxRowIndex + ", but was: " + row);
        }
        evictRow(row);
        for (int idx = row + 1; idx <= maxRowIndex; ++idx) {
            moveRow(idx, idx - 1);
        }
        --maxRowIndex;
    }

    @Override
    public void deleteRow(Row<T> row) {
        validateRow(row);
        deleteRow(row.getRowIndex());
    }

    private void validateRow(Row<T> row) {
        if (row == null) {
            throw new IllegalArgumentException("row cannot be null");
        }
        if (row.getMatrix() != this) {
            throw new IllegalStateException("row does not belong to this matrix");
        }
    }

    @Override
    public void deleteColumn(int column) {
        if (column < 0 || column > maxColumnIndex) {
            throw new IndexOutOfBoundsException("column must be >= 0 and <= " + maxColumnIndex + ", but was: " + column);
        }
        evictColumn(column);
        for (int idx = column + 1; idx <= maxColumnIndex; ++idx) {
            moveColumn(idx, idx - 1);
        }
        --maxColumnIndex;
    }

    @Override
    public void deleteColumn(Column<T> column) {
        validateColumn(column);
        deleteColumn(column.getColumnIndex());
    }

    private void validateColumn(Column<T> column) {
        if (column == null) {
            throw new IllegalArgumentException("column cannot be null");
        }
        if (column.getMatrix() != this) {
            throw new IllegalStateException("column does not belong to this matrix");
        }
    }

    //expects fromIdx to be within bounds
    private void moveRow(int fromIdx, int toIdx) {
        maxRowIndex = Math.max(maxRowIndex, toIdx);
        evictRow(toIdx);

        IndexRow<T> row = rows.get(fromIdx);
        if (row != null) {
            updateRowIndices(row, toIdx);
            rows.put(toIdx, row);
        }

        rows.remove(fromIdx);
    }

    //expects fromIdx to be within bounds
    private void moveColumn(int fromIdx, int toIdx) {
        maxColumnIndex = Math.max(maxColumnIndex, toIdx);
        evictColumn(toIdx);

        IndexColumn<T> column = columns.get(fromIdx);
        if (column != null) {
            column.columnIndex = toIdx;
            columns.put(toIdx, column);
            columns.remove(fromIdx);
        }
        for (IndexRow<T> row : rows.values()) {
            IndexCell<T> cell = row.cells.get(fromIdx);
            if (cell != null) {
                cell.columnIndex = toIdx;
                row.cells.put(toIdx, cell);
                row.cells.remove(fromIdx);
            }
        }

    }

    //unchecked method, everything must be within bounds
    private void updateRowIndices(IndexRow<T> row, int newIdx) {
        row.rowIndex = newIdx;
        for (IndexCell<T> cell : row.cells.values()) {
            cell.rowIndex = newIdx;
        }
    }

    //unchecked method, everything must be within bounds
    private void updateColumnIndices(IndexColumn<T> column, int newIdx) {
        for (int rowIndex = 0; rowIndex <= maxRowIndex; ++rowIndex) {

            IndexRow<T> row = rows.get(rowIndex);
            if (row != null) {
                IndexCell<T> cell = row.cells.get(column.columnIndex);
                if (cell != null) {
                    cell.columnIndex = newIdx;
                }
            }
        }
        column.columnIndex = newIdx;
    }


    private void evictRow(int row) {
        IndexRow<T> r = rows.get(row);
        if (r != null) {
            r.delete();
            rows.remove(row);
        }
    }

    private void evictColumn(int column) {
        IndexColumn<T> c = columns.get(column);
        if (c != null) {
            c.delete();
            columns.remove(column);
        }

    }

    @Override
    public MutableCell<T> getCell(int row, int column) {
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
    public MutableColumn<T> getColumn(int column) {
        if (column < 0 || column > maxColumnIndex) {
            throw new IndexOutOfBoundsException("Column must be >= 0 and <= " + maxColumnIndex + ", but was: " + column);
        }
        IndexColumn<T> c = columns.get(column);
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
    public Iterable<MutableRow<T>> mutableRows() {
        if (mutableRowsIterable == null) {
            mutableRowsIterable = new MutableRowsIterable<>(this);
        }
        return mutableRowsIterable;
    }

    @Override
    public Iterable<MutableColumn<T>> mutableColumns() {
        if (mutableColumnsIterable == null) {
            mutableColumnsIterable = new MutableColumnsIterable<>(this);
        }
        return mutableColumnsIterable;
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
    public int getRowSize() {
        return maxRowIndex + 1;
    }

    @Override
    public int getColumnSize() {
        return maxColumnIndex + 1;
    }

    @Override
    public MutableMatrix<T> map() {
        IndexMatrix<T> matrix = new IndexMatrix<>(getRowSize(), getColumnSize());
        map(matrix, 0, getRowSize(), 0, getColumnSize(), Functions.<T>passTrough());
        return matrix;
    }

    @Override
    public <R> MutableMatrix<R> map(Function<? super T, ? extends R> function) {
        if (function == null) {
            throw new IllegalArgumentException("function cannot be null");
        }

        IndexMatrix<R> matrix = new IndexMatrix<>(getRowSize(), getColumnSize());
        map(matrix, 0, getRowSize(), 0, getColumnSize(), function);
        return matrix;
    }

    @Override
    public MutableMatrix<T> map(int rowBeginIdx, int rowEndIdx,
                                int columnBeginIdx, int columnEndIdx) {
        validateIndices(rowBeginIdx, rowEndIdx, columnBeginIdx, columnEndIdx);

        IndexMatrix<T> result = new IndexMatrix<>(rowEndIdx - rowBeginIdx, columnEndIdx - columnBeginIdx);

        map(result, rowBeginIdx, rowEndIdx, columnBeginIdx, columnEndIdx, Functions.<T>passTrough());
        return result;
    }

    @Override
    public <R> MutableMatrix<R> map(int rowBeginIdx, int rowEndIdx,
                                    int columnBeginIdx, int columnEndIdx,
                                    Function<? super T, ? extends R> function) {
        validateIndices(rowBeginIdx, rowEndIdx, columnBeginIdx, columnEndIdx);

        if (function == null) {
            throw new IllegalArgumentException("function cannot be null");
        }

        IndexMatrix<R> result = new IndexMatrix<>(rowEndIdx - rowBeginIdx, columnEndIdx - columnBeginIdx);

        map(result, rowBeginIdx, rowEndIdx, columnBeginIdx, columnEndIdx, function);
        return result;
    }

    private void validateIndices(int rowBeginIdx, int rowEndIdx, int columnBeginIdx, int columnEndIdx) {
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
    }


    private <R> void map(IndexMatrix<R> m,
                         int rowBeginIdx, int rowEndIdx,
                         int columnBeginIdx, int columnEndIdx,
                         Function<? super T, ? extends R> function) {
        for (int rowIdx = rowBeginIdx; rowIdx < rowEndIdx; ++rowIdx) {
            IndexRow<T> row = rows.get(rowIdx);
            if (row != null) {
                for (int columnIdx = columnBeginIdx; columnIdx < columnEndIdx; ++columnIdx) {
                    IndexCell<T> cell = row.cells.get(columnIdx);
                    if (cell != null && !cell.isBlank()) {
                        R value = function.apply(rowIdx, columnIdx, cell.getValue());
                        m.put(rowIdx - rowBeginIdx, columnIdx - columnBeginIdx, value);
                    }
                }
            }
        }
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


    private static final class IndexColumn<T> implements MutableColumn<T> {
        private int columnIndex;
        private final IndexMatrix<T> matrix;
        private ColumnCellsIterable<T> cellsIterable = null;
        private MutableColumnCellsIterable<T> mutableCellsIterable = null;

        private boolean deleted = false;


        private IndexColumn(IndexMatrix<T> matrix, int columnIndex) {
            this.columnIndex = columnIndex;
            this.matrix = matrix;
        }

        @Override
        public T get(int row) {
            assertState();
            if (row < 0 || row > matrix.maxRowIndex) {
                throw new IndexOutOfBoundsException("Row must be >= 0 and <= " + matrix.maxRowIndex + ", but was: " + row);
            }
            IndexRow<T> r = matrix.rows.get(row);
            return r != null ? r.get(columnIndex) : null;
        }

        @Override
        public MutableCell<T> getCell(int row) {
            assertState();
            if (row < 0 || row > matrix.maxRowIndex) {
                throw new IndexOutOfBoundsException("Row must be >= 0 and <= " + matrix.maxRowIndex + ", but was: " + row);
            }
            IndexRow<T> r = matrix.getRow(row);
            return r.getCell(columnIndex);
        }

        @Override
        public T put(int row, T value) {
            assertState();
            return matrix.put(row, columnIndex, value);
        }

        @Override
        public int getColumnIndex() {
            assertState();
            return columnIndex;
        }

        @Override
        public MutableMatrix<T> getMatrix() {
            assertState();
            return matrix;
        }

        @Override
        public Iterator<T> iterator() {
            assertState();
            return new ColumnIterator<>(this);
        }

        @Override
        public Iterable<Cell<T>> cells() {
            assertState();
            if (cellsIterable == null) {
                cellsIterable = new ColumnCellsIterable<>(this);
            }
            return cellsIterable;
        }

        @Override
        public Iterable<MutableCell<T>> mutableCells() {
            assertState();
            if (mutableCellsIterable == null) {
                mutableCellsIterable = new MutableColumnCellsIterable<>(this);
            }
            return mutableCellsIterable;
        }

        @Override
        public void fillBlanks(Function<? super T, ? extends T> function) {
            assertState();
            for (int rowIndex = 0; rowIndex <= matrix.maxRowIndex; ++rowIndex) {
                MutableCell<T> cell = matrix.getRow(rowIndex).getCell(columnIndex);
                if (cell.isBlank()) {
                    T value = function.apply(rowIndex, columnIndex, null);
                    cell.setValue(value);
                }
            }
        }

        @Override
        public void fill(Function<? super T, ? extends T> function) {
            assertState();
            for (int rowIndex = 0; rowIndex <= matrix.maxRowIndex; ++rowIndex) {
                MutableCell<T> cell = matrix.getRow(rowIndex).getCell(columnIndex);
                T value = function.apply(rowIndex, columnIndex, null);
                cell.setValue(value);
            }
        }

        @Override
        public void clear() {
            assertState();
            for (IndexRow<T> row : matrix.rows.values()) {
                IndexCell<T> cell = row.cells.get(columnIndex);
                if (cell != null) {
                    cell.clear();
                }
            }
        }

        public void delete() {
            if (deleted) return;

            for (IndexRow<T> row : matrix.rows.values()) {
                IndexCell<T> cell = row.cells.get(columnIndex);
                if (cell != null) {
                    cell.delete();
                    row.cells.remove(columnIndex);
                }
            }

            deleted = true;
        }

        private void assertState() {
            if (deleted) {
                throw new IllegalStateException("row has been deleted");
            }
        }
    }


    private static final class IndexRow<T> implements MutableRow<T> {
        private final IndexMap<IndexCell<T>> cells;
        private final IndexMatrix<T> matrix;
        private int rowIndex;

        private boolean deleted = false;

        private RowCellsIterable<T> cellsIterable = null;
        private MutableRowCellsIterable<T> mutableCellsIterable = null;

        private IndexRow(IndexMatrix<T> matrix, int rowIndex) {
            this.matrix = matrix;
            this.rowIndex = rowIndex;
            //prevents excess array resizing
            cells = new IndexMap<>(matrix.maxRowIndex + 1);
        }

        @Override
        public MutableMatrix<T> getMatrix() {
            assertState();
            return matrix;
        }

        @Override
        public int getRowIndex() {
            assertState();
            return rowIndex;
        }

        public T put(int column, T value) {
            assertState();
            T previous = null;
            IndexCell<T> cell = cells.get(column);
            if (cell == null) {
                cell = new IndexCell<>(matrix, rowIndex, column);
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
        public MutableCell<T> getCell(int column) {
            assertState();
            if (column < 0 || column > matrix.maxColumnIndex) {
                throw new IndexOutOfBoundsException("Column must be >= 0 and <= " + matrix.maxColumnIndex + ", but was: " + column);
            }
            IndexCell<T> cell = cells.get(column);
            if (cell == null) {
                cell = new IndexCell<>(matrix, rowIndex, column);
                cells.put(column, cell);
            }
            return cell;
        }

        @Override
        public T get(int column) {
            assertState();
            if (column < 0 || column > matrix.maxColumnIndex) {
                throw new IndexOutOfBoundsException("Column must be >= 0 and <= " + matrix.maxColumnIndex + ", but was: " + column);
            }
            IndexCell<T> cell = cells.get(column);
            return cell != null ? cell.getValue() : null;
        }

        @Override
        public void clear() {
            assertState();
            for (IndexCell<T> cell : cells.values()) {
                cell.clear();
            }
        }

        @Override
        public void fill(Function<? super T, ? extends T> function) {
            assertState();
            for (int columnIndex = 0; columnIndex <= matrix.maxColumnIndex; ++columnIndex) {
                MutableCell<T> cell = getCell(columnIndex);
                T value = function.apply(rowIndex, columnIndex, null);
                cell.setValue(value);
            }
        }

        @Override
        public void fillBlanks(Function<? super T, ? extends T> function) {
            assertState();
            for (int columnIndex = 0; columnIndex <= matrix.maxColumnIndex; ++columnIndex) {
                MutableCell<T> cell = getCell(columnIndex);
                if (cell.isBlank()) {
                    T value = function.apply(rowIndex, columnIndex, null);
                    cell.setValue(value);
                }
            }
        }

        @Override
        public Iterator<T> iterator() {
            assertState();
            return new RowIterator<>(this);
        }

        @Override
        public Iterable<Cell<T>> cells() {
            assertState();
            if (cellsIterable == null) {
                cellsIterable = new RowCellsIterable<>(this);
            }
            return cellsIterable;
        }

        @Override
        public Iterable<MutableCell<T>> mutableCells() {
            if (mutableCellsIterable == null) {
                mutableCellsIterable = new MutableRowCellsIterable<>(this);
            }
            return mutableCellsIterable;
        }

        public void delete() {
            if (deleted) return;


            for (IndexCell<T> cell : cells.values()) {
                cell.delete();
            }

            cells.clear();
            deleted = true;
        }

        private void assertState() {
            if (deleted) {
                throw new IllegalStateException("row has been deleted");
            }
        }
    }


    private static final class IndexCell<T> implements MutableCell<T> {
        private static final Object BLANK = new Object();

        private IndexMatrix<T> matrix;
        private int rowIndex;
        private int columnIndex;
        private T value;

        private boolean deleted = false;

        private IndexCell(IndexMatrix<T> matrix, int rowIndex, int columnIndex, T value) {
            this.matrix = matrix;
            this.rowIndex = rowIndex;
            this.columnIndex = columnIndex;
            this.value = value;
        }

        private IndexCell(IndexMatrix<T> matrix, int rowIndex, int columnIndex) {
            this(matrix, rowIndex, columnIndex, IndexCell.<T>blank());
        }

        @SuppressWarnings("unchecked")
        private static <T> T blank() {
            return (T) BLANK;
        }

        @Override
        public MutableMatrix<T> getMatrix() {
            assertState();
            return matrix;
        }

        @Override
        public MutableRow<T> getRow() {
            assertState();
            return matrix.getRow(rowIndex);
        }

        @Override
        public MutableColumn<T> getColumn() {
            assertState();
            return matrix.getColumn(columnIndex);
        }

        @Override
        public T getValue() {
            assertState();
            return value == BLANK ? null : value;
        }

        @Override
        public void setValue(T value) {
            assertState();
            this.value = value;
        }

        @Override
        public void clear() {
            assertState();
            this.value = blank();
        }

        @Override
        public int getColumnIndex() {
            assertState();
            return columnIndex;
        }

        @Override
        public int getRowIndex() {
            assertState();
            return rowIndex;
        }

        @Override
        public boolean isBlank() {
            assertState();
            return value == BLANK;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("(Cell ");
            if (deleted) {
                sb.append("--deleted--");
            } else {
                sb.append(rowIndex);
                sb.append(":");
                sb.append(columnIndex);
                if (!isBlank()) {
                    sb.append(" [");
                    sb.append(getValue());
                    sb.append("]");
                }
            }
            sb.append(")");
            return sb.toString();
        }

        public void delete() {
            if (deleted) return;

            //prevent leaking
            value = null;
            matrix = null;

            deleted = true;
        }

        private void assertState() {
            if (deleted) {
                throw new IllegalStateException("row has been deleted");
            }
        }
    }


    private static abstract class AbstractRowsIterator<T, R extends Row<T>> implements Iterator<R> {
        final IndexMatrix<T> matrix;
        private int index = -1;
        private boolean deleted = false;

        private AbstractRowsIterator(IndexMatrix<T> matrix) {
            this.matrix = matrix;
        }

        protected abstract R rowForIndex(int idx);

        @Override
        public boolean hasNext() {
            return index < matrix.maxRowIndex;
        }

        @Override
        public R next() {
            if (++index > matrix.maxRowIndex) {
                throw new NoSuchElementException();
            }
            deleted = false;
            return rowForIndex(index);
        }

        @Override
        public void remove() {
            if (index < 0 || deleted) {
                throw new IllegalStateException();
            }
            matrix.deleteRow(index);
            deleted = true;
            --index;
        }
    }

    private static class MutableRowsIterator<T> extends AbstractRowsIterator<T, MutableRow<T>> {
        private MutableRowsIterator(IndexMatrix<T> matrix) {
            super(matrix);
        }

        @Override
        protected MutableRow<T> rowForIndex(int idx) {
            return matrix.getRow(idx);
        }
    }

    private static class RowsIterator<T> extends AbstractRowsIterator<T, Row<T>> {
        private RowsIterator(IndexMatrix<T> matrix) {
            super(matrix);
        }

        @Override
        protected Row<T> rowForIndex(int idx) {
            return matrix.getRow(idx);
        }
    }


    private static abstract class AbstractColumnsIterator<T, C extends Column<T>> implements Iterator<C> {
        final IndexMatrix<T> matrix;
        private int index = -1;
        private boolean deleted = false;


        private AbstractColumnsIterator(IndexMatrix<T> matrix) {
            this.matrix = matrix;
        }

        protected abstract C columnForIndex(int idx);

        @Override
        public boolean hasNext() {
            return index < matrix.maxColumnIndex;
        }

        @Override
        public C next() {
            if (++index > matrix.maxColumnIndex) {
                throw new NoSuchElementException();
            }
            deleted = false;
            return columnForIndex(index);
        }

        @Override
        public void remove() {
            if (index < 0 || deleted) {
                throw new IllegalStateException();
            }
            matrix.deleteColumn(index);
            deleted = true;
            --index;
        }
    }

    private static class ColumnsIterator<T> extends AbstractColumnsIterator<T, Column<T>> {
        private ColumnsIterator(IndexMatrix<T> matrix) {
            super(matrix);
        }

        @Override
        protected Column<T> columnForIndex(int idx) {
            return matrix.getColumn(idx);
        }
    }

    private static class MutableColumnsIterator<T> extends AbstractColumnsIterator<T, MutableColumn<T>> {
        private MutableColumnsIterator(IndexMatrix<T> matrix) {
            super(matrix);
        }

        @Override
        protected MutableColumn<T> columnForIndex(int idx) {
            return matrix.getColumn(idx);
        }
    }

    private static class RowIterator<T> implements Iterator<T> {
        private final IndexRow<T> row;
        private int index = -1;
        private boolean deleted = false;

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
            deleted = false;
            return row.get(index);
        }

        @Override
        public void remove() {
            if (index < 0 || deleted) {
                throw new IllegalStateException();
            }
            deleted = true;
            IndexCell<T> cell = row.cells.get(index);
            if (cell != null) {
                cell.clear();
            }
        }
    }

    private static class ColumnIterator<T> implements Iterator<T> {
        private final IndexColumn<T> column;
        private int index = -1;
        private boolean deleted = false;


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
            if (index < 0 || deleted) {
                throw new IllegalStateException();
            }
            deleted = true;
            IndexRow<T> row = column.matrix.rows.get(index);
            if (row != null) {
                IndexCell<T> cell = row.cells.get(column.columnIndex);
                if (cell != null) {
                    cell.clear();
                }
            }
        }
    }

    private static class RowCellIterator<T> implements Iterator<Cell<T>> {
        private final IndexRow<T> row;
        private int index = -1;
        private boolean deleted = false;


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
            deleted = false;
            return row.getCell(index);
        }

        @Override
        public void remove() {
            if (index < 0 || deleted) {
                throw new IllegalStateException();
            }
            deleted = true;
            row.getCell(index).clear();
        }
    }

    private static class MutableRowCellIterator<T> implements Iterator<MutableCell<T>> {
        private final IndexRow<T> row;
        private int index = -1;
        private boolean deleted = false;


        private MutableRowCellIterator(IndexRow<T> row) {
            this.row = row;
        }

        @Override
        public boolean hasNext() {
            return index < row.matrix.maxColumnIndex;
        }

        @Override
        public MutableCell<T> next() {
            if (++index > row.matrix.maxColumnIndex) {
                throw new NoSuchElementException();
            }
            deleted = false;
            return row.getCell(index);
        }

        @Override
        public void remove() {
            if (index < 0 || deleted) {
                throw new IllegalStateException();
            }
            deleted = true;
            row.getCell(index).clear();
        }
    }

    private static class ColumnCellIterator<T> implements Iterator<Cell<T>> {
        private final IndexColumn<T> column;
        private int index = -1;
        private boolean deleted = false;


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
            deleted = false;
            return column.getCell(index);
        }

        @Override
        public void remove() {
            if (index < 0 || deleted) {
                throw new IllegalStateException();
            }
            deleted = true;
            column.getCell(index).clear();
        }
    }

    private static class MutableColumnCellIterator<T> implements Iterator<MutableCell<T>> {
        private final IndexColumn<T> column;
        private int index = -1;
        private boolean deleted = false;


        private MutableColumnCellIterator(IndexColumn<T> column) {
            this.column = column;
        }

        @Override
        public boolean hasNext() {
            return index < column.matrix.maxRowIndex;
        }

        @Override
        public MutableCell<T> next() {
            if (++index > column.matrix.maxRowIndex) {
                throw new NoSuchElementException();
            }
            deleted = false;
            return column.getCell(index);
        }

        @Override
        public void remove() {
            if (index < 0 || deleted) {
                throw new IllegalStateException();
            }
            deleted = true;
            column.getCell(index).clear();
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

    private static class MutableColumnCellsIterable<T> implements Iterable<MutableCell<T>> {
        private final IndexColumn<T> column;

        private MutableColumnCellsIterable(IndexColumn<T> column) {
            this.column = column;
        }

        @Override
        public Iterator<MutableCell<T>> iterator() {
            return new MutableColumnCellIterator<>(column);
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

    private static class MutableRowCellsIterable<T> implements Iterable<MutableCell<T>> {
        private final IndexRow<T> row;

        private MutableRowCellsIterable(IndexRow<T> row) {
            this.row = row;
        }

        @Override
        public Iterator<MutableCell<T>> iterator() {
            return new MutableRowCellIterator<>(row);
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

    private static class MutableColumnsIterable<T> implements Iterable<MutableColumn<T>> {
        private final IndexMatrix<T> matrix;

        private MutableColumnsIterable(IndexMatrix<T> matrix) {
            this.matrix = matrix;
        }

        @Override
        public Iterator<MutableColumn<T>> iterator() {
            return new MutableColumnsIterator<>(matrix);
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

    private static class MutableRowsIterable<T> implements Iterable<MutableRow<T>> {
        private final IndexMatrix<T> matrix;

        private MutableRowsIterable(IndexMatrix<T> matrix) {
            this.matrix = matrix;
        }

        @Override
        public Iterator<MutableRow<T>> iterator() {
            return new MutableRowsIterator<>(matrix);
        }
    }
}


