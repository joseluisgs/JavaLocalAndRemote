package dev.joseluisgs.repository;

import dev.joseluisgs.error.TenistaError;
import dev.joseluisgs.mapper.TenistaMapper;
import dev.joseluisgs.models.Tenista;
import dev.joseluisgs.rest.TenistasApiRest;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;

public class TenistasRepositoryRemote implements TenistasRepository {
    private final Logger logger = LoggerFactory.getLogger(TenistasRepositoryRemote.class);
    private final TenistasApiRest restClient;

    public TenistasRepositoryRemote(TenistasApiRest restClient) {
        this.restClient = restClient;
    }

    @Override
    public Mono<Either<TenistaError, List<Tenista>>> getAll() {
        logger.debug("Obteniendo todos los tenistas de la api rest");

        return restClient.getAll()
                .flatMap(dtoList -> {
                    List<Tenista> tenistas = dtoList.stream().map(TenistaMapper::toTenista).toList();
                    return Mono.just(Either.<TenistaError, List<Tenista>>right(tenistas));
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    logger.error("Error obteniendo todos los tenistas", e);
                    return Mono.just(Either.left(new TenistaError.RemoteError("No se ha obteniendo todos los tenistas de la api rest ->" + e.getMessage())));
                });
    }


    @Override
    public Mono<Either<TenistaError, Tenista>> getById(Long id) {
        logger.debug("Obteniendo tenista con id {} de la api rest", id);
        return restClient.getById(id).flatMap(dto -> {
                    Tenista tenista = TenistaMapper.toTenista(dto);
                    return Mono.just(Either.<TenistaError, Tenista>right(tenista));
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    //logger.error("Error obteniendo tenista con id {}", id, e);
                    return Mono.just(Either.left(new TenistaError.RemoteError("No se ha encontrado tenista en la api rest con id " + id + " -> " + e.getMessage())));
                });
        //.switchIfEmpty(Mono.just(Either.left(new TenistaError.RemoteError("sin respuesta de la api"))));
    }

    @Override
    public Mono<Either<TenistaError, Tenista>> save(Tenista tenista) {
        logger.debug("Guardando tenista {} en la api rest", tenista);
        var timeStamp = LocalDateTime.now();
        tenista.id(Tenista.NEW_ID).createdAt(timeStamp).updatedAt(timeStamp);
        var tenistaDto = TenistaMapper.toTenistaDto(tenista);
        return restClient.save(tenistaDto)
                .flatMap(dto -> {
                    Tenista tenistaGuardado = TenistaMapper.toTenista(tenistaDto);
                    return Mono.just(Either.<TenistaError, Tenista>right(tenistaGuardado));
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    //logger.error("Error guardando tenista {}", tenista, e);
                    return Mono.just(Either.left(new TenistaError.RemoteError("No se ha guardando tenista en la api rest ->" + e.getMessage())));
                });
        //.switchIfEmpty(Mono.just(Either.left(new TenistaError.StorageError("No se ha guardando tenista"))));
    }

    @Override
    public Mono<Either<TenistaError, Tenista>> update(Long id, Tenista tenista) {
        logger.debug("Actualizando tenista con id {} en la api rest {}", id, tenista);
        var timeStamp = LocalDateTime.now();
        tenista.updatedAt(timeStamp);
        var tenistaDto = TenistaMapper.toTenistaDto(tenista);
        return restClient.update(id, tenistaDto)
                .flatMap(dto -> {
                    Tenista tenistaGuardado = TenistaMapper.toTenista(tenistaDto);
                    return Mono.just(Either.<TenistaError, Tenista>right(tenistaGuardado));
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    //logger.error("Error actualizando tenista con id {}", id, e);
                    return Mono.just(Either.left(new TenistaError.RemoteError("No se ha actualizando tenista en la api rest con id " + id + " ->" + e.getMessage())));
                });
        //.switchIfEmpty(Mono.just(Either.left(new TenistaError.RemoteError(id))));
    }

    @Override
    public Mono<Either<TenistaError, Long>> delete(Long id) {
        logger.debug("Borrando tenista con id {} en la api rest", id);
        return restClient.delete(id)
                .then(Mono.defer(() -> Mono.just(Either.<TenistaError, Long>right(id))))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    //logger.error("Error borrando tenista con id {}", id, e);
                    return Mono.just(Either.left(new TenistaError.RemoteError("No se ha borrando tenista en la api rest con id " + id + " ->" + e.getMessage())));
                });
        //.switchIfEmpty(Mono.just(Either.left(new TenistaError.NotFound(id))));
    }
}
