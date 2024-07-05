package dev.joseluisgs.service;

import dev.joseluisgs.cache.TenistasCache;
import dev.joseluisgs.error.TenistaError;
import dev.joseluisgs.models.Tenista;
import dev.joseluisgs.notification.Notification;
import dev.joseluisgs.notification.TenistasNotifications;
import dev.joseluisgs.repository.TenistasRepositoryLocal;
import dev.joseluisgs.repository.TenistasRepositoryRemote;
import dev.joseluisgs.storage.TenistasStorageCsv;
import dev.joseluisgs.storage.TenistasStorageJson;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.Duration;
import java.util.List;

import static reactor.core.scheduler.Schedulers.boundedElastic;

public class TenistasServiceImpl implements TenistasService {
    private final Logger logger = LoggerFactory.getLogger(TenistasServiceImpl.class);

    private final TenistasRepositoryLocal localRepository;
    private final TenistasRepositoryRemote remoteRepository;
    private final TenistasCache cache;
    private final TenistasStorageCsv csvStorage;
    private final TenistasStorageJson jsonStorage;
    private final TenistasNotifications notificationsService;

    public TenistasServiceImpl(TenistasRepositoryLocal localRepository, TenistasRepositoryRemote remoteRepository, TenistasCache cache, TenistasStorageCsv csvStorage, TenistasStorageJson jsonStorage, TenistasNotifications notificationsService) {
        this.localRepository = localRepository;
        this.remoteRepository = remoteRepository;
        this.cache = cache;
        this.csvStorage = csvStorage;
        this.jsonStorage = jsonStorage;
        this.notificationsService = notificationsService;
    }

    public Flux<Notification<Tenista>> getNotifications() {
        return notificationsService.getNotifications();
    }

    @Override
    public Mono<Either<TenistaError, List<Tenista>>> getAll(boolean fromRemote) {
        logger.debug("Obteniendo todos los tenistas");

        if (!fromRemote) {
            return localRepository.getAll().subscribeOn(boundedElastic());

        } else {
            // remoteRepository.getAll() devuelve un Mono<List<Tenista>>
            // Luego de obtener los datos remotos, los guardamos en el repositorio local
            // Y devolvemos los datos locales
            return remoteRepository.getAll()
                    .flatMap(remoteTenistas -> localRepository.saveAll(remoteTenistas.get())
                            .then(localRepository.getAll())
                            .doOnNext(tenistas -> cache.clear()))
                    .subscribeOn(boundedElastic());
        }
    }


    @Override
    public Mono<Either<TenistaError, Tenista>> getById(long id) {
        logger.debug("Obteniendo tenista por id: {}", id);

        // Primero buscamos en la cache, si está, lo devolvemos
        Tenista cachedTenista = cache.get(id);
        if (cachedTenista != null) {
            logger.debug("Tenista encontrado en cache");
            return Mono.just(Either.right(cachedTenista));
        }
        /*
        // Estamos haciendo uso de codigo bloqueante, mejor dejar esto para el main, que recoge las cosas
        // Si no está en la cache, buscamos en el repositorio local y metemos en la cache
        var localTenista = localRepository.getById(id).block();
        if (localTenista != null && localTenista.isRight()) {

            cache.put(id, localTenista.get());
            return Mono.just(localTenista);
        }

        // Si no está en el repositorio local, buscamos en el repositorio remotoy metemos en local y cache
       var remoteTenista = remoteRepository.getById(id)
                .subscribeOn(boundedElastic())
                .block();
        if (remoteTenista != null && remoteTenista.isRight()) {
            localRepository.save(remoteTenista.get())
                    .subscribeOn(boundedElastic())
                    .blockOptional()
                    .ifPresent(tenista -> cache.put(id, tenista.get()));
            return Mono.just(remoteTenista);
        }

        // Si no está en el repositorio remoto, devolvemos un error
        return Mono.just(Either.left(new TenistaError.NotFound(id)));
        */

        // Sin codigo bloqueante

        return localRepository.getById(id).subscribeOn(boundedElastic())
                .flatMap(resultLocal -> {
                    if (resultLocal.isRight()) {
                        cache.put(id, resultLocal.get());
                        logger.debug("Tenista encontrado en repositorio local");
                        return Mono.just(resultLocal);
                    }
                    // Buscamos en el repositorio remoto
                    return remoteRepository.getById(id).subscribeOn(boundedElastic())
                            .flatMap(resultRemote -> {
                                if (resultRemote.isRight()) {
                                    logger.debug("Tenista encontrado en repositorio remoto");
                                    return localRepository.save(resultRemote.get())
                                            .subscribeOn(boundedElastic())
                                            .doOnNext(saved -> cache.put(id, saved.get()))
                                            .thenReturn(resultRemote);
                                }
                                // Si no encontramos el tenista, devolvemos un error
                                return Mono.just(Either.left(new TenistaError.NotFound(id)));
                            });
                });

    }


    @Override
    public Mono<Either<TenistaError, Tenista>> save(Tenista tenista) {
        return null;
    }

    @Override
    public Mono<Either<TenistaError, Tenista>> update(long id, Tenista tenista) {
        return null;
    }

    @Override
    public Mono<Either<TenistaError, Long>> delete(long id) {
        return null;
    }

    @Override
    public Mono<Either<TenistaError, Integer>> importData(File file) {
        return null;
    }

    @Override
    public Mono<Either<TenistaError, Integer>> export(File file, boolean fromRemote) {
        return null;
    }

    @Override
    public void refresh() {
        logger.debug("Inicializando TenistasServiceImpl");
        // Ejecuta loadData inmediatamente y luego a intervalos regulares
        Flux.concat(
                        Mono.fromRunnable(this::loadData), // Ejecuta inmediatamente
                        Flux.interval(Duration.ofMillis(REFRESH_TIME)) // Luego cada REFRESH_TIME
                                .flatMap(tick -> Mono.fromRunnable(this::loadData))
                                .onBackpressureDrop() // Opcional, para manejar presión de demanda
                )
                .subscribeOn(boundedElastic())
                .subscribe(
                        next -> logger.debug("Refresco de datos ejecutado"),
                        error -> logger.error("Error durante el refresco de datos", error)
                );
    }

    @Override
    public void loadData() {
        logger.debug("Refrescando el repositorio local con los datos remotos");
        localRepository.removeAll()
                .then(remoteRepository.getAll())
                .flatMap(remoteTenistas -> localRepository.saveAll(remoteTenistas.get())
                        .doOnNext(savedTenistas -> {
                            notificationsService.send(new Notification<>(
                                    Notification.Type.REFRESH,
                                    null,
                                    "Nuevos datos disponibles: " + savedTenistas.get()));
                            cache.clear();
                        })).subscribe();
    }
}
