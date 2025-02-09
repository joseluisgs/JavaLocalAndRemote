package dev.joseluisgs.storage;


import dev.joseluisgs.dto.TenistaDto;
import dev.joseluisgs.error.TenistaError;
import dev.joseluisgs.mapper.TenistaMapper;
import dev.joseluisgs.models.Tenista;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@Singleton
public class TenistasStorageCsv implements TenistasStorage {
    private static final Logger logger = LoggerFactory.getLogger(TenistasStorageCsv.class);

    @Override
    public Mono<Either<TenistaError.StorageError, List<Tenista>>> importFile(File file) {
        // Uso de Mono.fromSupplier para ejecutar de forma asíncrona
        return Mono.fromSupplier(() -> {
                    logger.info("Importando Tenistas de CSV asíncrono: {}", file);
                    return readLines(file);
                    // Nos aseguramos de que se ejecute en un hilo no bloqueante
                    // Para ello usamos el Scheduler boundedElastic que es un Scheduler que se ajusta a la demanda y es adecuado para operaciones de E/S no bloqueantes.
                }).subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    //logger.error("Error al importar Tenistas de CSV: {}", e.getMessage());
                    return Mono.just(Either.left(new TenistaError.StorageError("Error al importar Tenistas de CSV: " + e.getMessage())));
                });
    }

    @Override
    public Mono<Either<TenistaError.StorageError, Integer>> exportFile(File file, List<Tenista> data) {
        return Mono.fromCallable(() -> {
                    logger.debug("Exportando Tenistas a CSV asíncrono: {}", file);
                    return Utils.ensureFileCanExists(file).<Either<TenistaError.StorageError, Integer>>fold(
                            error -> {
                                logger.error("Error al exportar Tenistas a CSV: {}", error.getMessage());
                                return Either.left(error);
                            },
                            f -> {
                                try {
                                    // Write header
                                    Files.writeString(f.toPath(), "id,nombre,pais,altura,peso,puntos,mano,fecha_nacimiento,createdAt,updatedAt,deletedAt,isDeleted\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                                    // Write data
                                    String dataLines = data.stream()
                                            .map(TenistaMapper::toTenistaDto)
                                            .map(tenista -> String.join(",",
                                                    String.valueOf(tenista.id()),
                                                    tenista.nombre(),
                                                    tenista.pais(),
                                                    String.valueOf(tenista.altura()),
                                                    String.valueOf(tenista.peso()),
                                                    String.valueOf(tenista.puntos()),
                                                    tenista.mano(),
                                                    tenista.fechaNacimiento(),
                                                    tenista.createdAt(),
                                                    tenista.updatedAt(),
                                                    String.valueOf(tenista.isDeleted())))
                                            .collect(Collectors.joining("\n"));
                                    Files.writeString(f.toPath(), dataLines, StandardOpenOption.APPEND);
                                    return Either.right(data.size());
                                } catch (IOException e) {
                                    //logger.error("Error al exportar Tenistas a CSV: {}", e.getMessage());
                                    return Either.left(new TenistaError.StorageError("al exportar Tenistas a CSV: " + e.getMessage()));
                                }
                            }
                    );
                }).subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    //logger.error("Error al exportar Tenistas a CSV: {}", e.getMessage());
                    return Mono.just(Either.left(new TenistaError.StorageError("Error al exportar Tenistas a CSV: " + e.getMessage())));
                });
    }

    private Either<TenistaError.StorageError, List<Tenista>> readLines(File file) {
        if (!file.exists()) {
            logger.debug("El fichero no existe: {}", file.getAbsolutePath());
            return Either.left(new TenistaError.StorageError("El fichero no existe: " + file.getAbsolutePath()));
        } else {

            try (var lines = Files.lines(file.toPath(), StandardCharsets.UTF_8)) {
                logger.info("Leyendo líneas del fichero: " + file.getAbsolutePath());
                return Either.right(lines // Files.lines devuelve un Stream<String>
                        .skip(1) // Skip header
                        .map(line -> line.split(",")) // Split each line
                        .map(fila -> stream(fila).map(String::trim).toArray(String[]::new))
                        .map(this::parseLine) // Parse each line
                        .toList() // Collect to List
                );
            } catch (IOException e) {
                //logger.error("Error al leer el fichero: {}: {}", file.getAbsolutePath(), e.getMessage());
                return Either.left(new TenistaError.StorageError("ERROR al leer el fichero " + file.getAbsolutePath() + ": " + e.getMessage()));
            }
        }
    }

    private Tenista parseLine(String[] parts) {
        logger.debug("Parseando línea: {}", String.join(",", parts));


        var dto = new TenistaDto(
                Long.parseLong(parts[0]),
                parts[1],
                parts[2].trim(),
                Integer.parseInt(parts[3]),
                Integer.parseInt(parts[4]),
                Integer.parseInt(parts[5]),
                parts[6],
                parts[7],
                parts.length > 8 ? parts[8] : null,
                parts.length > 9 ? parts[9] : null,
                parts.length > 10 ? Boolean.parseBoolean(parts[10]) : null
        );

        return TenistaMapper.toTenista(dto);

    }
}