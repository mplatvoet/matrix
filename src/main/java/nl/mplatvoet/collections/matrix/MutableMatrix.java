package nl.mplatvoet.collections.matrix;


import nl.mplatvoet.collections.matrix.fn.Function;
import nl.mplatvoet.collections.matrix.range.Range;

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

    MutableRow<T> insertRow(int row);

    MutableRow<T> insertRow(Row<T> row);

    MutableColumn<T> insertColumn(int column);

    MutableColumn<T> insertColumn(Column<T> column);

    void deleteRow(int row);

    void deleteRow(Row<T> row);

    void deleteColumn(int column);

    void deleteColumn(Column<T> column);

    void clear();

    void fill(Function<? super T, ? extends T> function);

    void fillBlanks(Function<? super T, ? extends T> function);
}
