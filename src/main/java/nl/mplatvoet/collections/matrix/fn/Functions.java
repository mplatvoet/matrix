package nl.mplatvoet.collections.matrix.fn;

public class Functions {

    private static final Function<Object, Object> PASS_TROUGH_FUNCTION = new Function<Object, Object>() {
        @Override
        public void apply(int row, int column, Object value, DetachedCell<Object> result) {
            result.setValue(value);
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> Function<? super T, T> passTrough() {
        return (Function<? super T, T>) PASS_TROUGH_FUNCTION;
    }
}
