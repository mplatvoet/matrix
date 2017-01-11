package nl.mplatvoet.collections.matrix;


import nl.mplatvoet.collections.matrix.fn.CellMapFunction;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;

import static nl.mplatvoet.collections.matrix.args.Arguments.checkArgument;


public final class Matrices {
    private Matrices() {
        //no instances allowed
    }

    public static <T> MutableMatrix<T> mutableOf() {
        return MutableArrayMatrix.of();
    }

    public static <T> MutableMatrix<T> mutableOf(int rows, int columns) {
        return MutableArrayMatrix.of(rows, columns);
    }

    public static <T> MutableMatrix<T> mutableOf(int rows, int columns, CellMapFunction<T, T> fill) {
        return MutableArrayMatrix.of(rows, columns, fill);
    }

    public static <T> Matrix<T> of(int rows, int columns, Function<MutableCell<T>, T> fill) {
        return ImmutableMatrix.of(rows, columns, fill);
    }

    public static <T, C extends Iterable<T>, R extends Iterable<C>> MutableMatrix<T> copyOf(R source) {
        checkArgument(source == null, "source cannot be null");

        MutableMatrix<T> matrix = mutableOf();
        int row = -1;
        for (C c : source) {
            ++row;
            if (c == null) continue;

            int col = -1;
            for (T value : c) {
                ++col;
                if (value == null) continue;

                matrix.put(row, col, value);
            }
        }
        return matrix;
    }


    public static <T> Matrix<T> copyOf(T[][] source) {
        return ImmutableMatrix.copyOf(source);
    }

    public static <T> Matrix<T> copyOf(Matrix<T> matrix) {
        return ImmutableMatrix.copyOf(matrix);
    }

    public static <T> Matrix<T> copyOf(Matrix<T> matrix, Range range) {
        return ImmutableMatrix.copyOf(matrix, range);
    }

    public static <T, R> Matrix<R> copyOf(Matrix<T> matrix, CellMapFunction<T, R> transform) {
        return ImmutableMatrix.copyOf(matrix, transform);
    }

    public static <T, R> Matrix<R> copyOf(Matrix<T> matrix, Range range, CellMapFunction<T, R> transform) {
        return ImmutableMatrix.copyOf(matrix, range, transform);
    }

    public static <T> MutableMatrix<T> mutableCopyOf(T[][] source) {
        return MutableArrayMatrix.copyOf(source);
    }

    public static <T> MutableMatrix<T> mutableCopyOf(Matrix<T> matrix) {
        return MutableArrayMatrix.copyOf(matrix);
    }

    public static <T> MutableMatrix<T> mutableCopyOf(Matrix<T> matrix, Range range) {
        return MutableArrayMatrix.copyOf(matrix, range);
    }

    public static <T, R> MutableMatrix<R> mutableCopyOf(Matrix<T> matrix, CellMapFunction<T, R> transform) {
        return MutableArrayMatrix.copyOf(matrix, transform);
    }

    public static <T, R> MutableMatrix<R> mutableCopyOf(Matrix<T> matrix, Range range, CellMapFunction<T, R> transform) {
        return MutableArrayMatrix.copyOf(matrix, range, transform);
    }


    public static <T> void sortBy(MutableLine<T> line) {
        sortBy(line, NaturalComparator.INSTANCE);
    }

    public static <T> void sortBy(MutableLine<T> line, Comparator<? super T> comparator) {
        checkArgument(line == null, "line cannot be null");
        checkArgument(comparator == null, "comparator cannot be null");

        if (line instanceof MutableColumn) {
            sortByColumn((MutableColumn<T>) line, comparator);
        } else if (line instanceof MutableRow) {
            sortByRow((MutableRow<T>) line, comparator);
        } else {
            throw new IllegalArgumentException("unknown line type: " + line);
        }
    }

    private static <T> void sortByColumn(MutableColumn<T> column, Comparator<? super T> comparator) {
        int rowSize = column.getMatrix().getRowSize();
        if (rowSize <= 1) return;
        quickSort(new ColumnSortable<>(column, comparator), 0, rowSize - 1);
    }

    private static <T> void sortByRow(MutableRow<T> row, Comparator<? super T> comparator) {
        int columnSize = row.getMatrix().getColumnSize();
        if (columnSize <= 1) return;
        quickSort(new RowSortable<>(row, comparator), 0, columnSize - 1);
    }


