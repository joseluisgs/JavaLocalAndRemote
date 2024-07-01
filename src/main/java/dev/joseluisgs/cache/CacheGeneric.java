package dev.joseluisgs.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class CacheGeneric<K, T> implements Cache<K, T> {
    private static final Logger logger = LoggerFactory.getLogger(CacheGeneric.class);
    private final int cacheSize;
    private final LinkedHashMap<K, T> cache;

    public CacheGeneric(int cacheSize) {
        this.cacheSize = cacheSize;
        this.cache = new LinkedHashMap<K, T>(cacheSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, T> eldest) {
                return size() > CacheGeneric.this.cacheSize;
            }
        };
    }

    @Override
    public T get(K key) {
        logger.debug("Obteniendo el valor de la clave: {}", key);
        return cache.get(key);
    }

    @Override
    public void put(K key, T value) {
        logger.debug("Añadiendo a cache el valor de la clave: {}", key);
        cache.put(key, value);
    }

    @Override
    public void remove(K key) {
        logger.debug("Eliminando de cache el valor de la clave: {}", key);
        cache.remove(key);
    }

    @Override
    public void clear() {
        logger.debug("Limpiando la cache");
        cache.clear();
    }

    @Override
    public int size() {
        logger.debug("Obteniendo el tamaño de la cache");
        return cache.size();
    }

    @Override
    public Set<K> keys() {
        logger.debug("Obteniendo las claves de la cache");
        return cache.keySet();
    }

    @Override
    public Collection<T> values() {
        logger.debug("Obteniendo los valores de la cache");
        return cache.values();
    }

    @Override
    public boolean containsKey(K key) {
        logger.debug("Comprobando si existe la clave en la cache: {}", key);
        return cache.containsKey(key);
    }

    @Override
    public boolean containsValue(T value) {
        logger.debug("Comprobando si existe el valor en la cache: {}", value);
        return cache.containsValue(value);
    }

    @Override
    public boolean isEmpty() {
        logger.debug("Comprobando si la cache está vacía");
        return cache.isEmpty();
    }

    @Override
    public boolean isNotEmpty() {
        logger.debug("Comprobando si la cache no está vacía");
        return !isEmpty();
    }
}