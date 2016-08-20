package nl.mplatvoet.collections.matrix;

import nl.mplatvoet.collections.matrix.args.Arguments;
import nl.mplatvoet.collections.matrix.fn.Function;
import nl.mplatvoet.collections.matrix.range.Range;

import java.util.Comparator;
import java.util.Iterator;

import static nl.mplatvoet.collections.matrix.args.Arguments.checkArgument;


public final class Matrices {
    private Matrices() {
        //no instances allowed
    }

    public static <T> MutableMatrix<T> mutableOf() {
        return IndexMatrix.of();
    }

    public static <T> MutableMatrix<T> mutableOf(int rows, int columns) {
        return IndexMatrix.of(rows, columns);
    }

    public static <T> MutableMatrix<T> mutableOf(int rows, int columns, Function<?, T> fill) {
        return IndexMatrix.of(rows, columns, fill);
    }

    public static <T> Matrix<T> of(int rows, int columns, Function<?, ? extends T> fill) {
        return ImmutableMatrix.of(rows, columns, fill);
    }


    public static <T> Matrix<T> copyOf(Matrix<? extends T> matrix) {
        return ImmutableMatrix.copyOf(matrix);
    }

    public static <T> Matrix<T> copyOf(Matrix<? extends T> matrix, Range range) {
        return ImmutableMatrix.copyOf(matrix, range);
    }

    public static <T, R> Matrix<R> copyOf(Matrix<? extends T> matrix, Function<? super T,  R> transform) {
        return ImmutableMatrix.copyOf(matrix, transform);
    }

    public static <T, R> Matrix<R> copyOf(Matrix<? extends T> matrix, Range range, Function<? super T,  R> transform) {
        return ImmutableMatrix.copyOf(matrix, range, transform);
    }

    public static <T> MutableMatrix<T> mutableCopyOf(Matrix<? extends T> matrix) {
        return IndexMatrix.copyOf(matrix);
    }

    public static <T> MutableMatrix<T> mutableCopyOf(Matrix<? extends T> matrix, Range range) {
        return IndexMatrix.copyOf(matrix, range);
    }

    public static <T, R> MutableMatrix<R> mutableCopyOf(Matrix<? extends T> matrix, Function<? super T,  R> transform) {
        return IndexMatrix.copyOf(matrix, transform);
    }

    public static <T, R> MutableMatrix<R> mutableCopyOf(Matrix<? extends T> matrix, Range range, Function<? super T,  R> transform) {
        return IndexMatrix.copyOf(matrix, range, transform);
    }


    public static <T> void sortByColumn(MutableColumn<T> column, Comparator<? super T> comparator) {
        checkArgument(column == null, "column cannot be null");
        checkArgument(comparator == null, "comparator cannot be null");
        int rowSize = column.getMatrix().getRowSize();
        if (rowSize <= 1) return;
        quickSort(column, comparator, 0, rowSize -1);
    }


    public static String toString(Matrix<?> matrix) {
        checkArgument(matrix == null, "matrix cannot be null");
        return matrix.getClass().getName() + "[" + matrix.getRowSize() + ":" + matrix.getColumnSize() + "]";
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

    private static <T> void quickSort(MutableColumn<T> column, Comparator<? super T> comparator, int low, int high) {
        if (low >= high)
            return;

        // pick the pivot
        int middle = low + (high - low) / 2;
        T pivot = column.get(middle);

        // make left < pivot and right > pivot
        int i = low, j = high;
        while (i <= j) {
            while (comparator.compare(column.get(i), pivot) < 0) {
                i++;
            }

            while (comparator.compare(column.get(j), pivot) > 0) {
                j--;
            }

            if (i <= j) {
                column.getMatrix().swapRow(i, j);
                i++;
                j--;
            }
        }

        if (low < j)
            quickSort(column, comparator, low, j);

        if (high > i)
            quickSort(column, comparator, i, high);
    }
}
