package nl.mplatvoet.collections.matrix;

import com.google.common.base.Optional;

public interface Line<T> extends Iterable<T> {
    default MatrixCell<T> firstValueCell() {
        return tryFirstValueCell().get();
    }

    default Optional<MatrixCell<T>> tryFirstValueCell() {
        for (MatrixCell<T> cell : this.cells()) {
            if (!cell.isBlank()) {
                return Optional.of(cell);
            }
        }
        return Optional.absent();
    }

    Matrix<T> getMatrix();

    T get(int idx);

    MatrixCell<T> getCell(int idx);

    Iterable<MatrixCell<T>> cells();
}
