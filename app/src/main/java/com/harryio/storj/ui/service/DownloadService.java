package com.harryio.storj.ui.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.harryio.storj.model.FilePointer;
import com.harryio.storj.model.Operation;
import com.harryio.storj.model.Token;
import com.harryio.storj.model.TokenModel;
import com.harryio.storj.notification.DownloadNotification;
import com.harryio.storj.util.network.ApiExecutor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.harryio.storj.notification.DownloadNotification.DOWNLOAD_NOTIFICATION_ID;

public class DownloadService extends Service {
    private static final String TAG = "DownloadService";

    private static final String KEY_BUCKET_ID = "BUCKET_ID";
    private static final String KEY_FILE_ID = "FILE_ID";
    private static final String ARG_BUCKET_ID = "com.harryio.BUCKET_ID";
    private static final String ARG_FILE_ID = "com.harryio.FILE_ID";

    private DownloadHandler downloadHandler;
    private ApiExecutor apiExecutor;
    private AtomicInteger numOfFiles = new AtomicInteger();
    private int filesDownloaded;
    private DownloadNotification notification;

    public static Intent getCallingIntent(Context context, String bucketId, String fileId) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(KEY_BUCKET_ID, bucketId);
        intent.putExtra(KEY_FILE_ID, fileId);

        return intent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread handlerThread = new HandlerThread("DownloadThread",
                Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();

        Looper looper = handlerThread.getLooper();
        downloadHandler = new DownloadHandler(looper);
        apiExecutor = ApiExecutor.getInstance(this);
        notification = new DownloadNotification(this);

        startForeground(DOWNLOAD_NOTIFICATION_ID, notification.getNotification());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Token getDownloadToken(String bucketId) {
        TokenModel tokenModel = new TokenModel(Operation.PULL);
        return apiExecutor.createToken(tokenModel, bucketId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        numOfFiles.set(numOfFiles.getAndIncrement());
        notification.update(numOfFiles.get()).show();

        String bucketId = intent.getStringExtra(KEY_BUCKET_ID);
        String fileId = intent.getStringExtra(KEY_FILE_ID);

        Message message = downloadHandler.obtainMessage();
        Bundle bundle = new Bundle(2);
        bundle.putString(ARG_BUCKET_ID, bucketId);
        bundle.putString(ARG_FILE_ID, fileId);
        message.setData(bundle);
        downloadHandler.sendMessage(message);

        return START_REDELIVER_INTENT;
    }

    private final class DownloadHandler extends Handler {
        public DownloadHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String bucketId = bundle.getString(ARG_BUCKET_ID);
            String fileId = bundle.getString(ARG_FILE_ID);

            Token token = getDownloadToken(bucketId);
            if (token != null) {
                List<FilePointer> filePointers = apiExecutor
                        .fetchFilePointers(bucketId, fileId, token.getToken());
                if (filePointers != null) {
                    Log.i(TAG, "Size of file Pointers: " + filePointers.size());
                    for (int i = 0, size = filePointers.size(); i < size; i++) {
                        FilePointer filePointer = filePointers.get(i);
                        Log.i(TAG, "File Pointer: " + filePointer.toString());
                    }
                }
            }

            numOfFiles.set(numOfFiles.getAndDecrement());
            if (numOfFiles.get() == 0) {
                stopForeground(true);
                stopSelf();
            } else {
                notification.update(numOfFiles.get()).show();
            }
        }
    }
}
