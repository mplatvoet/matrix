package nl.mplatvoet.collections.matrix;


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

    void fill(MatrixFunction<? super T, ? extends T> function);

    void fillBlanks(MatrixFunction<? super T, ? extends T> function);

    MutableMatrix<T> map();

    <R> MutableMatrix<R> map(MatrixFunction<? super T, ? extends R> function);

    MutableMatrix<T> map(int rowBeginIdx, int rowEndIdx, int columnBeginIdx, int columnEndIdx);

    <R> MutableMatrix<R> map(int rowBeginIdx, int rowEndIdx, int columnBeginIdx, int columnEndIdx, MatrixFunction<? super T, ? extends R> function);

    interface MutableCell<T> extends Matrix.Cell<T> {
        MutableMatrix<T> getMatrix();

        MutableRow<T> getRow();

        MutableColumn<T> getColumn();

        void setValue(T value);

        void clear();
    }

    interface MutableLine<T> extends Line<T> {
        MutableMatrix<T> getMatrix();

        MutableCell<T> getCell(int idx);

        T put(int idx, T value);

        void clear();

        void fill(MatrixFunction<? super T, ? extends T> function);

        void fillBlanks(MatrixFunction<? super T, ? extends T> function);

        Iterable<MutableCell<T>> mutableCells();
    }

    interface MutableColumn<T> extends MutableLine<T>, Column<T> {

    }

    interface MutableRow<T> extends MutableLine<T>, Row<T> {

    }
}
