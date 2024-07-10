package dev.joseluisgs.service;

import dev.joseluisgs.cache.TenistasCache;
import dev.joseluisgs.error.TenistaError;
import dev.joseluisgs.models.Tenista;
import dev.joseluisgs.notification.TenistasNotifications;
import dev.joseluisgs.repository.TenistasRepositoryLocal;
import dev.joseluisgs.repository.TenistasRepositoryRemote;
import dev.joseluisgs.storage.TenistasStorageCsv;
import dev.joseluisgs.storage.TenistasStorageJson;
import io.vavr.control.Either;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenistasServiceImplTest {
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
    @Mock
    private TenistasRepositoryLocal localRepository;
    @Mock
    private TenistasRepositoryRemote remoteRepository;
    @Mock
    private TenistasCache cache;
    @Mock
    private TenistasStorageCsv csvStorage;
    @Mock
    private TenistasStorageJson jsonStorage;
    @Mock
    private TenistasNotifications notificationsService;
    @InjectMocks
    private TenistasServiceImpl service;

    @Test
    @DisplayName("Obtener todos los tenistas desde repositorio local")
    void obtenerTodosDesdeRepositorioLocal() {

        when(localRepository.getAll()).thenReturn(Mono.just(Either.right(List.of(tenistaTest))));

        var resultado = service.getAll(false).blockOptional();

        assertAll("Verificación de obtener todos los tenistas",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isRight(), "Se ha obtenido resultado correcto"),
                () -> assertEquals(1, resultado.get().get().size(), "El número de tenistas correcto")
        );

        verify(localRepository, times(1)).getAll();
        verify(remoteRepository, times(0)).getAll();
    }

    @Test
    @DisplayName("Obtener todos los tenistas desde repositorio remoto")
    void obtenerTodosDesdeRepositorioRemoto() {

        when(remoteRepository.getAll()).thenReturn(Mono.just(Either.right(List.of(tenistaTest))));
        when(localRepository.removeAll()).thenReturn(Mono.just(Either.right(null)));
        when(localRepository.saveAll(any())).thenReturn(Mono.just(Either.right(1)));
        when(localRepository.getAll()).thenReturn(Mono.just(Either.right(List.of(tenistaTest))));

        var resultado = service.getAll(true).blockOptional();

        assertAll("Verificación de obtener todos los tenistas",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isRight(), "Se ha obtenido resultado correcto"),
                () -> assertEquals(1, resultado.get().get().size(), "El número de tenistas correcto")
        );

        verify(localRepository, times(1)).removeAll();
        verify(remoteRepository, times(2)).getAll();
        verify(localRepository, times(1)).saveAll(any());
        verify(localRepository, times(1)).getAll();
    }

    @Test
    @DisplayName("Obtener tenista por ID existente en cache")
    void obtenerTenistaPorIdEnCache() {
        when(cache.get(tenistaTest.getId())).thenReturn(tenistaTest);

        var resultado = service.getById(tenistaTest.getId()).blockOptional();

        assertAll("Verificación de obtener tenista por ID",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isRight(), "Se ha obtenido resultado correcto"),
                () -> assertEquals(tenistaTest, resultado.get().get(), "El tenista obtenido es correcto")
        );

        verify(cache, times(1)).get(tenistaTest.getId());
        verify(localRepository, times(0)).getById(tenistaTest.getId());
        verify(remoteRepository, times(0)).getById(tenistaTest.getId());
    }

    @Test
    @DisplayName("Obtener tenista por ID no existente en cache pero en local")
    void obtenerTenistaPorIdEnLocal() {
        when(cache.get(tenistaTest.getId())).thenReturn(null);
        when(localRepository.getById(tenistaTest.getId())).thenReturn(Mono.just(Either.right(tenistaTest)));

        var resultado = service.getById(tenistaTest.getId()).blockOptional();

        assertAll("Verificación de obtener tenista por ID",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isRight(), "Se ha obtenido resultado correcto"),
                () -> assertEquals(tenistaTest, resultado.get().get(), "El tenista obtenido es correcto")
        );

        verify(cache, times(1)).get(tenistaTest.getId());
        verify(localRepository, times(1)).getById(tenistaTest.getId());
        verify(remoteRepository, times(0)).getById(tenistaTest.getId());
    }

    @Test
    @DisplayName("Obtener tenista por ID no existente en cache ni en local")
    void obtenerTenistaPorIdEnRemoto() {
        when(cache.get(tenistaTest.getId())).thenReturn(null);
        when(localRepository.getById(tenistaTest.getId())).thenReturn(Mono.just(Either.left(null)));
        when(remoteRepository.getById(tenistaTest.getId())).thenReturn(Mono.just(Either.right(tenistaTest)));
        when(localRepository.save(tenistaTest)).thenReturn(Mono.just(Either.right(tenistaTest)));
        doNothing().when(cache).put(tenistaTest.getId(), tenistaTest);

        var resultado = service.getById(tenistaTest.getId()).blockOptional();

        assertAll("Verificación de obtener tenista por ID",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isRight(), "Se ha obtenido resultado correcto"),
                () -> assertEquals(tenistaTest, resultado.get().get(), "El tenista obtenido es correcto")
        );

        verify(cache, times(1)).get(tenistaTest.getId());
        verify(localRepository, times(1)).getById(tenistaTest.getId());
        verify(remoteRepository, times(1)).getById(tenistaTest.getId());
        verify(localRepository, times(1)).save(tenistaTest);
        verify(cache, times(1)).put(tenistaTest.getId(), tenistaTest);

    }

    @Test
    @DisplayName("Obtener tenista por ID no existente en cache ni en local ni en remoto")
    void obtenerTenistaPorIdNoExistente() {
        when(cache.get(tenistaTest.getId())).thenReturn(null);
        when(localRepository.getById(tenistaTest.getId())).thenReturn(Mono.just(Either.left(null)));
        when(remoteRepository.getById(tenistaTest.getId())).thenReturn(Mono.just(Either.left(new TenistaError.NotFound(tenistaTest.getId()))));

        var resultado = service.getById(tenistaTest.getId()).blockOptional();

        assertAll("Verificación de obtener tenista por ID",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isLeft(), "Se ha obtenido resultado correcto"),
                () -> assertEquals("ERROR: No se ha encontrado el tenista con id: 1", resultado.get().getLeft().getMessage(), "El mensaje de error es correcto")
        );

        verify(cache, times(1)).get(tenistaTest.getId());
        verify(localRepository, times(1)).getById(tenistaTest.getId());
        verify(remoteRepository, times(1)).getById(tenistaTest.getId());
        verify(localRepository, times(0)).save(tenistaTest);
        verify(cache, times(0)).put(tenistaTest.getId(), tenistaTest);
    }

    @Test
    @DisplayName("Guardar tenista correctamente")
    void guardarTenistaCorrectamente() {
        when(remoteRepository.save(tenistaTest)).thenReturn(Mono.just(Either.right(tenistaTest)));
        when(localRepository.save(tenistaTest)).thenReturn(Mono.just(Either.right(tenistaTest)));
        doNothing().when(cache).put(tenistaTest.getId(), tenistaTest);

        var resultado = service.save(tenistaTest).blockOptional();

        assertAll("Verificación de guardar tenista",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isRight(), "Se ha obtenido resultado correcto"),
                () -> assertEquals(tenistaTest, resultado.get().get(), "El tenista guardado es correcto")
        );

        verify(localRepository, times(1)).save(tenistaTest);
        verify(remoteRepository, times(1)).save(tenistaTest);
        verify(cache, times(1)).put(tenistaTest.getId(), tenistaTest);
    }

    @Test
    @DisplayName("Guardar tenista con error de validación")
    void guardarTenistaCorrectamenteConErrorDeValidacion() {
        var newTenista = Tenista.builder()
                .id(1L)
                .nombre("Roger Federer")
                .pais("Suiza")
                .altura(185)
                .peso(85)
                .puntos(-10)
                .mano(Tenista.Mano.DIESTRO)
                .fechaNacimiento(LocalDate.of(1981, 8, 8))
                .build();

        var resultado = service.save(newTenista).blockOptional();

        assertAll("Verificación de guardar tenista",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isLeft(), "Se ha obtenido resultado correcto"),
                () -> assertEquals("ERROR: Los puntos del tenista no pueden ser nulos o menores a 0", resultado.get().getLeft().getMessage(), "El mensaje de error es correcto")
        );

        verify(localRepository, times(0)).save(tenistaTest);
        verify(remoteRepository, times(0)).save(tenistaTest);
        verify(cache, times(0)).put(tenistaTest.getId(), tenistaTest);
    }

    @Test
    @DisplayName("Guardar tenista debe retornar error si no se puede guardar remotamente")
    void guardarTenistaConErrorEnRemoto() {
        when(remoteRepository.save(tenistaTest)).thenReturn(Mono.just(Either.left(new TenistaError.RemoteError("Error al guardar en remoto"))));

        var resultado = service.save(tenistaTest).blockOptional();

        assertAll("Verificación de guardar tenista",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isLeft(), "Se ha obtenido resultado correcto"),
                () -> assertEquals("ERROR: Error al guardar en remoto", resultado.get().getLeft().getMessage(), "El mensaje de error es correcto")
        );

        verify(localRepository, times(0)).save(tenistaTest);
        verify(remoteRepository, times(1)).save(tenistaTest);
        verify(cache, times(0)).put(tenistaTest.getId(), tenistaTest);
    }

    @Test
    @DisplayName("Actualizar tenista debe retornar tenista actualizado")
    void actualizarTenistaCorrectamente() {
        when(cache.get(tenistaTest.getId())).thenReturn(tenistaTest);
        when(remoteRepository.update(tenistaTest.getId(), tenistaTest)).thenReturn(Mono.just(Either.right(tenistaTest)));
        when(localRepository.update(tenistaTest.getId(), tenistaTest)).thenReturn(Mono.just(Either.right(tenistaTest)));
        doNothing().when(cache).put(tenistaTest.getId(), tenistaTest);

        var resultado = service.update(tenistaTest.getId(), tenistaTest).blockOptional();

        assertAll("Verificación de actualizar tenista",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isRight(), "Se ha obtenido resultado correcto"),
                () -> assertEquals(tenistaTest, resultado.get().get(), "El tenista actualizado es correcto")
        );

        verify(localRepository, times(1)).update(tenistaTest.getId(), tenistaTest);
        verify(remoteRepository, times(1)).update(tenistaTest.getId(), tenistaTest);
        verify(cache, times(1)).put(tenistaTest.getId(), tenistaTest);
    }

    @Test
    @DisplayName("Actualizar tenista debe retornar error si no se puede actualizar por validación")
    void actualizarTenistaConErrorDeValidacion() {
        var newTenista = Tenista.builder()
                .id(1L)
                .nombre("Roger Federer")
                .pais("Suiza")
                .altura(185)
                .peso(85)
                .puntos(-10)
                .mano(Tenista.Mano.DIESTRO)
                .fechaNacimiento(LocalDate.of(1981, 8, 8))
                .build();

        var resultado = service.update(newTenista.getId(), newTenista).blockOptional();

        assertAll("Verificación de actualizar tenista",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isLeft(), "Se ha obtenido resultado correcto"),
                () -> assertEquals("ERROR: Los puntos del tenista no pueden ser nulos o menores a 0", resultado.get().getLeft().getMessage(), "El mensaje de error es correcto")
        );

        verify(localRepository, times(0)).update(tenistaTest.getId(), tenistaTest);
        verify(remoteRepository, times(0)).update(tenistaTest.getId(), tenistaTest);
        verify(cache, times(0)).put(tenistaTest.getId(), tenistaTest);
    }

    @Test
    @DisplayName("Actualizar tenista debe retornar error si no se puede actualizar remotamente")
    void actualizarTenistaConErrorEnRemoto() {
        when(cache.get(tenistaTest.getId())).thenReturn(tenistaTest);
        when(remoteRepository.update(tenistaTest.getId(), tenistaTest)).thenReturn(Mono.just(Either.left(new TenistaError.RemoteError("Error al actualizar en remoto"))));

        var resultado = service.update(tenistaTest.getId(), tenistaTest).blockOptional();

        assertAll("Verificación de actualizar tenista",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isLeft(), "Se ha obtenido resultado correcto"),
                () -> assertEquals("ERROR: Error al actualizar en remoto", resultado.get().getLeft().getMessage(), "El mensaje de error es correcto")
        );

        verify(localRepository, times(0)).update(tenistaTest.getId(), tenistaTest);
        verify(remoteRepository, times(1)).update(tenistaTest.getId(), tenistaTest);
        verify(cache, times(0)).put(tenistaTest.getId(), tenistaTest);
    }

    @Test
    @DisplayName("Actualizar tenista debe retornar error porque el tenista no existe remotamente")
    void actualizarTenistaNoExistenteEnRemoto() {
        when(cache.get(tenistaTest.getId())).thenReturn(tenistaTest);
        when(remoteRepository.update(tenistaTest.getId(), tenistaTest)).thenReturn(Mono.just(Either.left(new TenistaError.NotFound(tenistaTest.getId()))));

        var resultado = service.update(tenistaTest.getId(), tenistaTest).blockOptional();

        assertAll("Verificación de actualizar tenista",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isLeft(), "Se ha obtenido resultado correcto"),
                () -> assertEquals("ERROR: No se ha encontrado el tenista con id: 1", resultado.get().getLeft().getMessage(), "El mensaje de error es correcto")
        );

        verify(localRepository, times(0)).update(tenistaTest.getId(), tenistaTest);
        verify(remoteRepository, times(1)).update(tenistaTest.getId(), tenistaTest);
        verify(cache, times(0)).put(tenistaTest.getId(), tenistaTest);
    }

    @Test
    @DisplayName("Borrar tenista debe retornar tenista borrado")
    void borrarTenistaCorrectamente() {
        when(cache.get(tenistaTest.getId())).thenReturn(tenistaTest);
        when(remoteRepository.delete(tenistaTest.getId())).thenReturn(Mono.just(Either.right(tenistaTest.getId())));
        when(localRepository.delete(tenistaTest.getId())).thenReturn(Mono.just(Either.right(tenistaTest.getId())));
        doNothing().when(cache).remove(tenistaTest.getId());

        var resultado = service.delete(tenistaTest.getId()).blockOptional();

        assertAll("Verificación de borrar tenista",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isRight(), "Se ha obtenido resultado correcto"),
                () -> assertEquals(tenistaTest.getId(), resultado.get().get(), "El tenista borrado es correcto")
        );

        verify(localRepository, times(1)).delete(tenistaTest.getId());
        verify(remoteRepository, times(1)).delete(tenistaTest.getId());
        verify(cache, times(1)).remove(tenistaTest.getId());
    }

    @Test
    @DisplayName("Borrar tenista debe retornar error si no se puede borrar remotamente")
    void borrarTenistaConErrorEnRemoto() {
        when(cache.get(tenistaTest.getId())).thenReturn(tenistaTest);
        when(remoteRepository.delete(tenistaTest.getId())).thenReturn(Mono.just(Either.left(new TenistaError.RemoteError("Error al borrar en remoto"))));

        var resultado = service.delete(tenistaTest.getId()).blockOptional();

        assertAll("Verificación de borrar tenista",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isLeft(), "Se ha obtenido resultado correcto"),
                () -> assertEquals("ERROR: Error al borrar en remoto", resultado.get().getLeft().getMessage(), "El mensaje de error es correcto")
        );

        verify(localRepository, times(0)).delete(tenistaTest.getId());
        verify(remoteRepository, times(1)).delete(tenistaTest.getId());
        verify(cache, times(0)).remove(tenistaTest.getId());
    }

    @Test
    @DisplayName("Borrar tenista debe retornar error si no se puede borrar remotamente")
    void borrarTenistaNoExistenteEnRemoto() {
        when(cache.get(tenistaTest.getId())).thenReturn(tenistaTest);
        when(remoteRepository.delete(tenistaTest.getId())).thenReturn(Mono.just(Either.left(new TenistaError.NotFound(tenistaTest.getId()))));

        var resultado = service.delete(tenistaTest.getId()).blockOptional();

        assertAll("Verificación de borrar tenista",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isLeft(), "Se ha obtenido resultado correcto"),
                () -> assertEquals("ERROR: No se ha encontrado el tenista con id: 1", resultado.get().getLeft().getMessage(), "El mensaje de error es correcto")
        );

        verify(localRepository, times(0)).delete(tenistaTest.getId());
        verify(remoteRepository, times(1)).delete(tenistaTest.getId());
        verify(cache, times(0)).remove(tenistaTest.getId());
    }

    @Test
    @DisplayName("Importar debe devolver Ok para fichero CSV")
    void importarFicheroCSV() {
        var file = new File("test.csv");

        when(csvStorage.importFile(file)).thenReturn(Mono.just(Either.right(List.of(tenistaTest))));
        when(localRepository.removeAll()).thenReturn(Mono.just(Either.right(null)));
        when(remoteRepository.save(tenistaTest)).thenReturn(Mono.just(Either.right(tenistaTest)));
        when(localRepository.save(tenistaTest)).thenReturn(Mono.just(Either.right(tenistaTest)));

        var resultado = service.importData(file).blockOptional();

        assertAll("Verificación de importar fichero CSV",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isRight(), "Se ha obtenido resultado correcto"),
                () -> assertEquals(1, resultado.get().get(), "El número de tenistas importados es correcto")
        );

        verify(csvStorage, times(1)).importFile(file);
        verify(localRepository, times(1)).removeAll();
        verify(remoteRepository, times(1)).save(tenistaTest);
        verify(localRepository, times(1)).save(tenistaTest);
    }

    @Test
    @DisplayName("Importar debe devolver Ok para fichero JSON")
    void importarFicheroJSON() {
        var file = new File("test.json");

        when(jsonStorage.importFile(file)).thenReturn(Mono.just(Either.right(List.of(tenistaTest))));
        when(localRepository.removeAll()).thenReturn(Mono.just(Either.right(null)));
        when(remoteRepository.save(tenistaTest)).thenReturn(Mono.just(Either.right(tenistaTest)));
        when(localRepository.save(tenistaTest)).thenReturn(Mono.just(Either.right(tenistaTest)));

        var resultado = service.importData(file).blockOptional();

        assertAll("Verificación de importar fichero JSON",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isRight(), "Se ha obtenido resultado correcto"),
                () -> assertEquals(1, resultado.get().get(), "El número de tenistas importados es correcto")
        );

        verify(jsonStorage, times(1)).importFile(file);
        verify(localRepository, times(1)).removeAll();
        verify(remoteRepository, times(1)).save(tenistaTest);
        verify(localRepository, times(1)).save(tenistaTest);
    }

    @Test
    @DisplayName("Importar debe devolver error si no importar")
    void importarFicheroError() {
        var file = new File("test.json");

        when(jsonStorage.importFile(file)).thenReturn(Mono.just(Either.left(new TenistaError.StorageError("Error al importar"))));

        var resultado = service.importData(file).blockOptional();

        assertAll("Verificación de importar fichero JSON",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isLeft(), "Se ha obtenido resultado correcto"),
                () -> assertEquals("ERROR: Error al importar", resultado.get().getLeft().getMessage(), "El mensaje de error es correcto")
        );

        verify(jsonStorage, times(1)).importFile(file);
        verify(localRepository, times(0)).removeAll();
        verify(remoteRepository, times(0)).save(tenistaTest);
        verify(localRepository, times(0)).save(tenistaTest);
    }

    @Test
    @DisplayName("Exportar debe devolver Ok para fichero CSV de manera local")
    void exportarFicheroCSVLocal() {
        var file = new File("test.csv");

        when(localRepository.getAll()).thenReturn(Mono.just(Either.right(List.of(tenistaTest))));
        when(csvStorage.exportFile(file, List.of(tenistaTest))).thenReturn(Mono.just(Either.right(1)));

        var resultado = service.exportData(file, false).blockOptional();

        assertAll("Verificación de exportar fichero CSV",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isRight(), "Se ha obtenido resultado correcto"),
                () -> assertEquals(1, resultado.get().get(), "El fichero exportado es correcto")
        );

        verify(localRepository, times(1)).getAll();
        verify(csvStorage, times(1)).exportFile(file, List.of(tenistaTest));
    }

    @Test
    @DisplayName("Exportar debe devolver Ok para fichero CSV de manera remota")
    void exportarFicheroCSVRemoto() {
        var file = new File("test.csv");

        when(remoteRepository.getAll()).thenReturn(Mono.just(Either.right(List.of(tenistaTest))));
        when(csvStorage.exportFile(file, List.of(tenistaTest))).thenReturn(Mono.just(Either.right(1)));

        var resultado = service.exportData(file, true).blockOptional();

        assertAll("Verificación de exportar fichero CSV",
                () -> assertTrue(resultado.isPresent(), "Se se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isRight(), "Se ha obtenido resultado correcto"),
                () -> assertEquals(1, resultado.get().get(), "El fichero exportado es correcto")
        );

        verify(remoteRepository, times(1)).getAll();
        verify(csvStorage, times(1)).exportFile(file, List.of(tenistaTest));
    }

    @Test
    @DisplayName("Exportar debe devolver Ok para fichero Json de manera local")
    void exportarFicheroJSONLocal() {
        var file = new File("test.json");

        when(localRepository.getAll()).thenReturn(Mono.just(Either.right(List.of(tenistaTest))));
        when(jsonStorage.exportFile(file, List.of(tenistaTest))).thenReturn(Mono.just(Either.right(1)));

        var resultado = service.exportData(file, false).blockOptional();

        assertAll("Verificación de exportar fichero JSON",
                () -> assertTrue(resultado.isPresent(), "Se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isRight(), "Se ha obtenido resultado correcto"),
                () -> assertEquals(1, resultado.get().get(), "El fichero exportado es correcto")
        );

        verify(localRepository, times(1)).getAll();
        verify(jsonStorage, times(1)).exportFile(file, List.of(tenistaTest));
    }

    @Test
    @DisplayName("Exportar debe devolver Ok para fichero Json de manera remota")
    void exportarFicheroJSONRemoto() {
        var file = new File("test.json");

        when(remoteRepository.getAll()).thenReturn(Mono.just(Either.right(List.of(tenistaTest))));
        when(jsonStorage.exportFile(file, List.of(tenistaTest))).thenReturn(Mono.just(Either.right(1)));

        var resultado = service.exportData(file, true).blockOptional();

        assertAll("Verificación de exportar fichero JSON",
                () -> assertTrue(resultado.isPresent(), "Se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isRight(), "Se ha obtenido resultado correcto"),
                () -> assertEquals(1, resultado.get().get(), "El fichero exportado es correcto")
        );

        verify(remoteRepository, times(1)).getAll();
        verify(jsonStorage, times(1)).exportFile(file, List.of(tenistaTest));
    }

    @Test
    @DisplayName("Exportar debe devolver error si no se puede exportar")
    void exportarFicheroError() {
        var file = new File("test.json");

        // Mocks
        when(localRepository.getAll()).thenReturn(Mono.just(Either.right(List.of(tenistaTest))));
        when(jsonStorage.exportFile(file, List.of(tenistaTest))).thenReturn(Mono.just(Either.left(new TenistaError.StorageError("Error al exportar"))));

        // Llamada al servicio
        var resultado = service.exportData(file, false).blockOptional();

        // Aserciones
        assertAll("Verificación de exportar fichero JSON",
                () -> assertTrue(resultado.isPresent(), "Se ha obtenido resultado"),
                () -> assertTrue(resultado.get().isLeft(), "Se ha obtenido un error en la exportación"),
                () -> assertEquals("ERROR: Error al exportar", resultado.get().getLeft().getMessage(), "El mensaje de error es correcto")
        );

        // Verificaciones de interacciones con los mocks
        verify(localRepository, times(1)).getAll();
        verify(jsonStorage, times(1)).exportFile(file, List.of(tenistaTest));
    }

    @Test
    @DisplayName("Load data debe cargar los datos")
    void loadData() {
        when(localRepository.removeAll()).thenReturn(Mono.just(Either.right(null)));
        when(remoteRepository.getAll()).thenReturn(Mono.just(Either.right(List.of(tenistaTest))));
        when(localRepository.saveAll(List.of(tenistaTest))).thenReturn(Mono.just(Either.right(1)));

        // Como no devuelve nada, no se puede hacer un assert, por eso se usa StepVerifier
        StepVerifier.create(
                        Mono.fromRunnable(() -> {
                            service.loadData();
                        }))
                .verifyComplete();


        verify(localRepository, times(1)).removeAll();
        verify(remoteRepository, times(1)).getAll();
        verify(localRepository, times(1)).saveAll(List.of(tenistaTest));
    }

    @Test
    @DisplayName("Refresh debe actualizar los datos")
    void refresh() {

        when(localRepository.removeAll()).thenReturn(Mono.just(Either.right(null)));
        when(remoteRepository.getAll()).thenReturn(Mono.just(Either.right(List.of(tenistaTest))));
        when(localRepository.saveAll(any())).thenReturn(Mono.just(Either.right(1)));

        // El método a testear
        service.refresh();

        // Esperar a que se complete la operación asincrónica, por eso se usa StepVerifier
        StepVerifier.create(Mono.fromRunnable(() -> {
                }))
                .thenAwait(Duration.ofMillis(100)) // Adjust this timing as necessary
                .verifyComplete();

        // Verify interactions
        verify(localRepository, times(1)).removeAll();
        verify(remoteRepository, times(1)).getAll();
        verify(localRepository, times(1)).saveAll(List.of(tenistaTest));
    }


}