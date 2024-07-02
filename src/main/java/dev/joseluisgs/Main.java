package dev.joseluisgs;


import dev.joseluisgs.models.Tenista;
import dev.joseluisgs.storage.TenistasStorageCsv;
import dev.joseluisgs.storage.TenistasStorageJson;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlite3.SQLitePlugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    // Vamos a probar jdbi en sqlite memoria


    public static void main(String[] args) throws InterruptedException {

        // Vamos a probar el storage CSV
        ArrayList<Tenista> tenistas = new ArrayList<>();
        var storageCsv = new TenistasStorageCsv();
        var fileImport = Path.of("data", "tenistas.csv").toFile();
        storageCsv.importFile(fileImport)
                .blockOptional()
                .ifPresentOrElse(
                        result -> result.fold(
                                left -> {
                                    System.out.println("Error: " + left.getMessage());
                                    return null; // No necesita devolver ningún valor en particular
                                },
                                right -> {
                                    int successValue = right.size();
                                    System.out.println("Tenistas Importados por CSV: " + successValue);
                                    tenistas.addAll(right);
                                    return null; // No necesita devolver ningún valor en particular
                                }
                        ),
                        () -> System.out.println("La operación ha devuelto un valor nulo")
                );

        // Ahora escribimos
        var fileExport = Path.of("data", "tenistas_export.csv").toFile();
        storageCsv.exportFile(fileExport, tenistas)
                .blockOptional()
                .ifPresentOrElse(
                        result -> result.fold(
                                left -> {
                                    System.out.println("Error: " + left.getMessage());
                                    return null; // No necesita devolver ningún valor en particular
                                },
                                right -> {
                                    int successValue = right;
                                    System.out.println("Tenistas Exportados por CSV: " + successValue);
                                    return null; // No necesita devolver ningún valor en particular
                                }
                        ),
                        () -> System.out.println("La operación ha devuelto un valor nulo")
                );

        // Ahora vamos a probar el storage JSON
        // LKimpiamos los tenistas
        tenistas.clear();
        var fileImportJson = Path.of("data", "tenistas.json").toFile();
        var storageJson = new TenistasStorageJson();
        storageJson.importFile(fileImportJson)
                .blockOptional()
                .ifPresentOrElse(
                        result -> result.fold(
                                left -> {
                                    System.out.println("Error: " + left.getMessage());
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

        // Ahora escribimos
        var fileExportJson = Path.of("data", "tenistas_export.json").toFile();
        storageJson.exportFile(fileExportJson, tenistas)
                .blockOptional()
                .ifPresentOrElse(
                        result -> result.fold(
                                left -> {
                                    System.out.println("Error: " + left.getMessage());
                                    return null; // No necesita devolver ningún valor en particular
                                },
                                right -> {
                                    int successValue = right;
                                    System.out.println("Tenistas Exportados JSON: " + successValue);
                                    return null; // No necesita devolver ningún valor en particular
                                }
                        ),
                        () -> System.out.println("La operación ha devuelto un valor nulo")
                );

        var db = Jdbi.create("jdbc:sqlite:tenistas").installPlugin(new SQLitePlugin());
        List<Map<String, Object>> results = db.withHandle(handle -> {
            // Creamos una tabla
            handle.execute("CREATE TABLE IF NOT EXISTS tenistas (id INTEGER PRIMARY KEY, nombre TEXT, edad INTEGER)");
            // Borramos los datos
            handle.execute("DELETE FROM tenistas");
            // Insertamos datos
            handle.execute("INSERT INTO tenistas (nombre, edad) VALUES (?, ?)", "Rafa Nadal", 34);
            handle.execute("INSERT INTO tenistas (nombre, edad) VALUES (?, ?)", "Roger Federer", 39);
            // Seleccionamos datos
            return handle.select("SELECT * FROM tenistas").mapToMap().list();
        });
        System.out.println("Tenistas de la base de datos");
        results.forEach(System.out::println);

    }

}