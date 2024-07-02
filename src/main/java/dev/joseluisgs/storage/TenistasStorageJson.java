package dev.joseluisgs.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.joseluisgs.dto.TenistaDto;
import dev.joseluisgs.error.TenistaError;
import dev.joseluisgs.mapper.TenistaMapper;
import dev.joseluisgs.models.Tenista;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.util.List;

public class TenistasStorageJson implements TenistasStorage {
    private static final Logger logger = LoggerFactory.getLogger(TenistasStorageJson.class);

    @Override
    public Mono<Either<TenistaError.StorageError, List<Tenista>>> importFile(File file) {
        return Mono.fromSupplier(() -> {
                    logger.debug("Importando Tenistas de JSON asíncrono: {}", file);
                    return readLines(file);
                    // Nos aseguramos de que se ejecute en un hilo no bloqueante
                    // Para ello usamos el Scheduler boundedElastic que es un Scheduler que se ajusta a la demanda y es adecuado para operaciones de E/S no bloqueantes.
                }).subscribeOn(Schedulers.boundedElastic())
                // Si hay un error, lo capturamos y devolvemos un error
                .onErrorResume(e -> {
                    logger.error("Error al importar Tenistas de JSON: {}", e.getMessage());
                    return Mono.just(Either.left(new TenistaError.StorageError("Error al importar Tenistas de JSON: " + e.getMessage())));
                });
    }


    private Either<TenistaError.StorageError, List<Tenista>> readLines(File file) {
        if (!file.exists()) {
            logger.debug("El fichero no existe: {}", file.getAbsolutePath());
            return Either.left(new TenistaError.StorageError("El fichero no existe: " + file.getAbsolutePath()));
        } else {

            try (FileInputStream fis = new FileInputStream(file)) {
                // Jackson nos permite leer un JSON y convertirlo en una lista de objetos
                var jsonMapper = new ObjectMapper();
                var tenistasDto = jsonMapper.readValue(fis, new TypeReference<List<TenistaDto>>() {
                });
                // Convertimos los DTO a Tenistas y los devolvemos
                return Either.right(tenistasDto.stream().map(TenistaMapper::toTenista).toList());
            } catch (IOException e) {
                logger.error("Error al leer el fichero: {}: {}", file.getAbsolutePath(), e.getMessage());
                return Either.left(new TenistaError.StorageError("Error al leer el fichero: " + file.getAbsolutePath() + ": " + e.getMessage()));
            }
        }

    }

    @Override
    public Mono<Either<TenistaError.StorageError, Integer>> exportFile(File file, List<Tenista> data) {
        return Mono.fromCallable(() -> {
                    logger.debug("Exportando Tenistas a JSON asíncrono: {}", file);
                    return ensureFileCanExists(file).<Either<TenistaError.StorageError, Integer>>fold(
                            error -> {
                                logger.error("Error al exportar Tenistas a JSON: {}", error.getMessage());
                                return Either.left(error);
                            },
                            f -> {
                                try (OutputStream fos = new FileOutputStream(f)) {
                                    // Jackson nos permite escribir un objeto en un JSON, con pretty printer para que sea más legible
                                    var jsonMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
                                    // Convertimos los Tenistas a DTO y los escribimos
                                    jsonMapper.writeValue(fos, data.stream().map(TenistaMapper::toTenistaDto).toList());
                                    // Devolvemos el número de elementos exportados
                                    return Either.right(data.size());
                                } catch (IOException e) {
                                    logger.error("Error al exportar Tenistas a JSON: {}", e.getMessage());
                                    return Either.left(new TenistaError.StorageError("Error al exportar Tenistas a JSON: " + e.getMessage()));
                                }
                            }
                    );
                }).subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    logger.error("Error al exportar Tenistas a JSON: {}", e.getMessage());
                    return Mono.just(Either.left(new TenistaError.StorageError("Error al exportar Tenistas a JSON: " + e.getMessage())));
                });
    }
}
