package nl.mplatvoet.collections.matrix;


import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import nl.mplatvoet.collections.matrix.args.Arguments;
import nl.mplatvoet.collections.matrix.fn.CellMapFunction;

public interface Matrix<T> {

    default Row<T> findFirstRow(Predicate<Cell<T>> predicate) {
        return tryFindFirstRow(predicate).get();
    }

    default Optional<Row<T>> tryFindFirstRow(Predicate<Cell<T>> predicate) {
        Arguments.checkArgument(predicate == null, "predicate cannot be null");
        for (Row<T> row : this.rows()) {
            for (Cell<T> cell : row.cells()) {
                if (predicate.apply(cell)) {
                    return Optional.of(row);
                }
            }
        }
        return Optional.absent();
    }


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
