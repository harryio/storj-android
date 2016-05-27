package com.harryio.storj;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StorjServiceProvider {
    private static StorjService storjService;

    public static StorjService getInstance() {
        if (storjService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request();
                            Headers headers = request.headers().newBuilder()
                                    .add("Content-Type", "application/json")
                                    .add("Accept", "application/json")
                                    .build();
                            request = request.newBuilder().headers(headers).build();
                            return chain.proceed(request);
                        }
                    })
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.storj.io/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            storjService = retrofit.create(StorjService.class);
        }

        return storjService;
    }
}
