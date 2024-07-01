package dev.joseluisgs.models;

import lombok.*;

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
    @NonNull
    private String nombre;
    @NonNull
    private String pais;
    private int altura;
    private int peso;
    private int puntos;
    @NonNull
    private Mano mano;
    @NonNull
    private LocalDate fechaNacimiento;
    @Builder.Default // Valor por defecto
    @NonNull
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default // Valor por defecto
    @NonNull
    private LocalDateTime updatedAt = LocalDateTime.now();
    @Builder.Default // Valor por defecto
    private boolean isDeleted = false;

    public enum Mano {
        DIESTRO,
        ZURDO
    }

}
