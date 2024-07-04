package dev.joseluisgs;


import dev.joseluisgs.database.JdbiManager;
import dev.joseluisgs.database.TenistasDao;
import dev.joseluisgs.models.Tenista;
import dev.joseluisgs.notification.Notification;
import dev.joseluisgs.notification.TenistasNotifications;
import dev.joseluisgs.repository.TenistasRepositoryLocal;
import dev.joseluisgs.repository.TenistasRepositoryRemote;
import dev.joseluisgs.rest.RetrofitClient;
import dev.joseluisgs.rest.TenistasApiRest;
import dev.joseluisgs.storage.TenistasStorageJson;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

public class Main {
    // Vamos a probar jdbi en sqlite memoria


    public static void main(String[] args) throws InterruptedException {


        // Vamos a probar el storage CSV
        ArrayList<Tenista> tenistas = new ArrayList<>();
        var fileImportJson = Path.of("data", "tenistas.json").toFile();
        var storageJson = new TenistasStorageJson();
        storageJson.importFile(fileImportJson)
                .blockOptional()
                .ifPresentOrElse(
                        result ->
                                result.fold(
                                        left -> {
                                            System.out.println(left.getMessage());
                                            return null; // No necesita devolver ningún valor en particular
                                        },
                                        right -> {
                                            int successValue = right.size();
                                            System.out.println("Tenistas Importados JSON: " + successValue);
                                            tenistas.addAll(right);
                                            return null; // No necesita devolver ningún valor en particular
                                        }
                                ),
                        () -> System.out.println("La operación ha devuelto un valor nulo")
                );

        // Creamos el JdbiManager
        JdbiManager<TenistasDao> jdbiManager = new JdbiManager<>("tenistas.db", TenistasDao.class);
        // Creamos el repositorio
        TenistasRepositoryLocal local = new TenistasRepositoryLocal(jdbiManager);
        // Seleccionamos todos los tenistas
        local.getAll()
                .blockOptional()
                .ifPresentOrElse(
                        result -> result.fold(
                                left -> {
                                    System.out.println(left.getMessage());
                                    return null; // No necesita devolver ningún valor en particular
                                },
                                right -> {
                                    int successValue = right.size();
                                    System.out.println("Tenistas recuperados: " + successValue);
                                    return null; // No necesita devolver ningún valor en particular
                                }
                        ),
                        () -> System.out.println("La operación ha devuelto un valor nulo")
                );

        var newTenista = tenistas.getFirst();
        // Insertamos un nuevo tenista
        local.save(newTenista)
                .blockOptional()
                .ifPresentOrElse(
                        result -> result.fold(
                                left -> {
                                    System.out.println(left.getMessage());
                                    return null; // No necesita devolver ningún valor en particular
                                },
                                right -> {
                                    System.out.println("Tenista insertado: " + right);
                                    return null; // No necesita devolver ningún valor en particular
                                }
                        ),
                        () -> System.out.println("La operación ha devuelto un valor nulo")
                );

        // buscamos un tenista
        local.getById(newTenista.getId())
                .blockOptional()
                .ifPresentOrElse(
                        result -> result.fold(
                                left -> {
                                    System.out.println(left.getMessage());
                                    return null; // No necesita devolver ningún valor en particular
                                },
                                right -> {
                                    System.out.println("Tenista encontrado: " + right);
                                    return null; // No necesita devolver ningún valor en particular
                                }
                        ),
                        () -> System.out.println("La operación ha devuelto un valor nulo")
                );

        // Buscamos un tenista que no existe
        local.getById(-1L)
                .blockOptional()
                .ifPresentOrElse(
                        result -> result.fold(
                                left -> {
                                    System.out.println(left.getMessage());
                                    return null; // No necesita devolver ningún valor en particular
                                },
                                right -> {
                                    System.out.println("Tenista encontrado: " + right);
                                    return null; // No necesita devolver ningún valor en particular
                                }
                        ),
                        () -> System.out.println("La operación ha devuelto un valor nulo")
                );

        // Actualizamos un tenista
        var updatedTenista = tenistas.get(1);
        local.update(newTenista.getId(), updatedTenista)
                .blockOptional()
                .ifPresentOrElse(
                        result -> result.fold(
                                left -> {
                                    System.out.println(left.getMessage());
                                    return null; // No necesita devolver ningún valor en particular
                                },
                                right -> {
                                    System.out.println("Tenista actualizado: " + right);
                                    return null; // No necesita devolver ningún valor en particular
                                }
                        ),
                        () -> System.out.println("La operación ha devuelto un valor nulo")
                );

        // Actualizamos un tenista que no existe
        local.update(-1L, updatedTenista)
                .blockOptional()
                .ifPresentOrElse(
                        result -> result.fold(
                                left -> {
                                    System.out.println(left.getMessage());
                                    return null; // No necesita devolver ningún valor en particular
                                },
                                right -> {
                                    System.out.println("Tenista actualizado: " + right);
                                    return null; // No necesita devolver ningún valor en particular
                                }
                        ),
                        () -> System.out.println("La operación ha devuelto un valor nulo")
                );

        // Eliminamos un tenista
        local.delete(newTenista.getId())
                .blockOptional()
                .ifPresentOrElse(
                        result -> result.fold(
                                left -> {
                                    System.out.println(left.getMessage());
                                    return null; // No necesita devolver ningún valor en particular
                                },
                                right -> {
                                    System.out.println("Tenista eliminado: " + right);
                                    return null; // No necesita devolver ningún valor en particular
                                }
                        ),
                        () -> System.out.println("La operación ha devuelto un valor nulo")
                );

        // Eliminamos un tenista que no existe
        local.delete(-1L)
                .blockOptional()
                .ifPresentOrElse(
                        result -> result.fold(
                                left -> {
                                    System.out.println(left.getMessage());
                                    return null; // No necesita devolver ningún valor en particular
                                },
                                right -> {
                                    System.out.println("Tenista eliminado: " + right);
                                    return null; // No necesita devolver ningún valor en particular
                                }
                        ),
                        () -> System.out.println("La operación ha devuelto un valor nulo")
                );

        // Eliminamos todos los tenistas
        local.removeAll()
                .blockOptional()
                .ifPresentOrElse(
                        result -> result.fold(
                                left -> {
                                    System.out.println(left.getMessage());
                                    return null; // No necesita devolver ningún valor en particular
                                },
                                right -> {
                                    System.out.println("Tenistas eliminados: " + right);
                                    return null; // No necesita devolver ningún valor en particular
                                }
                        ),
                        () -> System.out.println("La operación ha devuelto un valor nulo")
                );

        // Vamos a probar a insertar todos los tenistas
        local.saveAll(tenistas)
                .blockOptional()
                .ifPresentOrElse(
                        result -> result.fold(
                                left -> {
                                    System.out.println(left.getMessage());
                                    return null; // No necesita devolver ningún valor en particular
                                },
                                right -> {
                                    System.out.println("Tenistas insertados: " + right);
                                    return null; // No necesita devolver ningún valor en particular
                                }
                        ),
                        () -> System.out.println("La operación ha devuelto un valor nulo")
                );

        // Seleccionamos todos los tenistas
        // Acuerdate de esta forma con el map, porque lo vas a usar mucho con el Optional
        var lista = local.getAll()
                .blockOptional()
                .map(result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // Devuelve una lista vacía en caso de error
                        },
                        right -> {
                            return right; // Devuelve la lista de tenistas en caso de éxito
                        }
                ))
                .orElse(Collections.emptyList()); // En caso de Optional.empty()

        // Aquí se puede usar la variable "tenistas". Si es null, no se encontraron tenistas o hubo un error.
        System.out.println("Tenistas recuperados: " + lista.size());
        // Mostrar el id y el tenista
        lista.forEach(t -> System.out.println(t.getId() + " - " + t.getNombre()));


        // Probamos el repositorio remoto
        TenistasRepositoryRemote remote = new TenistasRepositoryRemote(RetrofitClient.getClient(TenistasApiRest.API_TENISTAS_URL).create(TenistasApiRest.class));

        // Obtenemos todos
        var tenistasRemotos = remote.getAll().blockOptional().map(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // Devuelve una lista vacía en caso de error
                        },
                        right -> {
                            System.out.println("Tenistas recuperados: " + right.size());
                            return right; // Devuelve la lista de tenistas en caso de éxito
                        }
                )
        ).orElse(Collections.emptyList()); // En caso de Optional.empty()

        // Mostrar el id y el tenista
        tenistasRemotos.forEach(t -> System.out.println(t.getId() + " - " + t.getNombre()));

        // Obtenemos un tenista con id 1
        remote.getById(1L).blockOptional().ifPresentOrElse(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // No necesita devolver ningún valor en particular
                        },
                        right -> {
                            System.out.println("Tenista encontrado: " + right);
                            return null; // No necesita devolver ningún valor en particular
                        }
                ),
                () -> System.out.println("La operación ha devuelto un valor nulo")
        );

        // Obtenemos un tenista que no existe -1
        remote.getById(-1L).blockOptional().ifPresentOrElse(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // No necesita devolver ningún valor en particular
                        },
                        right -> {
                            System.out.println("Tenista encontrado: " + right);
                            return null; // No necesita devolver ningún valor en particular
                        }
                ),
                () -> System.out.println("La operación ha devuelto un valor nulo")
        );

        // Actualizamos el tenista id 1, con este tenista
        var tenistaActualizado = tenistas.getFirst().nombre("Test Update").id(1L);
        remote.update(1L, tenistaActualizado).blockOptional().ifPresentOrElse(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // No necesita devolver ningún valor en particular
                        },
                        right -> {
                            System.out.println("Tenista actualizado: " + right);
                            return null; // No necesita devolver ningún valor en particular
                        }
                ),
                () -> System.out.println("La operación ha devuelto un valor nulo")
        );

        // Actualizamos un tenista que no existe
        remote.update(-1L, tenistaActualizado).blockOptional().ifPresentOrElse(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // No necesita devolver ningún valor en particular
                        },
                        right -> {
                            System.out.println("Tenista actualizado: " + right);
                            return null; // No necesita devolver ningún valor en particular
                        }
                ),
                () -> System.out.println("La operación ha devuelto un valor nulo")
        );

        // Eliminamos el tenista id 1
        remote.delete(1L).blockOptional().ifPresentOrElse(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // No necesita devolver ningún valor en particular
                        },
                        right -> {
                            System.out.println("Tenista eliminado: " + right);
                            return null; // No necesita devolver ningún valor en particular
                        }
                ),
                () -> System.out.println("La operación ha devuelto un valor nulo")
        );

        // Eliminamos un tenista que no existe
        remote.delete(-1L).blockOptional().ifPresentOrElse(
                result -> result.fold(
                        left -> {
                            System.out.println(left.getMessage());
                            return null; // No necesita devolver ningún valor en particular
                        },
                        right -> {
                            System.out.println("Tenista eliminado: " + right);
                            return null; // No necesita devolver ningún valor en particular
                        }
                ),
                () -> System.out.println("La operación ha devuelto un valor nulo")
        );

        TenistasNotifications tenistasNotifications = new TenistasNotifications();

        // Suscripción a las notificaciones
        tenistasNotifications.getNotifications().subscribe(notification -> {
            System.out.println("Nueva notificación recibida: " + notification);
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Envío de varias notificaciones de ejemplo
        tenistasNotifications.send(new Notification<>(Notification.Type.CREATE, tenistas.getFirst()));
        tenistasNotifications.send(new Notification<>(Notification.Type.UPDATE, tenistas.get(1)));
        tenistasNotifications.send(new Notification<>(Notification.Type.DELETE, tenistas.get(2)));
        tenistasNotifications.send(new Notification<>(Notification.Type.REFRESH));

        // Pausa para que se puedan procesar las notificaciones

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Cerramos la conexión
        System.out.println("Fin de la ejecución");
        System.exit(0);
    }

}