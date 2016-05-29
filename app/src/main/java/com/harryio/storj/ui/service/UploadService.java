package com.harryio.storj.ui.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.harryio.storj.database.KeyPairDAO;
import com.harryio.storj.model.Frame;
import com.harryio.storj.model.FrameModel;
import com.harryio.storj.util.Crypto;
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

        public void upload(Uri uri) {
            service.uploadFile(FileUtils.getFileFromUri(uri));
        }
    }

    private class UploadFile extends AsyncTask<Void, Void, Void> {
        private File file;
        private String frameId;

        public UploadFile(File file) {
            this.file = file;
        }

        public UploadFile(File file, String frameId) {
            this.file = file;
            this.frameId = frameId;
        }

        @Override
        protected Void doInBackground(Void... params) {
            KeyPairDAO keyPairDAO = KeyPairDAO.getInstance(UploadService.this);
            PrivateKey privateKey = keyPairDAO.getPrivateKey();

            byte[] originalFileBytes = FileUtils.fileToByteArray(file);
            Log.i(TAG, "Original file bytes: " + originalFileBytes.length);
            byte[] paddedFileArray = FileUtils.getPaddedByteArray(file);
            Log.i(TAG, "Padded file bytes: " + paddedFileArray.length);
            byte[] encryptedFileArray = Crypto.encrypt(paddedFileArray);
            Log.i(TAG, "Encrypted file bytes: " + encryptedFileArray.length);

            int length = encryptedFileArray.length;
            byte[] array1 = Arrays.copyOfRange(encryptedFileArray, 0, (length / 2) - 1);
            byte[] array2 = Arrays.copyOfRange(encryptedFileArray, length / 2, length - 1);
            byte[][] byteArray = new byte[][]{array1, array2};

//            for (int i = 0; i < byteArray.length; i++) {
//                byte[] filePart = byteArray[i];
//                String hash = Hex.toHexString(Crypto.sha256Digest(filePart));
//                ShardModel shardModel = new ShardModel(hash, filePart.length, i,
//                        new String[] {"2128bc38ed5140bb9ba8ddac16183eecc4c9ef63b0cd46b30f49b578737a7a52"},
//                        new String[] {"507f1f77bcf86cd799439011"});
//
//                Shard shard = apiExecutor.createShard(shardModel, frameId);
//                Log.i(TAG, "Shard: " + shard);
//                if (shard != null) {
//                    String channel = shard.getChannel();
//                    String port = channel.substring(channel.lastIndexOf(":") + 1);
//                    String hostname = channel.substring(1, channel.lastIndexOf(":"));
//
//                    JSONObject jsonObject = new JSONObject();
//                    try {
//                        jsonObject.put("token", shard.getToken());
//                        jsonObject.put("hash", shard.getHash());
//                        jsonObject.put("operation", shard.getOperation());
//                        String jsonString = jsonObject.toString();
//
//                        Socket socket = new Socket(hostname, Integer.parseInt(port));
//                        DataOutputStream dataOutputStream =
//                                new DataOutputStream(socket.getOutputStream());
//                        dataOutputStream.writeUTF(jsonString);
//                        dataOutputStream.write(filePart);
//                        dataOutputStream.close();
//
//                        BufferedReader reader = new BufferedReader(
//                                new InputStreamReader(socket.getInputStream()));
//                        StringBuilder stringBuilder = new StringBuilder();
//                        String line;
//                        while ((line = reader.readLine()) != null) {
//                            stringBuilder.append(line).append("\n");
//                        }
//
//                        Log.i(TAG, "Farmer output response:\n" + stringBuilder);
//                    } catch (JSONException | IOException | NullPointerException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }

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
