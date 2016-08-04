package com.harryio.storj.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.harryio.storj.R;
import com.harryio.storj.model.Bucket;
import com.harryio.storj.model.StorjFile;
import com.harryio.storj.ui.adapter.FileAdapter;
import com.harryio.storj.util.ConnectionDetector;
import com.harryio.storj.util.PrefUtils;
import com.harryio.storj.util.network.ApiExecutor;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.harryio.storj.util.PrefUtils.KEY_PASSWORD;
import static com.harryio.storj.util.PrefUtils.KEY_USERNAME;

public class BucketFilesActivity extends AppCompatActivity
        implements Toolbar.OnMenuItemClickListener {
    private static final String ARG_BUCKET = "com.harryio.BUCKET";

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

    private String bucketId;
    private String bucketName;
    private ApiExecutor apiExecutor;
    private Bucket bucket;
    private ProgressDialog deleteBucketDialog;
    private PrefUtils prefUtils;

    public static Intent getCallingIntent(Context context, Bucket bucket) {
        Intent intent = new Intent(context, BucketFilesActivity.class);
        intent.putExtra(ARG_BUCKET, bucket);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucket_files);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        bucket = bundle.getParcelable(ARG_BUCKET);
        bucketId = bucket.getId();
        bucketName = bucket.getName();
        apiExecutor = ApiExecutor.getInstance(this);
        gridView.setEmptyView(emptyView);

        prefUtils = PrefUtils.instance(this);

        setUpToolbar();
        setUpDialogs();

        fetchFiles();
    }

    private void setUpDialogs() {
        deleteBucketDialog = new ProgressDialog(this, R.style.StorjDialog);
        deleteBucketDialog.setMessage("Deleting bucket...");
    }

    private void setUpToolbar() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitle(bucketName);
        toolbar.inflateMenu(R.menu.menu_bucket_files);
        toolbar.setOnMenuItemClickListener(this);
    }

    private void fetchFiles() {
        if (ConnectionDetector.isConnectedToInternet(this)) {
            new FetchFiles().execute();
        } else {
            showErrorView("No internet connection");
        }
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
                        fetchFiles();
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

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bucket_delete:
                new AlertDialog.Builder(this, R.style.StorjDialog)
                        .setMessage("Are you sure you want to delete this bucket?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new DeleteBucketTask().execute();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return true;
        }
        return true;
    }

    private void onBucketDeleted() {
        Intent intent = new Intent("bucket_deleted");
        intent.putExtra("bucket", bucket);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        showMessage("Bucket successfully deleted");
        finish();
    }

    private class FetchFiles extends AsyncTask<Void, Void, List<StorjFile>> {
        @Override
        protected void onPreExecute() {
            showLoadingView();
        }

        @Override
        protected List<StorjFile> doInBackground(Void... params) {
            return apiExecutor.fetchFiles(bucketId);
        }

        @Override
        protected void onPostExecute(List<StorjFile> storjFiles) {
            if (storjFiles != null) {
                FileAdapter fileAdapter = new FileAdapter(BucketFilesActivity.this, storjFiles);
                gridView.setAdapter(fileAdapter);
                showContentView();
            } else {
                showErrorView("Network call failed");
            }
        }
    }

    private class DeleteBucketTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            deleteBucketDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            String username = prefUtils.getString(KEY_USERNAME, "");
            String password = prefUtils.getString(KEY_PASSWORD, "");

            return apiExecutor.deleteBucket(bucketId, username, password);
        }

        @Override
        protected void onPostExecute(Boolean deleted) {
            deleteBucketDialog.dismiss();
            if (deleted) {
                onBucketDeleted();
            } else {
                showMessage("Failed to delete bucket");
            }
        }
    }
}
