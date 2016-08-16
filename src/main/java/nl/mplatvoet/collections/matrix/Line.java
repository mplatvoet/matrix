package nl.mplatvoet.collections.matrix;

public interface Line<T> extends Iterable<T> {
    Matrix<T> getMatrix();

    T get(int idx);

    Cell<T> getCell(int idx);

    Iterable<Cell<T>> cells();
}
