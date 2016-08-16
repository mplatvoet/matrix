package nl.mplatvoet.collections.matrix;

public interface MutableCell<T> extends Cell<T> {
    MutableMatrix<T> getMatrix();

    MutableRow<T> getRow();

    MutableColumn<T> getColumn();

    void setValue(T value);

    void clear();
}
