package com.harryio.storj.util.network;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.harryio.storj.StorjService;
import com.harryio.storj.StorjServiceProvider;
import com.harryio.storj.model.Bucket;
import com.harryio.storj.model.BucketModel;
import com.harryio.storj.model.Frame;
import com.harryio.storj.model.FrameModel;
import com.harryio.storj.model.Shard;
import com.harryio.storj.model.ShardModel;
import com.harryio.storj.model.StorjFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class ApiExecutor {
    private static final String TAG = ApiExecutor.class.getSimpleName();
    private static final String METHOD_POST = "POST";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_PUT = "PUT";

    private static ApiExecutor apiExecutor;
    private HeaderGenerator headerGenerator;
    private StorjService storjService;
    private Gson gson;

    private ApiExecutor(Context context) {
        headerGenerator = HeaderGenerator.getInstance(context);
        storjService = StorjServiceProvider.getInstance();
        gson = new Gson();
    }

    public static ApiExecutor getInstance(Context context) {
        if (apiExecutor == null) {
            apiExecutor = new ApiExecutor(context);
        }
        return apiExecutor;
    }

    public Bucket createBucket(int storage, int transfer, String bucketName) {
        try {
            BucketModel bucketModel = new BucketModel(storage, transfer,
                    Collections.singletonList(headerGenerator.getHexEncodedPublicKey()), bucketName);
            String bucketModelJson = gson.toJson(bucketModel);
            String signature = headerGenerator.getHexEncodedSignature(METHOD_POST, "/buckets", bucketModelJson);
            String publicKey = headerGenerator.getHexEncodedPublicKey();

            Call<Bucket> call = storjService.createNewBucket(signature, publicKey, bucketModel);
            Response<Bucket> response = call.execute();

            if (response.isSuccessful()) {
                Log.i(TAG, "createBucket: call successful");
                return response.body();
            } else {
                Log.e(TAG, "createBucket: call failed");
            }
        } catch (IOException | InvalidKeyException | NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG, "createBucket: call failed", e);
        }

        return null;
    }

    public List<Bucket> fetchBuckets() {
        try {
            String nonce = String.valueOf(System.currentTimeMillis());
            String signature = headerGenerator.getHexEncodedSignature(METHOD_GET,
                    "/buckets", "__nonce=" + nonce);
            String publicKey = headerGenerator.getHexEncodedPublicKey();

            Call<List<Bucket>> call = storjService.fetchBuckets(signature, publicKey, nonce);
            Response<List<Bucket>> response = call.execute();

            if (response.isSuccessful()) {
                Log.i(TAG, "fetchBuckets: call successful");
                return response.body();
            } else {
                Log.e(TAG, "fetchBuckets: call failed");
            }
        } catch (IOException | InvalidKeyException e) {
            e.printStackTrace();
            Log.e(TAG, "fetchBuckets: call failed", e);
        }

        return null;
    }

    public Frame createFrame(FrameModel frameModel) {
        try {
            String frameModelJson = gson.toJson(frameModel);
            String signature = headerGenerator.getHexEncodedSignature(METHOD_POST,
                    "/frames", frameModelJson);
            String publickey = headerGenerator.getHexEncodedPublicKey();

            Call<Frame> call = storjService.createNewFrame(signature, publickey, frameModel);
            Response<Frame> response = call.execute();

            if (response.isSuccessful()) {
                Log.i(TAG, "createFrame: call successful");
                return response.body();
            } else {
                Log.e(TAG, "createFrame: call failed");
            }
        } catch (InvalidKeyException | IOException | NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG, "createFrame: call failed", e);
        }

        return null;
    }

    public Shard createShard(ShardModel shardModel, String frameId) {
        try {
            String shardModelJson = gson.toJson(shardModel);
            String signature = headerGenerator.getHexEncodedSignature(METHOD_PUT,
                    "/frames/" + frameId, shardModelJson);
            String publickey = headerGenerator.getHexEncodedPublicKey();

            Call<Shard> call = storjService.createNewShard(signature, publickey, frameId, shardModel);
            Response<Shard> response = call.execute();

            if (response.isSuccessful()) {
                Log.i(TAG, "createShard: call successful");
                return response.body();
            } else {
                Log.e(TAG, "createShard: call failed");
            }
        } catch (InvalidKeyException | IOException e) {
            e.printStackTrace();
            Log.e(TAG, "createShard: call failed", e);
        }

        return null;
    }

    public List<StorjFile> fetchFiles(String bucketId) {
        try {
            String nonce = String.valueOf(System.currentTimeMillis());
            String signature = headerGenerator.getHexEncodedSignature(METHOD_GET,
                    "/buckets/" + bucketId + "/files", "__nonce==" + nonce);
            String publickey = headerGenerator.getHexEncodedPublicKey();

            Call<List<StorjFile>> call = storjService.fetchFiles(signature, publickey, bucketId, nonce);
            Response<List<StorjFile>> response = call.execute();

            if (response.isSuccessful()) {
                Log.i(TAG, "fetchFiles: call successful");
                return response.body();
            } else {
                Log.e(TAG, "fetchFiles: call failed");
            }
        } catch (InvalidKeyException | IOException e) {
            e.printStackTrace();
            Log.e(TAG, "fetchFiles: call failed", e);
        }

        return null;
    }
}
