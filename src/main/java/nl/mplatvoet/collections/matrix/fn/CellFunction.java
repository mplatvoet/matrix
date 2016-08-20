package nl.mplatvoet.collections.matrix.fn;

import nl.mplatvoet.collections.matrix.Cell;

public interface CellFunction<T, C extends Cell<T>> {
    void apply(C cell);
}
