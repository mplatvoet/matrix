package nl.mplatvoet.collections.matrix;

public interface MutableLine<T> extends Line<T> {
    MutableMatrix<T> getMatrix();

    MutableCell<T> getCell(int idx);

    T put(int idx, T value);

    void clear();

    void fill(MatrixFunction<? super T, ? extends T> function);

    void fillBlanks(MatrixFunction<? super T, ? extends T> function);

    Iterable<MutableCell<T>> mutableCells();
}
