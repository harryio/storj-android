package com.harryio.storj;

import com.harryio.storj.model.Bucket;
import com.harryio.storj.model.BucketEntry;
import com.harryio.storj.model.BucketEntryModel;
import com.harryio.storj.model.CreateBucketModel;
import com.harryio.storj.model.Frame;
import com.harryio.storj.model.FrameModel;
import com.harryio.storj.model.Shard;
import com.harryio.storj.model.ShardModel;
import com.harryio.storj.model.StorjFile;
import com.harryio.storj.model.User;
import com.harryio.storj.model.UserStatus;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface StorjService {
    @POST("users")
    Call<UserStatus> registerUser(@Body User user);

    @GET("buckets")
    Call<List<Bucket>> fetchBuckets(@Header("x-signature") String signature,
                                    @Header("x-pubkey") String pubkey, @Query("__nonce") String nonce);

    @POST("buckets")
    Call<Bucket> createNewBucket(@Header("x-signature") String signature,
                                 @Header("x-pubkey") String pubkey, @Body CreateBucketModel createBucketModel);

    @POST("frames")
    Call<Frame> createNewFrame(@Header("x-signature") String signature,
                               @Header("x-pubkey") String pubkey, @Body FrameModel frameModel);

    @PUT("frames/{frame_id}")
    Call<Shard> createNewShard(@Header("x-signature") String signature,
                               @Header("x-pubkey") String pubkey, @Path("frame_id") String frameId,
                               @Body ShardModel shardModel);

    @PUT("frames/{frame_id}")
    Call<Shard> createNewShard(@Header("Authorization") String auth, @Path("frame_id") String frameId,
                               @Body ShardModel shardModel);

    @GET("buckets/{id}/files")
    Call<List<StorjFile>> fetchFiles(@Header("x-signature") String signature,
                                     @Header("x-pubkey") String pubkey, @Path("id") String id,
                                     @Query("__nonce") String nonce);

    @POST("buckets/{id}/files")
    Call<BucketEntry> storeFile(@Header("x-signature") String signature,
                                @Header("x-pubkey") String pubkey, @Path("id") String id,
                                @Body BucketEntryModel bucketEntryModel);

    @POST("buckets/{id}/files")
    Call<BucketEntry> storeFile(@Header("Authorization") String auth, @Path("id") String id,
                                @Body BucketEntryModel bucketEntryModel);

    @DELETE("buckets/{id}")
    Call<Void> deleteBucket(@Header("Authorization") String authorization,
                            @Path("id") String id);
}
