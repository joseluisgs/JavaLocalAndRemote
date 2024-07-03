package dev.joseluisgs;


import dev.joseluisgs.database.JdbiManager;
import dev.joseluisgs.database.TenistasDao;
import dev.joseluisgs.models.Tenista;
import dev.joseluisgs.repository.TenistasRepositoryLocal;
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


        // Probamos Retrofit
        TenistasApiRest api = RetrofitClient.getClient(TenistasApiRest.API_TENISTAS_URL).create(TenistasApiRest.class);

        api.getById(1L).blockOptional()
                .ifPresentOrElse(
                        result -> {
                            System.out.println("Tenista recuperado rest: " + result);
                        },
                        () -> System.out.println("No se ha encontrado el tenista")
                );

        api.getAll().blockOptional()
                .ifPresentOrElse(
                        result -> {
                            System.out.println("Tenistas recuperados rest: " + result.size());
                            result.forEach(t -> System.out.println(t.id() + " - " + t.nombre()));
                        },
                        () -> System.out.println("No se han encontrado tenistas")
                );

        System.out.println("Fin de la ejecución");
        System.exit(0);
    }

}