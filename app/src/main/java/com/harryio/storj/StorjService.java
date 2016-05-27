package com.harryio.storj;

import com.harryio.storj.model.Bucket;
import com.harryio.storj.model.BucketModel;
import com.harryio.storj.model.Frame;
import com.harryio.storj.model.FrameModel;
import com.harryio.storj.model.User;
import com.harryio.storj.model.UserStatus;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface StorjService {
    @POST("users")
    Call<UserStatus> registerUser(@Body User user);

    @GET("buckets")
    Call<List<Bucket>> fetchBuckets(@Header("x-signature") String signature,
                                    @Header("x-pubkey") String pubkey, @Query("__nonce") String nonce);

    @POST("buckets")
    Call<Bucket> createNewBucket(@Header("x-signature") String signature,
                                 @Header("x-pubkey") String pubkey, @Body BucketModel bucketModel);

    @POST("frames")
    Call<Frame> createNewFrame(@Header("x-signature") String signature,
                               @Header("x-pubkey") String pubkey, @Body FrameModel frameModel);
}
