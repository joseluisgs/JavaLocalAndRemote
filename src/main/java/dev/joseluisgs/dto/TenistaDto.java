package dev.joseluisgs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        @JsonProperty("fecha_nacimiento")
        String fechaNacimiento,
        @JsonProperty("created_at")
        String createdAt,
        @JsonProperty("updated_at")
        String updatedAt,
        @JsonProperty("is_deleted")
        Boolean isDeleted
) {
}