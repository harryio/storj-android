package com.harryio.storj;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StorjServiceProvider {
    private static StorjService storjService;

    public static StorjService getInstance() {
        if (storjService == null) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://app.storj.io")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            storjService = retrofit.create(StorjService.class);
        }

        return storjService;
    }
}
