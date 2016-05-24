package com.harryio.storj.ui.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.harryio.storj.R;
import com.harryio.storj.StorjService;
import com.harryio.storj.StorjServiceProvider;
import com.harryio.storj.database.KeyPairDAO;
import com.harryio.storj.model.Bucket;
import com.harryio.storj.ui.adapter.BucketGridAdapter;
import com.harryio.storj.util.ECUtils;

import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

public class BucketListFragment extends Fragment {
    private static final String TAG = BucketListFragment.class.getSimpleName();
    @Bind(R.id.gridView)
    GridView gridView;
    @Bind(R.id.loading_view)
    ProgressBar loadingView;

    private OnFragmentInteractionListener mListener;

    public BucketListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bucket_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        new FetchBucketTask().execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
    }

    private class FetchBucketTask extends AsyncTask<Void, Void, List<Bucket>> {
        @Override
        protected void onPreExecute() {
            gridView.setVisibility(View.GONE);
            loadingView.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Bucket> doInBackground(Void... params) {
            KeyPairDAO keyPairDAO = KeyPairDAO.getInstance(getContext());
            PublicKey publicKey = keyPairDAO.getPublicKey();
            PrivateKey privateKey = keyPairDAO.getPrivateKey();

            try {
                String nonce = String.valueOf(System.currentTimeMillis() / 1000L);
                String hexEncodedPublicKeyString = ECUtils.getHexEncodedPublicKey(publicKey);
                String toBeSignedString = "GET\n/buckets\n" + "__nonce=" + nonce;

                byte[] signatureBytes = ECUtils.sign(privateKey, toBeSignedString);
                String hexEncodedSignature = Hex.toHexString(signatureBytes);


                StorjService storjService = StorjServiceProvider.getInstance();
                Call<List<Bucket>> call = storjService
                        .fetchBuckets(hexEncodedSignature, hexEncodedPublicKeyString, nonce);
                Response<List<Bucket>> response = call.execute();

                if (response.isSuccessful()) {
                    Log.i(TAG, "Fetch Buckets call successful");
                    return response.body();
                } else {
                    Log.e(TAG, "Fetch Buckets call failed");
                }
            } catch (IOException | InvalidKeyException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Bucket> buckets) {
            if (buckets != null) {
                Context context = getContext();
                if (context != null) {
                    BucketGridAdapter adapter = new BucketGridAdapter(context, buckets);
                    gridView.setAdapter(adapter);
                    loadingView.setVisibility(View.GONE);
                    gridView.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
