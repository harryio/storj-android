package com.harryio.storj.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.harryio.storj.R;
import com.harryio.storj.model.StorjFile;
import com.harryio.storj.ui.adapter.FileAdapter;
import com.harryio.storj.util.ConnectionDetector;
import com.harryio.storj.util.network.ApiExecutor;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BucketFilesActivity extends AppCompatActivity {
    private static final String ARG_BUCKET_ID = "com.harryio.ARG_BUCKET_ID";
    private static final String ARG_BUCKET_NAME = "com.harryio.ARG_BUCKET_NAME";

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

    public static Intent getCallingIntent(Context context, String bucketId, String bucketName) {
        Intent intent = new Intent(context, BucketFilesActivity.class);
        intent.putExtra(ARG_BUCKET_ID, bucketId);
        intent.putExtra(ARG_BUCKET_NAME, bucketName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucket_files);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        bucketId = bundle.getString(ARG_BUCKET_ID);
        bucketName = bundle.getString(ARG_BUCKET_NAME);
        apiExecutor = ApiExecutor.getInstance(this);
        gridView.setEmptyView(emptyView);

        setUpToolbar();
        fetchFiles();
    }

    private void setUpToolbar() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitle(bucketName);
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
}
