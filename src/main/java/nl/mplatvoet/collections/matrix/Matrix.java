package nl.mplatvoet.collections.matrix;


public interface Matrix<T>  {
    T put(int row, int column, T value);

    void fillBlanks(CellValueFactory<? extends T> factory);

    void putAll(Matrix<? extends T> matrix);

    void putAll(Matrix<? extends T> matrix, int rowOffset, int columnOffset);

    Matrix<T> subMatrix(int rowBeginIdx, int rowEndIdx, int columnBeginIdx, int columnEndIdx);

    Matrix.Cell<T> getCell(int row, int column);

    T get(int row, int column);

    Matrix.Row<T> getRow(int row);

    Iterable<Row<T>> rows();

    Iterable<Column<T>> columns();

    Column<T> getColumn(int column);

    void clear();

    Matrix<T> shallowCopy();

    interface Cell<T> {
        Matrix<T> getMatrix();

        Matrix.Row<T> getRow();

        Column<T> getColumn();

        T getValue();

        void setValue(T value);

        void clear();

        int getColumnIndex();

        int getRowIndex();

        boolean isBlank();
    }

    interface Column<T> extends Iterable<T> {
        Matrix<T> getMatrix();

        int getColumnIndex();

        T get(int row);

        Matrix.Cell<T> getCell(int row);

        T put(int row, T value);

        void clear();

        void fillBlanks(CellValueFactory<? extends T> factory);

        Iterable<Cell<T>> cells();
    }

    interface Row<T> extends Iterable<T> {
        Matrix<T> getMatrix();

        int getRowIndex();

        T get(int column);

        Matrix.Cell<T> getCell(int column);

        T put(int column, T value);

        void clear();

        void fillBlanks(CellValueFactory<? extends T> factory);

        Iterable<Cell<T>> cells();
    }
}
