package com.harryio.storj;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.io.File;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    File[] imageFiles;

    public ViewPagerAdapter(FragmentManager fm, File[] imageFiles) {
        super(fm);
        this.imageFiles = imageFiles;
    }

    @Override
    public Fragment getItem(int position) {
        return ImageDetailFragment.newInstance(imageFiles[position]);
    }

    @Override
    public int getCount() {
        return imageFiles.length;
    }
}
