package dev.joseluisgs.di;

import dagger.Component;
import dev.joseluisgs.service.TenistasServiceImpl;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    TenistasServiceImpl getTenistasService();
}