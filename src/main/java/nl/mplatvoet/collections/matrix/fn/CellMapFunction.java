package nl.mplatvoet.collections.matrix.fn;

import nl.mplatvoet.collections.matrix.MatrixCell;
import nl.mplatvoet.collections.matrix.MutableCell;

public interface CellMapFunction<S, D> {
    void apply(MatrixCell<S> source, MutableCell<D> dest);
}
