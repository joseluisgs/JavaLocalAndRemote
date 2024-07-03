package dev.joseluisgs.repository;

import dev.joseluisgs.dto.TenistaDto;
import dev.joseluisgs.mapper.TenistaMapper;
import dev.joseluisgs.models.Tenista;
import dev.joseluisgs.rest.TenistasApiRest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenistasRepositoryRemoteTest {

    private final Tenista tenistaTest = Tenista.builder()
            .id(1L)
            .nombre("Roger Federer")
            .pais("Suiza")
            .altura(185)
            .peso(85)
            .puntos(9600)
            .mano(Tenista.Mano.DIESTRO)
            .fechaNacimiento(LocalDate.of(1981, 8, 8))
            .build();
    private final TenistaDto tenistaEntityTest = TenistaMapper.toTenistaDto(tenistaTest);

    @Mock
    private TenistasApiRest rest;

    @InjectMocks
    private TenistasRepositoryRemote repository;

    @Test
    @DisplayName("Obtener todos los tenistas - Correcto")
    void testGetAllCorrecto() {
        // Arrange
        List<TenistaDto> tenistaDtos = List.of(tenistaEntityTest);
        when(rest.getAll()).thenReturn(Mono.just(tenistaDtos));

        // Act
        var result = repository.getAll().blockOptional();

        // Assert
        assertAll(
                "Verificación de obtener todos los tenistas",
                () -> assertTrue(result.isPresent(), "El resultado no debe ser nulo"),
                () -> assertTrue(result.get().isRight(), "El resultado debe ser correcto"),
                () -> assertEquals(1, result.get().get().size(), "Debe haber un solo tenista"),
                () -> assertEquals("Roger Federer", result.get().get().get(0).getNombre(), "El nombre del tenista debe ser Roger Federer")
        );

        // Verify
        verify(rest, times(1)).getAll();
    }

    @Test
    @DisplayName("Obteniendo tenista por ID exitosamente")
    void getById_TenistaExitosamente() {
        // Arrange
        when(rest.getById(1L)).thenReturn(Mono.just(tenistaEntityTest));

        // Act
        var result = repository.getById(1L).blockOptional();

        // Assert
        assertAll(
                "Verificación de obtener tenista por ID",
                () -> assertTrue(result.isPresent(), "El resultado no debe ser nulo"),
                () -> assertTrue(result.get().isRight(), "El resultado debe ser correcto"),
                () -> assertEquals("Roger Federer", result.get().get().getNombre(), "El nombre del tenista debe ser Roger Federer")
        );

        // Verify
        verify(rest, times(1)).getById(1L);
    }

    @Test
    @DisplayName("Obteniendo tenista por ID no existente - 404")
    void getById_TenistaNoExistente() {
        // Arrange
        when(rest.getById(1L)).thenReturn(Mono.error(new RuntimeException("404 NOT FOUND")));

        // Act
        var result = repository.getById(1L).blockOptional();

        // Assert
        assertAll(
                "Verificación de obtener tenista por ID no existente",
                () -> assertTrue(result.isPresent(), "El resultado no debe ser nulo"),
                () -> assertTrue(result.get().isLeft(), "El resultado debe ser un error"),
                () -> assertEquals("ERROR: No se ha encontrado tenista en la api rest con id 1 -> 404 NOT FOUND", result.get().getLeft().getMessage(), "El mensaje de error debe ser correcto")
        );

        // Verify
        verify(rest, times(1)).getById(1L);
    }

    @Test
    @DisplayName("Guardar tenista - Correcto")
    void save_TenistaCorrectamente() {
        // Arrange
        when(rest.save(any(TenistaDto.class))).thenReturn(Mono.just(tenistaEntityTest));

        // Act
        var result = repository.save(tenistaTest).blockOptional();

        // Assert
        assertAll(
                "Verificación de guardar tenista",
                () -> assertTrue(result.isPresent(), "El resultado no debe ser nulo"),
                () -> assertTrue(result.get().isRight(), "El resultado debe ser correcto"),
                () -> assertEquals(tenistaTest.getNombre(), result.get().get().getNombre(), "El nombre del tenista guardado debe ser igual al original")
        );

        // Verify
        verify(rest, times(1)).save(any(TenistaDto.class));
    }

    @Test
    @DisplayName("Actualizar tenista exitosamente")
    void update_TenistaCorrectamente() {
        // Arrange
        when(rest.update(anyLong(), any(TenistaDto.class))).thenReturn(Mono.just(tenistaEntityTest));

        // Act
        var result = repository.update(1L, tenistaTest).blockOptional();

        // Assert
        assertAll(
                "Verificación de actualizar tenista",
                () -> assertTrue(result.isPresent(), "El resultado no debe ser nulo"),
                () -> assertTrue(result.get().isRight(), "El resultado debe ser correcto"),
                () -> assertEquals(tenistaTest.getNombre(), result.get().get().getNombre(), "El nombre del tenista actualizado debe ser igual al original")
        );

        // Verify
        verify(rest, times(1)).update(anyLong(), any(TenistaDto.class));
    }

    @Test
    @DisplayName("Actualizar tenista falla devolviendo 404")
    void update_TenistaFalla() {
        // Arrange
        when(rest.update(anyLong(), any(TenistaDto.class))).thenReturn(Mono.error(new RuntimeException("404 NOT FOUND")));

        // Act
        var result = repository.update(1L, tenistaTest).blockOptional();

        // Assert
        assertAll(
                "Verificación de fallo al actualizar tenista",
                () -> assertTrue(result.isPresent(), "El resultado no debe ser nulo"),
                () -> assertTrue(result.get().isLeft(), "El resultado debe ser un error"),
                () -> assertEquals("ERROR: No se ha actualizando tenista en la api rest con id 1 -> 404 NOT FOUND", result.get().getLeft().getMessage(), "El mensaje de error debe ser correcto")
        );

        // Verify
        verify(rest, times(1)).update(anyLong(), any(TenistaDto.class));
    }

    @Test
    @DisplayName("Eliminar tenista - Correcto")
    void delete_TenistaCorrectamente() {
        // Arrange
        when(rest.delete(anyLong())).thenReturn(Mono.empty());

        // Act
        var result = repository.delete(1L).blockOptional();

        // Assert
        assertAll(
                "Verificación de eliminar tenista",
                () -> assertTrue(result.isPresent(), "El resultado no debe ser nulo"),
                () -> assertTrue(result.get().isRight(), "El resultado debe ser correcto"),
                () -> assertEquals(1L, result.get().get().longValue(), "El id del tenista eliminado debe ser 1")
        );

        // Verify
        verify(rest, times(1)).delete(anyLong());
    }

    @Test
    @DisplayName("Eliminar tenista falla devolviendo 404")
    void delete_TenistaFalla() {
        // Arrange
        when(rest.delete(anyLong())).thenReturn(Mono.error(new RuntimeException("404 NOT FOUND")));

        // Act
        var result = repository.delete(1L).blockOptional();

        // Assert
        assertAll(
                "Verificación de fallo al eliminar tenista",
                () -> assertTrue(result.isPresent(), "El resultado no debe ser nulo"),
                () -> assertTrue(result.get().isLeft(), "El resultado debe ser un error"),
                () -> assertEquals("ERROR: No se ha borrando tenista en la api rest con id 1 -> 404 NOT FOUND", result.get().getLeft().getMessage(), "El mensaje de error debe ser correcto")
        );

        // Verify
        verify(rest, times(1)).delete(anyLong());
    }
}
