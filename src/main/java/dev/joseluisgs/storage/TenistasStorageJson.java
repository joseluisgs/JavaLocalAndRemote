package dev.joseluisgs.storage;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import dev.joseluisgs.dto.TenistaDto;
import dev.joseluisgs.error.TenistaError;
import dev.joseluisgs.mapper.TenistaMapper;
import dev.joseluisgs.models.Tenista;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TenistasStorageJson implements TenistasStorage {
    private static final Logger logger = LoggerFactory.getLogger(TenistasStorageJson.class);

    @Override
    public Mono<Either<TenistaError.StorageError, List<Tenista>>> importFile(File file) {
        return Mono.fromSupplier(() -> {
            logger.debug("Importando Tenistas de JSON asíncrono: {}", file);
            return readLines(file);
            // Nos aseguramos de que se ejecute en un hilo no bloqueante
            // Para ello usamos el Scheduler boundedElastic que es un Scheduler que se ajusta a la demanda y es adecuado para operaciones de E/S no bloqueantes.
        }).subscribeOn(Schedulers.boundedElastic());
    }


    private Either<TenistaError.StorageError, List<Tenista>> readLines(File file) {
        if (!file.exists()) {
            logger.debug("El fichero no existe: {}", file.getAbsolutePath());
            return Either.left(new TenistaError.StorageError("El fichero no existe: " + file.getAbsolutePath()));
        } else {

            try {
                var moshi = new Moshi.Builder().build();
                var listType = Types.newParameterizedType(List.class, TenistaDto.class);
                JsonAdapter<List<TenistaDto>> jsonAdapter = moshi.adapter(listType);

                String lines = Files.readString(file.toPath(), UTF_8);

                List<TenistaDto> tenistasDto = jsonAdapter.fromJson(lines);

                if (tenistasDto == null) {
                    logger.error("Error al leer el fichero: {}: No se han podido leer los datos", file.getAbsolutePath());
                    return Either.left(new TenistaError.StorageError("El fichero no contiene datos: " + file.getAbsolutePath()));
                }

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
                        try {
                            Moshi moshi = new Moshi.Builder().build();
                            var listType = Types.newParameterizedType(List.class, TenistaDto.class);
                            var jsonAdapter = moshi.adapter(listType).indent("  ");
                            List<TenistaDto> tenistasDto = data.stream().map(TenistaMapper::toTenistaDto).toList();
                            String json = jsonAdapter.toJson(tenistasDto);
                            Files.writeString(f.toPath(), json, UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                            return Either.right(data.size());
                        } catch (IOException e) {
                            logger.error("Error al exportar Tenistas a JSON: {}", e.getMessage());
                            return Either.left(new TenistaError.StorageError("Error al exportar Tenistas a JSON: " + e.getMessage()));
                        }
                    }
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
