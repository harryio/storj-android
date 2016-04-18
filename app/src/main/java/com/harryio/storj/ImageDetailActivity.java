package com.harryio.storj;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ImageDetailActivity extends AppCompatActivity {
    private static final String TAG = ImageDetailActivity.class.getSimpleName();
    private static final String ARG_IMAGE_FILES = "IMAGE_FILES";
    private static final String ARG_SELECTED_POSITION = "SELECTED_POSITION";

    @Bind(R.id.viewPager)
    ViewPager viewPager;

    private File[] imageFiles;
    private ViewPagerAdapter viewPagerAdapter;

    public static Intent getCallingIntent(Context context, File[] imageFiles, int selectedPosition) {
        Intent intent = new Intent(context, ImageDetailActivity.class);
        intent.putExtra(ARG_IMAGE_FILES, imageFiles);
        intent.putExtra(ARG_SELECTED_POSITION, selectedPosition);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        ButterKnife.bind(this);

        imageFiles = (File[]) getIntent().getExtras().get(ARG_IMAGE_FILES);
        int selectedPosition = getIntent().getIntExtra(ARG_SELECTED_POSITION, 0);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),
                imageFiles);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(selectedPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_image_detail_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_delete:
                deleteImage();
                return true;

            case R.id.menu_action_upload:
                //todo upload image here
                return true;

            default: break;
        }

        return false;
    }

    public void deleteImage() {
        new AlertDialog.Builder(this)
                .setMessage("Delete this photo?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int currentIndex = viewPager.getCurrentItem();

                        File file = viewPagerAdapter.getFile(currentIndex);
                        boolean isDeleted = file.delete();

                        if (!isDeleted) {
                            showMessage(file.getName() + " delete failed");
                        } else {
                            if (viewPagerAdapter.getCount() - 1 == 0) {
                                finish();
                                return;
                            }

                            viewPagerAdapter.removeItem(viewPager, currentIndex);
                            if (currentIndex == viewPagerAdapter.getCount())
                                currentIndex--;

                            viewPager.setCurrentItem(currentIndex);
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
