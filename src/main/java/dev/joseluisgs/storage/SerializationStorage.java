package dev.joseluisgs.storage;

import io.vavr.control.Either;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.List;

// Como solo devolvermos un Resultado o un Error, usamos Either
// Al ser solo un Result, usamos Mono (0 o 1)
// Tambien podriamos usar Flux si devolvemos mas de un resultado (0..n)

public interface SerializationStorage<T, E> {

    Mono<Either<E, List<T>>> importFile(File file);

    Mono<Either<E, Integer>> exportFile(File file, List<T> data);
}
