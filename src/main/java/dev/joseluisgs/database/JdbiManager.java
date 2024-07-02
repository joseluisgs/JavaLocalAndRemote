package dev.joseluisgs.database;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbiManager<T> {
    private final Logger logger = LoggerFactory.getLogger(JdbiManager.class);
    private final Jdbi jdbi;
    private final Class<T> daoClass;

    public JdbiManager(String database, Class<T> daoClass) {
        logger.debug("Creando JdbiManager con URL: {}", database);
        this.daoClass = daoClass;
        this.jdbi = Jdbi.create("jdbc:sqlite:" + database); // jdbc:sqlite:tenistas
        this.jdbi.installPlugin(new SqlObjectPlugin());
    }


    // Para operaciones con la base de datos que devuelven un valor
    public <R> R with(HandleFunction<T, R> handleFunction) {
        return jdbi.withExtension(daoClass, handleFunction::apply);
    }

    // Para operaciones con la base de datos que no devuelven un valor
    public void use(VoidHandleFunction<T> handleFunction) {
        jdbi.useExtension(daoClass, handleFunction::apply);
    }

    // Para transacciones con resultado
    public <R> R inTransaction(TransactionFunction<T, R> transactionFunction) {
        return jdbi.inTransaction(handle -> {
            T dao = handle.attach(daoClass);
            return transactionFunction.apply(dao);
        });
    }

    // Para transacciones sin resultado
    public void useTransaction(VoidTransactionFunction<T> transactionFunction) {
        jdbi.useTransaction(handle -> {
            T dao = handle.attach(daoClass);
            transactionFunction.apply(dao);
        });
    }

    @FunctionalInterface
    public interface HandleFunction<T, R> {
        R apply(T dao);
    }

    @FunctionalInterface
    public interface VoidHandleFunction<T> {
        void apply(T dao);
    }

    @FunctionalInterface
    public interface TransactionFunction<T, R> {
        R apply(T dao);
    }

    @FunctionalInterface
    public interface VoidTransactionFunction<T> {
        void apply(T dao);
    }
}
