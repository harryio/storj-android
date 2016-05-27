package com.harryio.storj.util.network;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.harryio.storj.StorjService;
import com.harryio.storj.StorjServiceProvider;
import com.harryio.storj.model.Bucket;
import com.harryio.storj.model.BucketModel;

import java.io.IOException;
import java.security.InvalidKeyException;
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

    public Bucket createBucket(BucketModel bucketModel) {
        try {
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
}
