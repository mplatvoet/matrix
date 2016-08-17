package nl.mplatvoet.collections.matrix;

import nl.mplatvoet.collections.matrix.fn.Function;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ImmutableMatrix<T> implements Matrix<T> {
    private final AbstractCell[][] cells;
    private final ImmutableRow[] rows;
    private final ImmutableColumn[] columns;

    private final Iterable<Row<T>> rowsIterable;
    private final Iterable<Column<T>> columnsIterable;

    private ImmutableMatrix(Matrix<? extends T> source) {
        if (source == null) {
            throw new IllegalArgumentException("source can not be null");
        }
        int rowSize = source.getRowSize();
        int columnSize = source.getColumnSize();

        cells = new AbstractCell[rowSize][columnSize];
        rows = new ImmutableRow[rowSize];
        columns = new ImmutableColumn[columnSize];

        for (int i = 0; i < rowSize; ++i) rows[i] = new ImmutableRow<>(this, i);
        for (int i = 0; i < columnSize; ++i) columns[i] = new ImmutableColumn<>(this, i);

        //don't use iterator, I want to match the previous set size
        for (int r = 0; r < rowSize; ++r) {
            Row<? extends T> row = source.getRow(r);
            for (int c = 0; c < columnSize; ++c) {
                Cell<? extends T> cell = row.getCell(c);
                cells[r][c] = cell.isBlank() ? new BlankCell<>(this, r, c) : new ValueCell<>(this, cell.getValue(), r, c);
            }
        }

        rowsIterable = new ArrayIterable<>(rows);
        columnsIterable = new ArrayIterable<>(columns);
    }

    @SuppressWarnings("unchecked")
    public static <T> Matrix<T> from(Matrix<? extends T> matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException("matrix can not be null");
        }
        if (matrix instanceof ImmutableMatrix) {
            return (Matrix<T>) matrix;
        }
        return new ImmutableMatrix<T>(matrix);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Cell<T> getCell(int row, int column) {
        return (Cell<T>) cells[row][column];
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
    public <R> Matrix<R> map(Function<? super T, ? extends R> function) {
        return null;
    }

    @Override
    public Matrix<T> map(int rowBeginIdx, int rowEndIdx, int columnBeginIdx, int columnEndIdx) {
        return null;
    }

    @Override
    public <R> Matrix<R> map(int rowBeginIdx, int rowEndIdx, int columnBeginIdx, int columnEndIdx, Function<? super T, ? extends R> function) {
        return null;
    }

    @Override
    public int getRowSize() {
        return rows.length;
    }

    @Override
    public int getColumnSize() {
        return columns.length;
    }


    private static abstract class AbstractCell<T> implements Cell<T> {
        private final ImmutableMatrix<T> matrix;
        private final int row;
        private final int column;

        protected AbstractCell(ImmutableMatrix<T> matrix, int row, int column) {
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

    private static final class BlankCell<T> extends AbstractCell<T> {
        protected BlankCell(ImmutableMatrix<T> matrix, int row, int column) {
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

    private static final class ValueCell<T> extends AbstractCell<T> {
        private final T value;

        protected ValueCell(ImmutableMatrix<T> matrix, T value, int row, int column) {
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

        protected AbstractLine(ImmutableMatrix<T> matrix) {
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

        protected ImmutableRow(ImmutableMatrix<T> matrix, int row) {
            super(matrix);
            this.row = row;
        }

        @Override
        public int getRowIndex() {
            return row;
        }

        @Override
        public Cell<T> getCell(int idx) {
            return matrix.getCell(row, idx);
        }

        @Override
        public Iterable<Cell<T>> cells() {
            return null;
        }

        @Override
        public Iterator<T> iterator() {
            return null;
        }
    }

    private static class ImmutableColumn<T> extends AbstractLine<T> implements Column<T> {
        private final int column;

        protected ImmutableColumn(ImmutableMatrix<T> matrix, int column) {
            super(matrix);
            this.column = column;
        }

        @Override
        public int getColumnIndex() {
            return column;
        }

        @Override
        public Cell<T> getCell(int idx) {
            return matrix.getCell(idx, column);
        }

        @Override
        public Iterable<Cell<T>> cells() {
            //TODO
            return null;
        }

        @Override
        public Iterator<T> iterator() {
            //TODO
            return null;
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
}
