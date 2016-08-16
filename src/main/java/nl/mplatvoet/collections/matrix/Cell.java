package nl.mplatvoet.collections.matrix;

public interface Cell<T> {
    Matrix<T> getMatrix();

    Row<T> getRow();

    Column<T> getColumn();

    T getValue();

    int getColumnIndex();

    int getRowIndex();

    boolean isBlank();
}
