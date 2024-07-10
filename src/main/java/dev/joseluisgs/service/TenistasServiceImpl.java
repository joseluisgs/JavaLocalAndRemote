package dev.joseluisgs.service;

import dev.joseluisgs.cache.TenistasCache;
import dev.joseluisgs.dto.TenistaDto;
import dev.joseluisgs.error.TenistaError;
import dev.joseluisgs.mapper.TenistaMapper;
import dev.joseluisgs.models.Tenista;
import dev.joseluisgs.notification.Notification;
import dev.joseluisgs.notification.TenistasNotifications;
import dev.joseluisgs.repository.TenistasRepositoryLocal;
import dev.joseluisgs.repository.TenistasRepositoryRemote;
import dev.joseluisgs.storage.TenistasStorageCsv;
import dev.joseluisgs.storage.TenistasStorageJson;
import dev.joseluisgs.validator.TenistaValidator;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static reactor.core.scheduler.Schedulers.boundedElastic;

// Que quede claro
// map es sincrono, flatMap es asincrono, por eso para operaciones asincronas usamos flatMap
// En este caso, estamos usando flatMap porque estamos haciendo operaciones asincronas
//

@Singleton

public class TenistasServiceImpl implements TenistasService {
    private final Logger logger = LoggerFactory.getLogger(TenistasServiceImpl.class);

    private final TenistasRepositoryLocal localRepository;
    private final TenistasRepositoryRemote remoteRepository;
    private final TenistasCache cache;
    private final TenistasStorageCsv csvStorage;
    private final TenistasStorageJson jsonStorage;
    private final TenistasNotifications notificationsService;

    @Inject
    public TenistasServiceImpl(TenistasRepositoryLocal localRepository, TenistasRepositoryRemote remoteRepository, TenistasCache cache, TenistasStorageCsv csvStorage, TenistasStorageJson jsonStorage, TenistasNotifications notificationsService) {
        this.localRepository = localRepository;
        this.remoteRepository = remoteRepository;
        this.cache = cache;
        this.csvStorage = csvStorage;
        this.jsonStorage = jsonStorage;
        this.notificationsService = notificationsService;
    }

    public Flux<Notification<TenistaDto>> getNotifications() {
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
            return remoteRepository.getAll().subscribeOn(boundedElastic())
                    .then(localRepository.removeAll())
                    .then(remoteRepository.getAll()
                            .flatMap(remoteTenistas -> localRepository.saveAll(remoteTenistas.get())))
                    .then(localRepository.getAll())
                    .doOnNext(tenistas -> {
                        cache.clear();
                    });
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

        return localRepository.getById(id).subscribeOn(boundedElastic())
                .flatMap(resultLocal -> {
                    if (resultLocal.isRight()) {
                        cache.put(id, resultLocal.get());
                        logger.debug("Tenista encontrado en repositorio local");
                        return Mono.just(resultLocal);
                    }
                    // Buscamos en el repositorio remoto
                    return remoteRepository.getById(id).subscribeOn(boundedElastic())
                            .flatMap(resultRemote -> { // Si lo encontramos, lo guardamos en local y en cache
                                if (resultRemote.isRight()) { // Si lo encontramos, lo guardamos en local y en cache
                                    logger.debug("Tenista encontrado en repositorio remoto");
                                    return localRepository.save(resultRemote.get()).subscribeOn(boundedElastic())
                                            .doOnNext(saved -> cache.put(id, saved.get())); // Guardamos en cache

                                }
                                // Si no encontramos el tenista, devolvemos un error
                                return Mono.just(Either.left(new TenistaError.NotFound(id)));
                            });
                });

    }


    @Override
    public Mono<Either<TenistaError, Tenista>> save(Tenista tenista) {
        logger.debug("Guardando tenista: {}", tenista);

        // Primero validamos el tenista
        var validation = TenistaValidator.validate(tenista);
        if (validation.isLeft()) {
            logger.debug("Error validando tenista: {}", validation.getLeft());
            return Mono.just(Either.left(validation.getLeft()));
        }

        // Salvamos en remoto y luego en local y metemos en cache
        return remoteRepository.save(tenista).subscribeOn(boundedElastic()).flatMap(resultRemote -> {
            if (resultRemote.isRight()) { // Si se ha guardado correctamente en remoto
                logger.debug("Tenista guardado remotamente: {}", resultRemote.get());
                return localRepository.save(resultRemote.get()).subscribeOn(boundedElastic()) // Guardamos en local
                        .doOnNext(saved -> { // Si se ha guardado correctamente, lo guardamos en la cache y notificamos
                            cache.put(saved.get().getId(), saved.get());
                            notificationsService.send(new Notification<>(
                                    Notification.Type.CREATE,
                                    TenistaMapper.toTenistaDto(saved.get()),
                                    "Tenista creado con id: " + saved.get().getId()));
                        });
            }
            return Mono.just(resultRemote); // Devolvemos el resultado
        });

    }

