package nl.mplatvoet.collections.matrix;

import nl.mplatvoet.collections.map.ArrayMap;
import nl.mplatvoet.collections.map.IntKeyMap;
import nl.mplatvoet.collections.matrix.args.Arguments;
import nl.mplatvoet.collections.matrix.fn.CellMapFunction;
import nl.mplatvoet.collections.matrix.fn.DetachedCell;
import nl.mplatvoet.collections.matrix.fn.Functions;
import nl.mplatvoet.collections.matrix.range.Range;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static nl.mplatvoet.collections.matrix.args.Arguments.*;

public class MutableArrayMatrix<T> implements MutableMatrix<T> {
    private final IntKeyMap<IndexRow<T>> rows;
    private final IntKeyMap<IndexColumn<T>> columns;

    private RowsIterable<T> rowsIterable = null;
    private ColumnsIterable<T> columnsIterable = null;
    private MutableRowsIterable<T> mutableRowsIterable = null;
    private MutableColumnsIterable<T> mutableColumnsIterable = null;

    private int maxRowIndex = -1;
    private int maxColumnIndex = -1;

    private MutableArrayMatrix(T[][] source) {
        checkArgument(source == null, "source cannot be null");
        int rows = source.length;
        int columns = maxColumn(source);


        maxRowIndex = rows - 1;
        maxColumnIndex = columns - 1;

        this.rows = new ArrayMap<>(rows);
        this.columns = new ArrayMap<>(columns);

        for (int r = 0; r < rows; r++) {
            T[] values = source[r];
            for (int c = 0; c < values.length; c++) {
                put(r, c, values[c]);
            }
        }

    }

    private int maxColumn(T[][] source) {
        int max = 0;
        for (T[] ts : source) {
            max = Math.max(max, ts.length);
        }
        return max;
    }

    @SuppressWarnings("unchecked")
    private MutableArrayMatrix(int initialRows, int initialColumns, CellMapFunction<T, T> function) {
        checkArgument(initialRows < 0, "initialRows must be >= 0, but was %s", initialRows);
        checkArgument(initialColumns < 0, "initialColumns must be >= 0, but was %s", initialColumns);

        maxRowIndex = initialRows - 1;
        maxColumnIndex = initialColumns - 1;

        rows = new ArrayMap<>(initialRows);
        columns = new ArrayMap<>(initialColumns);

        if (function != null) {
            cells(function);
        }
    }

    private <S> MutableArrayMatrix(Matrix<S> matrix, Range range, CellMapFunction<S, T> transform) {
        Arguments.checkArgument(matrix == null, "matrix cannot be null");
        Arguments.checkArgument(range == null, "range cannot be null");
        Arguments.checkArgument(transform == null, "transform cannot be null");
        Arguments.checkArgument(!range.fits(matrix), "%s does not fit in provided %s", range, matrix);

        int rowSize = range.getRowSize();
        int columnSize = range.getColumnSize();
        maxRowIndex = -1;
        maxColumnIndex = -1;

        rows = new ArrayMap<>(rowSize);
        columns = new ArrayMap<>(columnSize);
        fillCells(matrix, range, transform);
    }

    public static <T> MutableMatrix<T> of() {
        return of(0, 0);
    }

    public static <T> MutableMatrix<T> of(int rows, int columns) {
        return new MutableArrayMatrix<>(rows, columns, null);
    }

    public static <T> MutableMatrix<T> of(int rows, int columns, CellMapFunction<T, T> fill) {
        Arguments.checkArgument(fill == null, "cells function cannot be null");
        return new MutableArrayMatrix<>(rows, columns, fill);
    }

    public static <T> MutableMatrix<T> copyOf(T[][] source) {
        return new MutableArrayMatrix<>(source);
    }

    @SuppressWarnings("unchecked")
    public static <T> MutableMatrix<T> copyOf(Matrix<T> matrix) {
        return copyOf(matrix, Range.of(matrix), Functions.<T, T>passTrough());
    }

    public static <T> MutableMatrix<T> copyOf(Matrix<T> matrix, Range range) {
        if (range != null && range.matches(matrix)) {
            return copyOf(matrix);
        }
        return copyOf(matrix, range, Functions.<T, T>passTrough());
    }

    public static <T, R> MutableMatrix<R> copyOf(Matrix<T> matrix, CellMapFunction<T, R> transform) {
        return copyOf(matrix, Range.of(matrix), transform);
    }

