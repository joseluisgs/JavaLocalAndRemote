package dev.joseluisgs.service;

import dev.joseluisgs.error.TenistaError;
import dev.joseluisgs.models.Tenista;
import io.vavr.control.Either;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.List;

public interface TenistasService {
    long REFRESH_TIME = 5000L; // 5 segundos

    Mono<Either<TenistaError, List<Tenista>>> getAll(boolean fromRemote);

    Mono<Either<TenistaError, Tenista>> getById(long id);

    Mono<Either<TenistaError, Tenista>> save(Tenista tenista);

    Mono<Either<TenistaError, Tenista>> update(long id, Tenista tenista);

    Mono<Either<TenistaError, Long>> delete(long id);

    Mono<Either<TenistaError, Integer>> importData(File file);

    Mono<Either<TenistaError, Integer>> exportData(File file, boolean fromRemote);

    void refresh();

    void loadData();
}