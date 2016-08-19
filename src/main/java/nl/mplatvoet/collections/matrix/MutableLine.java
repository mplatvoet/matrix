package nl.mplatvoet.collections.matrix;

import nl.mplatvoet.collections.matrix.fn.Function;

public interface MutableLine<T> extends Line<T> {
    MutableMatrix<T> getMatrix();

    MutableCell<T> getCell(int idx);

    T put(int idx, T value);

    void clear();

    void fill(Function<? super T, T> function);

    void fillBlanks(Function<? super T, T> function);

    Iterable<MutableCell<T>> mutableCells();
}
