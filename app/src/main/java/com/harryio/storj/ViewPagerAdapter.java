package com.harryio.storj;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    List<File> imageFiles;

    public ViewPagerAdapter(FragmentManager fm, File[] imageFiles) {
        super(fm);
        this.imageFiles = new ArrayList<>(Arrays.asList(imageFiles));
    }

    @Override
    public Fragment getItem(int position) {
        return ImageDetailFragment.newInstance(imageFiles.get(position));
    }

    @Override
    public int getCount() {
        return imageFiles.size();
    }

    void removeItem(ViewPager viewPager, int position) {
        viewPager.setAdapter(null);
        imageFiles.remove(position);
        viewPager.setAdapter(this);
    }

    File getFile(int position) {
        return imageFiles.get(position);
    }
}
