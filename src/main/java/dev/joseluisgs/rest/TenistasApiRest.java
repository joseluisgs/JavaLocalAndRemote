package dev.joseluisgs.rest;

import dev.joseluisgs.dto.TenistaDto;
import reactor.core.publisher.Mono;
import retrofit2.http.*;

import java.util.List;

public interface TenistasApiRest {
    String API_TENISTAS_URL = "https://my-json-server.typicode.com/joseluisgs/KotlinLocalAndRemote/";

    @GET("tenistas")
    Mono<List<TenistaDto>> getAll();

    @GET("tenistas/{id}")
    Mono<TenistaDto> getById(@Path("id") Long id);

    @POST("tenistas")
    Mono<TenistaDto> save(@Body TenistaDto tenista);

    @PUT("tenistas/{id}")
    Mono<TenistaDto> update(@Path("id") Long id, @Body TenistaDto tenista);

    @DELETE("tenistas/{id}")
    Mono<Void> delete(@Path("id") Long id);
}
