package nl.mplatvoet.collections.matrix.fn;

public interface Function<T, R> {
    R apply(int row, int column, T value);
}
