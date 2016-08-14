package nl.mplatvoet.collections.matrix;


public interface Matrix<T> {
    Cell<T> getCell(int row, int column);

    T get(int row, int column);

    Row<T> getRow(int row);

    Column<T> getColumn(int column);

    Iterable<Row<T>> rows();

    Iterable<Column<T>> columns();

    T put(int row, int column, T value);

    void putAll(Matrix<? extends T> matrix);

    void putAll(Matrix<? extends T> matrix, int rowOffset, int columnOffset);

    Row<T> insertRowBefore(int row);

    Row<T> insertRowAfter(int row);

    Row<T> insertRowBefore(Row<T> row);

    Row<T> insertRowAfter(Row<T> row);

    Column<T> insertColumnBefore(int column);

    Column<T> insertColumnAfter(int column);

    Column<T> insertColumnBefore(Column<T> column);

    Column<T> insertColumnAfter(Column<T> column);


    void deleteRow(int row);

    void deleteRow(Row<T> row);

    void deleteColumn(int column);

    void deleteColumn(Column<T> column);

    Matrix<T> subMatrix(int rowBeginIdx, int rowEndIdx, int columnBeginIdx, int columnEndIdx);

    void clear();

    void fill(MatrixFunction<? super T, ? extends T> function);

    void fillBlanks(MatrixFunction<? super T, ? extends T> function);

    <R> Matrix<R> map(MatrixFunction<? super T, ? extends R> function);

    Matrix<T> shallowCopy();

    int getRowSize();

    int getColumnSize();


    interface Cell<T> {
        Matrix<T> getMatrix();

        Row<T> getRow();

        Column<T> getColumn();

        T getValue();

        void setValue(T value);

        void clear();

        int getColumnIndex();

        int getRowIndex();

        boolean isBlank();
    }

    interface Line<T> extends Iterable<T> {
        Matrix<T> getMatrix();

        T get(int idx);

        Cell<T> getCell(int idx);

        T put(int idx, T value);

        void clear();

        void fill(MatrixFunction<? super T, ? extends T> function);

        void fillBlanks(MatrixFunction<? super T, ? extends T> function);

        Iterable<Cell<T>> cells();
    }

    interface Column<T> extends Line<T> {
        int getColumnIndex();
    }

    interface Row<T> extends Line<T> {
        int getRowIndex();
    }
}
