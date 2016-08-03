package com.harryio.storj.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.harryio.storj.R;
import com.harryio.storj.model.Bucket;
import com.harryio.storj.ui.adapter.BucketAdapter;
import com.harryio.storj.ui.service.UploadService;
import com.harryio.storj.util.ConnectionDetector;
import com.harryio.storj.util.PrefUtils;
import com.harryio.storj.util.network.ApiExecutor;

import java.io.File;
import java.text.SimpleDateFormat;
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
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStoragePublicDirectory;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements
        Toolbar.OnMenuItemClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 100;
    private static final String IMAGE_DIRECTORY_NAME = "Storj";

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.gridView)
    GridView gridView;
    @Bind(R.id.emptyView)
    View emptyView;
    @Bind(R.id.loading_view)
    View loadingView;
    @Bind(R.id.error_view)
    View errorView;
    @Bind(R.id.rootView)
    View rootView;

    private ApiExecutor apiExecutor;
    private PrefUtils prefUtils;
    private File imageFolder;
    private BucketAdapter bucketAdapter;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        apiExecutor = ApiExecutor.getInstance(this);
        MainActivityPermissionsDispatcher.setUpImageFolderWithCheck(this);
        setUpToolbar();

        prefUtils = PrefUtils.instance(this);
        boolean isTutorialShown = prefUtils
                .getBoolean(PrefUtils.KEY_IS_TUTORIAL_SHOWN, false);
        if (!isTutorialShown) {
            showTutorial();
        }

        setUpGridView();
        fetchBuckets();
    }

    private void setUpGridView() {
        gridView.setEmptyView(emptyView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bucket bucket = (Bucket) parent.getItemAtPosition(position);
                Intent intent = BucketFilesActivity.getCallingIntent(MainActivity.this,
                        bucket.getId(), bucket.getName());
                startActivity(intent);
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(MainActivity.this, R.style.StorjDialog)
                        .setTitle("Select default bucket")
                        .setMessage("Set this bucket as default for uploading files to cloud?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Bucket bucket = (Bucket) parent.getItemAtPosition(position);
                                String bucketId = bucket.getId();
                                prefUtils.storeString(PrefUtils.KEY_DEFAULT_BUCKET_ID, bucketId);
                                bucketAdapter.setDefaultBucketId(bucketId);
                                bucketAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();

                return true;
            }
        });
    }

    private void setUpToolbar() {
        toolbar.inflateMenu(R.menu.menu_activity_main);
        toolbar.setOnMenuItemClickListener(this);
    }

    private void showTutorial() {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(200);
        config.setMaskColor(ContextCompat.getColor(this, R.color.black_75));
        config.setRenderOverNavigationBar(false);
        config.setContentTextColor(ContextCompat.getColor(this, R.color.white));
        config.setDismissTextColor(ContextCompat.getColor(this, R.color.colorAccent));

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this);
        sequence.setConfig(config);
        sequence.addSequenceItem(toolbar.findViewById(R.id.action_capture_image),
                "Click this button to capture image and automatically " +
                        "upload captured image directly to cloud.", "NEXT");
        sequence.addSequenceItem(toolbar.findViewById(R.id.action_create_bucket),
                "Click here to create new bucket on the cloud", "GOT IT")
                .setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
                    @Override
                    public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
                        PrefUtils.instance(MainActivity.this)
                                .storeBoolean(PrefUtils.KEY_IS_TUTORIAL_SHOWN, true);
                    }
                });
        sequence.start();
    }

    private void fetchBuckets() {
        if (ConnectionDetector.isConnectedToInternet(this)) {
            new FetchBucketTask().execute();
        } else {
            showErrorView("No internet connection");
        }
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
                    startService(UploadService.getCallingIntent(this, fileUri));
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

    private void showLoadingView() {
        gridView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
    }

    private void showErrorView(String message) {
        gridView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);

        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_INDEFINITE)
                .setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fetchBuckets();
                    }
                });
        snackbar.setActionTextColor(Color.RED);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    private void showContentView() {
        errorView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        gridView.setVisibility(View.VISIBLE);
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

        //noinspection deprecation
        final AlertDialog dialog = new AlertDialog.Builder(this, R.style.StorjDialog)
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
            return apiExecutor.createBucket(storage, transfer, bucketName);
        }

        @Override
        protected void onPostExecute(Bucket bucket) {
            super.onPostExecute(bucket);
            progressDialog.dismiss();
            if (bucket != null) {
                String defaultBucketId = prefUtils.getString(PrefUtils.KEY_DEFAULT_BUCKET_ID, null);
                if (TextUtils.isEmpty(defaultBucketId)) {
                    String bucketId = bucket.getId();
                    prefUtils.storeString(PrefUtils.KEY_DEFAULT_BUCKET_ID, bucketId);
                    bucketAdapter.setDefaultBucketId(bucketId);
                }
                bucketAdapter.addItem(bucket);
            } else {
                showMessage("Failed to create bucket");
            }
        }
    }

    private class FetchBucketTask extends AsyncTask<Void, Void, List<Bucket>> {
        @Override
        protected void onPreExecute() {
            showLoadingView();
        }

        @Override
        protected List<Bucket> doInBackground(Void... params) {
            return apiExecutor.fetchBuckets();
        }

        @Override
        protected void onPostExecute(List<Bucket> buckets) {
            if (buckets != null) {
                bucketAdapter = new BucketAdapter(MainActivity.this, buckets);
                String defaultBucketId = prefUtils.getString(PrefUtils.KEY_DEFAULT_BUCKET_ID, null);
                if (TextUtils.isEmpty(defaultBucketId)) {
                    if (buckets.size() > 0) {
                        Bucket bucket = buckets.get(0);
                        defaultBucketId = bucket.getId();
                        prefUtils.storeString(PrefUtils.KEY_DEFAULT_BUCKET_ID, defaultBucketId);
                    }
                }
                bucketAdapter.setDefaultBucketId(defaultBucketId);
                gridView.setAdapter(bucketAdapter);
                showContentView();
            } else {
                showErrorView("Network call failed");
            }
        }
    }
}