package dev.joseluisgs.models;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Podemos usar Lombok para generar los métodos de acceso
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

    // Mi me gusta mucho usar la interfaz fluida en setter.

    public static Tenista of(long id, @NonNull String nombre, @NonNull String pais, int altura, int peso, int puntos, @NonNull Mano mano, @NonNull LocalDate fechaNacimiento, @NonNull LocalDateTime createdAt, @NonNull LocalDateTime updatedAt, boolean isDeleted) {
        return new Tenista(id, nombre, pais, altura, peso, puntos, mano, fechaNacimiento, createdAt, updatedAt, isDeleted);
    }

    public static Tenista of() {
        return new Tenista();
    }

    public Tenista id(long id) {
        this.id = id;
        return this;
    }

    public Tenista nombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public Tenista pais(String pais) {
        this.pais = pais;
        return this;
    }

    public Tenista altura(int altura) {
        this.altura = altura;
        return this;
    }

    public Tenista peso(int peso) {
        this.peso = peso;
        return this;
    }

    public Tenista puntos(int puntos) {
        this.puntos = puntos;
        return this;
    }

    public Tenista mano(Mano mano) {
        this.mano = mano;
        return this;
    }

    public Tenista fechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
        return this;
    }

    public Tenista createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Tenista updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }


    public enum Mano {
        DIESTRO,
        ZURDO
    }
    

}
