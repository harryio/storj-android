package com.harryio.storj.ui.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;

import com.harryio.storj.model.BucketEntry;
import com.harryio.storj.model.BucketEntryModel;
import com.harryio.storj.model.Frame;
import com.harryio.storj.model.FrameModel;
import com.harryio.storj.model.Shard;
import com.harryio.storj.model.ShardModel;
import com.harryio.storj.notification.ErrorNotification;
import com.harryio.storj.notification.UploadNotification;
import com.harryio.storj.notification.UploadSuccessfulNotification;
import com.harryio.storj.util.EncryptUtils;
import com.harryio.storj.util.FileUtils;
import com.harryio.storj.util.PrefUtils;
import com.harryio.storj.util.UploadUtils;
import com.harryio.storj.util.network.ApiExecutor;
import com.harryio.storj.util.network.StorjWebSocketAdapter;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.NoSuchPaddingException;

import static com.harryio.storj.notification.UploadNotification.UPLOAD_NOTIFICATION_ID;
import static com.harryio.storj.util.PrefUtils.KEY_DEFAULT_BUCKET_ID;
import static com.harryio.storj.util.PrefUtils.KEY_PASSWORD;
import static com.harryio.storj.util.PrefUtils.KEY_USERNAME;

public class UploadService extends Service {
    private static final String TAG = "UploadService2";
    private static final String KEY_FILE_URI = "FILE_URI";
    private UploadHandler uploadHandler;
    private UploadNotification notification;
    private ApiExecutor apiExecutor;
    private PrefUtils prefUtils;
    private AtomicInteger numOfFiles = new AtomicInteger();
    private int filesUploaded;
    public UploadService() {
        super();
    }

    public static Intent getCallingIntent(Context context, Uri fileUri) {
        Intent intent = new Intent(context, UploadService.class);
        intent.putExtra(KEY_FILE_URI, fileUri);
        return intent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("UploadThread",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        Looper looper = thread.getLooper();
        uploadHandler = new UploadHandler(looper);
        notification = new UploadNotification(this);
        apiExecutor = ApiExecutor.getInstance(this);
        prefUtils = PrefUtils.instance(this);
        startForeground(UPLOAD_NOTIFICATION_ID, notification.getNotification());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        numOfFiles.set(numOfFiles.incrementAndGet());
        notification.update(numOfFiles.get()).show();

        Uri uri = intent.getParcelableExtra(KEY_FILE_URI);
        Message message = uploadHandler.obtainMessage();
        message.obj = uri;
        uploadHandler.sendMessage(message);

        return START_REDELIVER_INTENT;
    }

    private Frame createFrame() {
        FrameModel frameModel = new FrameModel();
        return apiExecutor.createFrame(frameModel);
    }

    private boolean uploadFile(String frameId, File file) {
        try {
            File encryptedFile = File.createTempFile(file.getName(), null);
            EncryptUtils.encrypt(file, encryptedFile, "StrongPassword");

            List<ShardModel> shards = UploadUtils.getShards(encryptedFile);
            for (int i = 0, size = shards.size(); i < size; ++i) {
                ShardModel shardModel = shards.get(i);
                String username = prefUtils.getString(KEY_USERNAME, "");
                String password = prefUtils.getString(KEY_PASSWORD, "");
                Shard shard = apiExecutor.createShard(shardModel, frameId, username, password);

                if (shard != null) {
                    String authJson = UploadUtils.getAuthJson(shard.getHash(),
                            shard.getOperation(), shard.getToken());
                    String farmerAddress = UploadUtils.getFarmerAddress(shard.getFarmer());

                    WebSocket webSocket = new WebSocketFactory()
                            .createSocket(farmerAddress);
                    CountDownLatch latch = new CountDownLatch(1);
                    webSocket.addListener(new StorjWebSocketAdapter(shardModel.getShardPath(),
                            authJson, latch));
                    webSocket.connectAsynchronously();
                    latch.await();
                } else {
                    return false;
                }
            }
        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | InterruptedException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private BucketEntry createBucketEntry(File file, String frameId) {
        BucketEntryModel bucketEntryModel = UploadUtils.getBucketEntryModel(file, frameId);
        String bucketId = prefUtils.getString(KEY_DEFAULT_BUCKET_ID, null);
        String username = prefUtils.getString(KEY_USERNAME, "");
        String password = prefUtils.getString(KEY_PASSWORD, "");

        if (bucketId != null) {
            return apiExecutor.storeFileInBucket(bucketEntryModel, bucketId, username, password);
        }

        return null;
    }

    private void showUploadErrorNotification() {
        new ErrorNotification(this, UUID.randomUUID().hashCode())
                .setError("Error while uploading file").show();
    }

    private final class UploadHandler extends Handler {
        public UploadHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Frame frame = createFrame();
            File file = FileUtils.getFileFromUri((Uri) msg.obj);

            if (frame != null) {
                boolean isUploadSuccessful = uploadFile(frame.getId(), file);
                if (isUploadSuccessful) {
                    BucketEntry bucketEntry = createBucketEntry(file, frame.getId());
                    if (bucketEntry != null) {
                        filesUploaded++;
                        new UploadSuccessfulNotification(UploadService.this)
                                .update(filesUploaded).show();
                    } else {
                        showUploadErrorNotification();
                    }
                } else {
                    showUploadErrorNotification();
                }
            } else {
                showUploadErrorNotification();
            }

            numOfFiles.set(numOfFiles.decrementAndGet());
            if (numOfFiles.get() == 0) {
                stopForeground(true);
                stopSelf();
            } else {
                notification.update(numOfFiles.get()).show();
            }
        }
    }
}
