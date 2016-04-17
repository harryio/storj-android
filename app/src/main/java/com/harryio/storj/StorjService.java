package com.harryio.storj;

import com.harryio.storj.model.User;
import com.harryio.storj.model.UserStatus;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface StorjService {
    @POST("/users")
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    Call<UserStatus> registerUser(@Body User user);

    @GET("/activations/{token}")
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    Call<UserStatus> activateAccount(@Path("token") String token);

    @POST("/keys")
    @Headers({
            "Content-Type: application/json"
    })
    Call<Void> registerKey(@Body Key key);

    @GET("/keys")
    @Headers({
            "Content-Type: application/json"
    })
    Call<Void> getKeys();
}
