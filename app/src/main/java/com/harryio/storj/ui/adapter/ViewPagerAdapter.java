package com.harryio.storj.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.harryio.storj.ui.fragment.ImageDetailFragment;

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

    public void removeItem(ViewPager viewPager, int position) {
        viewPager.setAdapter(null);
        imageFiles.remove(position);
        viewPager.setAdapter(this);
    }

    public File getFile(int position) {
        return imageFiles.get(position);
    }
}
