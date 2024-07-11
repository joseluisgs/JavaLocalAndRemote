package dev.joseluisgs;


import dev.joseluisgs.di.DaggerAppComponent;
import dev.joseluisgs.models.Tenista;
import dev.joseluisgs.service.TenistasServiceImpl;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static reactor.core.scheduler.Schedulers.boundedElastic;

public class Main {
    // Vamos a probar jdbi en sqlite memoria


    public static void main(String[] args) throws InterruptedException {

        System.out.println("🎾🎾 Hola Tenistas! 🎾🎾");

        // Creamos el servicio
        /*TenistasServiceImpl tenistasService = new TenistasServiceImpl(
                new TenistasRepositoryLocal(new JdbiManager<>("tenistas.db", TenistasDao.class)),
                new TenistasRepositoryRemote(RetrofitClient.getClient(TenistasApiRest.API_TENISTAS_URL).create(TenistasApiRest.class)),
                new TenistasCacheImpl(5),
                new TenistasStorageCsv(),
                new TenistasStorageJson(),
                new TenistasNotifications()
        );*/

        TenistasServiceImpl tenistasService = DaggerAppComponent.create().getTenistasService();

        // Recogemos las notificaciones
        System.out.println("🔊 Escuchando notificaciones de tenistas 🔊");
        var notifications = tenistasService.getNotifications().subscribe(notification -> {
            switch (notification.type()) {
                case CREATE ->
                        System.out.println("🟢 Notificación de creación de tenista: " + notification.message() + " -> " + notification.item());
                case UPDATE ->
                        System.out.println("🟠 Notificación de actualización de tenista: " + notification.message() + " -> " + notification.item());
                case DELETE ->
                        System.out.println("🔴 Notificación de eliminación de tenista: " + notification.message());
                case REFRESH -> System.out.println("🔵 Notificación de refresco de tenistas: " + notification.message());
            }
        });

        System.out.println("🔄 Refrescamos los tenistas automáticamente 🔄");
        tenistasService.enableAutoRefresh();

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
        tenistasService.delete(2L).subscribeOn(boundedElastic()).blockOptional().ifPresent(
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

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("🔇 Desactivamos la escucha de notificaciones de tenistas 🔇");
        tenistasService.disableAutoRefresh();
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

        // Obtenemos la lista de tenistas, ya sabemos cuales son
        var tenistasActuales = tenistasService.getAll(true).blockOptional().get().get();

        // Tenistas ordenados con ranking, es decir, por puntos de mayor a menor
        System.out.println("Tenistas ordenados por ranking:");
        tenistasActuales.stream()
                .sorted(Comparator.comparing(Tenista::getPuntos).reversed())
                .forEach(t -> System.out.printf("Ranking: %d: %s -> %d%n", t.getPuntos(), t.getNombre(), t.getPuntos()));


        //  Media de altura de los tenistas
        var mediaAltura = tenistasActuales.stream().mapToInt(Tenista::getAltura).average().orElse(0.0);
        System.out.printf("Media de altura de los tenistas: %.2f%n", mediaAltura);

        // Media de peso de los tenistas
        var mediaPeso = tenistasActuales.stream().mapToInt(Tenista::getPeso).average().orElse(0.0);
        System.out.printf("Media de peso de los tenistas: %.2f%n", mediaPeso); // Media de peso de los tenistas con 2 decimales

        // Tenista más alto
        var tenistaMasAlto = tenistasActuales.stream().max(Comparator.comparing(Tenista::getAltura)).orElse(null);
        System.out.printf("Tenista más alto: " + tenistaMasAlto);

        // Tenistas españoles
        var tenistasEspanoles = tenistasActuales.stream().filter(t -> t.getPais().equals("España"));
        System.out.println("Tenistas españoles:" + tenistasEspanoles);

        // Tenistas agrupados por paises
        System.out.println("Tenistas agrupados por paises:");
        var tenistasPorPais = tenistasActuales.stream().collect(groupingBy(Tenista::getPais));
        tenistasPorPais.forEach((pais, lista) -> System.out.println(pais + " -> " + lista));

        // Tenistas agrupados por paises y ordenados por puntos decendentes
        System.out.println("Tenistas agrupados por paises y ordenados por puntos decendentes:");
        var tenistasPorPaisOrdenados = tenistasActuales.stream()
                .collect(Collectors.groupingBy(
                        Tenista::getPais,
                        () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                tenistasLista -> tenistasLista.stream()
                                        .sorted(Comparator.comparing(Tenista::getPuntos).reversed())
                                        .collect(Collectors.toList())
                        )
                ));

        tenistasPorPaisOrdenados.forEach((pais, lista) -> System.out.println(pais + " -> " + lista));

        // Puntuación total de los tenistas agrupados por paises
        System.out.println("Puntuación total de los tenistas agrupados por paises:");
        var puntuacionTotalPorPais = tenistasActuales.stream()
                .collect(groupingBy(Tenista::getPais, Collectors.summingInt(Tenista::getPuntos)));

        // País con puntuación total más alta
        var paisMasPuntos = puntuacionTotalPorPais.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(e -> e.getKey())
                .orElse("Desconocido");

        System.out.printf("País con más puntos: %s -> %d%n", paisMasPuntos, puntuacionTotalPorPais.get(paisMasPuntos));


        System.out.println("👋👋 Adiós Tenistas! 👋👋");

        // Cerramos la conexión
        System.exit(0);
    }

}