    public static String toString(Matrix<?> matrix) {
        checkArgument(matrix == null, "matrix cannot be null");
        return matrix.getClass().getSimpleName() + "[" + matrix.getRowSize() + ":" + matrix.getColumnSize() + "]";
    }

    public static int hashCode(Matrix<?> matrix) {
        checkArgument(matrix == null, "matrix cannot be null");

        int h = ((Integer) matrix.getRowSize()).hashCode() ^ ((Integer) matrix.getColumnSize()).hashCode();
        for (Row<?> row : matrix.rows()) {
            for (Object value : row) {
                h += value == null ? 0 : value.hashCode();
            }
        }
        return h;
    }

    public static boolean equals(Matrix<?> first, Matrix<?> second) {
        if (first == second) return true;
        if (first == null || second == null) return false;
        if (first.getRowSize() != second.getRowSize()) return false;
        if (first.getColumnSize() != second.getColumnSize()) return false;

        Iterator<? extends Row<?>> firstRowsIter = first.rows().iterator();
        Iterator<? extends Row<?>> secondRowsIter = second.rows().iterator();
        while (firstRowsIter.hasNext() && secondRowsIter.hasNext()) {
            Iterator<?> firstValueIter = firstRowsIter.next().iterator();
            Iterator<?> secondValueIter = secondRowsIter.next().iterator();
            while (firstValueIter.hasNext() && secondValueIter.hasNext()) {
                Object firstValue = firstValueIter.next();
                Object secondValue = secondValueIter.next();
                if (!valueEquals(firstValue, secondValue)) {
                    return false;
                }
            }
            if (firstValueIter.hasNext() || secondValueIter.hasNext()) {
                return false;
            }
        }

        return firstRowsIter.hasNext() == secondRowsIter.hasNext();
    }

    private static boolean valueEquals(Object first, Object second) {
        if (first == second) return true;
        if (first == null || second == null) return false;
        //
        return first.equals(second);
    }

    private static <T> void quickSort(Sortable<T> sortable, int low, int high) {
        if (low >= high)
            return;

        // pick the pivot
        int middle = low + (high - low) / 2;
        T pivot = sortable.get(middle);

        // make left < pivot and right > pivot
        int i = low, j = high;
        while (i <= j) {
            while (sortable.compare(sortable.get(i), pivot) < 0) {
                i++;
            }

            while (sortable.compare(sortable.get(j), pivot) > 0) {
                j--;
            }

            if (i <= j) {
                sortable.swap(i, j);
                i++;
                j--;
            }
        }

        if (low < j)
            quickSort(sortable, low, j);

        if (high > i)
            quickSort(sortable, i, high);
    }

    private interface Sortable<T> {
        T get(int i);

        void swap(int i, int j);

        int compare(T first, T second);
    }

    private static class ColumnSortable<T> extends LineSortable<T, MutableColumn<T>> {

        protected ColumnSortable(MutableColumn<T> line, Comparator<? super T> comparator) {
            super(line, comparator);
        }

        @Override
        protected void swap(MutableColumn<T> line, int i, int j) {
            line.getMatrix().swapRow(i, j);
        }
    }

    private static class RowSortable<T> extends LineSortable<T, MutableRow<T>> {

        protected RowSortable(MutableRow<T> line, Comparator<? super T> comparator) {
            super(line, comparator);
        }

        @Override
        protected void swap(MutableRow<T> line, int i, int j) {
            line.getMatrix().swapColumn(i, j);
        }
    }

    private static abstract class LineSortable<T, E extends MutableLine<T>> implements Sortable<T> {
        private final E line;
        private final Comparator<? super T> comparator;

        protected LineSortable(E line, Comparator<? super T> comparator) {
            this.line = line;
            this.comparator = comparator;
        }

        protected abstract void swap(E line, int i, int j);

        @Override
        public T get(int i) {
            return line.get(i);
        }

        @Override
        public void swap(int i, int j) {
            swap(line, i, j);
        }

        @Override
        public int compare(T first, T second) {
            return comparator.compare(first, second);
        }
    }

    private static class NaturalComparator implements Comparator<Object> {
        public static final NaturalComparator INSTANCE = new NaturalComparator();

        private NaturalComparator() {
            //hide
        }

        @SuppressWarnings("unchecked")
        @Override
        public int compare(Object first, Object second) {
            //We want CCEs if these classes don't implement Comparable
            Comparable<Object> compFirst = (Comparable<Object>) first;
            Comparable<Object> secondFirst = (Comparable<Object>) second;
            return compFirst.compareTo(second);
        }
    }

}
