package com.harryio.storj.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.harryio.storj.R;
import com.harryio.storj.StorjService;
import com.harryio.storj.StorjServiceProvider;
import com.harryio.storj.database.KeyPairDAO;
import com.harryio.storj.model.Bucket;
import com.harryio.storj.model.BucketModel;
import com.harryio.storj.ui.adapter.ImageGridAdapter;
import com.harryio.storj.util.Crypto;
import com.harryio.storj.util.ECUtils;
import com.harryio.storj.util.SharedPrefUtils;

import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import retrofit2.Call;
import retrofit2.Response;

import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStoragePublicDirectory;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 100;

    private static final String IMAGE_DIRECTORY_NAME = "Storj";

    @Bind(R.id.imageList)
    GridView imageList;
    @Bind(R.id.empty_view)
    LinearLayout emptyView;

    private File imageFolder;
    private ProgressDialog deleteProgressDialog;
    private ImageGridAdapter imageGridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check logged in status of the user
        //If user is not logged in, launch the SignUpActivity and finish this activity
        //Else continue with this activity
        boolean isLoggedIn = SharedPrefUtils.instance(this)
                .getBoolean(SharedPrefUtils.KEY_IS_USER_LOGGED_IN, false);
        if (!isLoggedIn) {
            Intent intent = SignUpActivity.getCallingIntent(this);
            startActivity(intent);
            finish();
        } else {
            setContentView(R.layout.activity_main);
            ButterKnife.bind(this);

            MainActivityPermissionsDispatcher.setUpImageFolderWithCheck(this);
            setUpGridView();
            new FetchBucketTask().execute();
        }
    }

    private void setUpGridView() {
        imageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = ImageDetailActivity.getCallingIntent(MainActivity.this,
                        imageFolder.listFiles(), position);
                startActivity(intent);
            }
        });
        imageList.setEmptyView(emptyView);
        imageList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        imageList.setMultiChoiceModeListener(new GridView.MultiChoiceModeListener() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.context_menu, menu);
                mode.setTitle("Select photos");
                mode.setSubtitle("One item selected");
                return true;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                int selectCount = imageList.getCheckedItemCount();
                switch (selectCount) {
                    case 0:
                        //Do nothing
                        break;

                    case 1:
                        mode.setSubtitle("One item selected");
                        break;

                    default:
                        mode.setSubtitle(selectCount + " photos selected");
                }
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_action_delete:
                        SparseBooleanArray checkedItemPositions = imageList.getCheckedItemPositions();
                        int size = imageList.getCheckedItemCount();
                        ArrayList<File> files = new ArrayList<>(size);

                        for (int i = 0; i < checkedItemPositions.size(); ++i) {
                            int key = checkedItemPositions.keyAt(i);
                            if (checkedItemPositions.get(key)) {
                                File image = (File) imageList.getItemAtPosition(key);
                                if (image != null) {
                                    files.add(image);
                                }
                            }
                        }

                        if (!files.isEmpty()) {
                            deleteFiles(files, mode);
                        }

                        break;

                    case R.id.menu_action_upload:
                        //todo upload items here
                        mode.finish();
                        break;

                    case R.id.menu_action_select_all:
                        int count = imageList.getAdapter().getCount();
                        for (int i = 0; i < count; i++) {
                            imageList.setItemChecked(i, true);
                        }
                        break;
                }

                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });

        File imageFolder = getImageFolder();

        if (imageFolder != null) {
            imageGridAdapter = new ImageGridAdapter(this, imageFolder);
            imageList.setAdapter(imageGridAdapter);
        }
    }

    /*
    Show files deletion progress in a dialog
     */
    private void deleteFiles(final ArrayList<File> toBeDeletedFiles, final ActionMode mode) {
        String message = toBeDeletedFiles.size() == 1 ? "Are you sure you want to " +
                "delete this item?" : "Are you sure you want to delete selected items?";

        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteProgressDialog = new ProgressDialog(MainActivity.this);
                        deleteProgressDialog.setTitle("Deleting Files");
                        deleteProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        deleteProgressDialog.setCancelable(false);
                        deleteProgressDialog.setIndeterminate(false);
                        deleteProgressDialog.setMax(toBeDeletedFiles.size());
                        deleteProgressDialog.setProgress(0);
                        deleteProgressDialog.show();

                        final int size = toBeDeletedFiles.size();
                        for (int i = 0; i < size; i++) {
                            File file = toBeDeletedFiles.get(i);
                            boolean isDeleted = file.delete();

                            if (!isDeleted) {
                                showMessage("Delete Failed: " + file.getName());
                            }

                            deleteProgressDialog.setProgress(i + 1);
                        }

                        deleteProgressDialog.dismiss();
                        mode.finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    /**
     * Show a short toast message
     * @param message message to be displayed in toast
     */
    private void showMessage(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void setUpImageFolder() {
        imageFolder = getImageFolder();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_CAPTURE_IMAGE:
                    //todo upload camera image here
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void showRationaleMessageDialog(String message,
                                            final PermissionRequest permissionRequest) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        permissionRequest.proceed();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        permissionRequest.cancel();
                    }
                })
                .create()
                .show();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.take_picture_button:
                captureImage();
                break;
        }
    }

    void captureImage() {
        Uri fileUri = getOutputMediaFileUri();

        if (fileUri != null) {
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(captureIntent, REQUEST_CODE_CAPTURE_IMAGE);
        }
    }

    private Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    private File getOutputMediaFile() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());

        return imageFolder == null ? null : new File(imageFolder.getPath() +
                File.separator + "IMG_" + timestamp + ".jpg");
    }

    private File getImageFolder() {
        File mediaStorageDir = new File(getExternalStoragePublicDirectory(DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs())
                return null;
        }

        return mediaStorageDir;
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationaleForWriteExternalStorage(PermissionRequest permissionRequest) {
        showRationaleMessageDialog("You need to provide access to external storage",
                permissionRequest);
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showDeniedPermissionMessage() {
        Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (imageGridAdapter != null) {
            imageGridAdapter.notifyDataSetChanged();

        }
    }

    private class FetchBucketTask extends AsyncTask<Void, Void, List<Bucket>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Bucket> doInBackground(Void... params) {
            KeyPairDAO keyPairDAO = KeyPairDAO.getInstance(MainActivity.this);
            PublicKey publicKey = keyPairDAO.getPublicKey();
            PrivateKey privateKey = keyPairDAO.getPrivateKey();

            try {
                String nonce = String.valueOf(System.currentTimeMillis() / 1000L);
                String hexEncodedPublicKeyString = ECUtils.getHexEncodedPublicKey(publicKey);
                String toBeSignedString = "GET\n/buckets\n" + "__nonce=" + nonce;

                byte[] signatureBytes = ECUtils.sign(privateKey, toBeSignedString);
                String hexEncodedSignature = Hex.toHexString(signatureBytes);


                StorjService storjService = StorjServiceProvider.getInstance();
                Call<List<Bucket>> call = storjService
                        .fetchBuckets(hexEncodedSignature, hexEncodedPublicKeyString, nonce);
                Response<List<Bucket>> response = call.execute();

                if (response.isSuccessful()) {
                    Log.i(TAG, "Fetch Buckets call successful");
                    Log.i(TAG, "List size: " + response.body().size());
                } else {
                    Log.e(TAG, "Fetch Buckets call failed");
                }
            } catch (IOException | InvalidKeyException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Bucket> buckets) {
            super.onPostExecute(buckets);
        }
    }

    private class CreateBucketTask extends AsyncTask<Void, Void, Bucket> {
        private final String bucketName;
        private final int storage;
        private final int transfer;

        public CreateBucketTask(String bucketName, int storage, int transfer) {
            this.bucketName = bucketName;
            this.storage = storage;
            this.transfer = transfer;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bucket doInBackground(Void... params) {
            KeyPairDAO keyPairDAO = KeyPairDAO.getInstance(MainActivity.this);
            //Fetch public and private key from the database
            PublicKey publicKey = keyPairDAO.getPublicKey();
            PrivateKey privateKey = keyPairDAO.getPrivateKey();

            try {
                //Convert public key fetched from the database to hex encoded string
                // representation of the public key
                String hexEncodedPublicKeyString = ECUtils.getHexEncodedPublicKey(publicKey);
                //Create new bucket object to be sent with the api call
                BucketModel bucketModel = new BucketModel(storage, transfer,
                        Collections.singletonList(hexEncodedPublicKeyString), bucketName);
                //Create json representation of the bucket model
                Gson gson = new Gson();
                String bucketModelJson = gson.toJson(bucketModel);
                //Construct a string according to instructions provided at
                // https://github.com/Storj/bridge/blob/master/doc/auth.md#ecdsa-signatures
                String toBeSignedString =  "POST\n/buckets\n" + bucketModelJson;
                Log.d(TAG, "ToBeSignedString: " + toBeSignedString);
//                byte[] digest = Crypto.sha256Digest(toBeSignedString);
//                byte[] signatureBytes = ECUtils.sign(privateKey, digest);
                byte[] signatureBytes = Crypto.signString("SHA256withECDSA", "SC", privateKey, toBeSignedString);

                //Hex encode the signature
                String hexEncodedSignature = Hex.toHexString(signatureBytes);

                //Make api call to create a new bucket
                StorjService storjService = StorjServiceProvider.getInstance();
                Call<Bucket> call = storjService.createNewBucket(hexEncodedSignature,
                        hexEncodedPublicKeyString, bucketModel);
                Response<Bucket> response = call.execute();

                if (response.isSuccessful()) {
                    return response.body();
                } else {
                    Log.e(TAG, "Create new bucket call failed");
                }
            } catch (IOException | NullPointerException | InvalidKeyException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bucket bucket) {
            super.onPostExecute(bucket);
            if (bucket != null) {
                Log.i(TAG, "Buckets Size " + bucket.toString());
            }
        }
    }
}