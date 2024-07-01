package dev.joseluisgs.mapper;

import dev.joseluisgs.dto.TenistaDto;
import dev.joseluisgs.models.Tenista;
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
    void toTenistaDto() {
        TenistaDto dto = TenistaMapper.toTenistaDto(tenistaTest);

        assertAll(
                () -> assertEquals(tenistaTest.getId(), dto.id()),
                () -> assertEquals(tenistaTest.getNombre(), dto.nombre()),
                () -> assertEquals(tenistaTest.getPais(), dto.pais()),
                () -> assertEquals(tenistaTest.getAltura(), dto.altura()),
                () -> assertEquals(tenistaTest.getPeso(), dto.peso()),
                () -> assertEquals(tenistaTest.getPuntos(), dto.puntos()),
                () -> assertEquals(tenistaTest.getMano().name(), dto.mano()),
                () -> assertEquals(tenistaTest.getFechaNacimiento(), LocalDate.parse(dto.fechaNacimiento())),
                () -> assertEquals(tenistaTest.getCreatedAt(), LocalDateTime.parse(dto.createdAt())),
                () -> assertEquals(tenistaTest.getUpdatedAt(), LocalDateTime.parse(dto.updatedAt())),
                () -> assertEquals(tenistaTest.isDeleted(), dto.isDeleted())
        );
    }

    @Test
    void toTenista() {
        TenistaDto dto = new TenistaDto(
                1L, "Roger Federer", "Suiza", 185, 85, 8000, "DIESTRO",
                "1981-08-08", "2023-01-01T00:00:00", "2023-01-01T00:00:00", false
        );

        Tenista tenista = TenistaMapper.toTenista(dto);

        assertAll(
                () -> assertEquals(dto.id(), tenista.getId()),
                () -> assertEquals(dto.nombre(), tenista.getNombre()),
                () -> assertEquals(dto.pais(), tenista.getPais()),
                () -> assertEquals(dto.altura(), tenista.getAltura()),
                () -> assertEquals(dto.peso(), tenista.getPeso()),
                () -> assertEquals(dto.puntos(), tenista.getPuntos()),
                () -> assertEquals(dto.mano(), tenista.getMano().name()),
                () -> assertEquals(LocalDate.parse(dto.fechaNacimiento()), tenista.getFechaNacimiento()),
                () -> assertEquals(LocalDateTime.parse(dto.createdAt()), tenista.getCreatedAt()),
                () -> assertEquals(LocalDateTime.parse(dto.updatedAt()), tenista.getUpdatedAt()),
                () -> assertEquals(dto.isDeleted(), tenista.isDeleted())
        );
    }
}