    @Override
    public Mono<Either<TenistaError, Tenista>> update(long id, Tenista tenista) {
        logger.debug("Actualizando tenista: {}", tenista);

        // Primero validamos el tenista
        var validation = TenistaValidator.validate(tenista);
        if (validation.isLeft()) {
            return Mono.just(Either.left(validation.getLeft()));
        }
        // Primero ejecutamos this.getById(id) para obtener el tenista actual
        // No es necesario validar si existe, ya que si no existe, devolverá un error el remoto
        // pero lo dejo para que cojáis soltura con el código y con el flujo
        return this.getById(id).subscribeOn(boundedElastic()).flatMap(result -> {
            if (result.isRight()) {
                logger.debug("Tenista encontrado remotamente: {}", result.get());
                return remoteRepository.update(id, tenista).subscribeOn(boundedElastic()).flatMap(resultRemote -> {
                    if (resultRemote.isRight()) {
                        logger.debug("Tenista actualizado remotamente: {}", resultRemote.get());
                        return localRepository.update(id, resultRemote.get()).subscribeOn(boundedElastic()) // Actualizamos en local
                                .doOnNext(updated -> { // Si se ha actualizado correctamente, lo actualizamos en la cache y notificamos
                                    cache.put(updated.get().getId(), updated.get());
                                    notificationsService.send(new Notification<>(
                                            Notification.Type.UPDATE,
                                            TenistaMapper.toTenistaDto(updated.get()),
                                            "Tenista actualizado con id: " + updated.get().getId()));
                                });
                    }
                    return Mono.just(resultRemote); // Devolvemos el error que ya viene del getById, si no podemos actualizarlo
                });
            }
            return Mono.just(result); // Devolvemos el error que ya viene del getById, si no podemos crealo
        });
    }

    @Override
    public Mono<Either<TenistaError, Long>> delete(long id) {
        logger.debug("Borrando tenista con id: {}", id);
        // Primero ejecutamos this.getById(id) para obtener el tenista actual
        // No es necesario validar si existe, ya que si no existe, devolverá un error el remoto
        // pero lo dejo para que cojáis soltura con el código y con el flujo
        return this.getById(id).subscribeOn(boundedElastic()).flatMap(result -> {
            if (result.isRight()) {
                logger.debug("Tenista encontrado remotamente: {}", result.get());
                // eliminamos de remoto
                return remoteRepository.delete(id).subscribeOn(boundedElastic()).flatMap(
                        resultRemote -> {
                            if (resultRemote.isRight()) {
                                logger.debug("Tenista eliminado remotamente: {}", resultRemote.get());
                                // eliminamos de local
                                return localRepository.delete(id).subscribeOn(boundedElastic())
                                        .doOnNext(deleted -> {
                                            cache.remove(id);
                                            notificationsService.send(new Notification<>(
                                                    Notification.Type.DELETE,
                                                    null,
                                                    "Tenista eliminado con id: " + id));
                                        });
                            }
                            return Mono.just(resultRemote); // Devolvemos el error que ya viene del getById, si no podemos eliminarlo
                        });
            }
            return Mono.just(Either.left(result.getLeft()));
        });
    }

    @Override
    public Mono<Either<TenistaError, Integer>> importData(File file) {
        logger.debug("Importando datos desde el fichero: {}", file.getName());
        return switch (file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase()) {
            case "csv" -> this.importDataCsv(file);
            case "json" -> this.importDataJson(file);
            default -> Mono.just(Either.left(new TenistaError.StorageError("Formato de fichero no soportado")));
        };
    }

    private Mono<Either<TenistaError, Integer>> importDataJson(File file) {
        logger.debug("Importando datos desde el fichero JSON: {}", file.getName());
        return jsonStorage.importFile(file).subscribeOn(boundedElastic()).flatMap(result -> {
            if (result.isRight()) {
                return Mono.just(saveAll(result.get()));
            }
            return Mono.just(Either.left(result.getLeft()));
        });
    }

    private Mono<Either<TenistaError, Integer>> importDataCsv(File file) {
        logger.debug("Importando datos desde el fichero CSV: {}", file.getName());
        return csvStorage.importFile(file).subscribeOn(boundedElastic()).flatMap(result -> {
            if (result.isRight()) {
                return Mono.just(saveAll(result.get()));
            }
            return Mono.just(Either.left(result.getLeft()));
        });

    }

