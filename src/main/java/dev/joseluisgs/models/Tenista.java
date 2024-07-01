package dev.joseluisgs.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data // Data class con getters, setters, equals, hashcode, toString
@AllArgsConstructor // Constructor con todos los argumentos
@NoArgsConstructor // Constructor vacío
@Builder // Patrón Builder
public class Tenista {
    public static final long NEW_ID = 0L;

    @Builder.Default // Valor por defecto
    private long id = NEW_ID;
    private String nombre;
    private String pais;
    private int altura;
    private int peso;
    private int puntos;
    private Mano mano;
    private LocalDate fechaNacimiento;
    @Builder.Default // Valor por defecto
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default // Valor por defecto
    private LocalDateTime updatedAt = LocalDateTime.now();
    @Builder.Default // Valor por defecto
    private boolean isDeleted = false;

    public enum Mano {
        DIESTRO,
        ZURDO
    }

}
