package dev.joseluisgs.database;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

public record TenistaEntity(
        @ColumnName("id")
        long id,
        @ColumnName("nombre")
        String nombre,
        @ColumnName("pais")
        String pais,
        @ColumnName("altura")
        int altura,
        @ColumnName("peso")
        int peso,
        @ColumnName("puntos")
        int puntos,
        @ColumnName("mano")
        String mano,
        @ColumnName("fecha_nacimiento")
        String fecha_nacimiento,
        @ColumnName("created_at")
        String created_at,
        @ColumnName("updated_at")
        String updated_at,
        @ColumnName("is_deleted")
        Boolean is_deleted
) {
}