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
    void importFileShouldReturnErrorIfFileDoesNotExist() {
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
    void importFileShouldReturnListIfFileExists(@TempDir Path tempDir) throws IOException {
        var validFile = new File(tempDir.toFile(), "tenistas.csv");

        String fileContent = """
                id,nombre,pais,altura,peso,puntos,mano,fechaNacimiento,createdAt,updatedAt,deletedAt,isDeleted
                1,Roger Federer,Suiza,185,85,9600,DIESTRO,1981-08-08,,,
                """;


        Files.writeString(validFile.toPath(), fileContent);

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
    void exportFileShouldWriteTenistasToFile(@TempDir Path tempDir) {
        var file = new File(tempDir.toFile(), "tenistas_output.csv");


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
    void exportFileShouldReturnErrorIfOperationFails() {
        var invalidFile = new File("/invalid/path/tenistas_export.csv");

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