    public static <T, R> MutableMatrix<R> copyOf(Matrix<T> matrix, Range range, CellMapFunction<T, R> transform) {
        //TODO check if range is empty and return a special empty matrix

        return new MutableArrayMatrix<>(matrix, range, transform);
    }

    private <S> void fillCells(Matrix<S> source, Range range, CellMapFunction<S, T> map) {
        final int rOffset = range.getRowBeginIndex();
        final int cOffset = range.getColumnBeginIndex();
        //don't use iterator, I want to match the previous set size
        DetachedCell<T> result = new DetachedCell<>();
        for (int r = 0; r < range.getRowSize(); ++r) {
            Row<S> row = source.getRow(r + rOffset);
            for (int c = 0; c < range.getColumnSize(); ++c) {
                MatrixCell<S> cell = row.getCell(c + cOffset);
                result.apply(r, c);
                map.apply(cell, result);
                if (!result.isBlank()) {
                    put(r, c, result.getValue());
                    result.clear();
                }
                result.reset();
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
    public void cells(CellMapFunction<T, T> function) {
        checkArgument(function == null, "function cannot be null");

        for (int rowIndex = 0; rowIndex <= maxRowIndex; ++rowIndex) {
            IndexRow<T> row = getRow(rowIndex);
            row.cells(function);
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

        if (matrix instanceof MutableArrayMatrix) {
            putAllIndexMatrix((MutableArrayMatrix<T>) matrix, rowOffset, columnOffset);
        } else {
            putAllGenericMatrix(matrix, rowOffset, columnOffset);
        }

    }

    private void putAllGenericMatrix(Matrix<? extends T> matrix, int rowOffset, int columnOffset) {
        for (Row<? extends T> row : matrix.rows()) {
            for (MatrixCell<? extends T> cell : row.cells()) {
                if (!cell.isBlank()) {
                    int rowIdx = cell.getRowIndex() + rowOffset;
                    int columnIdx = cell.getColumnIndex() + columnOffset;
                    put(rowIdx, columnIdx, cell.getValue());
                }
            }
        }
    }

    private void putAllIndexMatrix(MutableArrayMatrix<? extends T> matrix, int rowOffset, int columnOffset) {
        final IntKeyMap<? extends IndexRow<? extends T>> rows = matrix.rows;
        for (int r = 0, rowLength = rows.size(); r < rowLength; ++r) {
            final IndexRow<? extends T> row = rows.get(r);
            if (row == null) continue;

            final ArrayMap<? extends IndexMatrixCell<? extends T>> cells = row.cells;
            for (int c = 0, colLength = cells.size(); c < colLength; ++c) {
                final IndexMatrixCell<? extends T> cell = cells.get(c);
                if (cell != null && !cell.isBlank()) {
                    put(r + rowOffset, c + columnOffset, cell.getValue());
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
        validateRow(row, "row");
    }

    private void validateRow(Row<T> row, String rowName) {
        checkArgument(row == null, "%s cannot be null", rowName);
        checkState(row.getMatrix() != this, "%s does not belong to this matrix", rowName);
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
        validateColumn(column, "column");
    }

    private void validateColumn(Column<T> column, String columnName) {
        checkArgument(column == null, "%s cannot be null", columnName);
        checkState(column.getMatrix() != this, "%s does not belong to this matrix", columnName);
    }

    @Override
    public void swapRow(Row<T> firstRow, Row<T> secondRow) {
        validateRow(firstRow, "firstRow");
        validateRow(secondRow, "secondRow");
        swapRow(firstRow.getRowIndex(), secondRow.getRowIndex());
    }

    @Override
    public void swapRow(int firstRow, int secondRow) {
        checkIndex(firstRow < 0 || firstRow > maxRowIndex, "firstRow must be >= 0 and <= %s, but was %s", maxRowIndex, firstRow);
        checkIndex(secondRow < 0 || secondRow > maxRowIndex, "secondRow must be >= 0 and <= %s, but was %s", maxRowIndex, secondRow);
        if (firstRow == secondRow) return;

        IndexRow<T> first = rows.get(firstRow);
        IndexRow<T> second = rows.get(secondRow);
        rows.remove(firstRow);
        rows.remove(secondRow);

        if (first != null) {
            updateRowIndices(first, secondRow);
            rows.put(secondRow, first);
        }

        if (second != null) {
            updateRowIndices(second, firstRow);
            rows.put(firstRow, second);
        }
    }

    @Override
    public void swapColumn(Column<T> firstColumn, Column<T> secondColumn) {
        validateColumn(firstColumn, "firstColumn");
        validateColumn(secondColumn, "secondColumn");
        swapColumn(firstColumn.getColumnIndex(), secondColumn.getColumnIndex());
    }


    @Override
    public void swapColumn(int firstColumn, int secondColumn) {
        checkIndex(firstColumn < 0 || firstColumn > maxColumnIndex
                , "firstColumn must be >= 0 and <= %s, but was %s", maxColumnIndex, firstColumn);
        checkIndex(secondColumn < 0 || secondColumn > maxColumnIndex
                , "secondColumn must be >= 0 and <= %s, but was %s", maxColumnIndex, secondColumn);
        if (firstColumn == secondColumn) return;

        IndexColumn<T> first = columns.get(firstColumn);
        IndexColumn<T> second = columns.get(secondColumn);
        columns.remove(firstColumn);
        columns.remove(secondColumn);

        if (first != null) {
            first.columnIndex = secondColumn;
            columns.put(secondColumn, first);
        }

        if (second != null) {
            second.columnIndex = firstColumn;
            columns.put(firstColumn, second);
        }
        for (int r = 0, rowLength = rows.size(); r < rowLength; ++r) {
            final IndexRow<T> row = rows.get(r);
            if (row == null) continue;

            IndexMatrixCell<T> firstCell = row.cells.get(firstColumn);
            IndexMatrixCell<T> secondCell = row.cells.get(secondColumn);
            row.cells.remove(firstColumn);
            row.cells.remove(secondColumn);

            if (firstCell != null) {
                firstCell.columnIndex = secondColumn;
                row.cells.put(secondColumn, firstCell);
            }

            if (secondCell != null) {
                secondCell.columnIndex = firstColumn;
                row.cells.put(firstColumn, secondCell);
            }
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
        for (int r = 0, rowLength = rows.size(); r < rowLength; ++r) {
            final IndexRow<T> row = rows.get(r);
            if (row == null) continue;
            IndexMatrixCell<T> cell = row.cells.get(fromIdx);
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
        for (IndexMatrixCell<T> cell : row.cells.values()) {
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
    public MutableMatrixCell<T> getCell(int row, int column) {
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
        checkIndex(column < 0 || column > maxColumnIndex, "column must be >= 0 and <= %s, but was %s", maxColumnIndex, column);
        IndexColumn<T> c = columns.get(column);
        if (c == null) {
            c = new IndexColumn<>(this, column);
            columns.put(column, c);
        }
        return c;
    }

    @Override
    public void clear() {
        for (int r = 0, rowLength = rows.size(); r < rowLength; ++r) {
            final IndexRow<T> row = rows.get(r);
            if (row == null) continue;
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
    public <R> Matrix<R> map(CellMapFunction<T, R> function) {
        return ImmutableMatrix.copyOf(this, function);
    }

    @Override
    public Matrix<T> map(Range range) {
        return ImmutableMatrix.copyOf(this, range);
    }

    @Override
    public <R> Matrix<R> map(Range range, CellMapFunction<T, R> function) {
        return ImmutableMatrix.copyOf(this, range, function);
    }

    private static final class IndexColumn<T> implements MutableColumn<T> {
        private final MutableArrayMatrix<T> matrix;
        private int columnIndex;
        private ColumnCellsIterable<T> cellsIterable = null;
        private MutableColumnCellsIterable<T> mutableCellsIterable = null;

        private boolean deleted = false;


        private IndexColumn(MutableArrayMatrix<T> matrix, int columnIndex) {
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
        public MutableMatrixCell<T> getCell(int row) {
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
        public Iterable<MatrixCell<T>> cells() {
            assertState();
            if (cellsIterable == null) {
                cellsIterable = new ColumnCellsIterable<>(this);
            }
            return cellsIterable;
        }

        @Override
        public Iterable<MutableMatrixCell<T>> mutableCells() {
            assertState();
            if (mutableCellsIterable == null) {
                mutableCellsIterable = new MutableColumnCellsIterable<>(this);
            }
            return mutableCellsIterable;
        }


        @Override
        public void cells(CellMapFunction<T, T> function) {
            assertState();
            for (int rowIndex = 0; rowIndex <= matrix.maxRowIndex; ++rowIndex) {
                MutableMatrixCell<T> cell = matrix.getRow(rowIndex).getCell(columnIndex);
                function.apply(cell, cell);
            }
        }

        @Override
        public void clear() {
            assertState();
            for (int r = 0, rowLength = matrix.rows.size(); r < rowLength; ++r) {
                final IndexRow<T> row = matrix.rows.get(r);

                if (row == null) continue;
                IndexMatrixCell<T> cell = row.cells.get(columnIndex);
                if (cell != null) {
                    cell.clear();
                }
            }
        }

        private void delete() {
            if (deleted) return;

            for (int r = 0, rowLength = matrix.rows.size(); r < rowLength; ++r) {
                final IndexRow<T> row = matrix.rows.get(r);
                if (row == null) continue;

                IndexMatrixCell<T> cell = row.cells.get(columnIndex);
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
        private final ArrayMap<IndexMatrixCell<T>> cells;
        private final MutableArrayMatrix<T> matrix;
        private int rowIndex;

        private boolean deleted = false;

        private RowCellsIterable<T> cellsIterable = null;
        private MutableRowCellsIterable<T> mutableCellsIterable = null;

        private IndexRow(MutableArrayMatrix<T> matrix, int rowIndex) {
            this.matrix = matrix;
            this.rowIndex = rowIndex;
            //prevents excess array resizing
            cells = new ArrayMap<>(matrix.maxRowIndex + 1);
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
            IndexMatrixCell<T> cell = cells.get(column);
            if (cell == null) {
                cell = new IndexMatrixCell<>(matrix, rowIndex, column);
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
        public MutableMatrixCell<T> getCell(int column) {
            assertState();
            if (column < 0 || column > matrix.maxColumnIndex) {
                throw new IndexOutOfBoundsException("Column must be >= 0 and <= " + matrix.maxColumnIndex + ", but was: " + column);
            }
            IndexMatrixCell<T> cell = cells.get(column);
            if (cell == null) {
                cell = new IndexMatrixCell<>(matrix, rowIndex, column);
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
            IndexMatrixCell<T> cell = cells.get(column);
            return cell != null ? cell.getValue() : null;
        }

        @Override
        public void clear() {
            assertState();
            for (IndexMatrixCell<T> cell : cells.values()) {
                cell.clear();
            }
        }

        @Override
        public void cells(CellMapFunction<T, T> function) {
            assertState();
            for (int columnIndex = 0; columnIndex <= matrix.maxColumnIndex; ++columnIndex) {
                MutableMatrixCell<T> cell = getCell(columnIndex);
                function.apply(cell, cell);
            }
        }

        @Override
        public Iterator<T> iterator() {
            assertState();
            return new RowIterator<>(this);
        }

        @Override
        public Iterable<MatrixCell<T>> cells() {
            assertState();
            if (cellsIterable == null) {
                cellsIterable = new RowCellsIterable<>(this);
            }
            return cellsIterable;
        }

        @Override
        public Iterable<MutableMatrixCell<T>> mutableCells() {
            if (mutableCellsIterable == null) {
                mutableCellsIterable = new MutableRowCellsIterable<>(this);
            }
            return mutableCellsIterable;
        }


        private void delete() {
            if (deleted) return;


            for (IndexMatrixCell<T> cell : cells.values()) {
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


    private static final class IndexMatrixCell<T> implements MutableMatrixCell<T> {
        private static final Object BLANK = new Object();

        private MutableArrayMatrix<T> matrix;
        private int rowIndex;
        private int columnIndex;
        private T value;

        private boolean deleted = false;

        private IndexMatrixCell(MutableArrayMatrix<T> matrix, int rowIndex, int columnIndex, T value) {
            this.matrix = matrix;
            this.rowIndex = rowIndex;
            this.columnIndex = columnIndex;
            this.value = value;
        }

        private IndexMatrixCell(MutableArrayMatrix<T> matrix, int rowIndex, int columnIndex) {
            this(matrix, rowIndex, columnIndex, IndexMatrixCell.<T>blank());
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
        final MutableArrayMatrix<T> matrix;
        private int index = -1;
        private boolean deleted = false;

        private AbstractRowsIterator(MutableArrayMatrix<T> matrix) {
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
        private MutableRowsIterator(MutableArrayMatrix<T> matrix) {
            super(matrix);
        }

        @Override
        protected MutableRow<T> rowForIndex(int idx) {
            return matrix.getRow(idx);
        }
    }

    private static class RowsIterator<T> extends AbstractRowsIterator<T, Row<T>> {
        private RowsIterator(MutableArrayMatrix<T> matrix) {
            super(matrix);
        }

        @Override
        protected Row<T> rowForIndex(int idx) {
            return matrix.getRow(idx);
        }
    }


    private static abstract class AbstractColumnsIterator<T, C extends Column<T>> implements Iterator<C> {
        final MutableArrayMatrix<T> matrix;
        private int index = -1;
        private boolean deleted = false;


        private AbstractColumnsIterator(MutableArrayMatrix<T> matrix) {
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
        private ColumnsIterator(MutableArrayMatrix<T> matrix) {
            super(matrix);
        }

        @Override
        protected Column<T> columnForIndex(int idx) {
            return matrix.getColumn(idx);
        }
    }

    private static class MutableColumnsIterator<T> extends AbstractColumnsIterator<T, MutableColumn<T>> {
        private MutableColumnsIterator(MutableArrayMatrix<T> matrix) {
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
            IndexMatrixCell<T> cell = row.cells.get(index);
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
                IndexMatrixCell<T> cell = row.cells.get(column.columnIndex);
                if (cell != null) {
                    cell.clear();
                }
            }
        }
    }

    private static class RowCellIterator<T> implements Iterator<MatrixCell<T>> {
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
        public MatrixCell<T> next() {
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

    private static class MutableRowCellIterator<T> implements Iterator<MutableMatrixCell<T>> {
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
        public MutableMatrixCell<T> next() {
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

    private static class ColumnCellIterator<T> implements Iterator<MatrixCell<T>> {
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
        public MatrixCell<T> next() {
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

    private static class MutableColumnCellIterator<T> implements Iterator<MutableMatrixCell<T>> {
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
        public MutableMatrixCell<T> next() {
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

    private static class ColumnCellsIterable<T> implements Iterable<MatrixCell<T>> {
        private final IndexColumn<T> column;

        private ColumnCellsIterable(IndexColumn<T> column) {
            this.column = column;
        }

        @Override
        public Iterator<MatrixCell<T>> iterator() {
            return new ColumnCellIterator<>(column);
        }
    }

    private static class MutableColumnCellsIterable<T> implements Iterable<MutableMatrixCell<T>> {
        private final IndexColumn<T> column;

        private MutableColumnCellsIterable(IndexColumn<T> column) {
            this.column = column;
        }

        @Override
        public Iterator<MutableMatrixCell<T>> iterator() {
            return new MutableColumnCellIterator<>(column);
        }
    }

    private static class RowCellsIterable<T> implements Iterable<MatrixCell<T>> {
        private final IndexRow<T> row;

        private RowCellsIterable(IndexRow<T> row) {
            this.row = row;
        }

        @Override
        public Iterator<MatrixCell<T>> iterator() {
            return new RowCellIterator<>(row);
        }
    }

    private static class MutableRowCellsIterable<T> implements Iterable<MutableMatrixCell<T>> {
        private final IndexRow<T> row;

        private MutableRowCellsIterable(IndexRow<T> row) {
            this.row = row;
        }

        @Override
        public Iterator<MutableMatrixCell<T>> iterator() {
            return new MutableRowCellIterator<>(row);
        }
    }

    private static class ColumnsIterable<T> implements Iterable<Column<T>> {
        private final MutableArrayMatrix<T> matrix;

        private ColumnsIterable(MutableArrayMatrix<T> matrix) {
            this.matrix = matrix;
        }

        @Override
        public Iterator<Column<T>> iterator() {
            return new ColumnsIterator<>(matrix);
        }
    }

    private static class MutableColumnsIterable<T> implements Iterable<MutableColumn<T>> {
        private final MutableArrayMatrix<T> matrix;

        private MutableColumnsIterable(MutableArrayMatrix<T> matrix) {
            this.matrix = matrix;
        }

        @Override
        public Iterator<MutableColumn<T>> iterator() {
            return new MutableColumnsIterator<>(matrix);
        }
    }

    private static class RowsIterable<T> implements Iterable<Row<T>> {
        private final MutableArrayMatrix<T> matrix;

        private RowsIterable(MutableArrayMatrix<T> matrix) {
            this.matrix = matrix;
        }

        @Override
        public Iterator<Row<T>> iterator() {
            return new RowsIterator<>(matrix);
        }
    }

    private static class MutableRowsIterable<T> implements Iterable<MutableRow<T>> {
        private final MutableArrayMatrix<T> matrix;

        private MutableRowsIterable(MutableArrayMatrix<T> matrix) {
            this.matrix = matrix;
        }

        @Override
        public Iterator<MutableRow<T>> iterator() {
            return new MutableRowsIterator<>(matrix);
        }
    }
}


