package nl.mplatvoet.collections.matrix;

import nl.mplatvoet.collections.matrix.args.Arguments;
import nl.mplatvoet.collections.matrix.fn.Function;
import nl.mplatvoet.collections.matrix.fn.Functions;
import nl.mplatvoet.collections.matrix.range.Range;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static nl.mplatvoet.collections.matrix.args.Arguments.*;

public class IndexMatrix<T> implements MutableMatrix<T> {
    private final IndexMap<IndexRow<T>> rows;
    private final IndexMap<IndexColumn<T>> columns;

    private RowsIterable<T> rowsIterable = null;
    private ColumnsIterable<T> columnsIterable = null;
    private MutableRowsIterable<T> mutableRowsIterable = null;
    private MutableColumnsIterable<T> mutableColumnsIterable = null;

    private int maxRowIndex = -1;
    private int maxColumnIndex = -1;

    @SuppressWarnings("unchecked")
    private IndexMatrix(int initialRows, int initialColumns, Function<?, ? extends T> function) {
        checkArgument(initialRows < 0, "initialRows must be >= 0, but was %s", initialRows);
        checkArgument(initialColumns < 0, "initialColumns must be >= 0, but was %s", initialColumns);

        maxRowIndex = initialRows - 1;
        maxColumnIndex = initialColumns - 1;

        rows = new IndexMap<>(initialRows);
        columns = new IndexMap<>(initialColumns);

        if (function != null) {
            //Casting is safe because initial value is always null
            fill((Function<? super T, ? extends T>) function);
        }
    }

    private <S> IndexMatrix(Matrix<? extends S> matrix, Range range, Function<? super S, ? extends T> transform) {
        Arguments.checkArgument(matrix == null, "matrix cannot be null");
        Arguments.checkArgument(range == null, "range cannot be null");
        Arguments.checkArgument(transform == null, "transform cannot be null");
        Arguments.checkArgument(!range.fits(matrix), "%s does not fit in provided %s", range, matrix);

        int rowSize = range.getRowSize();
        int columnSize = range.getColumnSize();
        maxRowIndex = -1;
        maxColumnIndex = -1;

        rows = new IndexMap<>(rowSize);
        columns = new IndexMap<>(columnSize);
        fillCells(matrix, range, transform);
    }

    public static <T> MutableMatrix<T> of() {
        return of(0, 0);
    }

    public static <T> MutableMatrix<T> of(int rows, int columns) {
        return new IndexMatrix<>(rows, columns, null);
    }

    public static <T> MutableMatrix<T> of(int rows, int columns, Function<?, ? extends T> fill) {
        Arguments.checkArgument(fill == null, "fill function cannot be null");
        return new IndexMatrix<>(rows, columns, fill);
    }


    @SuppressWarnings("unchecked")
    public static <T> MutableMatrix<T> copyOf(Matrix<? extends T> matrix) {
        return copyOf(matrix, Range.of(matrix), Functions.<T>passTrough());
    }

    public static <T> MutableMatrix<T> copyOf(Matrix<? extends T> matrix, Range range) {
        if (range != null && range.matches(matrix)) {
            return copyOf(matrix);
        }
        return copyOf(matrix, range, Functions.<T>passTrough());
    }

    public static <T, R> MutableMatrix<R> copyOf(Matrix<? extends T> matrix, Function<? super T, ? extends R> transform) {
        return copyOf(matrix, Range.of(matrix), transform);
    }

    public static <T, R> MutableMatrix<R> copyOf(Matrix<? extends T> matrix, Range range, Function<? super T, ? extends R> transform) {
        //TODO check if range is empty and return a special empty matrix

        return new IndexMatrix<R>(matrix, range, transform);
    }

