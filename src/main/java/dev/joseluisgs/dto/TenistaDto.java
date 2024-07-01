package dev.joseluisgs.dto;

import com.google.gson.annotations.SerializedName;

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
        String nombre,
        String pais,
        int altura,
        int peso,
        int puntos,
        String mano,
        @SerializedName("fecha_nacimiento") String fechaNacimiento,
        @SerializedName("created_at") String createdAt,
        @SerializedName("updated_at") String updatedAt,
        @SerializedName("is_deleted") Boolean isDeleted
) {
}