package nl.mplatvoet.collections.matrix.fn;

public interface Function<T, R> {
    void apply(int row, int column, T value, DetachedCell<R> result);
}