    private Either<TenistaError, Integer> saveAll(List<Tenista> tenistas) {
        logger.debug("Guardando tenistas: {}", tenistas);
        AtomicInteger contador = new AtomicInteger(); // Para contar los tenistas guardados atómicamente
        localRepository.removeAll().subscribeOn(boundedElastic())
                // lanzamos un flujo de tenistas y los guardamos
                .thenMany(Flux.fromIterable(tenistas))
                .flatMap(remoteRepository::save)
                .flatMap(remoteTenista -> localRepository.save(remoteTenista.get()))
                .doOnNext(saved -> {
                    contador.incrementAndGet();
                    cache.put(saved.get().getId(), saved.get());
                }).blockLast(); // Bloqueamos para esperar a que se guarden todos los tenistas, es el único bloqueante
        return Either.right(contador.get());
    }

    @Override
    public Mono<Either<TenistaError, Integer>> exportData(File file, boolean fromRemote) {
        logger.debug("Exportando datos al fichero: {}", file.getName());
        return switch (file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase()) {
            case "csv" -> this.exportDataCsv(file, fromRemote);
            case "json" -> this.exportDataJson(file, fromRemote);
            default -> Mono.just(Either.left(new TenistaError.StorageError("Formato de fichero no soportado")));
        };
    }

    private Mono<Either<TenistaError, Integer>> exportDataCsv(File file, boolean fromRemote) {
        logger.debug("Exportando datos al fichero CSV: {}", file.getName());
        if (fromRemote) {
            return remoteRepository.getAll().subscribeOn(boundedElastic())
                    .flatMap(remoteTenistas -> csvStorage.exportFile(file, remoteTenistas.get()))
                    // Devolvemos el número de tenistas exportados o el error mapeado
                    .map(result -> {
                        if (result.isRight()) {
                            return Either.right(result.get());
                        }
                        return Either.left(result.getLeft());
                    });
        } else {
            return localRepository.getAll().subscribeOn(boundedElastic())
                    .flatMap(localTenistas -> csvStorage.exportFile(file, localTenistas.get()))
                    // Devolvemos el número de tenistas exportados o el error mapeado
                    .map(result -> {
                        if (result.isRight()) {
                            return Either.right(result.get());
                        }
                        return Either.left(result.getLeft());
                    });
        }
    }

    private Mono<Either<TenistaError, Integer>> exportDataJson(File file, boolean fromRemote) {
        logger.debug("Exportando datos al fichero JSON: {}", file.getName());
        if (fromRemote) {
            return remoteRepository.getAll().subscribeOn(boundedElastic())
                    .flatMap(remoteTenistas -> jsonStorage.exportFile(file, remoteTenistas.get()))
                    // Devolvemos el número de tenistas exportados o el error mapeado
                    .map(result -> {
                        if (result.isRight()) {
                            return Either.right(result.get());
                        }
                        return Either.left(result.getLeft());
                    });
        } else {
            return localRepository.getAll().subscribeOn(boundedElastic())
                    .flatMap(localTenistas -> jsonStorage.exportFile(file, localTenistas.get()))
                    // Devolvemos el número de tenistas exportados o el error mapeado
                    .map(result -> {
                        if (result.isRight()) {
                            return Either.right(result.get());
                        }
                        return Either.left(result.getLeft());
                    });
        }
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
                // Como son void no necesitamos hacer nada con el resultado me subscribo para que se ejecute
                .subscribe(
                        next -> logger.debug("Refresco de datos ejecutado"),
                        error -> logger.error("Error durante el refresco de datos", error)
                );
    }

    @Override
    public void loadData() {
        logger.debug("Refrescando el repositorio local con los datos remotos");
        localRepository.removeAll().subscribeOn(boundedElastic())
                .then(remoteRepository.getAll())
                .flatMap(remoteTenistas -> localRepository.saveAll(remoteTenistas.get())
                        .doOnNext(savedTenistas -> {
                            notificationsService.send(new Notification<>(
                                    Notification.Type.REFRESH,
                                    null,
                                    "Nuevos datos disponibles: " + savedTenistas.get()));
                            cache.clear(); // Limpiamos la cache
                        }))
                // Como son void no necesitamos hacer nada con el resultado me subscribo para que se ejecute
                .subscribe(
                        next -> logger.debug("Datos refrescados"),
                        error -> logger.error("Error refrescando los datos", error)
                );
    }

}
