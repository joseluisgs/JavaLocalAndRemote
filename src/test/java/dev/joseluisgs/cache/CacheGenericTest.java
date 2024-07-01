package dev.joseluisgs.cache;


import dev.joseluisgs.models.Tenista;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CacheGenericTest {

    private Tenista createRandomTenista() {
        Random random = new Random();
        return new Tenista(
                random.nextLong(),
                "Tenista",
                "Pais",
                180,
                75,
                1000,
                Tenista.Mano.DIESTRO,
                LocalDate.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                false
        );
    }

    @Test
    void debeDevolverUnElementoExistenteEnBaseSuClave() {
        CacheGeneric<Long, Tenista> cache = new CacheGeneric<>(2);
        Tenista tenista1 = createRandomTenista();

        cache.put(tenista1.getId(), tenista1);

        assertAll(
                () -> assertNotNull(cache.get(tenista1.getId())),
                () -> assertEquals(tenista1, cache.get(tenista1.getId()))
        );
    }

    @Test
    void debeDevolverNullSiElementoNoExisteEnBaseSuClave() {
        CacheGeneric<Long, Tenista> cache = new CacheGeneric<>(2);
        Long nonExistingId = new Random().nextLong();

        assertNull(cache.get(nonExistingId));
    }

    @Test
    void debeIntroducirElementosEnTodaCache() {
        CacheGeneric<Long, Tenista> cache = new CacheGeneric<>(3);
        Tenista tenista1 = createRandomTenista();
        Tenista tenista2 = createRandomTenista();

        cache.put(tenista1.getId(), tenista1);
        cache.put(tenista2.getId(), tenista2);

        assertAll(
                () -> assertEquals(2, cache.size()),
                () -> assertEquals(tenista1, cache.get(tenista1.getId())),
                () -> assertEquals(tenista2, cache.get(tenista2.getId()))
        );
    }

    @Test
    void debeEliminarUnTenistaSiLimiteSuperaAlIntroducir() {
        CacheGeneric<Long, Tenista> cache = new CacheGeneric<>(2);
        Tenista tenista1 = createRandomTenista();
        Tenista tenista2 = createRandomTenista();
        Tenista tenista3 = createRandomTenista();
        Tenista tenista4 = createRandomTenista();

        cache.put(tenista1.getId(), tenista1);
        cache.put(tenista2.getId(), tenista2);
        cache.put(tenista1.getId(), tenista1); // Access tenista1 to make it least recently used
        cache.put(tenista3.getId(), tenista3);
        cache.put(tenista4.getId(), tenista4);
        cache.put(tenista1.getId(), tenista1); // Access tenista1 again to preserve it

        assertAll(
                () -> assertEquals(2, cache.size()),
                () -> assertFalse(cache.containsKey(tenista2.getId())),
                () -> assertFalse(cache.containsKey(tenista3.getId())),
                () -> assertEquals(tenista1, cache.get(tenista1.getId())),
                () -> assertEquals(tenista4, cache.get(tenista4.getId()))
        );
    }

    @Test
    void debeEliminarLosElementosDeLaCache() {
        CacheGeneric<Long, Tenista> cache = new CacheGeneric<>(2);
        Tenista tenista1 = createRandomTenista();
        Tenista tenista2 = createRandomTenista();

        cache.put(tenista1.getId(), tenista1);
        cache.put(tenista2.getId(), tenista2);

        cache.clear();

        assertAll(
                () -> assertEquals(0, cache.size()),
                () -> assertFalse(cache.containsKey(tenista1.getId())),
                () -> assertFalse(cache.containsKey(tenista2.getId()))
        );
    }

    @Test
    void debeDevolverClavesYValoresCorrectos() {
        CacheGeneric<Long, Tenista> cache = new CacheGeneric<>(2);
        Tenista tenista1 = createRandomTenista();
        Tenista tenista2 = createRandomTenista();

        cache.put(tenista1.getId(), tenista1);
        cache.put(tenista2.getId(), tenista2);

        assertAll(
                () -> assertEquals(Set.of(tenista1.getId(), tenista2.getId()), cache.keys()),
                () -> assertEquals(List.of(tenista1, tenista2), cache.values().stream().toList())
        );
    }

    @Test
    void debeComprobarExistenciaDeValorEnBaseSuClave() {
        CacheGeneric<Long, Tenista> cache = new CacheGeneric<>(2);
        Tenista tenista1 = createRandomTenista();

        cache.put(tenista1.getId(), tenista1);

        assertAll(
                () -> assertTrue(cache.containsKey(tenista1.getId())),
                () -> assertFalse(cache.containsKey(-99L))
        );
    }

    @Test
    void debeComprobarSiExisteUnValor() {
        CacheGeneric<Long, Tenista> cache = new CacheGeneric<>(2);
        Tenista tenista1 = createRandomTenista();
        Tenista tenista2 = createRandomTenista();

        cache.put(tenista1.getId(), tenista1);

        assertAll(
                () -> assertTrue(cache.containsValue(tenista1)),
                () -> assertFalse(cache.containsValue(tenista2))
        );
    }

    @Test
    void debeComprobarSiEstaVacia() {
        CacheGeneric<Long, Tenista> cache = new CacheGeneric<>(2);

        assertAll(
                () -> assertTrue(cache.isEmpty()),
                () -> assertFalse(cache.isNotEmpty())
        );

        Tenista tenista1 = createRandomTenista();
        cache.put(tenista1.getId(), tenista1);

        assertAll(
                () -> assertFalse(cache.isEmpty()),
                () -> assertTrue(cache.isNotEmpty())
        );
    }
}
