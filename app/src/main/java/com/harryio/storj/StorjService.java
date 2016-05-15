package com.harryio.storj;

import com.harryio.storj.model.User;
import com.harryio.storj.model.UserStatus;

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
    Call<UserStatus> registerUser(@Body User user);
}
