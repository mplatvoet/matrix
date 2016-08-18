package nl.mplatvoet.collections.matrix.fn;

public class Functions {

    private static final Function<Object, Object> PASS_TROUGH_FUNCTION = new Function<Object, Object>() {
        @Override
        public Object apply(int row, int column, Object value) {
            return value;
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> Function<? super T, ? extends T> passTrough() {
        return (Function<? super T, ? extends T>) PASS_TROUGH_FUNCTION;
    }
}
