package dev.joseluisgs.database;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mapeador de TenistaEntity
 * Podemos tener un mapeador para ResultSet y otro para ResultSet y StatementContext
 * Y así poder tener más control sobre el mapeo si es más complejo.
 */
public class TenistaEntityMapper implements RowMapper<TenistaEntity> {
    public TenistaEntityMapper() {
        super();
    }

    public TenistaEntity map(ResultSet rs, StatementContext ctx) throws SQLException {

        return new TenistaEntity(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getString("pais"),
                rs.getInt("altura"),
                rs.getInt("peso"),
                rs.getInt("puntos"),
                rs.getString("mano"),
                rs.getString("fecha_nacimiento"),
                rs.getString("created_at"),
                rs.getString("updated_at"),
                rs.getBoolean("is_deleted")
        );
    }
}