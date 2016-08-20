package nl.mplatvoet.collections.matrix.fn;

import nl.mplatvoet.collections.matrix.MatrixCell;
import nl.mplatvoet.collections.matrix.MutableCell;

public class Functions {

    private static final CellMapFunction<Object, Object> PASS_TROUGH_FUNCTION = new CellMapFunction<Object, Object>() {
        @Override
        public void apply(MatrixCell<Object> source, MutableCell<Object> dest) {
            if (!source.isBlank()) {
                dest.setValue(source.getValue());
            }
        }
    };

    @SuppressWarnings("unchecked")
    public static <S, D> CellMapFunction<S, D> passTrough() {
        return (CellMapFunction<S, D>) PASS_TROUGH_FUNCTION;
    }


    public static <S, D> CellMapFunction<S, D> noBlanks(CellMapFunction<S, D> target) {
        return new BlanksCellMapFunction<>(target, false);
    }

    public static <S, D> CellMapFunction<S, D> blanks(CellMapFunction<S, D> target) {
        return new BlanksCellMapFunction<>(target, true);
    }

    private static class BlanksCellMapFunction<S, D> implements CellMapFunction<S, D> {
        private final CellMapFunction<S, D> target;
        private final boolean blanks;

        private BlanksCellMapFunction(CellMapFunction<S, D> target, boolean blanks) {
            this.target = target;
            this.blanks = blanks;
        }

        @Override
        public void apply(MatrixCell<S> source, MutableCell<D> dest) {
            if (source.isBlank() == blanks) {
                target.apply(source, dest);
            }
        }
    }
}
