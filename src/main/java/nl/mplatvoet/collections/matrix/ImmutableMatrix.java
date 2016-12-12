package nl.mplatvoet.collections.matrix;

import nl.mplatvoet.collections.matrix.args.Arguments;
import nl.mplatvoet.collections.matrix.fn.CellMapFunction;
import nl.mplatvoet.collections.matrix.fn.DetachedCell;
import nl.mplatvoet.collections.matrix.fn.Functions;
import nl.mplatvoet.collections.matrix.range.Range;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class ImmutableMatrix<T> implements Matrix<T> {
    private final AbstractMatrixCell[][] cells;
    private final ImmutableRow[] rows;
    private final ImmutableColumn[] columns;

    private final Iterable<Row<T>> rowsIterable;
    private final Iterable<Column<T>> columnsIterable;

    private ImmutableMatrix(T[][] source) {
        Arguments.checkArgument(source == null, "source cannot be null");

        int rowSize = source.length;
        int columnSize = maxColumn(source);

        cells = new AbstractMatrixCell[rowSize][columnSize];

        for (int r = 0; r < rowSize; r++) {
            T[] values = source[r];
            int c = 0;
            for (; c < values.length; c++) {
                cells[r][c] = new ValueMatrixCell<>(this, values[c], r,c);
            }
            for (; c < columnSize; c++) {
                cells[r][c] = new BlankMatrixCell<>(this, r,c);
            }
        }

        rows = new ImmutableRow[rowSize];
        columns = new ImmutableColumn[columnSize];
        for (int i = 0; i < rowSize; ++i) rows[i] = new ImmutableRow<>(this, i);
        for (int i = 0; i < columnSize; ++i) columns[i] = new ImmutableColumn<>(this, i);

        rowsIterable = new ArrayIterable<>(rows);
        columnsIterable = new ArrayIterable<>(columns);
    }

    private int maxColumn(T[][] source) {
        int max = 0;
        for (T[] ts : source) {
            max = Math.max(max, ts.length);
        }
        return max;
    }

    private <S> ImmutableMatrix(Matrix<S> matrix, Range range, CellMapFunction<S, T> map) {
        Arguments.checkArgument(matrix == null, "matrix cannot be null");
        Arguments.checkArgument(range == null, "range cannot be null");
        Arguments.checkArgument(map == null, "map cannot be null");
        Arguments.checkArgument(!range.fits(matrix), "%s does not fit in provided %s", range, matrix);

        int rowSize = range.getRowSize();
        int columnSize = range.getColumnSize();

        cells = new AbstractMatrixCell[rowSize][columnSize];
        fillCells(matrix, range, map);


        rows = new ImmutableRow[rowSize];
        columns = new ImmutableColumn[columnSize];
        for (int i = 0; i < rowSize; ++i) rows[i] = new ImmutableRow<>(this, i);
        for (int i = 0; i < columnSize; ++i) columns[i] = new ImmutableColumn<>(this, i);

        rowsIterable = new ArrayIterable<>(rows);
        columnsIterable = new ArrayIterable<>(columns);
    }

    private ImmutableMatrix(int rows, int columns, Function<MutableCell<T>, T> fill) {
        Arguments.checkArgument(rows < 0, "row must be >= 0 but was %s", rows);
        Arguments.checkArgument(columns < 0, "row must be >= 0 but was %s", columns);
        Arguments.checkArgument(fill == null, "cells function cannot be null");


        cells = new AbstractMatrixCell[rows][columns];
        fillCells(fill);


        this.rows = new ImmutableRow[rows];
        this.columns = new ImmutableColumn[columns];
        for (int i = 0; i < rows; ++i) this.rows[i] = new ImmutableRow<>(this, i);
        for (int i = 0; i < columns; ++i) this.columns[i] = new ImmutableColumn<>(this, i);

        rowsIterable = new ArrayIterable<>(this.rows);
        columnsIterable = new ArrayIterable<>(this.columns);
    }

    public static <T> Matrix<T> of(int rows, int columns, Function<MutableCell<T>, T> fill) {
        return new ImmutableMatrix<>(rows, columns, fill);
    }

    public static <T> Matrix<T> copyOf(T[][] source) {
        return new ImmutableMatrix<>(source);
    }

    @SuppressWarnings("unchecked")
    public static <T> Matrix<T> copyOf(Matrix<T> matrix) {
        if (matrix instanceof ImmutableMatrix) {
            return matrix;
        }

        return copyOf(matrix, Range.of(matrix), Functions.<T, T>passTrough());
    }

    public static <T> Matrix<T> copyOf(Matrix<T> matrix, Range range) {
        if (range != null && range.matches(matrix)) {
            return copyOf(matrix);
        }
        return copyOf(matrix, range, Functions.<T, T>passTrough());
    }

    public static <T, R> Matrix<R> copyOf(Matrix<T> matrix, CellMapFunction<T, R> transform) {
        return copyOf(matrix, Range.of(matrix), transform);
    }

    public static <T, R> Matrix<R> copyOf(Matrix<T> matrix, Range range, CellMapFunction<T, R> map) {
        //TODO check if range is empty and return a special empty matrix

        return new <T>ImmutableMatrix<R>(matrix, range, map);
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
                if (result.isBlank()) {
                    cells[r][c] = new BlankMatrixCell<>(this, r, c);
                } else {
                    cells[r][c] = new ValueMatrixCell<>(this, result.getValue(), r, c);
                }
                result.clear();
            }
        }
    }


    private void fillCells(Function<MutableCell<T>, T> fn) {
        DetachedCell<T> cell = new DetachedCell<>();
        for (int r = 0; r < cells.length; ++r) {
            for (int c = 0; c < cells[r].length; ++c) {
                cell.apply(r, c);
                fn.apply(cell);
                if (cell.isBlank()) {
                    cells[r][c] = new BlankMatrixCell<>(this, r, c);
                } else {
                    cells[r][c] = new ValueMatrixCell<>(this, cell.getValue(), r, c);
                }
                cell.clear();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public MatrixCell<T> getCell(int row, int column) {
        return (MatrixCell<T>) cells[row][column];
    }

    @Override
    public T get(int row, int column) {
        return getCell(row, column).getValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Row<T> getRow(int row) {
        return (Row<T>) rows[row];
    }

    @SuppressWarnings("unchecked")
    @Override
    public Column<T> getColumn(int column) {
        return (Column<T>) columns[column];
    }

    @Override
    public Iterable<Row<T>> rows() {
        return rowsIterable;
    }

    @Override
    public Iterable<Column<T>> columns() {
        return columnsIterable;
    }

    @Override
    public Matrix<T> map() {
        return this;
    }

    @Override
    public <R> Matrix<R> map(CellMapFunction<T, R> map) {
        return copyOf(this, map);
    }

    @Override
    public Matrix<T> map(Range range) {
        return copyOf(this, range);
    }

    @Override
    public <R> Matrix<R> map(Range range, CellMapFunction<T, R> map) {
        return copyOf(this, range, map);
    }

    @Override
    public int getRowSize() {
        return rows.length;
    }

    @Override
    public int getColumnSize() {
        return columns.length;
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


    private static abstract class AbstractMatrixCell<T> implements MatrixCell<T> {
        private final ImmutableMatrix<T> matrix;
        private final int row;
        private final int column;

        AbstractMatrixCell(ImmutableMatrix<T> matrix, int row, int column) {
            this.matrix = matrix;
            this.row = row;
            this.column = column;
        }

        @Override
        public Matrix<T> getMatrix() {
            return matrix;
        }

        @Override
        public Row<T> getRow() {
            return matrix.getRow(row);
        }

        @Override
        public Column<T> getColumn() {
            return matrix.getColumn(column);
        }

        @Override
        public int getColumnIndex() {
            return column;
        }

        @Override
        public int getRowIndex() {
            return row;
        }
    }

    private static final class BlankMatrixCell<T> extends AbstractMatrixCell<T> {
        BlankMatrixCell(ImmutableMatrix<T> matrix, int row, int column) {
            super(matrix, row, column);
        }

        @Override
        public T getValue() {
            return null;
        }

        @Override
        public boolean isBlank() {
            return true;
        }
    }

    private static final class ValueMatrixCell<T> extends AbstractMatrixCell<T> {
        private final T value;

        ValueMatrixCell(ImmutableMatrix<T> matrix, T value, int row, int column) {
            super(matrix, row, column);
            this.value = value;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public boolean isBlank() {
            return false;
        }
    }


    private static abstract class AbstractLine<T> implements Line<T> {
        protected final ImmutableMatrix<T> matrix;

        AbstractLine(ImmutableMatrix<T> matrix) {
            this.matrix = matrix;
        }

        @Override
        public Matrix<T> getMatrix() {
            return matrix;
        }

        @Override
        public T get(int idx) {
            return getCell(idx).getValue();
        }
    }

    private static class ImmutableRow<T> extends AbstractLine<T> implements Row<T> {
        private final int row;
        private ArrayIterable<MatrixCell<T>> cells;

        ImmutableRow(ImmutableMatrix<T> matrix, int row) {
            super(matrix);
            this.row = row;
            this.cells = new ArrayIterable<>(matrix.cells[row]);
        }

        @Override
        public int getRowIndex() {
            return row;
        }

        @Override
        public MatrixCell<T> getCell(int idx) {
            return matrix.getCell(row, idx);
        }

        @Override
        public Iterable<MatrixCell<T>> cells() {
            return cells;
        }

        @Override
        public Iterator<T> iterator() {
            return new CellValueIterator<>(cells.iterator());
        }
    }

    private static class ImmutableColumn<T> extends AbstractLine<T> implements Column<T> {
        private final int column;
        private final ColumnCellIterable<T> cellIterable;

        ImmutableColumn(ImmutableMatrix<T> matrix, int column) {
            super(matrix);
            this.column = column;
            cellIterable = new ColumnCellIterable<>(matrix.cells, column);
        }

        @Override
        public int getColumnIndex() {
            return column;
        }

        @Override
        public MatrixCell<T> getCell(int idx) {
            return matrix.getCell(idx, column);
        }

        @Override
        public Iterable<MatrixCell<T>> cells() {
            return cellIterable;
        }

        @Override
        public Iterator<T> iterator() {
            return new CellValueIterator<>(cellIterable.iterator());
        }
    }

    private static final class ColumnCellIterable<T> implements Iterable<MatrixCell<T>> {
        private final MatrixCell[][] cells;
        private final int column;

        private ColumnCellIterable(MatrixCell[][] cells, int column) {
            this.cells = cells;
            this.column = column;
        }

        @Override
        public Iterator<MatrixCell<T>> iterator() {
            return new ColumnCellIterator<>(cells, column);
        }
    }

    private static final class ColumnCellIterator<T> implements Iterator<MatrixCell<T>> {
        private final MatrixCell[][] cells;
        private final int column;
        private int idx = -1;

        private ColumnCellIterator(MatrixCell[][] cells, int column) {
            this.cells = cells;
            this.column = column;
        }


        @Override
        public boolean hasNext() {
            return idx + 1 < cells.length;
        }

        @SuppressWarnings("unchecked")
        @Override
        public MatrixCell<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return (MatrixCell<T>) cells[idx][column];
        }


        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class ArrayIterable<T> implements Iterable<T> {
        private final Object[] array;

        private ArrayIterable(Object[] array) {
            this.array = array;
        }

        @Override
        public Iterator<T> iterator() {
            return new ArrayIterator<>(array);
        }
    }

    private static final class ArrayIterator<T> implements Iterator<T> {
        private final Object[] array;
        private int index = -1;

        private ArrayIterator(Object[] array) {
            this.array = array;
        }

        @Override
        public boolean hasNext() {
            return (index + 1) < array.length;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return (T) array[++index];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class CellValueIterator<T> implements Iterator<T> {
        private final Iterator<MatrixCell<T>> cellIterator;

        private CellValueIterator(Iterator<MatrixCell<T>> cellIterator) {
            this.cellIterator = cellIterator;
        }

        @Override
        public boolean hasNext() {
            return cellIterator.hasNext();
        }

        @Override
        public T next() {
            return cellIterator.next().getValue();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
