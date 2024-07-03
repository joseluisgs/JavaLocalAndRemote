package dev.joseluisgs.mapper;

import dev.joseluisgs.database.TenistaEntity;
import dev.joseluisgs.dto.TenistaDto;
import dev.joseluisgs.models.Tenista;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TenistaMapperTest {

    private final Tenista tenistaTest = new Tenista(
            1L, "Roger Federer", "Suiza", 185, 85, 8000, Tenista.Mano.DIESTRO,
            LocalDate.parse("1981-08-08"), LocalDateTime.parse("2023-01-01T00:00:00"), LocalDateTime.parse("2023-01-01T00:00:00"), false
    );

    @Test
    @DisplayName("Debería mapear de Tenista a TenistaDto")
    void fromTenistaToTenistaDto() {
        TenistaDto dto = TenistaMapper.toTenistaDto(tenistaTest);

        assertAll(
                "Verificar que los atributos se mapean correctamente de Tenista a TenistaDto",
                () -> assertEquals(tenistaTest.getId(), dto.id(), "El ID debería coincidir"),
                () -> assertEquals(tenistaTest.getNombre(), dto.nombre(), "El nombre debería coincidir"),
                () -> assertEquals(tenistaTest.getPais(), dto.pais(), "El país debería coincidir"),
                () -> assertEquals(tenistaTest.getAltura(), dto.altura(), "La altura debería coincidir"),
                () -> assertEquals(tenistaTest.getPeso(), dto.peso(), "El peso debería coincidir"),
                () -> assertEquals(tenistaTest.getPuntos(), dto.puntos(), "Los puntos deberían coincidir"),
                () -> assertEquals(tenistaTest.getMano().name(), dto.mano(), "La mano debería coincidir"),
                () -> assertEquals(tenistaTest.getFechaNacimiento(), LocalDate.parse(dto.fechaNacimiento()), "La fecha de nacimiento debería coincidir"),
                () -> assertEquals(tenistaTest.getCreatedAt(), LocalDateTime.parse(dto.createdAt()), "La fecha de creación debería coincidir"),
                () -> assertEquals(tenistaTest.getUpdatedAt(), LocalDateTime.parse(dto.updatedAt()), "La fecha de actualización debería coincidir"),
                () -> assertEquals(tenistaTest.isDeleted(), dto.isDeleted(), "El estado de eliminado debería coincidir")
        );
    }

    @Test
    @DisplayName("Debería mapear de TenistaDto a Tenista")
    void fromTenistaDtoToTenista() {
        TenistaDto dto = new TenistaDto(
                1L, "Roger Federer", "Suiza", 185, 85, 8000, "DIESTRO",
                "1981-08-08", "2023-01-01T00:00:00", "2023-01-01T00:00:00", false
        );

        Tenista tenista = TenistaMapper.toTenista(dto);

        assertAll(
                "Verificar que los atributos se mapean correctamente de TenistaDto a Tenista",
                () -> assertEquals(dto.id(), tenista.getId(), "El ID debería coincidir"),
                () -> assertEquals(dto.nombre(), tenista.getNombre(), "El nombre debería coincidir"),
                () -> assertEquals(dto.pais(), tenista.getPais(), "El país debería coincidir"),
                () -> assertEquals(dto.altura(), tenista.getAltura(), "La altura debería coincidir"),
                () -> assertEquals(dto.peso(), tenista.getPeso(), "El peso debería coincidir"),
                () -> assertEquals(dto.puntos(), tenista.getPuntos(), "Los puntos deberían coincidir"),
                () -> assertEquals(dto.mano(), tenista.getMano().name(), "La mano debería coincidir"),
                () -> assertEquals(LocalDate.parse(dto.fechaNacimiento()), tenista.getFechaNacimiento(), "La fecha de nacimiento debería coincidir"),
                () -> assertEquals(LocalDateTime.parse(dto.createdAt()), tenista.getCreatedAt(), "La fecha de creación debería coincidir"),
                () -> assertEquals(LocalDateTime.parse(dto.updatedAt()), tenista.getUpdatedAt(), "La fecha de actualización debería coincidir"),
                () -> assertEquals(dto.isDeleted(), tenista.isDeleted(), "El estado de eliminado debería coincidir")
        );
    }

    @Test
    @DisplayName("Debería mapear de Tenista a TenistaEntity")
    void fromTenistaToTenistaEntity() {
        TenistaEntity tenista = TenistaMapper.toTenistaEntity(tenistaTest);

        assertAll(
                "Verificar que los atributos se mapean correctamente de Tenista a TenistaEntity",
                () -> assertEquals(tenistaTest.getId(), tenista.id(), "El ID debería coincidir"),
                () -> assertEquals(tenistaTest.getNombre(), tenista.nombre(), "El nombre debería coincidir"),
                () -> assertEquals(tenistaTest.getPais(), tenista.pais(), "El país debería coincidir"),
                () -> assertEquals(tenistaTest.getAltura(), tenista.altura(), "La altura debería coincidir"),
                () -> assertEquals(tenistaTest.getPeso(), tenista.peso(), "El peso debería coincidir"),
                () -> assertEquals(tenistaTest.getPuntos(), tenista.puntos(), "Los puntos deberían coincidir"),
                () -> assertEquals(tenistaTest.getMano().name(), tenista.mano(), "La mano debería coincidir"),
                () -> assertEquals(tenistaTest.getFechaNacimiento().toString(), tenista.fecha_nacimiento(), "La fecha de nacimiento debería coincidir"),
                () -> assertEquals(tenistaTest.getCreatedAt().toString(), tenista.created_at(), "La fecha de creación debería coincidir"),
                () -> assertEquals(tenistaTest.getUpdatedAt().toString(), tenista.updated_at(), "La fecha de actualización debería coincidir"),
                () -> assertEquals(tenistaTest.isDeleted(), tenista.is_deleted(), "El estado de eliminado debería coincidir")
        );
    }

    @Test
    @DisplayName("Debería mapear de TenistaEntity a Tenista")
    void fromTenistaEntityToTenista() {
        TenistaEntity tenista = new TenistaEntity(
                1L, "Roger Federer", "Suiza", 185, 85, 8000, "DIESTRO",
                "1981-08-08", "2023-01-01T00:00:00", "2023-01-01T00:00:00", false
        );

        Tenista tenistaTest = TenistaMapper.toTenista(tenista);

        assertAll(
                "Verificar que los atributos se mapean correctamente de TenistaEntity a Tenista",
                () -> assertEquals(tenista.id(), tenistaTest.getId(), "El ID debería coincidir"),
                () -> assertEquals(tenista.nombre(), tenistaTest.getNombre(), "El nombre debería coincidir"),
                () -> assertEquals(tenista.pais(), tenistaTest.getPais(), "El país debería coincidir"),
                () -> assertEquals(tenista.altura(), tenistaTest.getAltura(), "La altura debería coincidir"),
                () -> assertEquals(tenista.peso(), tenistaTest.getPeso(), "El peso debería coincidir"),
                () -> assertEquals(tenista.puntos(), tenistaTest.getPuntos(), "Los puntos deberían coincidir"),
                () -> assertEquals(tenista.mano(), tenistaTest.getMano().name(), "La mano debería coincidir"),
                () -> assertEquals(LocalDate.parse(tenista.fecha_nacimiento()), tenistaTest.getFechaNacimiento(), "La fecha de nacimiento debería coincidir"),
                () -> assertEquals(LocalDateTime.parse(tenista.created_at()), tenistaTest.getCreatedAt(), "La fecha de creación debería coincidir"),
                () -> assertEquals(LocalDateTime.parse(tenista.updated_at()), tenistaTest.getUpdatedAt(), "La fecha de actualización debería coincidir"),
                () -> assertEquals(tenista.is_deleted(), tenistaTest.isDeleted(), "El estado de eliminado debería coincidir")
        );
    }
}
