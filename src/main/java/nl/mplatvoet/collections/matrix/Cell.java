package nl.mplatvoet.collections.matrix;

public interface Cell<T> {
    T getValue();

    int getColumnIndex();

    int getRowIndex();

    boolean isBlank();
}
