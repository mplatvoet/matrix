package nl.mplatvoet.collections.matrix;

import nl.mplatvoet.collections.matrix.args.Arguments;
import nl.mplatvoet.collections.matrix.fn.Function;
import nl.mplatvoet.collections.matrix.range.Range;

import java.util.Iterator;


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


    public static String toString(Matrix<?> matrix) {
        Arguments.checkArgument(matrix == null, "matrix cannot be null");
        return matrix.getClass().getName() + "[" + matrix.getRowSize() + ":" + matrix.getColumnSize() + "]";
    }

    public static int hashCode(Matrix<?> matrix) {
        Arguments.checkArgument(matrix == null, "matrix cannot be null");

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
}
