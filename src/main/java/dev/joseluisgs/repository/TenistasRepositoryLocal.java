package dev.joseluisgs.repository;

import dev.joseluisgs.database.JdbiManager;
import dev.joseluisgs.database.TenistasDao;
import dev.joseluisgs.error.TenistaError;
import dev.joseluisgs.mapper.TenistaMapper;
import dev.joseluisgs.models.Tenista;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;

public class TenistasRepositoryLocal implements TenistasRepository {
    private final Logger logger = LoggerFactory.getLogger(TenistasRepositoryLocal.class);
    JdbiManager<TenistasDao> db;


    public TenistasRepositoryLocal(JdbiManager<TenistasDao> jdbiManager) {
        this.db = jdbiManager;
        init();
    }

    private void init() {
        logger.debug("Inicializando el repositorio local");
        db.with(dao -> {
            logger.debug("Creando tabla si no existe");
            dao.createTable(); // Creamos la tabla si no existe
            logger.debug("Borrando todos los registros");
            dao.removeAll(); // Borramos todos los registros
            return null;
        });
    }


    @Override
    public Mono<Either<TenistaError, List<Tenista>>> getAll() {
        logger.debug("Obteniendo todos los tenistas de la bd");

        return Mono.fromSupplier(() -> {
                    var lista = db.with(TenistasDao::selectAll).stream()
                            .map(TenistaMapper::toTenista)
                            .toList();
                    return Either.<TenistaError, List<Tenista>>right(lista);
                }).subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    //logger.error("Error obteniendo todos los tenistas", e);
                    return Mono.just(Either.left(new TenistaError.DatabaseError("No se han obtenido todos los tenistas ->" + e.getMessage())));
                });
    }

    @Override
    public Mono<Either<TenistaError, Tenista>> getById(Long id) {
        logger.debug("Obteniendo tenista por ID: {} de la bd", id);

        return Mono.fromSupplier(() -> {
                    var tenista = db.with(dao -> dao.selectById(id)).map(TenistaMapper::toTenista);
                    // Devolvemos el tenista o un error si no existe
                    return tenista.map(Either::<TenistaError, Tenista>right).orElseGet(() -> Either.left(new TenistaError.DatabaseError("No se ha encontrado tenista en la bd con id " + id)));
                }).subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    //logger.error("Error obteniendo tenista con id {}", id, e);
                    return Mono.just(Either.left(new TenistaError.DatabaseError("No se ha encontrado tenista en la bd con id " + id + " -> " + e.getMessage())));
                });

        /**
         * Otra forma de hacerlo con if else que te liarás menos ;)
         * return Mono.fromSupplier(() -> {
         *            var tenista = db.with(dao -> dao.selectById(id)).map(TenistaMapper::toTenista);
         *            // Devolvemos el tenista o un error si no existe
         *            if (tenista.isPresent()) {
         *            return Either.right(tenista.get());
         *            } else {
         *            return Either.left(new TenistaError.NotFound(id));
         *            }
         */
    }

    @Override
    public Mono<Either<TenistaError, Tenista>> save(Tenista tenista) {
        logger.debug("Guardando tenista {} en la bd", tenista);

        return Mono.fromSupplier(() -> {
                    var timeStamp = LocalDateTime.now();
                    var tenistaEntity = TenistaMapper.toTenistaEntity(tenista);
                    var id = db.with(dao -> dao.insert(
                            tenistaEntity.nombre(),
                            tenistaEntity.pais(),
                            tenistaEntity.altura(),
                            tenistaEntity.peso(),
                            tenistaEntity.puntos(),
                            tenistaEntity.mano(),
                            tenistaEntity.fecha_nacimiento(),
                            timeStamp.toString(),
                            timeStamp.toString()
                    ));

                    if (id == null) {
                        return Either.<TenistaError, Tenista>left(new TenistaError.DatabaseError("No se ha guardado tenista en la bd"));
                    }
                    
                    // Devolvemos el tenista con el id y las fechas
                    tenista.id(id).createdAt(timeStamp).updatedAt(timeStamp);
                    return Either.<TenistaError, Tenista>right(tenista);
                }).subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    //logger.error("Error insertando tenista con id", e);
                    return Mono.just(Either.left(new TenistaError.DatabaseError("No se ha insertando tenista en la bd ->" + e.getMessage())));
                });
    }

    @Override
    public Mono<Either<TenistaError, Tenista>> update(Long id, Tenista tenista) {
        logger.debug("Actualizando tenista con id {} {} en la bd", id, tenista);

        return Mono.fromSupplier(() -> {

                    var updatedAt = LocalDateTime.now();
                    var tenistaEntity = TenistaMapper.toTenistaEntity(tenista);

                    int updateCount = db.with(dao -> dao.update(
                            id,
                            tenistaEntity.nombre(),
                            tenistaEntity.pais(),
                            tenistaEntity.altura(),
                            tenistaEntity.peso(),
                            tenistaEntity.puntos(),
                            tenistaEntity.mano(),
                            tenistaEntity.fecha_nacimiento(),
                            updatedAt.toString(),
                            tenistaEntity.is_deleted()
                    ));

                    // Si no se ha actualizado devolvemos un error
                    if (updateCount == 0) {
                        return Either.<TenistaError, Tenista>left(new TenistaError.NotFound(id));
                    }

                    tenista.setUpdatedAt(updatedAt);
                    return Either.<TenistaError, Tenista>right(tenista);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    //logger.error("Error actualizando tenista con id {}", id, e);
                    return Mono.just(Either.left(new TenistaError.DatabaseError("No se ha actualizando tenista en la bd con id " + id + "->" + e.getMessage())));
                });
    }

    @Override
    public Mono<Either<TenistaError, Long>> delete(Long id) {
        logger.debug("Borrando tenista con id {} en la bd", id);
        return Mono.fromSupplier(() -> {
                    int deleteCount = db.with(dao -> dao.delete(id));
                    if (deleteCount == 0) {
                        return Either.<TenistaError, Long>left(new TenistaError.NotFound(id));
                    }
                    return Either.<TenistaError, Long>right(id);
                }).subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    //logger.error("Error borrando tenista con id {}", id, e);
                    return Mono.just(Either.left(new TenistaError.DatabaseError("No se ha borrando tenista en la bd con id " + id + "->" + e.getMessage())));
                });
    }

    public Mono<Either<TenistaError, Void>> removeAll() {
        logger.debug("Borrando todos los tenistas de la bd");
        return Mono.fromSupplier(() -> {
                    db.use(TenistasDao::removeAll);
                    return Either.<TenistaError, Void>right(null);
                }).subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    //logger.error("Error borrando todos los tenistas", e);
                    return Mono.just(Either.left(new TenistaError.DatabaseError("No se han borrando todos los tenistas de la bd ->" + e.getMessage())));
                });
    }

    public Mono<Either<TenistaError, Integer>> saveAll(List<Tenista> tenistas) {
        logger.debug("Guardando {} tenistas en la bd", tenistas.size());
        return Mono.fromSupplier(() -> {
                    // Lo hacemos en una transacción
                    db.useTransaction(dao -> {
                        var timeStamp = LocalDateTime.now();
                        var tenistasEntity = tenistas.stream().map(TenistaMapper::toTenistaEntity).toList();
                        tenistasEntity.forEach(t -> {
                            // Insertamos
                            dao.insert(
                                    t.nombre(),
                                    t.pais(),
                                    t.altura(),
                                    t.peso(),
                                    t.puntos(),
                                    t.mano(),
                                    t.fecha_nacimiento(),
                                    timeStamp.toString(),
                                    timeStamp.toString()
                            );
                        });

                    });
                    return Either.<TenistaError, Integer>right(tenistas.size());
                }).subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    //logger.error("Error insertando tenistas", e);
                    return Mono.just(Either.left(new TenistaError.DatabaseError("No se han insertando tenistas en la bd ->" + e.getMessage())));
                });
    }


}
