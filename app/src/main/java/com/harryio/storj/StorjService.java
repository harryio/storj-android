package com.harryio.storj;

import com.harryio.storj.model.SignUpResult;
import com.harryio.storj.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface StorjService {
    @POST("/users")
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    Call<SignUpResult> registerUser(@Body User user);
}
