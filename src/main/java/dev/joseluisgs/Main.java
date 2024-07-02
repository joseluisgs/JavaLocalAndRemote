package dev.joseluisgs;


import dev.joseluisgs.database.JdbiManager;
import dev.joseluisgs.database.TenistasDao;
import dev.joseluisgs.mapper.TenistaMapper;
import dev.joseluisgs.models.Tenista;
import dev.joseluisgs.storage.TenistasStorageJson;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlite3.SQLitePlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.nio.file.Path;
import java.util.ArrayList;

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

        var db = Jdbi.create("jdbc:sqlite:tenistas")
                .installPlugin(new SQLitePlugin())
                .installPlugin(new SqlObjectPlugin());
        var result = db.withExtension(TenistasDao.class, dao -> {
            dao.createTable();
            // Borramos todos los tenistas
            dao.removeAll();
            // Insertamos los tenistas
            var tenista = TenistaMapper.toTenistaEntity(tenistas.getFirst());
            var id = dao.insert(
                    tenista.nombre(),
                    tenista.pais(),
                    tenista.altura(),
                    tenista.peso(),
                    tenista.puntos(),
                    tenista.mano(),
                    tenista.fecha_nacimiento(),
                    tenista.created_at(),
                    tenista.updated_at()
            );
            System.out.println("Tenista insertado: " + id);

            // Seleccionamos todos los tenistas
            dao.getAll().forEach(System.out::println);

            // Seleccionamos un tenista
            var tenistaEntity = dao.findById(id);
            System.out.println("Tenista encontrado: " + tenistaEntity);

            // Actualizamos un tenista
            var tenistaUpdate = TenistaMapper.toTenistaEntity(tenistas.get(2));

            var idUpdate = dao.update(
                    1,
                    tenistaUpdate.nombre(),
                    tenistaUpdate.pais(),
                    tenistaUpdate.altura(),
                    tenistaUpdate.peso(),
                    tenistaUpdate.puntos(),
                    tenistaUpdate.mano(),
                    tenistaUpdate.fecha_nacimiento(),
                    tenistaUpdate.updated_at(),
                    tenistaUpdate.is_deleted()
            );

            System.out.println("Tenista actualizado: " + idUpdate);

            // Seleccionamos todos los tenistas
            dao.getAll().forEach(System.out::println);

            // Borramos un tenista
            var idDelete = dao.delete(1);
            System.out.println("Tenista borrado: " + idDelete);

            // Seleccionamos todos los tenistas
            dao.getAll().forEach(System.out::println);

            return dao.getAll();
        });
        System.out.println("Tenistas de la base de datos");
        System.out.println(result.size());
        result.forEach(System.out::println);

        JdbiManager<TenistasDao> dbManager = new JdbiManager<>("jdbc:sqlite:tenistas", TenistasDao.class);

        var res = dbManager.with(TenistasDao::getAll);
        System.out.println("Tenistas de la base de datos 2");
    }

}