package nl.mplatvoet.collections.map;


import java.util.Map;

public interface IntKeyMap<V> extends Map<Integer, V> {

    boolean containsKey(int idx);

    V get(int idx);

    V put(int idx, V value);

    V remove(int idx);
}
