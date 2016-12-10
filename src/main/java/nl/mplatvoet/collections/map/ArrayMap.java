package nl.mplatvoet.collections.map;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class ArrayMap<V> implements IntKeyMap<V>, Serializable, Cloneable {

    /**
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private static final int DEFAULT_CAPACITY = 10;
    private static final Object NULL_MARKER = new Object();

    private transient Object[] entries;
    private transient int size = 0;

    private transient EntrySet entrySet = null;
    private transient KeySet keySet = null;
    private transient ValuesCollection valuesCollection = null;

    public ArrayMap() {
        this(DEFAULT_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public ArrayMap(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity must be >= 0");
        }
        if (initialCapacity > MAX_ARRAY_SIZE) {
            throw new IllegalArgumentException("initialCapacity exceeds maximum capacity: " + MAX_ARRAY_SIZE);
        }
        initialCapacity = Math.max(initialCapacity, DEFAULT_CAPACITY);
        entries = new Object[initialCapacity];
    }

    private Object mask(Object value) {
        return value == null ? NULL_MARKER : value;
    }

    @SuppressWarnings("unchecked")
    private V unmask(Object value) {
        if (value == NULL_MARKER) {
            return null;
        }
        if (value instanceof ArrayMap.KeyEntry) {
            return ((ArrayMap<V>.KeyEntry) value).getValue();
        }
        return (V) value;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof Integer) {
            int idx = (Integer) key;
            return containsKey(idx);
        }
        return false;
    }

    @Override
    public boolean containsKey(int idx) {
        return idx >= 0 && idx < entries.length && entries[idx] != null;
    }

    @Override
    public boolean containsValue(Object value) {
        Object masked = mask(value);
        for (Object entry : entries) {
            if (entry != null) {
                if (entry == masked) return true;
                if (entry instanceof ArrayMap.KeyEntry) {
                    entry = ((ArrayMap.KeyEntry)entry).getValue();
                }
                if (entry.equals(masked)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        if (key instanceof Integer) {
            int idx = (Integer) key;
            return get(idx);
        }
        return null;
    }

    @Override
    public V get(int idx) {
        if (idx >= 0 && idx < entries.length) {
            Object entry = entries[idx];
            return unmask(entry);
        }
        return null;
    }

    @Override
    public V put(Integer key, V value) {
        if (key == null) {
            throw new NullPointerException("key can not be null");
        }
        int idx = key;
        return put(idx, value);
    }

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
        }
        return unmask(previous);
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


    @Override
    public V remove(Object key) {
        if (key instanceof Integer) {
            int idx = (Integer) key;
            return remove(idx);
        }
        return null;
    }

    @Override
    public V remove(int idx) {
        Object currentValue = removeRaw(idx);
        return currentValue == null ? null : unmask(currentValue);
    }

    private Object removeRaw(int idx) {
        if (idx >= 0 && idx < entries.length) {
            Object entry = entries[idx];
            entries[idx] = null;
            if (entry != null) {
                --size;
            }
            return unmask(entry);
        }
        return null;
    }




    @Override
    public void putAll(Map<? extends Integer, ? extends V> m) {
        for (Entry<? extends Integer, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        Arrays.fill(entries, null);
        size = 0;
    }

    @Override
    public Set<Integer> keySet() {
        if (keySet == null) {
            keySet = new KeySet();
        }
        return keySet;
    }

    @Override
    public Collection<V> values() {
        if (valuesCollection == null) {
            valuesCollection = new ValuesCollection();
        }
        return valuesCollection;
    }

    @Override
    public Set<Entry<Integer, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    private int maxSetIndex(Object[] entries) {
        for (int i = entries.length - 1; i >= 0; --i) {
            if (entries[i] != null) return i;
        }
        return -1;
    }

    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        final int size = this.size;
        s.defaultWriteObject();
        s.writeInt(size);

        if (size > 0) {
            final Object[] entries = this.entries;
            final int requiredCapacity = maxSetIndex(entries) + 1;

            s.writeInt(requiredCapacity);

            int written = 0;
            for (int i = 0; i < entries.length && written < size; ++i) {
                final Object entry = entries[i];
                if (entry == null) continue;
                final V unmasked = unmask(entry);
                if (unmasked == null) {
                    s.writeInt(i == 0 ? Integer.MIN_VALUE : -i);
                } else {
                    s.writeInt(i);
                    s.writeObject(unmasked);
                }
                ++written;
            }
        }
    }


    private void readObject(java.io.ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        final int size = s.readInt();
        if (size == 0) {
            this.entries = new Object[DEFAULT_CAPACITY];
        } else {
            final int requiredCapacity = s.readInt();
            this.size = size;
            this.entries = new Object[requiredCapacity];

            for (int i = 0; i < size; i++) {
                final int idx = s.readInt();
                if (idx < 0) {
                    entries[idx == Integer.MIN_VALUE ? 0 : -idx] = NULL_MARKER;
                } else {
                    entries[idx] = s.readObject();
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        ArrayMap<V> result;
        try {
            result = (ArrayMap<V>) super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }

        result.size = this.size;
        Object[] src = this.entries;
        result.entries = new Object[maxSetIndex(src) +1];


        int cloned = 0;
        for (int i = 0; i < src.length && cloned < size; ++i) {
            final Object entry = src[i];
            if (entry == null) continue;

            //mask(unmask(entry)) will unwrap any Entry instance
            result.entries[i] = mask(unmask(entry));

            ++cloned;
        }

        return result;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Map))
            return false;
        Map<?,?> m = (Map<?,?>) o;
        if (m.size() != size())
            return false;

        try {
            for (Entry<Integer, V> e : entrySet()) {
                int key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(m.get(key)))
                        return false;
                }
            }
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int h = 0;
        for (Entry<Integer, V> integerVEntry : entrySet()) h += integerVEntry.hashCode();
        return h;
    }

    public String toString() {
        Iterator<Entry<Integer,V>> i = entrySet().iterator();
        if (! i.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            Entry<Integer,V> e = i.next();
            int key = e.getKey();
            V value = e.getValue();
            sb.append(key);
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value);
            if (! i.hasNext())
                return sb.append('}').toString();
            sb.append(',').append(' ');
        }
    }


    private class KeyEntry implements Map.Entry<Integer, V> {
        private final int key;
        private V value;

        private KeyEntry(int key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Integer getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V current = this.value;
            this.value = value;
            return current;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof Entry) {
                Entry entry = (Entry) other;
                if (((Integer) this.key).equals(entry.getKey())) {
                    Object otherValue = entry.getValue();
                    return (value == null && otherValue == null) ||
                            (value != null && value.equals(otherValue));
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return ((Integer) key).hashCode() ^ (value == null ? 0 : value.hashCode());
        }
    }

    private class ValuesCollection extends AbstractCollection<V> {
        @Override
        public boolean contains(Object o) {
            return containsValue(o);
        }

        @Override
        public Iterator<V> iterator() {
            return new ValuesIterator();
        }

        @Override
        public Object[] toArray() {
            return toArray(new Object[size()]);
        }

        @Override
        public boolean remove(Object o) {
            Object masked = mask(o);
            Object[] entries = ArrayMap.this.entries;
            for (int i = 0; i < entries.length; i++) {
                Object entry = entries[i];
                if (entry != null) {
                    if (entry == masked) return true;
                    if (entry instanceof ArrayMap.KeyEntry) {
                        entry = ((ArrayMap.KeyEntry)entry).getValue();
                    }
                    if (entry.equals(masked)) {
                        ArrayMap.this.remove(i);
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            if (c == null) throw new NullPointerException();
            if (c == this) return false;

            boolean altered = false;
            Object[] entries = ArrayMap.this.entries;
            for (int i = 0; i < entries.length; i++) {
                Object entry = entries[i];
                if (entry == null) continue;
                Object unmasked = unmask(entry);
                if (!c.contains(unmasked)) {
                    ArrayMap.this.remove(i);
                    altered = true;
                }
            }
            return altered;
        }


        @Override
        V valueOf(int key, V value) {
            return value;
        }
    }

    private abstract class AbstractSet<T> extends AbstractCollection<T> implements Set<T> {

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;

            if (!(o instanceof Set))
                return false;
            Collection<?> c = (Collection<?>) o;
            if (c.size() != size())
                return false;
            try {
                return containsAll(c);
            } catch (ClassCastException | NullPointerException unused)   {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int h = 0;
            Iterator<T> i = iterator();
            while (i.hasNext()) {
                T obj = i.next();
                if (obj != null)
                    h += obj.hashCode();
            }
            return h;
        }
    }

    private abstract class AbstractCollection<T> implements Collection<T> {

        @Override
        public int size() {
            return ArrayMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return ArrayMap.this.isEmpty();
        }

        @Override
        public Object[] toArray() {
            return toArray(new Object[size()]);
        }

        abstract T valueOf(int key, V value);

        @Override
        @SuppressWarnings("unchecked")
        public <E> E[] toArray(E[] a) {
            final int size = size();


            E[] result = a.length >= size ? a :
                    (E[]) java.lang.reflect.Array
                            .newInstance(a.getClass().getComponentType(), size);

            int idx = 0;
            Object[] entries = ArrayMap.this.entries;
            for (int key = 0; key < entries.length && idx < size; ++key) {
                Object entry = entries[key];
                if (entry != null) {
                    result[idx] = (E) valueOf(key, unmask(entry));
                    ++idx;
                }
            }

            for (; idx < result.length; ++idx) {
                result[idx] = null;
            }

            return result;

        }

        @Override
        public boolean add(T element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            //noinspection ConstantConditions
            if (c == null) {
                throw new NullPointerException(); //according to spec
            }
            for (Object o : c) {
                if (!contains(o)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            if (c == null) throw new NullPointerException();

            boolean removed = false;
            for (Object o : c) {
                removed |= remove(o);
            }
            return removed;

        }

        @Override
        public void clear() {
            ArrayMap.this.clear();
        }

        public String toString() {
            Iterator<T> it = iterator();
            if (! it.hasNext())
                return "[]";

            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (;;) {
                T e = it.next();
                sb.append(e == this ? "(this Collection)" : e);
                if (! it.hasNext())
                    return sb.append(']').toString();
                sb.append(',').append(' ');
            }
        }
    }

    private class KeySet extends AbstractSet<Integer> {
        @Override
        public boolean contains(Object o) {
            if (o == null) {
                throw new NullPointerException(); //according to spec
            }

            Integer idx = (Integer) o; // potential CCE is according to spec
            return idx >= 0 && idx < ArrayMap.this.entries.length && ArrayMap.this.entries[idx] != null;
        }

        @Override
        public Iterator<Integer> iterator() {
            return new KeyIterator();
        }

        @Override
        public boolean remove(Object o) {
            if (o == null) throw new NullPointerException();

            int key = (int) o;
            return ArrayMap.this.removeRaw(key) != null;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            if (c == null) throw new NullPointerException();
            if (c == this) return false;

            boolean altered = false;
            Object[] entries = ArrayMap.this.entries;
            for (int i = 0; i < entries.length; i++) {
                Object entry = entries[i];
                if (entry == null) continue;

                if (!c.contains(i)) {
                    ArrayMap.this.remove(i);
                    altered = true;
                }
            }
            return altered;
        }


        @Override
        Integer valueOf(int key, V value) {
            return key;
        }
    }

    private class EntrySet extends AbstractSet<Entry<Integer, V>>  {

        @Override
        public Iterator<Entry<Integer, V>> iterator() {
            return new ArrayIterator();
        }

        @Override
        public Object[] toArray() {
            return toArray(new Object[ArrayMap.this.size]);
        }

        @Override
        Entry<Integer, V> valueOf(int key, V value) {
            return getEntry(key, value);
        }

        @SuppressWarnings("unchecked")
        private Entry<Integer, V> getEntry(int idx, Object currentEntry) {
            if (currentEntry instanceof ArrayMap.KeyEntry) {
                return (Entry<Integer, V>) currentEntry;
            }
            V value = currentEntry == NULL_MARKER ? null : (V) currentEntry;
            final KeyEntry keyEntry = new KeyEntry(idx, value);
            ArrayMap.this.entries[idx] = keyEntry;
            return keyEntry;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <E> E[] toArray(E[] a) {
            final int size = size();


            E[] result = a.length >= size ? a :
                    (E[]) java.lang.reflect.Array
                            .newInstance(a.getClass().getComponentType(), size);

            int idx = 0;
            Object[] entries = ArrayMap.this.entries;
            for (int key = 0; key < entries.length && idx < size; ++key) {
                Object entry = entries[key];
                if (entry != null) {
                    result[idx] = (E) getEntry(key, entry);
                    ++idx;
                }
            }

            for (; idx < result.length; ++idx) {
                result[idx] = null;
            }

            return result;

        }

        @Override
        public boolean remove(Object o) {
            if (o == null) {
                throw new NullPointerException(); // according to spec
            }
            Entry entry = (Entry) o; // potential CCE is according to spec
            if (entry.getKey() instanceof Integer) {
                int idx = (int) entry.getKey();
                Object masked = mask(entry.getValue());
                Object[] entries = ArrayMap.this.entries;
                if (idx >= 0 && idx < entries.length) {
                    if (masked.equals(entries[idx])) {
                        ArrayMap.this.remove(idx);
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            //noinspection ConstantConditions
            if (c == null) {
                throw new NullPointerException(); //according to spec
            }
            if (c == this) return true;

            for (Object o : c) {
                if (!contains(o)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            //noinspection ConstantConditions
            if (c == null) {
                throw new NullPointerException(); // according to spec
            }
            if (c.isEmpty()) {
                int size = ArrayMap.this.size;
                ArrayMap.this.clear();
                return size > 0;
            }

            boolean modified = false;
            Object[] entries = ArrayMap.this.entries;
            for (int i = 0, length = entries.length; i < length; i++) {
                Object entry = entries[i];
                if (entry != null && !c.contains(getEntry(i, entry))) {
                    ArrayMap.this.remove(i);
                    modified = true;
                }
            }

            return modified;
        }

        @Override
        public boolean contains(Object o) {
            if (o == null) {
                throw new NullPointerException(); //according to spec
            }

            Entry entry = (Entry) o; // potential CCE is according to spec
            if (entry.getKey() instanceof Integer) {
                int idx = (int) entry.getKey();
                if (idx >= 0 && idx < ArrayMap.this.entries.length) {
                    return entry.equals(unmask(ArrayMap.this.entries[idx]));
                }
            }
            return false;
        }

        private class ArrayIterator extends AbstractArrayIterator<Entry<Integer, V>> {
            @Override
            Entry<Integer, V> valueOf(int key, V value) {
                return getEntry(key, value);
            }
        }
    }

    private class KeyIterator extends AbstractArrayIterator<Integer> {
        @Override
        Integer valueOf(int key, V value) {
            return key;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class ValuesIterator extends AbstractArrayIterator<V> {
        @Override
        V valueOf(int key, V value) {
            return value;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private abstract class AbstractArrayIterator<T> implements Iterator<T> {
        private int index = -1;
        private boolean removed = false;

        @Override
        public boolean hasNext() {
            Object[] entries = ArrayMap.this.entries;
            for (int i = index + 1; i < entries.length; ++i) {
                if (entries[i] != null) return true;
            }
            return false;
        }

        abstract T valueOf(int key, V value);

        @Override
        public T next() {
            Object[] entries = ArrayMap.this.entries;
            for (int i = index + 1; i < entries.length; ++i) {
                Object entry = entries[i];
                if (entry != null) {
                    removed = false;
                    index = i;
                    return valueOf(i, unmask(entry));
                }
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            if (index < 0) {
                throw new IllegalStateException("next() has not been called");
            }
            if (removed) {
                throw new IllegalStateException("remove() has already been called");
            }
            ArrayMap.this.remove(index);
            removed = true;
        }
    }


}
