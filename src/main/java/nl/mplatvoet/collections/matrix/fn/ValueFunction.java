package nl.mplatvoet.collections.matrix.fn;

public interface ValueFunction<T, R> {
    R apply(int row, int column, T value);
}
