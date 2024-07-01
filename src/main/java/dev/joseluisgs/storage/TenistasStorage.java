package dev.joseluisgs.storage;

import dev.joseluisgs.error.TenistaError;
import dev.joseluisgs.models.Tenista;
import io.vavr.control.Either;
import reactor.util.annotation.NonNull;

import java.io.File;
import java.io.IOException;

public interface TenistasStorage extends SerializationStorage<Tenista, TenistaError.StorageError> {

    @NonNull
    // Implementación por defecto para un metodo de la interfaz
    default Either<TenistaError.StorageError, File> ensureFileCanExists(File file) {

        // Devolvemos un Mono de Either con un error o un fichero
        /*return Mono.fromCallable(() -> {
            // Try es un tipo de Vavr que nos permite hacer operaciones que pueden fallar
            Try<File> result = Try.of(() -> {
                if (file.exists() || file.createNewFile()) {
                    return file;
                } else {
                    // Si no se puede crear el fichero lanzamos una excepción
                    throw new IOException("Error al crear el fichero " + file.getAbsolutePath());
                }
            });
            // Devolvemos el resultado, si ha ido bien el fichero, si no el error que mapea a nuestro error
            return result.toEither()
                    .mapLeft(e -> new TenistaError.StorageError("Error al acceder al fichero "
                            + file.getAbsolutePath() + ": " + e.getMessage()));
        });*/

        // Otra forma
        try {
            if (file.exists() || file.createNewFile()) {
                return Either.right(file);
            } else {
                return Either.left(new TenistaError.StorageError("Error al acceder al fichero "
                        + file.getAbsolutePath() + ": " + "No se ha podido crear"));
            }
        } catch (IOException e) {
            return Either.left(new TenistaError.StorageError("Error al acceder al fichero "
                    + file.getAbsolutePath() + ": " + e.getMessage()));
        }
    }
}