package nl.mplatvoet.collections.matrix.args;

public final class Arguments {
    private Arguments() {
        //no instances allowed
    }

    public static void checkArgument(boolean invalid, String format, Object ... args) {
        if (invalid) {
            throw new IllegalArgumentException(String.format(format, args));
        }
    }

    public static void checkIndex(boolean invalid, String format, Object ... args) {
        if (invalid) {
            throw new IndexOutOfBoundsException(String.format(format, args));
        }
    }

    public static void checkState(boolean invalid, String format, Object ... args) {
        if (invalid) {
            throw new IllegalStateException(String.format(format, args));
        }
    }
}
