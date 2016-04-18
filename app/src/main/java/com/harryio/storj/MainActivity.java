package com.harryio.storj;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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

import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStoragePublicDirectory;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 100;

    private static final String IMAGE_DIRECTORY_NAME = "Storj";

    @Bind(R.id.imageList)
    GridView imageList;

    private File imageFolder;
    private ProgressDialog deleteProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        MainActivityPermissionsDispatcher.setUpImageFolderWithCheck(this);
        setUpGridView();
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
            ImageGridAdapter imageGridAdapter = new ImageGridAdapter(this, imageFolder);
            imageList.setAdapter(imageGridAdapter);
        }
    }

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
}