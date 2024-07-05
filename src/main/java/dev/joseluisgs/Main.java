package dev.joseluisgs;


import dev.joseluisgs.cache.TenistasCacheImpl;
import dev.joseluisgs.database.JdbiManager;
import dev.joseluisgs.database.TenistasDao;
import dev.joseluisgs.notification.TenistasNotifications;
import dev.joseluisgs.repository.TenistasRepositoryLocal;
import dev.joseluisgs.repository.TenistasRepositoryRemote;
import dev.joseluisgs.rest.RetrofitClient;
import dev.joseluisgs.rest.TenistasApiRest;
import dev.joseluisgs.service.TenistasServiceImpl;
import dev.joseluisgs.storage.TenistasStorageCsv;
import dev.joseluisgs.storage.TenistasStorageJson;

import java.nio.file.Path;
import java.util.Collections;

import static reactor.core.scheduler.Schedulers.boundedElastic;

public class Main {
    // Vamos a probar jdbi en sqlite memoria


    public static void main(String[] args) throws InterruptedException {

        System.out.println("🎾🎾 Hola Tenistas! 🎾🎾");

        // Creamos el servicio
        TenistasServiceImpl tenistasService = new TenistasServiceImpl(
                new TenistasRepositoryLocal(new JdbiManager<>("tenistas.db", TenistasDao.class)),
                new TenistasRepositoryRemote(RetrofitClient.getClient(TenistasApiRest.API_TENISTAS_URL).create(TenistasApiRest.class)),
                new TenistasCacheImpl(5),
                new TenistasStorageCsv(),
                new TenistasStorageJson(),
                new TenistasNotifications()
        );

        // Recogemos las notificaciones
        System.out.println("🔊 Escuchando notificaciones de tenistas 🔊");
        var notifications = tenistasService.getNotifications().subscribe(notification -> {
            switch (notification.type()) {
                case CREATE ->
                        System.out.println("🟢 Notificación de creación de tenista:: " + notification.message() + " -> " + notification.item());
                case UPDATE ->
                        System.out.println("🟠 Notificación de actualización de tenista:: " + notification.message() + " -> " + notification.item());
                case DELETE ->
                        System.out.println("🔴 Notificación de eliminación de tenista:: " + notification.message() + " -> " + notification.item());
                case REFRESH ->
                        System.out.println("🔵 Notificación de refresco de tenistas:: " + notification.message());
            }
        });

        tenistasService.refresh();

        // Esperamos un poco
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Obtenemos todos los tenistas
        var tenistas = tenistasService.getAll(false).blockOptional().map(
                        result -> result.fold(
                                left -> {
                                    System.out.println(left.getMessage());
                                    return null; // Devuelve una lista vacía en caso de error
                                },
                                right -> {
                                    System.out.println("Tenistas recuperados: " + right.size());
                                    System.out.println(right);
                                    return right; // Devuelve la lista de tenistas en caso de éxito
                                }
                        )
                )
                .orElse(Collections.emptyList()); // En caso de Optional.empty()

        // Obtenemos un tenista que existe
        tenistasService.getById(1L).blockOptional().ifPresent(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // No necesita devolver ningún valor en particular
                        },
                        right -> {
                            System.out.println("Tenista encontrado: " + right);
                            return null; // No necesita devolver ningún valor en particular
                        }
                )
        );

        // Obtenemos un tenista que no existe
        tenistasService.getById(-1L).blockOptional().ifPresent(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // No necesita devolver ningún valor en particular
                        },
                        right -> {
                            System.out.println("Tenista encontrado: " + right);
                            return null; // No necesita devolver ningún valor en particular
                        }
                )
        );

        // Guardamos un tenista
        var tenista = tenistas.getFirst().nombre("Test Insert");
        tenistasService.save(tenista).subscribeOn(boundedElastic()).blockOptional().ifPresent(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // No necesita devolver ningún valor en particular
                        },
                        right -> {
                            System.out.println("Tenista guardado: " + right);
                            return null; // No necesita devolver ningún valor en particular
                        }
                )
        );

        // Obtenemos todos los tenistas
        tenistasService.getAll(false).blockOptional().map(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // Devuelve una lista vacía en caso de error
                        },
                        right -> {
                            System.out.println("Tenistas recuperados: " + right.size());
                            System.out.println(right);
                            return right; // Devuelve la lista de tenistas en caso de éxito
                        }
                )
        );

        // Actualizamos un tenista
        var tenistaUpdate = tenistas.getFirst().nombre("Test Update").id(1L);
        tenistasService.update(1L, tenistaUpdate).subscribeOn(boundedElastic()).blockOptional().ifPresent(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // No necesita devolver ningún valor en particular
                        },
                        right -> {
                            System.out.println("Tenista actualizado: " + right);
                            return null; // No necesita devolver ningún valor en particular
                        }
                )
        );

        // Obtenemos todos los tenistas
        tenistasService.getAll(false).blockOptional().map(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // Devuelve una lista vacía en caso de error
                        },
                        right -> {
                            System.out.println("Tenistas recuperados: " + right.size());
                            System.out.println(right);
                            return right; // Devuelve la lista de tenistas en caso de éxito
                        }
                )
        );

        // Actualizamos un tenista que no existe
        tenistasService.update(-1L, tenistaUpdate).subscribeOn(boundedElastic()).blockOptional().ifPresent(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // No necesita devolver ningún valor en particular
                        },
                        right -> {
                            System.out.println("Tenista actualizado: " + right);
                            return null; // No necesita devolver ningún valor en particular
                        }
                )
        );

        // Eliminamos un tenista
        tenistasService.delete(1L).subscribeOn(boundedElastic()).blockOptional().ifPresent(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // No necesita devolver ningún valor en particular
                        },
                        right -> {
                            System.out.println("Tenista eliminado: " + right);
                            return null; // No necesita devolver ningún valor en particular
                        }
                )
        );

        // Obtenemos todos los tenistas
        tenistasService.getAll(false).blockOptional().map(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // Devuelve una lista vacía en caso de error
                        },
                        right -> {
                            System.out.println("Tenistas recuperados: " + right.size());
                            System.out.println(right);
                            return right; // Devuelve la lista de tenistas en caso de éxito
                        }
                )
        );

        // Eliminamos un tenista que no existe
        tenistasService.delete(-1L).subscribeOn(boundedElastic()).blockOptional().ifPresent(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // No necesita devolver ningún valor en particular
                        },
                        right -> {
                            System.out.println("Tenista eliminado: " + right);
                            return null; // No necesita devolver ningún valor en particular
                        }
                )
        );

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("🔇 Desactivamos la escucha de notificaciones de tenistas 🔇");
        notifications.dispose();

        // Pruebas de ficheros CSV Import
        var csvImport = Path.of("data", "tenistas2.csv").toFile();
        tenistasService.importData(csvImport).subscribeOn(boundedElastic()).blockOptional().ifPresent(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // No necesita devolver ningún valor en particular
                        },
                        right -> {
                            System.out.println("Tenistas importados: " + right);
                            return null; // No necesita devolver ningún valor en particular
                        }
                )
        );

        // Pruebas con ficheros JSON Import
        var jsonImport = Path.of("data", "tenistas3.json").toFile();
        tenistasService.importData(jsonImport).subscribeOn(boundedElastic()).blockOptional().ifPresent(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // No necesita devolver ningún valor en particular
                        },
                        right -> {
                            System.out.println("Tenistas importados: " + right);
                            return null; // No necesita devolver ningún valor en particular
                        }
                )
        );

        // Pruebas con ficheros CSV Export
        var csvExport = Path.of("data", "tenistas_export.csv").toFile();
        tenistasService.exportData(csvExport, true).subscribeOn(boundedElastic()).blockOptional().ifPresent(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // No necesita devolver ningún valor en particular
                        },
                        right -> {
                            System.out.println("Tenistas exportados: " + right);
                            return null; // No necesita devolver ningún valor en particular
                        }
                )
        );

        // Pruebas con ficheros JSON Export
        var jsonExport = Path.of("data", "tenistas_export.json").toFile();
        tenistasService.exportData(jsonExport, true).subscribeOn(boundedElastic()).blockOptional().ifPresent(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // No necesita devolver ningún valor en particular
                        },
                        right -> {
                            System.out.println("Tenistas exportados: " + right);
                            return null; // No necesita devolver ningún valor en particular
                        }
                )
        );


        // Cerramos la conexión
        System.exit(0);
    }

}