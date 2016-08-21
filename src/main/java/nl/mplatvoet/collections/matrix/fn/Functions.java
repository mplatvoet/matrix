package nl.mplatvoet.collections.matrix.fn;

import nl.mplatvoet.collections.matrix.Cell;
import nl.mplatvoet.collections.matrix.MatrixCell;
import nl.mplatvoet.collections.matrix.MutableCell;
import nl.mplatvoet.collections.matrix.args.Arguments;

import static nl.mplatvoet.collections.matrix.args.Arguments.checkArgument;

public class Functions {

    private static final CellMapFunction<Object, Object> PASS_TROUGH_FUNCTION = new CellMapFunction<Object, Object>() {
        @Override
        public void apply(MatrixCell<Object> source, MutableCell<Object> dest) {
            if (!source.isBlank()) {
                dest.setValue(source.getValue());
            }
        }
    };

    public static <T, C extends MutableCell<T>> CellFunction<T, C> cellFunctionOf(ValueFunction<T, T> valueFunction) {
        return new ValueFunctionCellFunction<>(valueFunction);
    }

    public static <S, D> CellMapFunction<S, D> cellMapFunctionOf(ValueFunction<S, D> valueFunction) {
        return new ValueFunctionCellMapFunction(valueFunction);
    }

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
            checkArgument(target == null, "target cannot be null");
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

    private static class ValueFunctionCellMapFunction<T, R> implements CellMapFunction<T, R> {
        private final ValueFunction<T, R> valueFunction;

        private ValueFunctionCellMapFunction(ValueFunction<T, R> valueFunction) {
            checkArgument(valueFunction == null, "valueFunction cannot be null");
            this.valueFunction = valueFunction;
        }

        @Override
        public void apply(MatrixCell<T> source, MutableCell<R> dest) {
            R result = valueFunction.apply(source.getRowIndex(), source.getColumnIndex(), source.getValue());
            dest.setValue(result);
        }
    }

    private static class ValueFunctionCellFunction<T, C extends MutableCell<T>> implements CellFunction<T, C> {
        private final ValueFunction<T, T> valueFunction;

        private ValueFunctionCellFunction(ValueFunction<T, T> valueFunction) {
            checkArgument(valueFunction == null, "valueFunction cannot be null");
            this.valueFunction = valueFunction;
        }

        @Override
        public void apply(C cell) {
            T result = valueFunction.apply(cell.getRowIndex(), cell.getColumnIndex(), cell.getValue());
            cell.setValue(result);
        }
    }
}
