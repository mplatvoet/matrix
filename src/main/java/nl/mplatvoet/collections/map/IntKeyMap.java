package nl.mplatvoet.collections.map;

import java.util.SortedMap;

public interface IntKeyMap<V> extends SortedMap<Integer, V> {

    boolean containsKey(int idx);

    V get(int idx);

    V put(int idx, V value);

    V remove(int idx);

    IntKeyMap<V> subMap(int fromKey, int toKey);

    IntKeyMap<V> headMap(int toKey);

    IntKeyMap<V> tailMap(int fromKey);
}
