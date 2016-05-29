package com.harryio.storj.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.harryio.storj.R;
import com.harryio.storj.model.Bucket;
import com.harryio.storj.ui.fragment.BucketListFragment;
import com.harryio.storj.ui.service.UploadService;
import com.harryio.storj.util.ConnectionDetector;
import com.harryio.storj.util.SharedPrefUtils;
import com.harryio.storj.util.network.ApiExecutor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

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

    private ApiExecutor apiExecutor;
    private File imageFolder;
    private ProgressDialog deleteProgressDialog;
    private boolean bound = false;
    private Uri fileUri;
    private UploadService.UploadBinder service;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MainActivity.this.service = (UploadService.UploadBinder) service;
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        MainActivityPermissionsDispatcher.setUpImageFolderWithCheck(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new BucketListFragment())
                .commit();
        setUpToolbar();

        boolean isTutorialShown = SharedPrefUtils.instance(this)
                .getBoolean(SharedPrefUtils.KEY_IS_TUTORIAL_SHOWN, false);
        if (!isTutorialShown) {
            showTutorial();
        }
    }

    private void setUpToolbar() {
        toolbar.inflateMenu(R.menu.menu_activity_main);
        toolbar.setOnMenuItemClickListener(this);
    }

    private void showTutorial() {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(200);
        config.setMaskColor(ContextCompat.getColor(this, R.color.colorPrimaryDark_75));
        config.setRenderOverNavigationBar(false);
        config.setContentTextColor(ContextCompat.getColor(this, R.color.white));
        config.setDismissTextColor(ContextCompat.getColor(this, R.color.colorAccent));

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this);
        sequence.setConfig(config);
        sequence.addSequenceItem(toolbar.findViewById(R.id.action_capture_image),
                "Click this button to capture image and automatically " +
                        "upload captured image directly to cloud.", "OK");
        sequence.addSequenceItem(toolbar.findViewById(R.id.action_create_bucket),
                "Click here to create new bucket on the cloud", "GOT IT")
                .setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
                    @Override
                    public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
                        SharedPrefUtils.instance(MainActivity.this)
                                .storeBoolean(SharedPrefUtils.KEY_IS_TUTORIAL_SHOWN, true);
                    }
                });
        sequence.start();
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
                    if (!UploadService.isRunning()) {
                        service.startUploadService();
                        MainActivity.this.service.upload(fileUri);
                    }
//                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
//                    if (fragment != null) {
//                        BucketListFragment bucketListFragment = (BucketListFragment) fragment;
//                        String bucketId = bucketListFragment.getBucketId();
//                        if (!UploadService.isRunning()) {
//                            service.startUploadService();
//                        }
//                    }
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
        fileUri = getOutputMediaFileUri();

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

            case R.id.action_show_tutorial:
                showTutorial();
                return true;

            case R.id.action_about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
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

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, UploadService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }
        super.onStop();
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
            return apiExecutor.createBucket(storage, transfer, bucketName);
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