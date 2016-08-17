package nl.mplatvoet.collections.matrix;


import nl.mplatvoet.collections.matrix.fn.Function;

public interface MutableMatrix<T> extends Matrix<T> {

    MutableCell<T> getCell(int row, int column);

    T get(int row, int column);

    MutableRow<T> getRow(int row);

    MutableColumn<T> getColumn(int column);

    Iterable<Row<T>> rows();

    Iterable<MutableRow<T>> mutableRows();

    Iterable<Column<T>> columns();

    Iterable<MutableColumn<T>> mutableColumns();

    T put(int row, int column, T value);

    void putAll(Matrix<? extends T> matrix);

    void putAll(Matrix<? extends T> matrix, int rowOffset, int columnOffset);

    MutableRow<T> insertRowBefore(int row);

    MutableRow<T> insertRowAfter(int row);

    MutableRow<T> insertRowBefore(Row<T> row);

    MutableRow<T> insertRowAfter(Row<T> row);

    MutableColumn<T> insertColumnBefore(int column);

    MutableColumn<T> insertColumnAfter(int column);

    MutableColumn<T> insertColumnBefore(Column<T> column);

    MutableColumn<T> insertColumnAfter(Column<T> column);

    void deleteRow(int row);

    void deleteRow(Row<T> row);

    void deleteColumn(int column);

    void deleteColumn(Column<T> column);

    void clear();

    void fill(Function<? super T, ? extends T> function);

    void fillBlanks(Function<? super T, ? extends T> function);

    MutableMatrix<T> map();

    <R> MutableMatrix<R> map(Function<? super T, ? extends R> function);

    MutableMatrix<T> map(int rowBeginIdx, int rowEndIdx, int columnBeginIdx, int columnEndIdx);

    <R> MutableMatrix<R> map(int rowBeginIdx, int rowEndIdx, int columnBeginIdx, int columnEndIdx, Function<? super T, ? extends R> function);

}
