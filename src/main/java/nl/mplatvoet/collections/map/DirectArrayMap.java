package nl.mplatvoet.collections.map;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class DirectArrayMap<V> implements IntKeyMap<V>, Serializable, Cloneable {
    private static final int DEFAULT_CAPACITY = 10;
    private static final Object NULL_MARKER = new Object();
    private final int startIndex;
    private final int endIndex;

    private transient ArrayHolder holder;

    private transient EntrySet entrySet = null;
    private transient KeySet keySet = null;
    private transient ValuesCollection valuesCollection = null;

    public DirectArrayMap() {
        this(DEFAULT_CAPACITY);
    }

    public DirectArrayMap(int initialCapacity) {
        this(new ArrayHolder(Math.max(initialCapacity, DEFAULT_CAPACITY)), 0, ArrayHolder.MAX_ARRAY_SIZE);
    }

    private DirectArrayMap(ArrayHolder holder, int startIndex, int endIndex) {
        this.holder = holder;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    private static Object mask(Object value) {
        return value == null ? NULL_MARKER : value;
    }

    @SuppressWarnings("unchecked")
    private static <V> V unmask(Object value) {
        if (value == NULL_MARKER) {
            return null;
        }
        if (value instanceof DirectArrayMap.KeyEntry) {
            return ((DirectArrayMap<V>.KeyEntry) value).getValue();
        }
        return (V) value;
    }

    private void validateLegalRange(int idx) {
        if (!isLegalRange(idx)) {
            throw new IllegalArgumentException("idx must be " + startIndex + " >= idx < " + endIndex);
        }
    }

    private boolean isLegalRange(int idx) {
        return idx >= startIndex && idx < endIndex;
    }

    @Override
    public int size() {
        if (isBaseMap()) {
            return holder.size;
        }
        if (startIndex == endIndex - 1) {
            return 0;
        }

        final Object[] entries = holder.entries;
        int subtract = 0;
        for (int i = 0; i < startIndex; ++i) {
            if (entries[i] != null) ++subtract;
        }
        for (int i = endIndex; i < entries.length; ++i) {
            if (entries[i] != null) ++subtract;
        }

        return holder.size - subtract;
    }

    @Override
    public boolean isEmpty() {
        if (isBaseMap()) {
            return holder.size == 0;
        }

        final Object[] entries = holder.entries;
        int maxLength = Math.min(entries.length, endIndex);
        for (int i = startIndex; i < maxLength; ++i) {
            if (entries[i] != null) return false;
        }
        return true;
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
        final Object[] entries = holder.entries;
        return isLegalRange(idx) && idx < entries.length && entries[idx] != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return containValueIdx(value) > -1;
    }

    private int containValueIdx(Object value) {
        Object[] entries = holder.entries;
        int maxLength = Math.min(entries.length, endIndex);
        for (int i = startIndex; i < maxLength; i++) {
            Object entry = entries[i];
            if (entry == null) continue;

            V entryValue = unmask(entry);

            if (Objects.equals(entryValue, value)) {
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
        final Object[] entries = holder.entries;
        if (isLegalRange(idx) && idx < entries.length) {
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
        validateLegalRange(idx);
        holder.ensureCapacity(idx + 1);

        final Object[] entries = holder.entries;
        Object previous = entries[idx];
        entries[idx] = mask(value);
        ++holder.modCount;
        if (previous == null) {
            ++holder.size;
            return null;
        }
        return unmask(previous);
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
        final Object[] entries = holder.entries;
        if (isLegalRange(idx) && idx < entries.length) {
            Object entry = entries[idx];
            entries[idx] = null;
            if (entry != null) {
                --holder.size;
                ++holder.modCount;
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
        if (holder.size == 0 || isEmpty()) return;

        final Object[] entries = holder.entries;
        if (isBaseMap()) {
            Arrays.fill(entries, null);
            holder.size = 0;
        } else {
            int deleted = 0;
            int maxLength = Math.min(endIndex, entries.length);
            for (int i = startIndex; i < maxLength; ++i) {
                if (entries[i] != null) {
                    entries[i] = null;
                    ++deleted;
                }
            }
            holder.size -= deleted;
        }
        ++holder.modCount;
    }

    private boolean isBaseMap() {
        return startIndex == 0 && endIndex == ArrayHolder.MAX_ARRAY_SIZE;
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
        int maxLength = Math.min(entries.length, endIndex);
        for (int i = maxLength - 1; i >= startIndex; --i) {
            if (entries[i] != null) return i;
        }
        return -1;
    }

    private int minSetIndex(Object[] entries) {
        int maxLength = Math.min(entries.length, endIndex);
        for (int i = startIndex; i < maxLength; ++i) {
            if (entries[i] != null) return i;
        }
        return -1;
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
        if (fromKey < startIndex || toKey > endIndex) {
            throw new IllegalArgumentException(
                    String.format("fromKey(%s) < startIndex(%s) || toKey(%s) > endIndex(%s) == false",
                            fromKey, startIndex, toKey, endIndex));
        }
        if (fromKey > toKey) {
            throw new IllegalArgumentException(String.format("fromKey(%s) not smaller than toKey(%s)", fromKey, toKey));
        }
        return new DirectArrayMap<>(holder, fromKey, toKey);
    }


    @Override
    public SortedMap<Integer, V> headMap(Integer toKey) {
        return headMap((int) toKey);
    }

    @Override
    public IntKeyMap<V> headMap(int toKey) {
        return subMap(startIndex, toKey);
    }

    @Override
    public SortedMap<Integer, V> tailMap(Integer fromKey) {
        return tailMap((int) fromKey);
    }

    @Override
    public IntKeyMap<V> tailMap(int fromKey) {
        return subMap(fromKey, endIndex);
    }

    @Override
    public Integer firstKey() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return minSetIndex(holder.entries);
    }

    @Override
    public Integer lastKey() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return maxSetIndex(holder.entries);
    }

    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        final int size = size();
        s.defaultWriteObject();
        s.writeInt(size());

        if (size > 0) {
            final Object[] entries = holder.entries;
            final int requiredCapacity = maxSetIndex(entries) + 1;

            s.writeInt(requiredCapacity);

            int written = 0;
            int maxLength = Math.min(entries.length, endIndex);
            for (int i = startIndex; i < maxLength && written < size; ++i) {
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
            holder = new ArrayHolder(DEFAULT_CAPACITY);
        } else {
            final int requiredCapacity = s.readInt();
            holder = new ArrayHolder(requiredCapacity);
            holder.size = size;

            final Object[] entries = holder.entries;
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
        DirectArrayMap<V> result;
        try {
            result = (DirectArrayMap<V>) super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }

        final int newSize = size();

        Object[] src = holder.entries;
        result.holder = new ArrayHolder(maxSetIndex(src) + 1);
        result.holder.size = newSize;

        final Object[] dest = result.holder.entries;
        int cloned = 0;
        int maxLength = Math.min(src.length, endIndex);
        for (int i = startIndex; i < maxLength && cloned < newSize; ++i) {
            final Object entry = src[i];
            if (entry == null) continue;

            //mask(unmask(entry)) will unwrap any Entry instance
            dest[i] = mask(unmask(entry));

            ++cloned;
        }

        return result;
    }

    public boolean equals(Object o) {
        if (o == this) return true;

        if (!(o instanceof Map)) return false;

        Map<?, ?> other = (Map<?, ?>) o;
        if (other.size() != size()) return false;

        try {
            Object[] entries = DirectArrayMap.this.holder.entries;
            int maxLength = Math.min(entries.length, endIndex);
            for (int key = startIndex; key < maxLength; ++key) {
                final Object entry = entries[key];
                if (entry == null) continue;

                V value = unmask(entry);
                final Object otherValue = other.get(key);
                if (value == null) {
                    if (!(otherValue == null && other.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(otherValue))
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

        Object[] entries;
        int size = 0;
        int modCount = 0;

        ArrayHolder(int initialCapacity) {
            if (initialCapacity < 0) {
                throw new IllegalArgumentException("initialCapacity must be >= 0");
            }
            if (initialCapacity > MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException("initialCapacity exceeds maximum capacity: " + MAX_ARRAY_SIZE);
            }
            entries = new Object[initialCapacity];
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
    }

    private class KeyEntry implements Entry<Integer, V> {
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
                DirectArrayMap.this.remove(idx);
                return true;
            }
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            if (c == null) throw new NullPointerException();
            if (c == this) return false;

            boolean altered = false;
            Object[] entries = DirectArrayMap.this.holder.entries;
            int maxLength = Math.min(entries.length, endIndex);
            for (int i = startIndex; i < maxLength; ++i) {
                Object entry = entries[i];
                if (entry == null) continue;
                Object unmasked = unmask(entry);
                if (!c.contains(unmasked)) {
                    DirectArrayMap.this.remove(i);
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
            return DirectArrayMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return DirectArrayMap.this.isEmpty();
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
            Object[] entries = DirectArrayMap.this.holder.entries;
            int maxLength = Math.min(entries.length, endIndex);
            for (int key = startIndex; key < maxLength && idx < size; ++key) {
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
            DirectArrayMap.this.clear();
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
            return DirectArrayMap.this.removeRaw(key) != null;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            if (c == null) throw new NullPointerException();
            if (c == this) return false;

            boolean altered = false;
            Object[] entries = DirectArrayMap.this.holder.entries;
            int maxLength = Math.min(entries.length, endIndex);
            for (int i = startIndex; i < maxLength; i++) {
                Object entry = entries[i];
                if (entry == null) continue;

                if (!c.contains(i)) {
                    DirectArrayMap.this.remove(i);
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

        @Override
        Entry<Integer, V> valueOf(int key, V value) {
            return getEntry(key, DirectArrayMap.this.holder.entries[key]);
        }

        @SuppressWarnings("unchecked")
        private Entry<Integer, V> getEntry(int idx, Object currentEntry) {
            if (currentEntry instanceof DirectArrayMap.KeyEntry) {
                return (Entry<Integer, V>) currentEntry;
            }
            V value = currentEntry == NULL_MARKER ? null : (V) currentEntry;
            final KeyEntry keyEntry = new KeyEntry(idx, value);
            DirectArrayMap.this.holder.entries[idx] = keyEntry;
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
            Object[] entries = DirectArrayMap.this.holder.entries;
            int maxLength = Math.min(entries.length, endIndex);
            for (int key = startIndex; key < maxLength && idx < size; ++key) {
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
                if (!isLegalRange(idx)) return false;

                Object masked = mask(entry.getValue());
                Object[] entries = DirectArrayMap.this.holder.entries;
                if (idx >= 0 && idx < entries.length) {
                    if (masked.equals(entries[idx])) {
                        DirectArrayMap.this.remove(idx);
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
                int size = DirectArrayMap.this.size();
                DirectArrayMap.this.clear();
                return size > 0;
            }

            boolean modified = false;
            Object[] entries = DirectArrayMap.this.holder.entries;
            int maxLength = Math.min(entries.length, endIndex);
            for (int i = startIndex; i < maxLength; ++i) {
                Object entry = entries[i];
                if (entry != null && !c.contains(getEntry(i, entry))) {
                    DirectArrayMap.this.remove(i);
                    modified = true;
                }
            }

            return modified;
        }

        @Override
        public boolean contains(Object o) {
            if (o == null) {
                throw new NullPointerException();
            }

            Entry entry = (Entry) o;
            if (entry.getKey() instanceof Integer) {
                int idx = (int) entry.getKey();
                final Object[] entries = DirectArrayMap.this.holder.entries;
                if (isLegalRange(idx) && idx < entries.length) {
                    Object value = entry.getValue();
                    Object rawEntry = entries[idx];
                    if (rawEntry == null) return false;

                    V thisValue = unmask(rawEntry);
                    if (thisValue == null && value == null) return true;
                    //
                    return value != null && value.equals(thisValue);
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
    }

    private class ValuesIterator extends AbstractArrayIterator<V> {
        @Override
        V valueOf(int key, V value) {
            return value;
        }
    }

    private abstract class AbstractArrayIterator<T> implements Iterator<T> {
        private int index = startIndex - 1;
        private boolean removed = false;
        private int expectedModCount;

        AbstractArrayIterator() {
            expectedModCount = holder.modCount;
        }

        @Override
        public boolean hasNext() {
            Object[] entries = DirectArrayMap.this.holder.entries;
            int maxLength = Math.min(entries.length, endIndex);
            for (int i = index + 1; i < maxLength; ++i) {
                if (entries[i] != null) return true;
            }
            return false;
        }

        abstract T valueOf(int key, V value);

        @Override
        public T next() {
            if (holder.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            Object[] entries = DirectArrayMap.this.holder.entries;
            int maxLength = Math.min(entries.length, endIndex);
            for (int i = index + 1; i < maxLength; ++i) {
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
            if (index < startIndex) {
                throw new IllegalStateException("next() has not been called");
            }
            if (holder.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (removed) {
                throw new IllegalStateException("remove() has already been called");
            }
            DirectArrayMap.this.remove(index);
            expectedModCount = holder.modCount;
            removed = true;
        }
    }
}
