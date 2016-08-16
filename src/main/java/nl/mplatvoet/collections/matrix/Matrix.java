package nl.mplatvoet.collections.matrix;


public interface Matrix<T> {
    Cell<T> getCell(int row, int column);

    T get(int row, int column);

    Row<T> getRow(int row);

    Column<T> getColumn(int column);

    Iterable<Row<T>> rows();

    Iterable<Column<T>> columns();

    Matrix<T> map();

    <R> Matrix<R> map(MatrixFunction<? super T, ? extends R> function);

    Matrix<T> map(int rowBeginIdx, int rowEndIdx, int columnBeginIdx, int columnEndIdx);

    <R> Matrix<R> map(int rowBeginIdx, int rowEndIdx, int columnBeginIdx, int columnEndIdx, MatrixFunction<? super T, ? extends R> function);

    int getRowSize();

    int getColumnSize();


    interface Cell<T> {
        Matrix<T> getMatrix();

        Row<T> getRow();

        Column<T> getColumn();

        T getValue();

        int getColumnIndex();

        int getRowIndex();

        boolean isBlank();
    }

    interface Line<T> extends Iterable<T> {
        Matrix<T> getMatrix();

        T get(int idx);

        Cell<T> getCell(int idx);

        Iterable<Cell<T>> cells();
    }

    interface Column<T> extends Line<T> {
        int getColumnIndex();
    }

    interface Row<T> extends Line<T> {
        int getRowIndex();
    }
}
