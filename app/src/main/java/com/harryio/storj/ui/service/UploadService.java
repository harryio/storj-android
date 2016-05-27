package com.harryio.storj.ui.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import com.harryio.storj.model.Frame;
import com.harryio.storj.model.FrameModel;
import com.harryio.storj.util.network.ApiExecutor;

import java.io.File;

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

    public void fetchFrame() {
        new CreateFrame().execute();
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

        public void fetchFrame() {
            service.fetchFrame();
        }
    }

    private class CreateFrame extends AsyncTask<Void, Void, Frame> {
        private File file;
        private String bucketId;


        public CreateFrame() {
        }

        @Override
        protected Frame doInBackground(Void... params) {
            FrameModel frameModel = new FrameModel();
            return apiExecutor.createFrame(frameModel);
        }

        @Override
        protected void onPostExecute(Frame frame) {
            if (frame != null) {
                //todo create shard here
            } else {
                stopSelf();
            }
        }
    }
}
