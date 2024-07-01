package dev.joseluisgs.cache;

import java.util.Collection;
import java.util.Set;

public interface Cache<K, T> {

    T get(K key);

    void put(K key, T value);

    void remove(K key);

    void clear();

    int size();

    Set<K> keys();

    Collection<T> values();

    boolean containsKey(K key);

    boolean containsValue(T value);

    boolean isEmpty();

    default boolean isNotEmpty() {
        return !isEmpty();
    }
}
