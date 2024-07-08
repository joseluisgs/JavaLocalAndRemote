package dev.joseluisgs.storage;

import dev.joseluisgs.error.TenistaError;
import io.vavr.control.Either;

import java.io.File;
import java.io.IOException;

public class Utils {

    // Implementación por defecto para un metodo de la interfaz
    public static Either<TenistaError.StorageError, File> ensureFileCanExists(File file) {

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
