package dev.joseluisgs.dto;

import com.squareup.moshi.Json;
import lombok.NonNull;

/**
 * Data Transfer Object para Tenista
 * Esta vez usamos Java Record, porque no necesitamos más
 * Usado para la representación de los datos de Tenista
 * ¿Qué es un Java Record?
 * Es una clase de datos inmutable, es decir, una clase que no puede ser modificada una vez creada.
 * Y tiene un constructor que inicializa todos los campos, y métodos para obtener los valores de los campos.
 * Es como una Data clase de Java, pero más concisa.
 */

public record TenistaDto(
        long id,
        @NonNull
        String nombre,
        @NonNull
        String pais,
        int altura,
        int peso,
        int puntos,
        @NonNull
        String mano,
        @NonNull
        @Json(name = "fecha_nacimiento")
        String fechaNacimiento,
        @Json(name = "created_at")
        String createdAt,
        @Json(name = "updated_at")
        String updatedAt,
        @Json(name = "is_deleted")
        Boolean isDeleted
) {
}