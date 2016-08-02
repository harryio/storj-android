package com.harryio.storj.ui.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.harryio.storj.model.AuthorizationModel;
import com.harryio.storj.model.BucketEntry;
import com.harryio.storj.model.BucketEntryModel;
import com.harryio.storj.model.Frame;
import com.harryio.storj.model.FrameModel;
import com.harryio.storj.model.Shard;
import com.harryio.storj.model.ShardModel;
import com.harryio.storj.util.EncryptUtils;
import com.harryio.storj.util.FileUtils;
import com.harryio.storj.util.PrefUtils;
import com.harryio.storj.util.ShardUtils;
import com.harryio.storj.util.network.ApiExecutor;
import com.harryio.storj.util.network.StorjWebSocketAdapter;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.crypto.NoSuchPaddingException;

import static com.harryio.storj.util.PrefUtils.KEY_PASSWORD;
import static com.harryio.storj.util.PrefUtils.KEY_USERNAME;

public class UploadService extends Service {
    private static final String TAG = UploadService.class.getSimpleName();

    private static boolean running = false;
    private ApiExecutor apiExecutor;

    public static boolean isRunning() {
        return running;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        apiExecutor = ApiExecutor.getInstance(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new UploadBinder(this);
    }

    private void uploadFile(File file) {
        new CreateFrame(file).execute();
    }

    public class UploadBinder extends Binder {
        private final UploadService service;

        private UploadBinder(UploadService service) {
            this.service = service;
        }

        public void startUploadService() {
            startService(new Intent(UploadService.this, UploadService.class));
            running = true;
        }

        public void upload(Uri uri) {
            service.uploadFile(FileUtils.getFileFromUri(uri));
        }
    }

    private class UploadFile extends AsyncTask<Void, Void, Void> {
        private File file;
        private String frameId;

        public UploadFile(File file, String frameId) {
            this.file = file;
            this.frameId = frameId;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                File encryptedFile = File.createTempFile(file.getName(), null);
                //todo provide UI for setting encryption password
                EncryptUtils.encrypt(file, encryptedFile, "Password");

                List<ShardModel> shards = ShardUtils.getShards(encryptedFile);
                for (int i = 0, length = shards.size(); i < length; ++i) {
                    final ShardModel shardModel = shards.get(i);
                    final PrefUtils prefUtils = PrefUtils.instance(UploadService.this);
                    String username = prefUtils.getString(KEY_USERNAME, "");
                    String password = prefUtils.getString(KEY_PASSWORD, "");

                    Shard shard = apiExecutor.createShard(shardModel, frameId, username, password);

                    if (shard != null) {
                        try {
                            AuthorizationModel authModel = new AuthorizationModel(
                                    shard.getHash(), shard.getOperation(), shard.getToken());
                            Gson gson = new Gson();
                            final String authJson = gson.toJson(authModel, AuthorizationModel.class);
                            Log.i(TAG, "Json String: " + authJson);

                            final Shard.Farmer farmer = shard.getFarmer();
                            String port = farmer.getPort();
                            String hostname = farmer.getAddress();
                            String farmerAddress = "ws://" + hostname + ":" + port;

                            WebSocket webSocket = new WebSocketFactory()
                                    .createSocket(farmerAddress);
                            final CountDownLatch latch = new CountDownLatch(1);
                            webSocket.addListener(new StorjWebSocketAdapter(shardModel.getShardPath(),
                                    authJson, latch));
                            webSocket.connectAsynchronously();
                            latch.await();
                        } catch (InterruptedException  e) {
                            e.printStackTrace();
                        }
                    }

                    File shardFile = new File(shardModel.getShardPath());
                    shardFile.delete();
                }

                encryptedFile.delete();

                String mimetype = FileUtils.getMimeType(file);
                String filename = file.getName();
                BucketEntryModel bucketEntryModel =
                        new BucketEntryModel(frameId, mimetype, filename);

                String bucketId = PrefUtils.instance(UploadService.this)
                        .getString(PrefUtils.KEY_DEFAULT_BUCKET_ID, null);
                if (bucketId != null) {
                    BucketEntry bucketEntry = apiExecutor.storeFileInBucket(bucketEntryModel, bucketId);
                    if (bucketEntry != null) {
                        Log.i(TAG, bucketEntry.toString());
                    }
                } else {
                    Log.e(TAG, "Bucket Id is null");
                }
            } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException |
                    InvalidKeyException | NullPointerException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private class CreateFrame extends AsyncTask<Void, Void, Frame> {
        private File file;

        public CreateFrame(File file) {
            this.file = file;
        }

        @Override
        protected Frame doInBackground(Void... params) {
            FrameModel frameModel = new FrameModel();
            return apiExecutor.createFrame(frameModel);
        }

        @Override
        protected void onPostExecute(Frame frame) {
            if (frame != null) {
                new UploadFile(file, frame.getId()).execute();
            } else {
                stopSelf();
            }
        }
    }
}
