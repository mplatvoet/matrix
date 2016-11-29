package nl.mplatvoet.collections.map;

import java.util.Arrays;

public class ObjectArrayMap<V> implements IntKeyMap<V> {
    /**
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private static final int DEFAULT_CAPACITY = 10;
    private static final Object NULL_MARKER = new Object();

    private Object[] entries;

    private int size = 0;


    public ObjectArrayMap() {
        this(DEFAULT_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public ObjectArrayMap(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity must be >= 0");
        }
        if (initialCapacity > MAX_ARRAY_SIZE) {
            throw new IllegalArgumentException("initialCapacity exceeds maximum capacity: " + MAX_ARRAY_SIZE);
        }
        initialCapacity = Math.max(initialCapacity, DEFAULT_CAPACITY);
        entries = new Object[initialCapacity];
    }

    private Object mask(V value) {
        return value == null ? NULL_MARKER : value;
    }

    @SuppressWarnings("unchecked")
    private V unmask(Object value) {
        if (value == NULL_MARKER) {
            return null;
        }
        return (V) value;
    }

    @Override
    public int size() {
        return size;
    }


    @Override
    public boolean containsKey(int idx) {
        return idx >= 0 && idx < entries.length && entries[idx] != null;
    }


    @SuppressWarnings("unchecked")
    @Override
    public V get(int idx) {
        if (idx >= 0 && idx < entries.length) {
            return unmask(entries[idx]);
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    @Override
    public V put(int idx, V value) {
        if (idx < 0) {
            throw new IllegalArgumentException("key must be a positive number or 0, but was: " + idx);
        }
        ensureCapacity(idx + 1);

        Object previous = entries[idx];

        entries[idx] = mask(value);

        if (previous == null) {
            ++size;
            return null;
        } else {
            return unmask(previous);
        }
    }


    private void ensureCapacity(int minCapacity) {
        if (minCapacity - entries.length > 0) {
            int oldCapacity = entries.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0)
                newCapacity = minCapacity;
            if (newCapacity - MAX_ARRAY_SIZE > 0)
                throw new OutOfMemoryError();

            entries = Arrays.copyOf(entries, newCapacity);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(int idx) {
        if (idx >= 0 && idx < entries.length) {
            Object current = entries[idx];
            entries[idx] = null;
            if (current != null) {
                --size;
                return unmask(current);
            }
        }
        return null;
    }
}
