package com.harryio.storj.ui.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import com.harryio.storj.database.KeyPairDAO;
import com.harryio.storj.model.Frame;
import com.harryio.storj.model.FrameModel;
import com.harryio.storj.util.ECUtils;
import com.harryio.storj.util.FileUtils;
import com.harryio.storj.util.network.ApiExecutor;

import java.io.File;
import java.security.PrivateKey;
import java.util.Arrays;

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

    private void uploadFile(File file) {
        new UploadFile(file).execute();
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

        public void uploadFile(Uri uri) {
            service.uploadFile(FileUtils.getFileFromUri(uri));
        }

        public void fetchFrame() {
            service.fetchFrame();
        }
    }

    private class UploadFile extends AsyncTask<Void, Void, Void> {
        public File file;

        public UploadFile(File file) {
            this.file = file;
        }

        @Override
        protected Void doInBackground(Void... params) {
            KeyPairDAO keyPairDAO = KeyPairDAO.getInstance(UploadService.this);
            PrivateKey privateKey = keyPairDAO.getPrivateKey();

            byte[] paddedFileArray = FileUtils.getPaddedByteArray(file);
            byte[] encryptedFileArray = ECUtils.sign(privateKey, paddedFileArray);

            int length = encryptedFileArray.length;
            byte[] array1 = Arrays.copyOfRange(paddedFileArray, 0, (length / 2) - 1);
            byte[] array2 = Arrays.copyOfRange(paddedFileArray, length / 2, length - 1);


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
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
