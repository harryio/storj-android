package com.harryio.storj.util.network;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.harryio.storj.StorjService;
import com.harryio.storj.StorjServiceProvider;
import com.harryio.storj.database.KeyPairDAO;
import com.harryio.storj.model.Bucket;
import com.harryio.storj.model.BucketEntry;
import com.harryio.storj.model.BucketEntryModel;
import com.harryio.storj.model.CreateBucketModel;
import com.harryio.storj.model.FilePointer;
import com.harryio.storj.model.Frame;
import com.harryio.storj.model.FrameModel;
import com.harryio.storj.model.Shard;
import com.harryio.storj.model.ShardModel;
import com.harryio.storj.model.StorjFile;
import com.harryio.storj.model.Token;
import com.harryio.storj.model.TokenModel;
import com.harryio.storj.model.User;
import com.harryio.storj.model.UserStatus;
import com.harryio.storj.util.Crypto;
import com.harryio.storj.util.ECUtils;

import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class ApiExecutor {
    private static final String TAG = ApiExecutor.class.getSimpleName();
    private static final String METHOD_POST = "POST";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_DELETE = "DELETE";

    private static ApiExecutor apiExecutor;
    private KeyPairDAO keyPairDAO;
    private HeaderGenerator headerGenerator;
    private StorjService storjService;
    private Gson gson;

    private ApiExecutor(Context context) {
        keyPairDAO = KeyPairDAO.getInstance(context);
        headerGenerator = HeaderGenerator.getInstance(keyPairDAO);
        storjService = StorjServiceProvider.getInstance();
        gson = new Gson();
    }

    public static ApiExecutor getInstance(Context context) {
        if (apiExecutor == null) {
            apiExecutor = new ApiExecutor(context);
        }
        return apiExecutor;
    }

    public UserStatus registerUser(String email, String password) {
        try {
            //SHA-256 digest of password
            byte[] bytes = Crypto.sha256Digest(password);
            //Hex of SHA-256 digest of password
            password = Hex.toHexString(bytes);
            KeyPair keyPair = ECUtils.getKeyPair();
            String hexEncodedPublicString = ECUtils.getHexEncodedPublicKey(keyPair.getPublic());
            //Create new user
            User user = new User(email, password, hexEncodedPublicString);

            StorjService storjService = StorjServiceProvider.getInstance();
            Call<UserStatus> signUpResultCall = storjService.registerUser(user);

            //Make an api call to register user
            Response<UserStatus> response = signUpResultCall.execute();
            if (response.isSuccessful()) {
                //Api call was successful
                //Save public and private key to database
                keyPairDAO.insert(keyPair);
                return response.body();
            } else {
                //Api call failed
                //Print out the error response body
                final ResponseBody responseBody = response.errorBody();
                Log.e(TAG, "SignUp request failed:\n" + responseBody.string());
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.e(TAG, "No algorithm found for \"SHA-256\"");
        } catch (InvalidAlgorithmParameterException | NoSuchProviderException | IOException | NullPointerException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bucket createBucket(int storage, int transfer, String bucketName) {
        try {
            CreateBucketModel createBucketModel = new CreateBucketModel(storage, transfer,
                    Collections.singletonList(headerGenerator.getPublicKeyHeader()), bucketName);
            String bucketModelJson = gson.toJson(createBucketModel);
            String signature = headerGenerator.getSignatureHeader(METHOD_POST, "/buckets", bucketModelJson);
            String publicKey = headerGenerator.getPublicKeyHeader();

            Call<Bucket> call = storjService.createNewBucket(signature, publicKey, createBucketModel);
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
            String signature = headerGenerator.getSignatureHeader(METHOD_GET,
                    "/buckets", "__nonce=" + nonce);
            String publicKey = headerGenerator.getPublicKeyHeader();

            Call<List<Bucket>> call = storjService.fetchBuckets(signature, publicKey, nonce);
            Response<List<Bucket>> response = call.execute();

            if (response.isSuccessful()) {
                Log.i(TAG, "fetchBuckets: call successful");
                return response.body();
            } else {
                Log.e(TAG, "fetchBuckets: call failed");
            }
        } catch (IOException | InvalidKeyException | NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG, "fetchBuckets: call failed", e);
        }

        return null;
    }

    public List<StorjFile> fetchFiles(String bucketId) {
        try {
            String nonce = String.valueOf(System.currentTimeMillis());
            String signature = headerGenerator.getSignatureHeader(METHOD_GET,
                    "/buckets/" + bucketId + "/files", "__nonce=" + nonce);
            String publickey = headerGenerator.getPublicKeyHeader();

            Call<List<StorjFile>> call = storjService.fetchFiles(signature, publickey, bucketId, nonce);
            Response<List<StorjFile>> response = call.execute();

            if (response.isSuccessful()) {
                Log.i(TAG, "fetchFiles: call successful");
                return response.body();
            } else {
                Log.e(TAG, "fetchFiles: call failed");
            }
        } catch (InvalidKeyException | IOException | NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG, "fetchFiles: call failed", e);
        }

        return null;
    }

    public List<FilePointer> fetchFilePointers(String bucketId, String fileId, String token) {
        try {
            String skip = String.valueOf(0);
            String limit = String.valueOf(10);
            String nonce = UUID.randomUUID().toString();
            String queryString = "skip=" + skip + "&limit=" + limit + "&__nonce=" + nonce;

            String signature = headerGenerator.getSignatureHeader(METHOD_GET, "/buckets/" +
                    bucketId + "/files/" + fileId, queryString);
            String publickey = headerGenerator.getPublicKeyHeader();

            Call<List<FilePointer>> call = storjService.fetchFilePointers(signature, publickey, token,
                    bucketId, fileId, skip, limit, nonce);
            Response<List<FilePointer>> response = call.execute();

            if (response.isSuccessful()) {
                Log.i(TAG, "fetchFilePointers: call successful");
                return response.body();
            }
        } catch (InvalidKeyException | IOException e) {
            e.printStackTrace();
            Log.e(TAG, "fetchFilePointers: call failed", e);
        }

        return null;
    }

    public Frame createFrame(FrameModel frameModel) {
        try {
            String frameModelJson = gson.toJson(frameModel);
            String signature = headerGenerator.getSignatureHeader(METHOD_POST,
                    "/frames", frameModelJson);
            String publickey = headerGenerator.getPublicKeyHeader();

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
            String signature = headerGenerator.getSignatureHeader(METHOD_PUT,
                    "/frames/" + frameId, shardModelJson);
            String publickey = headerGenerator.getPublicKeyHeader();

            Call<Shard> call = storjService.createNewShard(signature, publickey, frameId, shardModel);
            Response<Shard> response = call.execute();

            if (response.isSuccessful()) {
                Log.i(TAG, "createShard: call successful");
                return response.body();
            } else {
                Log.e(TAG, "createShard: call failed");
            }
        } catch (InvalidKeyException | IOException | NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG, "createShard: call failed", e);
        }

        return null;
    }

    public Shard createShard(ShardModel shardModel, String frameId,
                             String username, String password) {
        try {
            String authHeader = HeaderGenerator.getAuthHeader(username, password);
            Log.i(TAG, "Authorization Header: " + authHeader);

            Call<Shard> call = storjService.createNewShard(authHeader, frameId, shardModel);
            Response<Shard> response = call.execute();

            if (response.isSuccessful()) {
                Log.i(TAG, "createShard: call successful");
                return response.body();
            } else {
                Log.e(TAG, "createShard: call failed");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "createShard: call failed", e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.i(TAG, "createShard: encryption failed");
        }

        return null;
    }

    public BucketEntry storeFileInBucket(BucketEntryModel bucketEntryModel, String bucketId) {
        try {
            String storeFileJson = gson.toJson(bucketEntryModel);
            String signature = headerGenerator.getSignatureHeader(METHOD_POST,
                    "/buckets/" + bucketId + "/files", storeFileJson);
            String publicKey = headerGenerator.getPublicKeyHeader();

            Call<BucketEntry> call = storjService.storeFile(signature, publicKey, bucketId, bucketEntryModel);
            Response<BucketEntry> response = call.execute();

            if (response.isSuccessful()) {
                Log.i(TAG, "storeFileInBucket: call successful");
                return response.body();
            } else {
                Log.e(TAG, "storeFileInBucket: call failed");
            }
        } catch (IOException | InvalidKeyException e) {
            e.printStackTrace();
            Log.e(TAG, "storeFileInBucket: call failed", e);
        }

        return null;
    }

    public BucketEntry storeFileInBucket(BucketEntryModel bucketEntryModel, String bucketId,
                                         String username, String password) {
        String authHeader = null;
        try {
            authHeader = HeaderGenerator.getAuthHeader(username, password);
            Call<BucketEntry> call = storjService.storeFile(authHeader, bucketId, bucketEntryModel);
            Response<BucketEntry> response = call.execute();

            if (response.isSuccessful()) {
                Log.i(TAG, "storeFileInBucket: call successful");
                return response.body();
            } else {
                Log.e(TAG, "storeFileInBucket: call failed");
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            Log.e(TAG, "storeFileInBucket: call failed", e);
        }

        return null;
    }

    public boolean deleteBucket(String bucketId, String username, String password) {
        try {
            String authHeader = HeaderGenerator.getAuthHeader(username, password);

            Call<Void> call = storjService.deleteBucket(authHeader, bucketId);
            Response<Void> response = call.execute();

            if (response.isSuccessful()) {
                Log.i(TAG, "deleteBucket: call successful");
                return true;
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Token createToken(TokenModel tokenModel, String bucketId) {
        try {
            String tokenJson = gson.toJson(tokenModel);
            String signature = headerGenerator.getSignatureHeader(METHOD_POST,
                    "buckets/" + bucketId + "/tokens", tokenJson);
            String pubkey = headerGenerator.getPublicKeyHeader();

            Call<Token> call = storjService.createToken(signature, pubkey, bucketId, tokenModel);
            Response<Token> response = call.execute();

            if (response.isSuccessful()) {
                Log.i(TAG, "createToken: call successful");
                return response.body();
            }
        } catch (IOException | InvalidKeyException e) {
            e.printStackTrace();
            Log.i(TAG, "createToken: call failed", e);
        }

        return null;
    }
}
