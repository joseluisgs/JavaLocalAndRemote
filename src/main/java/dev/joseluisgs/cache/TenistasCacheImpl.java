package dev.joseluisgs.cache;

import dev.joseluisgs.models.Tenista;

public class TenistasCacheImpl extends CacheGeneric<Long, Tenista> implements TenistasCache {
    public TenistasCacheImpl(int cacheSize) {
        super(cacheSize);
    }
}
