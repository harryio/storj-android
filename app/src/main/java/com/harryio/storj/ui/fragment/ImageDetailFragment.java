package com.harryio.storj.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.harryio.storj.R;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ImageDetailFragment extends Fragment {
    private static final String ARG_IMAGE_FILE = "IMAGE_FILE";

    private File file;
    private ImageView imageView;

    public ImageDetailFragment() {
        // Required empty public constructor
    }

    public static ImageDetailFragment newInstance(File file) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_IMAGE_FILE, file);

        ImageDetailFragment fragment = new ImageDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        file = (File) bundle.getSerializable(ARG_IMAGE_FILE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        imageView = (ImageView)
                inflater.inflate(R.layout.fragment_image_detail, container, false);

        return imageView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Picasso.with(getActivity())
                .load(file)
                .fit()
                .centerInside()
                .into(imageView);
    }
}
