package nl.mplatvoet.collections.matrix;

public interface MatrixCell<T> extends Cell<T> {
    Matrix<T> getMatrix();

    Row<T> getRow();

    Column<T> getColumn();
}
