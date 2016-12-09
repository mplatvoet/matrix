package nl.mplatvoet.collections.matrix;

public interface MutableCell<T> extends Cell<T> {
    T setValue(T value);

    void clear();
}
