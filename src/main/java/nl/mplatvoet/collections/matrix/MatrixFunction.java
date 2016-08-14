package nl.mplatvoet.collections.matrix;

public interface MatrixFunction<T, R> {
    R apply(int row, int column, T value);
}
