package nl.mplatvoet.collections.matrix;

public interface MutableCell<T> extends Cell<T> {
    void setValue(T value);

    void clear();
}
