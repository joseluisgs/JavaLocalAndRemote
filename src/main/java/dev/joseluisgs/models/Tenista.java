package dev.joseluisgs.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tenista {
    public static final long NEW_ID = 0L;

    private long id = NEW_ID;
    private String nombre;
    private String pais;
    private int altura;
    private int peso;
    private int puntos;
    private Mano mano;
    private LocalDate fechaNacimiento;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private boolean isDeleted = false;

    public enum Mano {
        DIESTRO,
        ZURDO
    }
    
}
