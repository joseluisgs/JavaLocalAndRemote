package dev.joseluisgs.rest;

import com.jakewharton.retrofit2.adapter.reactor.ReactorCallAdapterFactory;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.inject.Singleton;

@Singleton
public class RetrofitClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient(String baseUrl) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .addCallAdapterFactory(ReactorCallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
