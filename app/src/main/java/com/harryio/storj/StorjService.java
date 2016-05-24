package com.harryio.storj;

import com.harryio.storj.model.Bucket;
import com.harryio.storj.model.BucketModel;
import com.harryio.storj.model.User;
import com.harryio.storj.model.UserStatus;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface StorjService {
    @POST("users")
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    Call<UserStatus> registerUser(@Body User user);

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @GET("/buckets")
    Call<List<Bucket>> fetchBuckets(@Header("x-signature") String signature,
                                    @Header("x-pubkey") String pubkey, @Query("__nonce") String nonce);

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("buckets")
    Call<Bucket> createNewBucket(@Header("x-signature") String signature,
                                 @Header("x-pubkey") String pubkey, @Body BucketModel bucketModel);
}
