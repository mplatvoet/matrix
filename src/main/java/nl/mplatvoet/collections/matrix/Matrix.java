package nl.mplatvoet.collections.matrix;


import nl.mplatvoet.collections.matrix.fn.CellMapFunction;
import nl.mplatvoet.collections.matrix.range.Range;

public interface Matrix<T> {
    MatrixCell<T> getCell(int row, int column);

    T get(int row, int column);

    Row<T> getRow(int row);

    Column<T> getColumn(int column);

    Iterable<Row<T>> rows();

    Iterable<Column<T>> columns();

    Matrix<T> map();

    <R> Matrix<R> map(CellMapFunction<T, R> map);

    Matrix<T> map(Range range);

    <R> Matrix<R> map(Range range, CellMapFunction<T, R> map);

    int getRowSize();

    int getColumnSize();
}
