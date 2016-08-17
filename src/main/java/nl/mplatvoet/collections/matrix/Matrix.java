package nl.mplatvoet.collections.matrix;


import nl.mplatvoet.collections.matrix.fn.Function;

public interface Matrix<T> {
    Cell<T> getCell(int row, int column);

    T get(int row, int column);

    Row<T> getRow(int row);

    Column<T> getColumn(int column);

    Iterable<Row<T>> rows();

    Iterable<Column<T>> columns();

    Matrix<T> map();

    <R> Matrix<R> map(Function<? super T, ? extends R> function);

    Matrix<T> map(int rowBeginIdx, int rowEndIdx, int columnBeginIdx, int columnEndIdx);

    <R> Matrix<R> map(int rowBeginIdx, int rowEndIdx, int columnBeginIdx, int columnEndIdx, Function<? super T, ? extends R> function);

    int getRowSize();

    int getColumnSize();


}
