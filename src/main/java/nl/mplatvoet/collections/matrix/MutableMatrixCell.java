package nl.mplatvoet.collections.matrix;

public interface MutableMatrixCell<T> extends MatrixCell<T>, MutableCell<T> {
    MutableMatrix<T> getMatrix();

    MutableRow<T> getRow();

    MutableColumn<T> getColumn();
}
