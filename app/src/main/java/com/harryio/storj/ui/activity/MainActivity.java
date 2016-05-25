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
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.harryio.storj.R;
import com.harryio.storj.StorjService;
import com.harryio.storj.StorjServiceProvider;
import com.harryio.storj.database.KeyPairDAO;
import com.harryio.storj.model.Bucket;
import com.harryio.storj.model.BucketModel;
import com.harryio.storj.ui.fragment.BucketListFragment;
import com.harryio.storj.util.ConnectionDetector;
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
public class MainActivity extends AppCompatActivity implements
        BucketListFragment.OnFragmentInteractionListener, Toolbar.OnMenuItemClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 100;

    private static final String IMAGE_DIRECTORY_NAME = "Storj";
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private File imageFolder;
    private ProgressDialog deleteProgressDialog;

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
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new BucketListFragment())
                    .commit();
            setUpToolbar();
        }
    }

    private void setUpToolbar() {
        toolbar.inflateMenu(R.menu.menu_activity_main);
        toolbar.setOnMenuItemClickListener(this);
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
     *
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
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_create_bucket:
                showCreateBucketDialog();
                return true;

            case R.id.action_capture_image:
                captureImage();
                return true;

            case R.id.action_about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
        }

        return true;
    }

    private void showCreateBucketDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.create_new_bucket_dialog, null);
        final EditText bucketNameEdittext = (EditText) view.findViewById(R.id.bucket_name_editText);
        final EditText capacityEdittext = (EditText) view.findViewById(R.id.bucket_capacity_editText);
        final EditText transferEdittext = (EditText) view.findViewById(R.id.bucket_transfer_editText);

        final AlertDialog dialog = new AlertDialog.Builder(this, R.style.CreateBucketDialogTheme)
                .setTitle("Create new bucket")
                .setView(view, 80, 20, 80, 0)
                .setPositiveButton("Create", null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                Button createBucketButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                createBucketButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String bucketName = bucketNameEdittext.getText().toString();
                        String capacity = capacityEdittext.getText().toString();
                        String transfer = transferEdittext.getText().toString();

                        if (TextUtils.isEmpty(bucketName)) {
                            showMessage("Bucket name cannot be empty");
                        } else if (TextUtils.isEmpty(capacity)) {
                            showMessage("Please enter capacity for the bucket");
                        } else if (TextUtils.isEmpty(transfer)) {
                            showMessage("Please enter transfer limit for the bucket");
                        } else {
                            dialog.dismiss();
                            if (ConnectionDetector.isConnectedToInternet(
                                    MainActivity.this.getApplicationContext())) {
                                new CreateBucketTask(bucketName, Integer.parseInt(capacity),
                                        Integer.parseInt(transfer)).execute();
                            } else {
                                showMessage("No internet connection!");
                            }
                        }
                    }
                });
            }
        });

        dialog.show();
    }

    private class CreateBucketTask extends AsyncTask<Void, Void, Bucket> {
        private final String bucketName;
        private final int storage;
        private final int transfer;
        private ProgressDialog progressDialog;

        public CreateBucketTask(String bucketName, int storage, int transfer) {
            this.bucketName = bucketName;
            this.storage = storage;
            this.transfer = transfer;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog
                    .show(MainActivity.this, "", "Creating new Bucket", true);
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
                String toBeSignedString = "POST\n/buckets\n" + bucketModelJson;
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
            progressDialog.dismiss();
            if (bucket != null) {
                android.support.v4.app.Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
                if (fragment != null) {
                    BucketListFragment bucketListFragment = (BucketListFragment) fragment;
                    bucketListFragment.addNewBucket(bucket);
                    showMessage("Bucket successfully created");
                } else {
                    showMessage("Oops! Seems like something went wrong");
                }
            } else {
                showMessage("Failed to create bucket");
            }
        }
    }
}