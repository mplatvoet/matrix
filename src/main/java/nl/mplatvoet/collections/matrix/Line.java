package nl.mplatvoet.collections.matrix;

public interface Line<T> extends Iterable<T> {
    Matrix<T> getMatrix();

    T get(int idx);

    MatrixCell<T> getCell(int idx);

    Iterable<MatrixCell<T>> cells();
}
