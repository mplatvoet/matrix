package nl.mplatvoet.collections.matrix;

import java.util.*;

public class IndexMap<V> implements Map<Integer, V> {

    /**
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private static final int DEFAULT_CAPACITY = 10;

    private ValueEntry<V>[] entries;

    private int size = 0;

    private EntrySet entrySet = null;
    private KeySet keySet = null;
    private ValuesCollection valuesCollection = null;

    public IndexMap() {
        this(DEFAULT_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public IndexMap(int initialCapacity) {
        if(initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity must be >= 0");
        }
        if(initialCapacity > MAX_ARRAY_SIZE) {
            throw new IllegalArgumentException("initialCapacity exceeds maximum capacity: " + MAX_ARRAY_SIZE);
        }
        initialCapacity = Math.max(initialCapacity, DEFAULT_CAPACITY);
        entries = new ValueEntry[initialCapacity];

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
            return idx >= 0 && idx < entries.length && entries[idx] != null;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        for (ValueEntry entry : entries) {
            if (entry != null && entry.containsValue(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        if (key instanceof Integer) {
            int idx = (Integer) key;
            if (idx >= 0 && idx < entries.length) {
                ValueEntry<V> entry = entries[idx];
                return entry == null ? null : entry.getValue();
            }
        }
        return null;
    }

    @Override
    public V put(Integer key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("key can not be null");
        }
        if (key < 0) {
            throw new IllegalArgumentException("key must be a positive number or 0, but was: " + key);
        }
        int idx = key;
        ensureCapacity(idx + 1);

        ValueEntry<V> previous = entries[idx];

        entries[idx] = new ValueEntry<>(idx, value);

        if (previous == null) {
            ++size;
            return null;
        } else {
            return previous.getValue();
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


    @Override
    public V remove(Object key) {
        if (key instanceof Integer) {
            int idx = (Integer) key;
            if (idx >= 0 && idx < entries.length) {
                ValueEntry<V> entry = entries[idx];
                entries[idx] = null;
                if (entry == null) {
                    return null;
                } else {
                    --size;
                    return entry.getValue();
                }
            }
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
        for (int i = 0; i < entries.length; ++i) {
            entries[i] = null;
        }
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


    private static class ValueEntry<V> implements Map.Entry<Integer, V> {
        private final int key;
        private V value;

        private ValueEntry(int key, V value) {
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
            V old = this.value;
            this.value = value;
            return old;
        }

        boolean containsValue(Object value) {
            if (this.value == null && value == null) {
                return true;
            }
            //
            return this.value != null && this.value.equals(value);
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

            for (ValueEntry<V> entry : IndexMap.this.entries) {
                if (entry != null) {
                    V value = entry.getValue();
                    if (value == null && o == null) {
                        return true;
                    }
                    if (value != null && value.equals(o)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public Iterator<V> iterator() {
            return new ValuesIterator();
        }

        @Override
        public Object[] toArray() {
            return toArray(new Object[IndexMap.this.size]);
        }

        @Override
        V valueOf(ValueEntry<V> entry) {
            return entry.getValue();
        }
    }

    private abstract class AbstractCollection<T> implements Collection<T> {

        @Override
        public int size() {
            return IndexMap.this.size;
        }

        @Override
        public boolean isEmpty() {
            return IndexMap.this.isEmpty();
        }

        @Override
        public Object[] toArray() {
            return toArray(new Object[IndexMap.this.size]);
        }

        abstract T valueOf(ValueEntry<V> entry);

        @Override
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            final int size = IndexMap.this.size;


            T[] result = a.length >= size ? a :
                    (T[]) java.lang.reflect.Array
                            .newInstance(a.getClass().getComponentType(), size);

            int idx = 0;
            for (int i = 0; i < IndexMap.this.entries.length && idx < size; ++i) {
                ValueEntry<V> entry = IndexMap.this.entries[i];
                if (entry != null) {
                    result[idx] = (T) valueOf(entry);
                    ++idx;
                }
            }

            for (; idx < size; ++idx) {
                result[idx] = null;
            }

            return result;

        }

        @Override
        public boolean add(T integer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
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
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }

    private class KeySet extends AbstractCollection<Integer> implements Set<Integer> {
        @Override
        public boolean contains(Object o) {
            if (o == null) {
                throw new NullPointerException(); //according to spec
            }

            Integer idx = (Integer) o; // potential CCE is according to spec
            return idx >= 0 && idx < IndexMap.this.entries.length && IndexMap.this.entries[idx] != null;
        }

        @Override
        public Iterator<Integer> iterator() {
            return new KeyIterator();
        }


        @Override
        Integer valueOf(ValueEntry<V> entry) {
            return entry.getKey();
        }
    }

    private class EntrySet extends AbstractCollection<Entry<Integer, V>> implements Set<Entry<Integer, V>> {

        @Override
        public Iterator<Entry<Integer, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public Object[] toArray() {
            return toArray(new Object[IndexMap.this.size]);
        }

        @Override
        Entry<Integer, V> valueOf(ValueEntry<V> entry) {
            return entry;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            final int size = IndexMap.this.size;


            T[] result = a.length >= size ? a :
                    (T[]) java.lang.reflect.Array
                            .newInstance(a.getClass().getComponentType(), size);

            int idx = 0;
            for (int i = 0; i < IndexMap.this.entries.length && idx < size; ++i) {
                ValueEntry<V> entry = IndexMap.this.entries[i];
                if (entry != null) {
                    result[idx] = (T) entry;
                    ++idx;
                }
            }

            for (; idx < size; ++idx) {
                result[idx] = null;
            }

            return result;

        }

        @Override
        public boolean add(Entry<Integer, V> entry) {
            IndexMap.this.put(entry.getKey(), entry.getValue());
            return true;
        }

        @Override
        public boolean remove(Object o) {
            if (o == null) {
                throw new NullPointerException(); // according to spec
            }
            Entry entry = (Entry) o; // potential CCE is according to spec
            if (entry.getKey() instanceof Integer) {
                int idx = (int) entry.getKey();
                if (idx >= 0 && idx < IndexMap.this.entries.length) {
                    if (entry.equals(IndexMap.this.entries[idx])) {
                        IndexMap.this.remove(idx);
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
            for (Object o : c) {
                if (!contains(o)) {
                    return false;
                }
            }
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends Entry<Integer, V>> c) {
            //noinspection ConstantConditions
            if (c == null) {
                throw new NullPointerException(); //according to spec
            }
            boolean all = true;
            for (Entry<Integer, V> entry : c) {
                all &= add(entry);
            }
            return all;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            //noinspection ConstantConditions
            if (c == null) {
                throw new NullPointerException(); // according to spec
            }
            if (c.isEmpty()) {
                int size = IndexMap.this.size;
                IndexMap.this.clear();
                return size > 0;
            }

            boolean modified = false;
            for (ValueEntry<V> entry : IndexMap.this.entries) {
                if (entry != null && !c.contains(entry)) {
                    IndexMap.this.remove(entry.getKey());
                    modified = true;
                }
            }

            return modified;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            //noinspection ConstantConditions
            if (c == null) {
                throw new NullPointerException(); //according to spec
            }
            boolean all = true;
            for (Object o : c) {
                all &= remove(o);
            }
            return all;

        }

        @Override
        public boolean contains(Object o) {
            if (o == null) {
                throw new NullPointerException(); //according to spec
            }

            Entry entry = (Entry) o; // potential CCE is according to spec
            if (entry.getKey() instanceof Integer) {
                int idx = (int) entry.getKey();
                if (idx >= 0 && idx < IndexMap.this.entries.length) {
                    return entry.equals(IndexMap.this.entries[idx]);
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;

            if (!(o instanceof Collection))
                return false;

            Collection c = (Collection) o;
            if (c.size() != size())
                return false;
            try {
                return containsAll(c);
            } catch (ClassCastException unused) {
                return false;
            } catch (NullPointerException unused) {
                return false;
            }
        }

        public int hashCode() {
            int h = 0;
            for (ValueEntry<V> entry : IndexMap.this.entries) {
                if (entry != null) {
                    h += entry.hashCode();
                }
            }
            return h;
        }

        @Override
        public void clear() {
            IndexMap.this.clear();
        }
    }

    private class EntryIterator extends AbstractEntryIterator<Map.Entry<Integer, V>> {
        @Override
        Entry<Integer, V> valueOf(Entry<Integer, V> entry) {
            return entry;
        }
    }

    private class KeyIterator extends AbstractEntryIterator<Integer> {
        @Override
        Integer valueOf(Entry<Integer, V> entry) {
            return entry.getKey();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class ValuesIterator extends AbstractEntryIterator<V> {
        @Override
        V valueOf(Entry<Integer, V> entry) {
            return entry.getValue();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private abstract class AbstractEntryIterator<T> implements Iterator<T> {
        private int index = -1;
        private boolean removed = false;

        @Override
        public boolean hasNext() {
            for (int i = index + 1; i < IndexMap.this.entries.length; ++i) {
                if (IndexMap.this.entries[i] != null) return true;
            }
            return false;
        }

        abstract T valueOf(Entry<Integer, V> entry);

        @Override
        public T next() {
            for (int i = index + 1; i < IndexMap.this.entries.length; ++i) {
                ValueEntry<V> entry = IndexMap.this.entries[i];
                if (entry != null) {
                    removed = false;
                    index = i;
                    return valueOf(entry);
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
            IndexMap.this.remove(index);
            removed = true;
        }
    }


}
