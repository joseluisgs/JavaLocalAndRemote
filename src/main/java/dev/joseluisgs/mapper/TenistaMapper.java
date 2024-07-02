package dev.joseluisgs.mapper;

import dev.joseluisgs.database.TenistaEntity;
import dev.joseluisgs.dto.TenistaDto;
import dev.joseluisgs.models.Tenista;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mapeador de Tenista
 */
public class TenistaMapper {

    public static TenistaDto toTenistaDto(Tenista tenista) {
        return new TenistaDto(
                tenista.getId(),
                tenista.getNombre(),
                tenista.getPais(),
                tenista.getAltura(),
                tenista.getPeso(),
                tenista.getPuntos(),
                tenista.getMano().name(),
                tenista.getFechaNacimiento().toString(),
                tenista.getCreatedAt().toString(),
                tenista.getUpdatedAt().toString(),
                tenista.isDeleted()
        );
    }

    public static Tenista toTenista(TenistaDto tenistaDto) {
        return new Tenista(
                tenistaDto.id(),
                tenistaDto.nombre(),
                tenistaDto.pais(),
                tenistaDto.altura(),
                tenistaDto.peso(),
                tenistaDto.puntos(),
                Tenista.Mano.valueOf(tenistaDto.mano()),
                LocalDate.parse(tenistaDto.fechaNacimiento()),
                tenistaDto.createdAt() != null ? LocalDateTime.parse(tenistaDto.createdAt()) : LocalDateTime.now(),
                tenistaDto.updatedAt() != null ? LocalDateTime.parse(tenistaDto.updatedAt()) : LocalDateTime.now(),
                tenistaDto.isDeleted() != null ? tenistaDto.isDeleted() : false
        );
    }

    public static TenistaEntity toTenistaEntity(Tenista tenista) {
        return new TenistaEntity(
                tenista.getId(),
                tenista.getNombre(),
                tenista.getPais(),
                tenista.getAltura(),
                tenista.getPeso(),
                tenista.getPuntos(),
                tenista.getMano().name(),
                tenista.getFechaNacimiento().toString(),
                tenista.getCreatedAt().toString(),
                tenista.getUpdatedAt().toString(),
                tenista.isDeleted()
        );
    }

    public static Tenista toTenista(TenistaEntity tenistaEntity) {
        return new Tenista(
                tenistaEntity.id(),
                tenistaEntity.nombre(),
                tenistaEntity.pais(),
                tenistaEntity.altura(),
                tenistaEntity.peso(),
                tenistaEntity.puntos(),
                Tenista.Mano.valueOf(tenistaEntity.mano()),
                LocalDate.parse(tenistaEntity.fecha_nacimiento()),
                tenistaEntity.created_at() != null ? LocalDateTime.parse(tenistaEntity.created_at()) : LocalDateTime.now(),
                tenistaEntity.updated_at() != null ? LocalDateTime.parse(tenistaEntity.updated_at()) : LocalDateTime.now(),
                tenistaEntity.is_deleted() != null ? tenistaEntity.is_deleted() : false
        );
    }
}
