package com.harryio.storj;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ImageDetailActivity extends AppCompatActivity {
    private static final String ARG_IMAGE_FILES = "IMAGE_FILES";
    private static final String ARG_SELECTED_POSITION = "SELECTED_POSITION";

    @Bind(R.id.viewPager)
    ViewPager viewPager;

    private File[] imageFiles;
    private int selectedPosition;

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
        selectedPosition = getIntent().getIntExtra(ARG_SELECTED_POSITION, 0);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),
                imageFiles);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(selectedPosition);
    }
}
