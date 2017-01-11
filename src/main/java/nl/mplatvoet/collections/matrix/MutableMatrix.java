package nl.mplatvoet.collections.matrix;


import nl.mplatvoet.collections.matrix.fn.CellMapFunction;

public interface MutableMatrix<T> extends Matrix<T> {

    default void consolidate() {
        consolidateRows();
        consolidateColumns();
    }

    void consolidateRows();

    void consolidateColumns();

    MutableMatrixCell<T> getCell(int row, int column);

    T get(int row, int column);

    MutableRow<T> getRow(int row);

    MutableColumn<T> getColumn(int column);

    Iterable<MutableRow<T>> mutableRows();

    Iterable<MutableColumn<T>> mutableColumns();

    T put(int row, int column, T value);

    void putAll(Matrix<? extends T> matrix);

    void putAll(Matrix<? extends T> matrix, int rowOffset, int columnOffset);

    MutableRow<T> insertRow(int row);

    MutableRow<T> insertRow(Row<T> row);

    MutableColumn<T> insertColumn(int column);

    MutableColumn<T> insertColumn(Column<T> column);

    void swapRow(int firstRow, int secondRow);

    void swapRow(Row<T> firstRow, Row<T> secondRow);

    void swapColumn(Column<T> firstColumn, Column<T> secondColumn);

    void swapColumn(int firstColumn, int secondColumn);

    void deleteRow(int row);

    void deleteRow(Row<T> row);

    void deleteColumn(int column);

    void deleteColumn(Column<T> column);

    void clear();

    void cells(CellMapFunction<T, T> map);
}
