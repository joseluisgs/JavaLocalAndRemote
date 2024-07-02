package dev.joseluisgs.repository;

import io.vavr.control.Either;
import reactor.core.publisher.Mono;

import java.util.List;

public interface Repository<ID, T, E> {
    Mono<Either<E, List<T>>> getAll();

    Mono<Either<E, T>> getById(ID id);

    Mono<Either<E, T>> save(T t);

    Mono<Either<E, T>> update(ID id, T t);

    Mono<Either<E, ID>> delete(ID id);
}