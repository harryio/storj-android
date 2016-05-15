package com.harryio.storj;

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
