package dev.joseluisgs.cache;

import dev.joseluisgs.models.Tenista;

public interface TenistasCache extends Cache<Long, Tenista> {
    int TENISTAS_CACHE_SIZE = 5;
}
