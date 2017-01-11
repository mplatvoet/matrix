package nl.mplatvoet.collections.matrix;


import nl.mplatvoet.collections.matrix.fn.CellMapFunction;

public interface MutableLine<T> extends Line<T> {
    MutableMatrix<T> getMatrix();

    MutableMatrixCell<T> getCell(int idx);

    T put(int idx, T value);

    void clear();

    void cells(CellMapFunction<T, T> function);

    Iterable<MutableMatrixCell<T>> mutableCells();
}
