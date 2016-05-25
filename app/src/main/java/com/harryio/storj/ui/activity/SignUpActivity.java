package com.harryio.storj.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.harryio.storj.R;
import com.harryio.storj.StorjService;
import com.harryio.storj.StorjServiceProvider;
import com.harryio.storj.database.KeyPairDAO;
import com.harryio.storj.model.User;
import com.harryio.storj.model.UserStatus;
import com.harryio.storj.util.ConnectionDetector;
import com.harryio.storj.util.Crypto;
import com.harryio.storj.util.ECUtils;
import com.harryio.storj.util.SharedPrefUtils;

import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = SignUpActivity.class.getSimpleName();

    @Bind(R.id.signup_email_edittext)
    EditText emailEdittext;
    @Bind(R.id.signup_password_edittext)
    EditText passwordEdittext;
    @Bind(R.id.signUp_view)
    LinearLayout signupView;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, SignUpActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.signup_button)
    public void onSignUpButtonClicked() {
        //Reset error
        emailEdittext.setError(null);
        passwordEdittext.setError(null);

        String email = emailEdittext.getText().toString();
        String password = passwordEdittext.getText().toString();

        boolean shouldProceed = true;
        //Check for empty email
        if (TextUtils.isEmpty(email)) {
            shouldProceed = false;
            emailEdittext.setError(getString(R.string.error_email_empty));
        }
        //Check for invalid email address
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            shouldProceed = false;
            emailEdittext.setError(getString(R.string.error_invalid_email));
        }

        //Check for empty password
        if (TextUtils.isEmpty(password)) {
            shouldProceed = false;
            passwordEdittext.setError(getString(R.string.error_empty_password));
        }
        //Credentials verified, proceed to register user
        if (shouldProceed) {
            if (ConnectionDetector.isConnectedToInternet(getApplicationContext())) {
                new RegisterUserTask(email, password).execute();
            } else {
                Toast.makeText(SignUpActivity.this, "No internet connection!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class RegisterUserTask extends AsyncTask<Void, Void, UserStatus> {
        String email, password;

        RegisterUserTask(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Hide the signup form
            signupView.setVisibility(View.GONE);
            //Show progress bar
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected UserStatus doInBackground(Void... params) {
            try {
                //SHA-256 digest of password
                byte[] bytes = Crypto.sha256Digest(password);
                //Hex of SHA-256 digest of password
                password = Hex.toHexString(bytes);
                KeyPair keyPair = ECUtils.getKeyPair();
                String hexEncodedPublicString = ECUtils.getHexEncodedPublicKey(keyPair.getPublic());
                //Create new user
                User user = new User(email, password, hexEncodedPublicString);

                StorjService storjService = StorjServiceProvider.getInstance();
                Call<UserStatus> signUpResultCall = storjService.registerUser(user);

                //Make an api call to register user
                Response<UserStatus> response = signUpResultCall.execute();
                if (response.isSuccessful()) {
                    //Api call was successful
                    //Get body of the successful response
                    UserStatus result = response.body();
                    //Save public and private key to database
                    KeyPairDAO.getInstance(SignUpActivity.this).insert(keyPair);
                    //Set user as logged in
                    SharedPrefUtils.instance(SignUpActivity.this)
                            .storeBoolean(SharedPrefUtils.KEY_IS_USER_LOGGED_IN, true);
                    Log.d(TAG, "SignUp request successful:\n" + result.toString());
                    return result;
                } else {
                    //Api call failed
                    //Print out the error response body
                    final ResponseBody responseBody = response.errorBody();
                    Log.e(TAG, "SignUp request failed:\n" + responseBody.string());
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                Log.e(TAG, "No algorithm found for \"SHA-256\"");
            } catch (InvalidAlgorithmParameterException | NoSuchProviderException | IOException | NullPointerException | InvalidKeyException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(UserStatus userStatus) {
            super.onPostExecute(userStatus);

            if (userStatus == null) {
                //Show signup form again as call failed
                signupView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else {
                //Sign up was successful
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
