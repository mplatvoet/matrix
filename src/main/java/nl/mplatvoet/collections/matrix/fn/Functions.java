package nl.mplatvoet.collections.matrix.fn;

import nl.mplatvoet.collections.matrix.MatrixCell;
import nl.mplatvoet.collections.matrix.MutableCell;

import java.util.function.Function;

import static nl.mplatvoet.collections.matrix.args.Arguments.checkArgument;

public class Functions {

    private static final CellMapFunction<Object, Object> PASS_TROUGH_FUNCTION = (source, dest) -> {
        if (!source.isBlank()) {
            dest.setValue(source.getValue());
        }
    };

    public static <T, C extends MutableCell<T>> Function<C, T> cellFunctionOf(Function<T, T> valueFunction) {
        return new ValueFunction<>(valueFunction);
    }

    public static <S, D> CellMapFunction<S, D> cellMapFunctionOf(Function<S, D> valueFunction) {
        return new ValueFunctionCellMapFunction<>(valueFunction);
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
        private final Function<T, R> function;

        private ValueFunctionCellMapFunction(Function<T, R> function) {
            checkArgument(function == null, "function cannot be null");
            this.function = function;
        }

        @Override
        public void apply(MatrixCell<T> source, MutableCell<R> dest) {
            R result = function.apply(source.getValue());
            dest.setValue(result);
        }
    }

    private static class ValueFunction<T, C extends MutableCell<T>> implements Function<C, T> {
        private final Function<T, T> function;

        private ValueFunction(Function<T, T> valueFunction) {
            checkArgument(valueFunction == null, "function cannot be null");
            this.function = valueFunction;
        }

        @Override
        public T apply(C cell) {
            T result = function.apply(cell.getValue());
            cell.setValue(result);
            return result;
        }
    }
}
