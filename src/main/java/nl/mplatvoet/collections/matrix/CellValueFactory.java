package nl.mplatvoet.collections.matrix;

public interface CellValueFactory<T> {
    T create(int row, int column);
}
