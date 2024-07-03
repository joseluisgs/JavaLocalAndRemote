package dev.joseluisgs.storage;

import dev.joseluisgs.error.TenistaError;
import dev.joseluisgs.models.Tenista;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas para TenistasStorageJson")
class TenistasStorageJsonTest {

    private final TenistasStorageJson storage = new TenistasStorageJson();
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
    @TempDir // Inyectamos un directorio temporal
    Path tempDir;

    @Test
    @DisplayName("Importar archivo debe devolver error si no existe fichero")
    void importFileDebeDevolverErrorSiNoExisteFichero() {
        File nonExistentFile = new File(tempDir.toFile(), "fichero_no_existe.csv");

        var result = storage.importFile(nonExistentFile)
                .blockOptional();

        assertAll(
                "Resultados de la importación de archivo inexistente",
                () -> assertTrue(result.isPresent(), "El resultado debe estar presente"),
                () -> assertTrue(result.get().isLeft(), "El resultado debe contener un error"),
                () -> assertInstanceOf(TenistaError.StorageError.class, result.get().getLeft(), "El error debe ser de tipo StorageError"),
                () -> assertTrue(result.get().getLeft().getMessage().contains("ERROR: El fichero no existe"), "El mensaje del error debe contener 'ERROR: El fichero no existe'")
        );

    }

    @Test
    @DisplayName("Importar archivo debe devolver tenistas si existe fichero")
    void importFileDebeDevolverTenistasSiExisteFichero(@TempDir Path tempDir) throws IOException {
        var validFile = new File(tempDir.toFile(), "tenistas.json");

        String fileContent = """
                [
                    {
                        "id": 1,
                        "nombre": "Roger Federer",
                        "pais": "Suiza",
                        "altura": 185,
                        "peso": 85,
                        "puntos": 9600,
                        "mano": "DIESTRO",
                        "fecha_nacimiento": "1981-08-08"
                    }
                ]
                """;


        Files.writeString(validFile.toPath(), fileContent);

        var result = storage.importFile(validFile)
                .blockOptional();

        assertAll(
                "Resultados de la importación de archivo válido",
                () -> assertTrue(result.isPresent(), "El resultado debe estar presente"),
                () -> assertTrue(result.get().isRight(), "El resultado debe contener una lista de tenistas"),
                () -> assertEquals(1, result.get().get().size(), "El tamaño de la lista de tenistas debe ser 1"),
                () -> assertEquals("Roger Federer", result.get().get().get(0).getNombre(), "El nombre del primer tenista debe ser Roger Federer"),
                () -> assertEquals("Suiza", result.get().get().getFirst().getPais(), "El país del primer tenista debe ser Suiza")
        );

    }

    @Test
    @DisplayName("Exportar archivo debe devolver tenistas si escribe fichero")
    void exportFileDebeDevolverTenistasSiEscribeFichero(@TempDir Path tempDir) {
        var file = new File(tempDir.toFile(), "tenistas_output.json");

        List<Tenista> tenistas = List.of(tenistaTest);

        var result = storage.exportFile(file, tenistas)
                .blockOptional();

        assertAll(
                "Resultados de la exportación a archivo",
                () -> assertTrue(result.isPresent(), "El resultado debe estar presente"),
                () -> assertTrue(result.get().isRight(), "El resultado debe contener un valor correcto"),
                () -> assertEquals(1, result.get().get(), "El número de tenistas exportados debe ser 1")
        );
    }

    @Test
    @DisplayName("Exportar archivo debe devolver error si el fichero no existe")
    void exportFileDebeDevolverErrorSiFicheroNoExiste() {
        var invalidFile = new File("/invalid/path/tenistas_export.json");

        List<Tenista> tenistas = List.of(tenistaTest);

        var result = storage.exportFile(invalidFile, tenistas)
                .blockOptional();

        assertAll(
                "Resultados de la exportación a archivo inválido",
                () -> assertTrue(result.isPresent(), "El resultado debe estar presente"),
                () -> assertTrue(result.get().isLeft(), "El resultado debe contener un error"),
                () -> assertInstanceOf(TenistaError.StorageError.class, result.get().getLeft(), "El error debe ser de tipo StorageError"),
                () -> assertTrue(result.get().getLeft().getMessage().contains("Error al acceder al fichero"), "El mensaje del error debe contener 'Error al acceder al fichero'")
        );
    }
}
