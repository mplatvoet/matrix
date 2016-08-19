package nl.mplatvoet.collections.matrix.fn;

public final class Result<T> {
    private static final Object BLANK = new Object();

    private T value = blank();

    @SuppressWarnings("unchecked")
    private static <T> T blank() {
        return (T) BLANK;
    }

    public void clear() {
        value = blank();
    }

    public T getValue() {
        return isBlank() ? null : value;
    }

    public boolean isBlank() {
        return value == BLANK;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
