package nl.mplatvoet.collections.map;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class CompactArrayMap<V> implements IntKeyMap<V>, Serializable {
    private static final int DEFAULT_CAPACITY = 10;
    private final int startKey;
    private final int endKey;

    private transient ArrayHolder holder;

    private transient EntrySet entrySet = null;
    private transient KeySet keySet = null;
    private transient ValuesCollection valuesCollection = null;

    public CompactArrayMap() {
        this(DEFAULT_CAPACITY);
    }

    public CompactArrayMap(int initialCapacity) {
        this(new ArrayHolder(Math.max(initialCapacity, DEFAULT_CAPACITY)), 0, ArrayHolder.MAX_ARRAY_SIZE);
    }

    private CompactArrayMap(ArrayHolder holder, int startKey, int endKey) {
        this.holder = holder;
        this.startKey = startKey;
        this.endKey = endKey;
    }

    private void validateLegalRange(int idx) {
        if (!isLegalRange(idx)) {
            throw new IllegalArgumentException("idx must be " + startKey + " >= idx < " + endKey);
        }
    }

    private boolean isLegalRange(int idx) {
        return idx >= startKey && idx < endKey;
    }

    @Override
    public int size() {
        if (isBaseMap()) {
            return holder.size;
        }
        if (startKey == endKey - 1) {
            return 0;
        }

        final int offset = holder.searchKeyOffset(startKey);

        int size = 0;
        for (int i = offset; i < holder.size; ++i) {
            if (holder.entries[i].key >= endKey) return size;
            ++size;
        }
        return size;
    }


    @Override
    public boolean isEmpty() {
        if (isBaseMap()) {
            return holder.size == 0;
        }

        final int offset = holder.searchKeyOffset(startKey);
        return offset >= holder.size || holder.entries[offset].key >= endKey;
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
    public boolean containsKey(int key) {
        if (!isLegalRange(key)) {
            return false;
        }
        return holder.searchKeyIndex(key) >= 0;
    }

    @Override
    public boolean containsValue(Object value) {
        return containValueIdx(value) >= 0;
    }

    private int containValueIdx(Object value) {
        final int offset = holder.searchKeyOffset(startKey);

        for (int i = offset; i < holder.size; ++i) {
            KeyEntry entry = holder.entries[i];
            if (entry.key >= endKey) return -1;

            if (Objects.equals(entry.value, value)) {
                return i;
            }
        }
        return -1;
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
        if (isLegalRange(idx)) {
            return holder.get(idx);
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
    public V put(int key, V value) {
        validateLegalRange(key);
        return holder.put(key, value);
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
    public V remove(int key) {
        return holder.remove(key);
    }


    @Override
    public void putAll(Map<? extends Integer, ? extends V> m) {
        for (Entry<? extends Integer, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        if (holder.size != 0 && !isEmpty()) {
            holder.clear(startKey, endKey);
        }
    }

    private boolean isBaseMap() {
        return holder.isBaseMap(startKey, endKey);
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

    @Override
    public Comparator<? super Integer> comparator() {
        //natural ordering of the int key
        return null;
    }

    @Override
    public SortedMap<Integer, V> subMap(Integer fromKey, Integer toKey) {
        return subMap((int) fromKey, (int) toKey);
    }

    @Override
    public IntKeyMap<V> subMap(int fromKey, int toKey) {
        if (fromKey < startKey || toKey > endKey) {
            throw new IllegalArgumentException(
                    String.format("fromKey(%s) < startKey(%s) || toKey(%s) > endKey(%s) == false",
                            fromKey, startKey, toKey, endKey));
        }
        if (fromKey > toKey) {
            throw new IllegalArgumentException(String.format("fromKey(%s) not smaller than toKey(%s)", fromKey, toKey));
        }
        return new CompactArrayMap<>(holder, fromKey, toKey);
    }


    @Override
    public SortedMap<Integer, V> headMap(Integer toKey) {
        return headMap((int) toKey);
    }

    @Override
    public IntKeyMap<V> headMap(int toKey) {
        return subMap(startKey, toKey);
    }

    @Override
    public SortedMap<Integer, V> tailMap(Integer fromKey) {
        return tailMap((int) fromKey);
    }

    @Override
    public IntKeyMap<V> tailMap(int fromKey) {
        return subMap(fromKey, endKey);
    }

    @Override
    public Integer firstKey() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return holder.firstKey(startKey, endKey);
    }

    @Override
    public Integer lastKey() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return holder.lastKey(startKey, endKey);
    }

    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        final int size = size();
        s.defaultWriteObject();
        s.writeInt(size);

        if (size > 0) {
            final KeyEntry[] entries = holder.entries;
            final int offset = holder.searchKeyOffset(startKey);
            final int end = holder.searchKeyOffset(endKey);

            final int requiredCapacity = end - offset;
            s.writeInt(requiredCapacity);

            int maxLength = Math.min(holder.size, end);
            for (int i = offset; i < maxLength; ++i) {
                final KeyEntry entry = entries[i];
                s.writeInt(entry.key);
                s.writeObject(entry.value);
            }
        }
    }


    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        final int size = s.readInt();
        if (size == 0) {
            holder = new ArrayHolder(DEFAULT_CAPACITY);
        } else {
            final int requiredCapacity = s.readInt();
            holder = new ArrayHolder(requiredCapacity);
            holder.size = size;

            final KeyEntry[] entries = holder.entries;
            for (int i = 0; i < size; i++) {
                final int idx = s.readInt();
                final V value = (V) s.readObject();
                entries[i] = new KeyEntry<>(idx, value);
            }
        }
    }


    public boolean equals(Object o) {
        if (o == this) return true;

        if (!(o instanceof Map)) return false;

        Map<?, ?> other = (Map<?, ?>) o;
        if (other.size() != size()) return false;

        try {
            final int offset = isBaseMap() ? 0 : holder.searchKeyOffset(startKey);
            final int maxLength = isBaseMap() ? holder.size : Math.min(holder.size, holder.searchKeyOffset(endKey));
            for (int i = offset; i < maxLength; ++i) {
                final KeyEntry entry = holder.entries[i];
                final Object otherValue = other.get(entry.key);
                if (entry.value == null) {
                    if (!(otherValue == null && other.containsKey(entry.key)))
                        return false;
                } else {
                    if (!entry.value.equals(otherValue))
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
        for (Entry<Integer, V> entry : entrySet()) h += entry.hashCode();
        return h;
    }

    public String toString() {
        Iterator<Entry<Integer, V>> i = entrySet().iterator();
        if (!i.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (; ; ) {
            Entry<Integer, V> e = i.next();
            int key = e.getKey();
            V value = e.getValue();
            sb.append(key);
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value);
            if (!i.hasNext())
                return sb.append('}').toString();
            sb.append(',').append(' ');
        }
    }

    private static class ArrayHolder {
        /**
         * Some VMs reserve some header words in an array.
         * Attempts to allocate larger arrays may result in
         * OutOfMemoryError: Requested array size exceeds VM limit
         */
        private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

        KeyEntry[] entries;
        int size = 0;
        int modCount = 0;

        ArrayHolder(int initialCapacity) {
            if (initialCapacity < 0) {
                throw new IllegalArgumentException("initialCapacity must be >= 0");
            }
            if (initialCapacity > MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException("initialCapacity exceeds maximum capacity: " + MAX_ARRAY_SIZE);
            }
            entries = new KeyEntry[initialCapacity];
        }

        @SuppressWarnings("unchecked")
        <V> V remove(int key) {
            if (isOutOfRange(key)) return null;
            int keyIdx = searchKeyIndex(key);
            if (keyIdx >= 0) {
                V value = (V) entries[keyIdx].value;
                System.arraycopy(entries, keyIdx + 1, entries, keyIdx, entries.length - keyIdx - 1);
                entries[entries.length - 1] = null;
                --size;
                ++modCount;
                return value;
            }
            return null;
        }

        boolean delete(int key) {
            if (isOutOfRange(key)) return false;
            int keyIdx = searchKeyIndex(key);
            if (keyIdx >= 0) {
                System.arraycopy(entries, keyIdx + 1, entries, keyIdx, entries.length - keyIdx - 1);
                entries[entries.length - 1] = null;
                --size;
                ++modCount;
                return true;
            }
            return false;
        }

        void clear(int fromKey, int toKey) {
            if (isBaseMap(fromKey, toKey)) {
                Arrays.fill(entries, null);
                if (size > 0) {
                    ++modCount;
                }
                size = 0;
                return;
            }

            int startOffset = searchKeyOffset(fromKey);
            int endIndex = searchKeyOffset(toKey);
            int delta = endIndex - startOffset;
            if (delta > 0) {
                System.arraycopy(entries, startOffset + delta, entries, startOffset, entries.length - startOffset - delta);
                Arrays.fill(entries, entries.length - delta, entries.length, null);
                size -= delta;
                ++modCount;
            }
        }

        private Integer firstKey(int startKey, int endKey) {
            if (isBaseMap(startKey, endKey)) {
                return size == 0 ? null : entries[0].key;
            }
            final int offset = searchKeyOffset(startKey);

            if (offset < size) {
                final int key = entries[offset].key;
                if (key < endKey) {
                    return key;
                }
            }
            return null;
        }

        private Integer lastKey(int startKey, int endKey) {
            if (isBaseMap(startKey, endKey)) {
                return size == 0 ? null : entries[size - 1].key;
            }
            final int offset = searchKeyOffset(endKey) - 1;

            if (offset >= 0 && offset < size) {
                final int key = entries[offset].key;
                if (key >= startKey) {
                    return key;
                }
            }
            return null;

        }


        private boolean isOutOfRange(int key) {
            return size == 0 || key < entries[0].key || size > 0 && key > entries[size - 1].key;
        }

        @SuppressWarnings("unchecked")
        <V> V get(int key) {
            if (isOutOfRange(key)) return null;

            int keyIdx = searchKeyIndex(key);
            if (keyIdx >= 0) {
                return (V) entries[keyIdx].value;
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        <V> V put(int key, V value) {
            int keyIdx = searchKeyIndex(key);
            if (keyIdx < 0) {
                keyIdx = -(keyIdx + 1); //insertion point

                ++size;
                if (size - entries.length > 0) {
                    int newCapacity = determineNewCapacity(size);
                    KeyEntry[] newEntries = new KeyEntry[newCapacity];

                    System.arraycopy(entries, 0, newEntries, 0, keyIdx);
                    if (keyIdx + 1 < size) {
                        System.arraycopy(entries, keyIdx, newEntries, keyIdx + 1, entries.length - keyIdx);
                    }
                    entries = newEntries;
                } else {
                    System.arraycopy(entries, keyIdx, entries, keyIdx + 1, entries.length - (keyIdx + 1));
                }

                entries[keyIdx] = new KeyEntry(key, null);
            }

            final KeyEntry entry = entries[keyIdx];
            V oldValue = (V) entry.getValue();
            entry.setValue(value);

            ++modCount;
            return oldValue;
        }


        private int searchKeyIndex(int key) {
            int low = 0;
            int high = size - 1;

            while (low <= high) {
                int mid = (low + high) >>> 1;
                int midVal = entries[mid].key;

                if (midVal < key)
                    low = mid + 1;
                else if (midVal > key)
                    high = mid - 1;
                else
                    return mid;
            }
            return -(low + 1);
        }

        private int searchKeyOffset(int key) {
            final int offset = searchKeyIndex(key);
            return offset < 0 ? -(offset + 1) : offset;
        }

        private void ensureCapacity(int minCapacity) {
            if (minCapacity - entries.length > 0) {
                int newCapacity = determineNewCapacity(minCapacity);

                entries = Arrays.copyOf(entries, newCapacity);
            }
        }

        private int determineNewCapacity(int minCapacity) {
            int oldCapacity = entries.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0)
                newCapacity = minCapacity;
            if (newCapacity - MAX_ARRAY_SIZE > 0)
                throw new OutOfMemoryError();
            return newCapacity;
        }

        boolean isBaseMap(int startKey, int endKey) {
            return startKey == 0 && endKey == ArrayHolder.MAX_ARRAY_SIZE;
        }
    }

    private static class KeyEntry<V> implements Entry<Integer, V> {
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

        @Override
        public final String toString() {
            return key + "=" + value;
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
            final int idx = containValueIdx(o);
            if (idx > -1) {
                //Todo optimize
                CompactArrayMap.this.remove(holder.entries[idx].key);
                return true;
            }
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            if (c == null) throw new NullPointerException();
            if (c == this) return false;

            boolean altered = false;
            final int offset = isBaseMap() ? 0 : holder.searchKeyOffset(startKey);
            final int maxLength = isBaseMap() ? holder.size : Math.min(holder.size, holder.searchKeyOffset(endKey));
            for (int i = maxLength - 1; i >= offset; --i) {
                final KeyEntry entry = holder.entries[i];
                if (!c.contains(entry.value)) {
                    CompactArrayMap.this.remove(entry.key);
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
            } catch (ClassCastException | NullPointerException unused) {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int h = 0;
            for (T obj : this) {
                h += Objects.hashCode(obj);
            }
            return h;
        }
    }

    private abstract class AbstractCollection<T> implements Collection<T> {

        @Override
        public int size() {
            return CompactArrayMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return CompactArrayMap.this.isEmpty();
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
            final int offset = isBaseMap() ? 0 : holder.searchKeyOffset(startKey);
            final int maxLength = isBaseMap() ? holder.size : Math.min(holder.size, holder.searchKeyOffset(endKey));
            for (int i = offset; i < maxLength; ++i) {
                final KeyEntry entry = holder.entries[i];
                result[idx] = (E) valueOf(entry.key, (V) entry.value);
                ++idx;
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
            CompactArrayMap.this.clear();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;

            if (!(o instanceof Collection))
                return false;
            Collection<?> c = (Collection<?>) o;
            if (c.size() != size())
                return false;
            try {
                return containsAll(c);
            } catch (ClassCastException | NullPointerException unused) {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int h = 0;
            for (T obj : this) {
                h += Objects.hashCode(obj);
            }
            return h;
        }

        public String toString() {
            Iterator<T> it = iterator();
            if (!it.hasNext())
                return "[]";

            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (; ; ) {
                T e = it.next();
                sb.append(e == this ? "(this Collection)" : e);
                if (!it.hasNext())
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
            return containsKey(o);
        }

        @Override
        public Iterator<Integer> iterator() {
            return new KeyIterator();
        }

        @Override
        public boolean remove(Object o) {
            if (o == null) throw new NullPointerException();

            int key = (int) o;
            return holder.delete(key);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            if (c == null) throw new NullPointerException();
            if (c == this) return false;

            boolean altered = false;
            final int offset = isBaseMap() ? 0 : holder.searchKeyOffset(startKey);
            final int maxLength = isBaseMap() ? holder.size : Math.min(holder.size, holder.searchKeyOffset(endKey));
            for (int i = maxLength - 1; i >= offset; --i) {
                final KeyEntry entry = holder.entries[i];
                if (!c.contains(entry.key)) {
                    CompactArrayMap.this.remove(entry.key);
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

    private class EntrySet extends AbstractSet<Entry<Integer, V>> {

        @Override
        public Iterator<Entry<Integer, V>> iterator() {
            return new ArrayIterator();
        }

        @Override
        public Object[] toArray() {
            return toArray(new Object[size()]);
        }

        @SuppressWarnings("unchecked")
        @Override
        Entry<Integer, V> valueOf(int key, V value) {
            return holder.entries[holder.searchKeyIndex(key)];
        }


        @Override
        @SuppressWarnings("unchecked")
        public <E> E[] toArray(E[] a) {
            final int size = size();


            E[] result = a.length >= size ? a :
                    (E[]) java.lang.reflect.Array
                            .newInstance(a.getClass().getComponentType(), size);

            int idx = 0;
            final int offset = isBaseMap() ? 0 : holder.searchKeyOffset(startKey);
            final int maxLength = isBaseMap() ? holder.size : Math.min(holder.size, holder.searchKeyOffset(endKey));
            for (int i = offset; i < maxLength; ++i) {
                result[idx] = (E) holder.entries[i];
                ++idx;
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
                int key = (int) entry.getKey();
                if (!isLegalRange(key)) return false;

                Object value = entry.getValue();
                final int idx = holder.searchKeyIndex(key);
                if (idx >= 0) {
                    final KeyEntry e = holder.entries[idx];
                    if (Objects.equals(value, e.value)) {
                        holder.remove(key); //TODO removeIdx();
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
                int size = CompactArrayMap.this.size();
                CompactArrayMap.this.clear();
                return size > 0;
            }

            boolean altered = false;
            final int offset = isBaseMap() ? 0 : holder.searchKeyOffset(startKey);
            final int maxLength = isBaseMap() ? holder.size : Math.min(holder.size, holder.searchKeyOffset(endKey));
            for (int i = maxLength - 1; i >= offset; --i) {
                final KeyEntry entry = holder.entries[i];
                if (!c.contains(entry)) {
                    holder.remove(entry.key);
                    altered = true;
                }
            }
            return altered;
        }

        @Override
        public boolean contains(Object o) {
            if (o == null) {
                throw new NullPointerException();
            }

            Entry entry = (Entry) o;
            if (entry.getKey() instanceof Integer) {
                int key = (int) entry.getKey();
                if (isLegalRange(key)) {
                    final int idx = holder.searchKeyIndex(key);
                    if (idx < 0) return false;
                    final KeyEntry e = holder.entries[idx];
                    return e.equals(entry);
                }
            }
            return false;
        }

        private class ArrayIterator extends AbstractArrayIterator<Entry<Integer, V>> {
            @Override
            Entry<Integer, V> valueOf(KeyEntry entry) {
                return entry;
            }
        }
    }

    private class KeyIterator extends AbstractArrayIterator<Integer> {
        @Override
        Integer valueOf(KeyEntry entry) {
            return entry.key;
        }
    }

    private class ValuesIterator extends AbstractArrayIterator<V> {
        @Override
        V valueOf(KeyEntry entry) {
            return (V) entry.value;
        }
    }

    private abstract class AbstractArrayIterator<T> implements Iterator<T> {
        private int start = holder.searchKeyOffset(startKey);
        private int index = start - 1;
        private boolean removed = false;
        private int expectedModCount;

        AbstractArrayIterator() {
            expectedModCount = holder.modCount;
        }

        @Override
        public boolean hasNext() {
            final int next = index + 1;
            return next < holder.size && holder.entries[next].key < endKey;
        }

        abstract T valueOf(KeyEntry entry);

        @Override
        public T next() {
            if (holder.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }

            final int next = index + 1;
            if (next < holder.size) {
                KeyEntry entry = holder.entries[next];
                if (entry.key < endKey) {
                    removed = false;
                    ++index;
                    return valueOf(entry);
                }
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            if (index < start) {
                throw new IllegalStateException("next() has not been called");
            }
            if (holder.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (removed) {
                throw new IllegalStateException("remove() has already been called");
            }
            //TODO optimize
            CompactArrayMap.this.remove(holder.entries[index].key);
            expectedModCount = holder.modCount;
            --index;
            removed = true;
        }
    }
}
