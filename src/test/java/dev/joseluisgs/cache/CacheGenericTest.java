package dev.joseluisgs.cache;

import dev.joseluisgs.models.Tenista;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Debe devolver un elemento existente en base a su clave")
    void debeDevolverUnElementoExistenteEnBaseSuClave() {
        CacheGeneric<Long, Tenista> cache = new CacheGeneric<>(2);
        Tenista tenista1 = createRandomTenista();

        cache.put(tenista1.getId(), tenista1);

        assertAll(
                "Verificar que el tenista se guarda y se puede recuperar por su clave",
                () -> assertNotNull(cache.get(tenista1.getId()),
                        "El tenista debería estar presente en la caché"),
                () -> assertEquals(tenista1, cache.get(tenista1.getId()),
                        "El tenista recuperado debería ser igual al tenista almacenado")
        );
    }

    @Test
    @DisplayName("Debe devolver null si el elemento no existe en base a su clave")
    void debeDevolverNullSiElementoNoExisteEnBaseSuClave() {
        CacheGeneric<Long, Tenista> cache = new CacheGeneric<>(2);
        Long nonExistingId = new Random().nextLong();

        assertNull(cache.get(nonExistingId), "Debería devolver null si el tenista no existe en la caché");
    }

    @Test
    @DisplayName("Debe introducir elementos en toda la caché")
    void debeIntroducirElementosEnTodaCache() {
        CacheGeneric<Long, Tenista> cache = new CacheGeneric<>(3);
        Tenista tenista1 = createRandomTenista();
        Tenista tenista2 = createRandomTenista();

        cache.put(tenista1.getId(), tenista1);
        cache.put(tenista2.getId(), tenista2);

        assertAll(
                "Verificar que los tenistas se guardan y se pueden recuperar",
                () -> assertEquals(2, cache.size(), "La caché debería contener dos elementos"),
                () -> assertEquals(tenista1, cache.get(tenista1.getId()),
                        "El tenista1 recuperado debería ser igual al tenista1 almacenado"),
                () -> assertEquals(tenista2, cache.get(tenista2.getId()),
                        "El tenista2 recuperado debería ser igual al tenista2 almacenado")
        );
    }

    @Test
    @DisplayName("Debe eliminar un tenista si el límite se supera al introducir")
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
                "Verificar que la caché elimina el elemento menos usado cuando se supera el límite",
                () -> assertEquals(2, cache.size(), "La caché debería tener dos elementos"),
                () -> assertFalse(cache.containsKey(tenista2.getId()), "La caché no debería contener tenista2"),
                () -> assertFalse(cache.containsKey(tenista3.getId()), "La caché no debería contener tenista3"),
                () -> assertEquals(tenista1, cache.get(tenista1.getId()),
                        "El tenista1 debería estar aún en la caché"),
                () -> assertEquals(tenista4, cache.get(tenista4.getId()),
                        "El tenista4 debería estar en la caché")
        );
    }

    @Test
    @DisplayName("Debe eliminar los elementos de la caché")
    void debeEliminarLosElementosDeLaCache() {
        CacheGeneric<Long, Tenista> cache = new CacheGeneric<>(2);
        Tenista tenista1 = createRandomTenista();
        Tenista tenista2 = createRandomTenista();

        cache.put(tenista1.getId(), tenista1);
        cache.put(tenista2.getId(), tenista2);

        cache.clear();

        assertAll(
                "Verificar que la caché se vacía correctamente",
                () -> assertEquals(0, cache.size(), "La caché debería estar vacía"),
                () -> assertFalse(cache.containsKey(tenista1.getId()),
                        "La caché no debería contener tenista1"),
                () -> assertFalse(cache.containsKey(tenista2.getId()),
                        "La caché no debería contener tenista2")
        );
    }

    @Test
    @DisplayName("Debe devolver claves y valores correctos")
    void debeDevolverClavesYValoresCorrectos() {
        CacheGeneric<Long, Tenista> cache = new CacheGeneric<>(2);
        Tenista tenista1 = createRandomTenista();
        Tenista tenista2 = createRandomTenista();

        cache.put(tenista1.getId(), tenista1);
        cache.put(tenista2.getId(), tenista2);

        assertAll(
                "Verificar que la caché devuelve las claves y valores correctos",
                () -> assertEquals(Set.of(tenista1.getId(), tenista2.getId()), cache.keys(),
                        "Las claves devueltas deberían coincidir con las esperadas"),
                () -> assertEquals(List.of(tenista1, tenista2), cache.values().stream().toList(),
                        "Los valores devueltos deberían coincidir con los esperados")
        );
    }

    @Test
    @DisplayName("Debe comprobar la existencia de un valor en base a su clave")
    void debeComprobarExistenciaDeValorEnBaseSuClave() {
        CacheGeneric<Long, Tenista> cache = new CacheGeneric<>(2);
        Tenista tenista1 = createRandomTenista();

        cache.put(tenista1.getId(), tenista1);

        assertAll(
                "Verificar que la caché comprueba la existencia de un valor por clave correctamente",
                () -> assertTrue(cache.containsKey(tenista1.getId()),
                        "La caché debería contener el tenista1 por su clave"),
                () -> assertFalse(cache.containsKey(-99L),
                        "La caché no debería contener un tenista con una clave inexistente")
        );
    }

    @Test
    @DisplayName("Debe comprobar si existe un valor")
    void debeComprobarSiExisteUnValor() {
        CacheGeneric<Long, Tenista> cache = new CacheGeneric<>(2);
        Tenista tenista1 = createRandomTenista();
        Tenista tenista2 = createRandomTenista();

        cache.put(tenista1.getId(), tenista1);

        assertAll(
                "Verificar que la caché comprueba la existencia de un valor correctamente",
                () -> assertTrue(cache.containsValue(tenista1),
                        "La caché debería contener tenista1"),
                () -> assertFalse(cache.containsValue(tenista2),
                        "La caché no debería contener tenista2")
        );
    }

    @Test
    @DisplayName("Debe comprobar si está vacía")
    void debeComprobarSiEstaVacia() {
        CacheGeneric<Long, Tenista> cache = new CacheGeneric<>(2);

        assertAll(
                "Verificar que la caché comprueba si está vacía correctamente",
                () -> assertTrue(cache.isEmpty(), "La caché debería estar vacía"),
                () -> assertFalse(cache.isNotEmpty(), "La caché no debería estar no-vacía")
        );

        Tenista tenista1 = createRandomTenista();
        cache.put(tenista1.getId(), tenista1);

        assertAll(
                "Verificar que la caché comprueba si no está vacía correctamente",
                () -> assertFalse(cache.isEmpty(), "La caché no debería estar vacía"),
                () -> assertTrue(cache.isNotEmpty(), "La caché debería estar no-vacía")
        );
    }
}
