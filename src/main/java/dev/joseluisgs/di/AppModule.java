package dev.joseluisgs.di;

import dagger.Module;
import dagger.Provides;
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
import dev.joseluisgs.utils.ConfigProperties;

import javax.inject.Singleton;

import static dev.joseluisgs.rest.TenistasApiRest.API_TENISTAS_URL;

@Module
public class AppModule {

    private final ConfigProperties configProperties;

    public AppModule() {
        this.configProperties = new ConfigProperties("config.properties");
    }

    @Provides
    @Singleton
    public TenistasRepositoryLocal providesLocalRepository() {
        String dbUrl = configProperties.getProperty("database.name", "tenistas.db");
        return new TenistasRepositoryLocal(new JdbiManager<>(dbUrl, TenistasDao.class));
    }

    @Provides
    @Singleton
    public TenistasRepositoryRemote providesRemoteRepository() {
        String apiUrl = configProperties.getProperty("api.rest", API_TENISTAS_URL);
        return new TenistasRepositoryRemote(RetrofitClient.getClient(apiUrl).create(TenistasApiRest.class));
    }

    @Provides
    public TenistasCacheImpl providesCache() {
        return new TenistasCacheImpl(5);
    }

    @Provides
    @Singleton
    public TenistasStorageCsv providesStorageCsv() {
        return new TenistasStorageCsv();
    }

    @Provides
    @Singleton
    public TenistasStorageJson providesStorageJson() {
        return new TenistasStorageJson();
    }

    @Provides
    @Singleton
    public TenistasNotifications providesNotifications() {
        return new TenistasNotifications();
    }

    @Provides
    @Singleton
    public TenistasServiceImpl providesTenistasService(TenistasRepositoryLocal localRepo,
                                                       TenistasRepositoryRemote remoteRepo,
                                                       TenistasCacheImpl cache,
                                                       TenistasStorageCsv csvStorage,
                                                       TenistasStorageJson jsonStorage,
                                                       TenistasNotifications notifications) {
        return new TenistasServiceImpl(localRepo, remoteRepo, cache, csvStorage, jsonStorage, notifications);
    }
}
