package nl.mplatvoet.collections.map;

public interface IntKeyMap<V> {

    int size();

    boolean containsKey(int idx);

    V get(int idx);

    V put(int idx, V value);

    V remove(int idx);
}
