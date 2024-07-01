package dev.joseluisgs.storage;

import dev.joseluisgs.error.TenistaError;
import dev.joseluisgs.models.Tenista;
import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
    void importFileDebeDevolverErrorSiNoExisteFichero() {
        File nonExistentFile = new File(tempDir.toFile(), "fichero_no_existe.csv");

        Optional<Either<TenistaError.StorageError, List<Tenista>>> result = storage.importFile(nonExistentFile)
                .blockOptional();

        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertTrue(result.get().isLeft()),
                () -> assertInstanceOf(TenistaError.StorageError.class, result.get().getLeft()),
                () -> assertTrue(result.get().getLeft().getMessage().contains("ERROR: El fichero no existe"))
        );

    }

    @Test
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

        // Vamos a leer el fichero
        var texto = Files.readString(validFile.toPath());
        System.out.println(texto);

        Optional<Either<TenistaError.StorageError, List<Tenista>>> result = storage.importFile(validFile)
                .blockOptional();

        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertTrue(result.get().isRight()),
                () -> assertEquals(1, result.get().get().size()),
                () -> assertEquals("Roger Federer", result.get().get().get(0).getNombre()),
                () -> assertEquals("Suiza", result.get().get().getFirst().getPais())
        );

    }

    @Test
    void exportFileDebeDevolverTenistasSiEscribeFichero(@TempDir Path tempDir) {
        var file = new File(tempDir.toFile(), "tenistas_output.json");


        List<Tenista> tenistas = List.of(tenistaTest);

        Optional<Either<TenistaError.StorageError, Integer>> result = storage.exportFile(file, tenistas)
                .blockOptional();

        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertTrue(result.get().isRight()),
                () -> assertEquals(1, result.get().get())
        );
    }

    @Test
    void exportFileDebeDevolverErrorSiFicheroNoExiste() {
        var invalidFile = new File("/invalid/path/tenistas_export.json");

        List<Tenista> tenistas = List.of(tenistaTest);

        Optional<Either<TenistaError.StorageError, Integer>> result = storage.exportFile(invalidFile, tenistas)
                .blockOptional();

        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertTrue(result.get().isLeft()),
                () -> assertInstanceOf(TenistaError.StorageError.class, result.get().getLeft()),
                () -> assertTrue(result.get().getLeft().getMessage().contains("Error al acceder al fichero"))
        );
    }
}