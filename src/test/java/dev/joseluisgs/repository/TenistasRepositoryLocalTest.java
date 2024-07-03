package dev.joseluisgs.repository;

import dev.joseluisgs.database.JdbiManager;
import dev.joseluisgs.database.TenistaEntity;
import dev.joseluisgs.database.TenistasDao;
import dev.joseluisgs.error.TenistaError;
import dev.joseluisgs.mapper.TenistaMapper;
import dev.joseluisgs.models.Tenista;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TenistasRepositoryLocalTest {

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
    private final TenistaEntity tenistaEntityTest = TenistaMapper.toTenistaEntity(tenistaTest);

    @Mock
    TenistasDao dao;
    @Mock
    JdbiManager<TenistasDao> db;
    @InjectMocks
    TenistasRepositoryLocal repository;

    @BeforeEach
    void setUp() {

        // Definimos el comportamiento de los métodos del mock de JdbiManager

        // En este ejemplo, lenient().when(...) se usa para hacer que Mockito ignore el stubbing
        // innecesario si no se usa en las pruebas.

        // Para cuando usemos el método with de JdbiManager
        lenient().when(db.with(any())).thenAnswer(invocation -> {
            JdbiManager.HandleFunction<TenistasDao, TenistaEntity> function = invocation.getArgument(0);
            return function.apply(dao);
        });

        // Para cuando usemos el método use de JdbiManager
        lenient().doAnswer(invocation -> {
            JdbiManager.VoidHandleFunction<TenistasDao> function = invocation.getArgument(0);
            function.apply(dao);
            return null;  // Retornar null ya que el método es void
        }).when(db).use(any());


        // Para cuando usemos el método withTransaction de JdbiManager
        lenient().when(db.withTransaction(any())).thenAnswer(invocation -> {
            JdbiManager.TransactionFunction<TenistasDao, TenistaEntity> function = invocation.getArgument(0);
            return function.apply(dao);
        });

        // Para cuando usemos el método useTransaction de JdbiManager
        lenient().doAnswer(invocation -> {
            JdbiManager.VoidTransactionFunction<TenistasDao> function = invocation.getArgument(0);
            function.apply(dao);
            return null;  // Retornar null ya que el método es void
        }).when(db).useTransaction(any());

    }

    @Test
    @DisplayName("Obteniendo todos los tenistas exitosamente")
    void getAll_TenistasExitosamente() {
        // Arrange
        List<TenistaEntity> tenistasList = List.of(tenistaEntityTest);

        when(dao.selectAll()).thenReturn(tenistasList);

        // Act
        Optional<Either<TenistaError, List<Tenista>>> result = repository.getAll().blockOptional();

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent(), "El resultado no debe ser nulo"),
                () -> assertTrue(result.get().isRight(), "El resultado debe ser correcto"),
                () -> assertEquals(1, result.get().get().size(), "Debe haber un solo tenista"),
                () -> assertEquals("Roger Federer", result.get().get().getFirst().getNombre(), "El nombre del tenista debe ser Roger Federer")
        );

        // Verificamos que se ha llamado al método selectAll del dao 1 vez
        verify(dao, times(1)).selectAll();
    }


    @Test
    @DisplayName("Obteniendo tenista por ID exitosamente")
    void getById_TenistaExitosamente() {
        // Arrange
        when(dao.selectById(1L)).thenReturn(Optional.of(tenistaEntityTest));

        // Act
        Optional<Either<TenistaError, Tenista>> result = repository.getById(1L).blockOptional();

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent(), "El resultado no debe ser nulo"),
                () -> assertTrue(result.get().isRight(), "El resultado debe ser correcto"),
                () -> assertEquals("Roger Federer", result.get().get().getNombre(), "El nombre del tenista debe ser Roger Federer")
        );

        verify(dao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("Obteniendo tenista por ID no existente")
    void getById_TenistaNoExistente() {
        // Arrange
        when(dao.selectById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Either<TenistaError, Tenista>> result = repository.getById(1L).blockOptional();

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent(), "El resultado no debe ser nulo"),
                () -> assertTrue(result.get().isLeft(), "El resultado debe ser un error"),
                () -> assertEquals("ERROR: No se ha encontrado el tenista con id: 1", result.get().getLeft().getMessage(), "El mensaje de error debe ser correcto")
        );

        verify(dao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("Guardando tenista exitosamente")
    void save_TenistaExitosamente() {
        // Arrange
        when(dao.insert(
                eq(tenistaEntityTest.nombre()),
                eq(tenistaEntityTest.pais()),
                eq(tenistaEntityTest.altura()),
                eq(tenistaEntityTest.peso()),
                eq(tenistaEntityTest.puntos()),
                eq(tenistaEntityTest.mano()),
                eq(tenistaEntityTest.fecha_nacimiento()),
                anyString(), // Simplificamos this a anyString() para evitar problemas
                anyString()  // Simplificamos this a anyString() para evitar problemas
        )).thenReturn(1L);

        // Act
        Optional<Either<TenistaError, Tenista>> result = repository.save(tenistaTest).blockOptional();

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent(), "El resultado no debe ser nulo"),
                () -> assertTrue(result.get().isRight(), "El resultado debe ser correcto"),
                () -> assertEquals(1L, result.get().get().getId(), "El ID del tenista debe ser 1")
        );

        verify(dao, times(1)).insert(
                eq(tenistaEntityTest.nombre()),
                eq(tenistaEntityTest.pais()),
                eq(tenistaEntityTest.altura()),
                eq(tenistaEntityTest.peso()),
                eq(tenistaEntityTest.puntos()),
                eq(tenistaEntityTest.mano()),
                eq(tenistaEntityTest.fecha_nacimiento()),
                anyString(), // Simplificamos esto a anyString() para evitar problemas
                anyString()  // Simplificamos esto a anyString() para evitar problemas
        );
    }

    @Test
    @DisplayName("Actualizando tenista exitosamente")
    void update_TenistaExitosa() {
        // Arrange
        when(dao.update(
                eq(1L),
                eq(tenistaEntityTest.nombre()),
                eq(tenistaEntityTest.pais()),
                eq(tenistaEntityTest.altura()),
                eq(tenistaEntityTest.peso()),
                eq(tenistaEntityTest.puntos()),
                eq(tenistaEntityTest.mano()),
                eq(tenistaEntityTest.fecha_nacimiento()),
                anyString(),
                eq(tenistaEntityTest.is_deleted())))
                .thenReturn(1);

        // Act
        Optional<Either<TenistaError, Tenista>> result = repository.update(1L, tenistaTest).blockOptional();

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent(), "El resultado no debe ser nulo"),
                () -> assertTrue(result.get().isRight(), "El resultado debe ser correcto"),
                () -> assertEquals("Roger Federer", result.get().get().getNombre(), "El nombre del tenista debe ser Roger Federer")
        );

        verify(dao, times(1)).update(
                eq(1L),
                eq(tenistaEntityTest.nombre()),
                eq(tenistaEntityTest.pais()),
                eq(tenistaEntityTest.altura()),
                eq(tenistaEntityTest.peso()),
                eq(tenistaEntityTest.puntos()),
                eq(tenistaEntityTest.mano()),
                eq(tenistaEntityTest.fecha_nacimiento()),
                anyString(),
                eq(tenistaEntityTest.is_deleted()));
    }


    @Test
    @DisplayName("Actualizando tenista no existente")
    void update_TenistaNoExistente() {
        // Arrange
        when(dao.update(
                eq(1L),
                eq(tenistaEntityTest.nombre()),
                eq(tenistaEntityTest.pais()),
                eq(tenistaEntityTest.altura()),
                eq(tenistaEntityTest.peso()),
                eq(tenistaEntityTest.puntos()),
                eq(tenistaEntityTest.mano()),
                eq(tenistaEntityTest.fecha_nacimiento()),
                anyString(),
                eq(tenistaEntityTest.is_deleted())))
                .thenReturn(0);

        // Act
        Optional<Either<TenistaError, Tenista>> result = repository.update(1L, tenistaTest).blockOptional();

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent(), "El resultado no debe ser nulo"),
                () -> assertTrue(result.get().isLeft(), "El resultado debe ser un error"),
                () -> assertEquals("ERROR: No se ha encontrado el tenista con id: 1", result.get().getLeft().getMessage(), "El mensaje de error debe ser correcto")
        );

        verify(dao, times(1)).update(
                eq(1L),
                eq(tenistaEntityTest.nombre()),
                eq(tenistaEntityTest.pais()),
                eq(tenistaEntityTest.altura()),
                eq(tenistaEntityTest.peso()),
                eq(tenistaEntityTest.puntos()),
                eq(tenistaEntityTest.mano()),
                eq(tenistaEntityTest.fecha_nacimiento()),
                anyString(),
                eq(tenistaEntityTest.is_deleted()));
    }

    @Test
    @DisplayName("Borrando tenista exitosamente")
    void delete_TenistaExitosamente() {
        // Arrange
        when(dao.delete(1L)).thenReturn(1);

        // Act
        Optional<Either<TenistaError, Long>> result = repository.delete(1L).blockOptional();

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent(), "El resultado no debe ser nulo"),
                () -> assertTrue(result.get().isRight(), "El resultado debe ser correcto"),
                () -> assertEquals(1L, result.get().get(), "El ID del tenista borrado debe ser 1")
        );

        verify(dao, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Borrando tenista no existente")
    void delete_TenistaNoExistente() {
        // Arrange
        when(dao.delete(1L)).thenReturn(0);

        // Act
        Optional<Either<TenistaError, Long>> result = repository.delete(1L).blockOptional();

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent(), "El resultado no debe ser nulo"),
                () -> assertTrue(result.get().isLeft(), "El resultado debe ser un error"),
                () -> assertEquals("ERROR: No se ha encontrado el tenista con id: 1", result.get().getLeft().getMessage(), "El mensaje de error debe ser correcto")
        );

        verify(dao, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Borrando todos los tenistas exitosamente")
    void removeAll_TenistasExitosamente() {

        // Arrange
        doNothing().when(dao).removeAll();

        // Act
        Optional<Either<TenistaError, Void>> result = repository.removeAll().blockOptional();

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent(), "El resultado no debe ser nulo"),
                () -> assertTrue(result.get().isRight(), "El resultado debe ser correcto")
        );

        verify(dao, times(1)).removeAll();

    }

    @Test
    @DisplayName("Guardando todos los tenistas exitosamente")
    void saveAll_TenistasExitos() {
        // Arrange
        List<Tenista> tenistasList = List.of(tenistaTest);
        List<TenistaEntity> tenistaEntities = List.of(tenistaEntityTest);

        when(dao.insert(
                eq(tenistaEntityTest.nombre()),
                eq(tenistaEntityTest.pais()),
                eq(tenistaEntityTest.altura()),
                eq(tenistaEntityTest.peso()),  // Añade eq() para peso() también
                eq(tenistaEntityTest.puntos()),
                eq(tenistaEntityTest.mano()),
                eq(tenistaEntityTest.fecha_nacimiento()),
                anyString(),
                anyString()))
                .thenReturn(1L);

        // Act
        Optional<Either<TenistaError, Integer>> result = repository.saveAll(tenistasList).blockOptional();

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent(), "El resultado no debe ser nulo"),
                () -> assertTrue(result.get().isRight(), "El resultado debe ser correcto"),
                () -> assertEquals(1, result.get().get(), "El número de tenistas guardados debe ser 1")
        );

        verify(dao, times(1)).insert(
                eq(tenistaEntityTest.nombre()),
                eq(tenistaEntityTest.pais()),
                eq(tenistaEntityTest.altura()),
                eq(tenistaEntityTest.peso()),  // Añade eq() para peso() también en la verificación
                eq(tenistaEntityTest.puntos()),
                eq(tenistaEntityTest.mano()),
                eq(tenistaEntityTest.fecha_nacimiento()),
                anyString(),
                anyString());
    }


}