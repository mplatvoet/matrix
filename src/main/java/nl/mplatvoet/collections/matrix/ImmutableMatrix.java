package nl.mplatvoet.collections.matrix;

import nl.mplatvoet.collections.matrix.args.Arguments;
import nl.mplatvoet.collections.matrix.fn.Function;
import nl.mplatvoet.collections.matrix.fn.Functions;
import nl.mplatvoet.collections.matrix.range.Range;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ImmutableMatrix<T> implements Matrix<T> {
    private final AbstractCell[][] cells;
    private final ImmutableRow[] rows;
    private final ImmutableColumn[] columns;

    private final Iterable<Row<T>> rowsIterable;
    private final Iterable<Column<T>> columnsIterable;

    private <S> ImmutableMatrix(Matrix<S> matrix, Range range, Function<? super S, ? extends T> transform) {
        Arguments.checkArgument(matrix == null, "matrix cannot be null");
        Arguments.checkArgument(range == null, "range cannot be null");
        Arguments.checkArgument(transform == null, "transform cannot be null");
        Arguments.checkArgument(!range.fits(matrix), "%s does not fit in provided %s", range, matrix);

        int rowSize = range.getRowSize();
        int columnSize = range.getColumnSize();

        cells = new AbstractCell[rowSize][columnSize];
        fillCells(matrix, range, transform);


        rows = new ImmutableRow[rowSize];
        columns = new ImmutableColumn[columnSize];
        for (int i = 0; i < rowSize; ++i) rows[i] = new ImmutableRow<>(this, i);
        for (int i = 0; i < columnSize; ++i) columns[i] = new ImmutableColumn<>(this, i);

        rowsIterable = new ArrayIterable<>(rows);
        columnsIterable = new ArrayIterable<>(columns);
    }

    private ImmutableMatrix(int rows, int columns, Function<?, ? extends T> fill) {
        Arguments.checkArgument(rows < 0, "row must be >= 0 but was %s", rows);
        Arguments.checkArgument(columns < 0, "row must be >= 0 but was %s", columns);
        Arguments.checkArgument(fill == null, "fill function cannot be null");


        cells = new AbstractCell[rows][columns];
        fillCells(fill);


        this.rows = new ImmutableRow[rows];
        this.columns = new ImmutableColumn[columns];
        for (int i = 0; i < rows; ++i) this.rows[i] = new ImmutableRow<>(this, i);
        for (int i = 0; i < columns; ++i) this.columns[i] = new ImmutableColumn<>(this, i);

        rowsIterable = new ArrayIterable<>(this.rows);
        columnsIterable = new ArrayIterable<>(this.columns);
    }

    public static <T> Matrix<T> of(int rows, int columns, Function<?, ? extends T> fill) {
        return new ImmutableMatrix(rows, columns, fill);
    }

    @SuppressWarnings("unchecked")
    public static <T> Matrix<T> copyOf(Matrix<? extends T> matrix) {
        if (matrix instanceof ImmutableMatrix) {
            return (Matrix<T>) matrix;
        }

        return copyOf(matrix, Range.of(matrix), Functions.<T>passTrough());
    }

    public static <T> Matrix<T> copyOf(Matrix<? extends T> matrix, Range range) {
        if (range != null && range.matches(matrix)) {
            return copyOf(matrix);
        }
        return copyOf(matrix, range, Functions.<T>passTrough());
    }

    public static <T, R> Matrix<R> copyOf(Matrix<? extends T> matrix, Function<? super T, ? extends R> transform) {
        return copyOf(matrix, Range.of(matrix), transform);
    }

    public static <T, R> Matrix<R> copyOf(Matrix<? extends T> matrix, Range range, Function<? super T, ? extends R> transform) {
        //TODO check if range is empty and return a special empty matrix

        return new ImmutableMatrix<>(matrix, range, transform);
    }

    private <S> void fillCells(Matrix<S> source, Range range, Function<? super S, ? extends T> transform) {
        final int rOffset = range.getRowBeginIndex();
        final int cOffset = range.getColumnBeginIndex();
        //don't use iterator, I want to match the previous set size
        for (int r = 0; r < range.getRowSize(); ++r) {
            Row<S> row = source.getRow(r + rOffset);
            for (int c = 0; c < range.getColumnSize(); ++c) {
                Cell<S> cell = row.getCell(c + cOffset);
                if (cell.isBlank()) {
                    cells[r][c] = new BlankCell<>(this, r, c);
                } else {
                    T value = transform.apply(r, c, cell.getValue());
                    cells[r][c] = new ValueCell<>(this, value, r, c);
                }
            }
        }
    }

    private <S> void fillCells(Function<?, ? extends T> transform) {
        for (int r = 0; r < cells.length; ++r) {
            for (int c = 0; c < cells[r].length; ++c) {
                T value = transform.apply(r, c, null);
                cells[r][c] = new ValueCell<>(this, value, r, c);
            }
        }
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
        return copyOf(this, function);
    }

    @Override
    public Matrix<T> map(Range range) {
        return copyOf(this, range);
    }

    @Override
    public <R> Matrix<R> map(Range range, Function<? super T, ? extends R> function) {
        return copyOf(this, range, function);
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


    private static abstract class AbstractCell<T> implements Cell<T> {
        private final ImmutableMatrix<T> matrix;
        private final int row;
        private final int column;

        AbstractCell(ImmutableMatrix<T> matrix, int row, int column) {
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
        BlankCell(ImmutableMatrix<T> matrix, int row, int column) {
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

        ValueCell(ImmutableMatrix<T> matrix, T value, int row, int column) {
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
        private ArrayIterable<Cell<T>> cells;

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
        public Cell<T> getCell(int idx) {
            return matrix.getCell(row, idx);
        }

        @Override
        public Iterable<Cell<T>> cells() {
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
        public Cell<T> getCell(int idx) {
            return matrix.getCell(idx, column);
        }

        @Override
        public Iterable<Cell<T>> cells() {
            return cellIterable;
        }

        @Override
        public Iterator<T> iterator() {
            return new CellValueIterator<>(cellIterable.iterator());
        }
    }

    private static final class ColumnCellIterable<T> implements Iterable<Cell<T>> {
        private final Cell[][] cells;
        private final int column;

        private ColumnCellIterable(Cell[][] cells, int column) {
            this.cells = cells;
            this.column = column;
        }

        @Override
        public Iterator<Cell<T>> iterator() {
            return new ColumnCellIterator<>(cells, column);
        }
    }

    private static final class ColumnCellIterator<T> implements Iterator<Cell<T>> {
        private final Cell[][] cells;
        private final int column;
        private int idx = -1;

        private ColumnCellIterator(Cell[][] cells, int column) {
            this.cells = cells;
            this.column = column;
        }


        @Override
        public boolean hasNext() {
            return idx + 1 < cells.length;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Cell<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return (Cell<T>) cells[idx][column];
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
        private final Iterator<Cell<T>> cellIterator;

        private CellValueIterator(Iterator<Cell<T>> cellIterator) {
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
