package dev.joseluisgs.database;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface TenistasDao {
    @SqlUpdate("""
              CREATE TABLE IF NOT EXISTS TenistaEntity (
                      id INTEGER PRIMARY KEY,
                      nombre TEXT NOT NULL,
                      pais TEXT NOT NULL,
                      altura INTEGER NOT NULL,
                      peso INTEGER NOT NULL,
                      puntos INTEGER NOT NULL,
                      mano TEXT NOT NULL,
                      fecha_nacimiento TEXT NOT NULL,
                      created_at TEXT NOT NULL,
                      updated_at TEXT NOT NULL,
                      is_deleted INTEGER NOT NULL DEFAULT 0
                  )
            """)
    void createTable();


    // Remove all
    @SqlUpdate("DELETE FROM TenistaEntity")
    void removeAll();

    // select all
    @SqlQuery("SELECT * FROM TenistaEntity")
    // @RegisterRowMapper(TenistaEntityMapper.class)
    @RegisterConstructorMapper(TenistaEntity.class)
    List<TenistaEntity> getAll();

    // select by id
    @SqlQuery("SELECT * FROM TenistaEntity WHERE id = :id")
    @RegisterConstructorMapper(TenistaEntity.class)
    Optional<TenistaEntity> findById(@Bind("id") long id);

    // insert and return id
    @SqlUpdate("INSERT INTO TenistaEntity (nombre, pais, altura, peso, puntos, mano, fecha_nacimiento, created_at, updated_at) VALUES (:nombre, :pais, :altura, :peso, :puntos, :mano, :fecha_nacimiento, :created_at, :updated_at)")
    @GetGeneratedKeys("id")
    long insert(@Bind("nombre") String nombre,
                @Bind("pais") String pais,
                @Bind("altura") int altura,
                @Bind("peso") int peso,
                @Bind("puntos") int puntos,
                @Bind("mano") String mano,
                @Bind("fecha_nacimiento") String fechaNacimiento,
                @Bind("created_at") String createdAt,
                @Bind("updated_at") String updatedAt);

    // update
    @SqlUpdate("UPDATE TenistaEntity SET nombre = :nombre, pais = :pais, altura = :altura, peso = :peso, puntos = :puntos, mano = :mano, fecha_nacimiento = :fecha_nacimiento, updated_at = :updated_at, is_deleted = :is_deleted WHERE id = :id")
    int update(@Bind("id") long id,
               @Bind("nombre") String nombre,
               @Bind("pais") String pais,
               @Bind("altura") int altura,
               @Bind("peso") int peso,
               @Bind("puntos") int puntos,
               @Bind("mano") String mano,
               @Bind("fecha_nacimiento") String fechaNacimiento,
               @Bind("updated_at") String updatedAt,
               @Bind("is_deleted") boolean isDeleted);

    // delete
    @SqlUpdate("DELETE FROM TenistaEntity WHERE id = :id")
    int delete(@Bind("id") long id);

}
