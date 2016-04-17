package com.harryio.storj;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ImageDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ImageDetailFragment extends Fragment {
    private static final String ARG_IMAGE_FILE = "IMAGE_FILE";

    private OnFragmentInteractionListener mListener;
    private File file;
    private ImageView imageView;

    public static ImageDetailFragment newInstance(File file) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_IMAGE_FILE, file);

        ImageDetailFragment fragment = new ImageDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ImageDetailFragment() {
        // Required empty public constructor
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
                inflater.inflate(R.layout.fragment_blank, container, false);

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

    //    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    public interface OnFragmentInteractionListener {
    }
}