    private <S> void fillCells(Matrix<S> source, Range range, Function<? super S, ? extends T> transform) {
        final int rOffset = range.getRowBeginIndex();
        final int cOffset = range.getColumnBeginIndex();
        //don't use iterator, I want to match the previous set size
        for (int r = 0; r < range.getRowSize(); ++r) {
            Row<S> row = source.getRow(r + rOffset);
            for (int c = 0; c < range.getColumnSize(); ++c) {
                Cell<S> cell = row.getCell(c + cOffset);
                if (!cell.isBlank()) {
                    T value = transform.apply(r, c, cell.getValue());
                    put(r, c, value);
                }
            }
        }
    }

    @Override
    public T put(int row, int column, T value) {
        checkIndex(row < 0, "row must be >= 0, but was %s", row);
        checkIndex(column < 0, "column must be >= 0, but was %s", column);

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
        checkArgument(function == null, "function cannot be null");

        for (int rowIndex = 0; rowIndex <= maxRowIndex; ++rowIndex) {
            IndexRow<T> row = getRow(rowIndex);
            row.fillBlanks(function);
        }
    }

    @Override
    public void fill(Function<? super T, ? extends T> function) {
        checkArgument(function == null, "function cannot be null");

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
        checkArgument(matrix == null, "matrix cannot be null");
        checkIndex(rowOffset < 0, "rowOffset must be >= 0, but was %s", rowOffset);
        checkIndex(columnOffset < 0, "columnOffset must be >= 0, but was %s", columnOffset);

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
    public MutableRow<T> insertRow(int row) {
        checkIndex(row < 0, "row must be >= 0, but was %s", row);

        if (row <= maxRowIndex) {
            for (int idx = maxRowIndex; idx >= row; --idx) {
                moveRow(idx, idx + 1);
            }
        }
        maxRowIndex = Math.max(maxRowIndex, row);
        return getRow(row);
    }


    @Override
    public MutableRow<T> insertRow(Row<T> row) {
        validateRow(row);
        return insertRow(row.getRowIndex());
    }

    @Override
    public MutableColumn<T> insertColumn(int column) {
        checkIndex(column < 0, "column must be >= 0, but was %s", column);

        if (column <= maxColumnIndex) {
            for (int idx = maxColumnIndex; idx >= column; --idx) {
                moveColumn(idx, idx + 1);
            }
        }
        maxColumnIndex = Math.max(maxColumnIndex, column);
        return getColumn(column);
    }

    @Override
    public MutableColumn<T> insertColumn(Column<T> column) {
        validateColumn(column);
        return insertColumn(column.getColumnIndex());
    }

    @Override
    public void deleteRow(int row) {
        checkIndex(row < 0 || row > maxRowIndex, "row must be >= 0 and <= %s, but was %s", maxRowIndex, row);

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
        checkArgument(row == null, "row cannot be null");
        checkState(row.getMatrix() != this, "row does not belong to this matrix");
    }

    @Override
    public void deleteColumn(int column) {
        checkIndex(column < 0 || column > maxColumnIndex, "column must be >= 0 and <= %s, but was %s", maxColumnIndex, column);

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
        checkArgument(column == null, "row cannot be null");
        checkState(column.getMatrix() != this, "row does not belong to this matrix");
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
        return obj instanceof Matrix && Matrices.equals(this, (Matrix<?>) obj);
    }

    @Override
    public int hashCode() {
        return Matrices.hashCode(this);
    }

    @Override
    public String toString() {
        return Matrices.toString(this);
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
    public Matrix<T> map() {
        return ImmutableMatrix.copyOf(this);
    }

    @Override
    public <R> Matrix<R> map(Function<? super T, ? extends R> function) {
        return ImmutableMatrix.copyOf(this, function);
    }

    @Override
    public Matrix<T> map(Range range) {
        return ImmutableMatrix.copyOf(this, range);
    }

    @Override
    public <R> Matrix<R> map(Range range, Function<? super T, ? extends R> function) {
        return ImmutableMatrix.copyOf(this, range, function);
    }

    private static final class IndexColumn<T> implements MutableColumn<T> {
        private final IndexMatrix<T> matrix;
        private int columnIndex;
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

        private void delete() {
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


        private void delete() {
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

        private void delete() {
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


