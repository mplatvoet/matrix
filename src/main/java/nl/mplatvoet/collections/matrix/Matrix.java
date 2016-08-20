package nl.mplatvoet.collections.matrix;


import nl.mplatvoet.collections.matrix.fn.Function;
import nl.mplatvoet.collections.matrix.range.Range;

public interface Matrix<T> {
    MatrixCell<T> getCell(int row, int column);

    T get(int row, int column);

    Row<T> getRow(int row);

    Column<T> getColumn(int column);

    Iterable<Row<T>> rows();

    Iterable<Column<T>> columns();

    Matrix<T> map();

    <R> Matrix<R> map(Function<? super T, R> function);

    Matrix<T> map(Range range);

    <R> Matrix<R> map(Range range, Function<? super T, R> function);

    int getRowSize();

    int getColumnSize();
}
