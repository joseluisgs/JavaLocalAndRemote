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


public class TenistasStorageCsvTest {

    private final TenistasStorageCsv storage = new TenistasStorageCsv();
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
    @DisplayName("Importar fichero debe devolver error si no existe el fichero")
    void importFileDebeDevolverErrorSiNoExisteFichero() {
        File nonExistentFile = new File(tempDir.toFile(), "fichero_no_existe.csv");

        var result = storage.importFile(nonExistentFile)
                .blockOptional();

        assertAll("Verificación de resultados",
                () -> assertTrue(result.isPresent(), "El resultado debe estar presente"),
                () -> assertTrue(result.get().isLeft(), "El resultado debe ser un 'Left'"),
                () -> assertInstanceOf(TenistaError.StorageError.class, result.get().getLeft(), "El error debe ser una instancia de TenistaError.StorageError"),
                () -> assertTrue(result.get().getLeft().getMessage().contains("ERROR: El fichero no existe"), "El mensaje de error debe contener 'ERROR: El fichero no existe'")
        );

    }

    @Test
    @DisplayName("Importar fichero debe devolver tenistas si existe el fichero")
    void importFileDebeDevolverTenistasSiExisteFichero(@TempDir Path tempDir) throws IOException {
        var validFile = new File(tempDir.toFile(), "tenistas.csv");

        String fileContent = """
                id,nombre,pais,altura,peso,puntos,mano,fecha_nacimiento,createdAt,updatedAt,deletedAt,isDeleted
                1,Roger Federer,Suiza,185,85,9600,DIESTRO,1981-08-08,,,
                """;


        Files.writeString(validFile.toPath(), fileContent);

        var result = storage.importFile(validFile)
                .blockOptional();

        assertAll("Verificación de resultados",
                () -> assertTrue(result.isPresent(), "El resultado debe estar presente"),
                () -> assertTrue(result.get().isRight(), "El resultado debe ser un 'Right'"),
                () -> assertEquals(1, result.get().get().size(), "El tamaño de la lista debe ser 1"),
                () -> assertEquals("Roger Federer", result.get().get().get(0).getNombre(), "El nombre del tenista debe ser 'Roger Federer'"),
                () -> assertEquals("Suiza", result.get().get().get(0).getPais(), "El país del tenista debe ser 'Suiza'")
        );

    }

    @Test
    @DisplayName("Exportar fichero debe devolver tenistas si escribe en el fichero")
    void exportFileDebeDevolverTenistasSiEscribeFichero(@TempDir Path tempDir) {
        var file = new File(tempDir.toFile(), "tenistas_output.csv");

        List<Tenista> tenistas = List.of(tenistaTest);

        var result = storage.exportFile(file, tenistas)
                .blockOptional();

        assertAll("Verificación de resultados",
                () -> assertTrue(result.isPresent(), "El resultado debe estar presente"),
                () -> assertTrue(result.get().isRight(), "El resultado debe ser un 'Right'"),
                () -> assertEquals(1, result.get().get(), "El número de tenistas exportados debe ser 1")
        );
    }

    @Test
    @DisplayName("Exportar fichero debe devolver error si el fichero no existe")
    void exportFileDebeDevolverErrorSiFicheroNoExiste() {
        var invalidFile = new File("/invalid/path/tenistas_export.csv");

        List<Tenista> tenistas = List.of(tenistaTest);

        var result = storage.exportFile(invalidFile, tenistas)
                .blockOptional();

        assertAll("Verificación de resultados",
                () -> assertTrue(result.isPresent(), "El resultado debe estar presente"),
                () -> assertTrue(result.get().isLeft(), "El resultado debe ser un 'Left'"),
                () -> assertInstanceOf(TenistaError.StorageError.class, result.get().getLeft(), "El error debe ser una instancia de TenistaError.StorageError"),
                () -> assertTrue(result.get().getLeft().getMessage().contains("Error al acceder al fichero"), "El mensaje de error debe contener 'Error al acceder al fichero'")
        );
    }
}
