package com.harryio.storj;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.harryio.storj.model.UserStatus;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

public class ActivateAccountActivity extends AppCompatActivity {
    private static final String TAG = ActivateAccountActivity.class.getSimpleName();

    @Bind(R.id.account_token_edittext)
    EditText tokenEdittext;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.token_view)
    LinearLayout tokenView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate_account);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.later_button)
    public void onLaterButtonClicke() {
        //todo Launch another activity here
    }

    @OnClick(R.id.activate_button)
    public void onActivateButtonClicked() {
        //Reset error
        tokenEdittext.setError(null);
        String token = tokenEdittext.getText().toString();

        boolean shouldProceed = true;
        //Check for empty token
        if (TextUtils.isEmpty(token)) {
            tokenEdittext.setError(getString(R.string.error_empty_token));
            shouldProceed = false;
        }

        if (shouldProceed) {
            //Launch network call to activate account
            new ActivateAccountTask(token).execute();
        }
    }

    private class ActivateAccountTask extends AsyncTask<Void, Void, UserStatus> {
        String token;

        public ActivateAccountTask(String token) {
            this.token = token;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tokenView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected UserStatus doInBackground(Void... params) {
            StorjService storjService = StorjServiceProvider.getInstance();
            Call<UserStatus> call = storjService.activateAccount(token);
            try {
                final Response<UserStatus> response = call.execute();
                if (response.isSuccessful()) {
                    UserStatus userStatus = response.body();
                    Log.d(TAG, "Activate account call successful:\n" + userStatus.toString());
                    return userStatus;
                } else {
                    Log.e(TAG, "Activate account call failed\n" + response.errorBody().string());
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Activate Account call failed");
            }
            return null;
        }

        @Override
        protected void onPostExecute(UserStatus userStatus) {
            super.onPostExecute(userStatus);

            if (userStatus == null) {
                tokenView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else {
                // TODO: Launch another activity here
            }
        }
    }
}
