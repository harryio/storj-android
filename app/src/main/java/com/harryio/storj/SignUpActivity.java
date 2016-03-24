package com.harryio.storj;

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

import com.harryio.storj.model.SignUpResult;
import com.harryio.storj.model.User;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

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
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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
            new RegisterUserTask(email, password).execute();
        }
    }

    private class RegisterUserTask extends AsyncTask<Void, Void, SignUpResult> {
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
        protected SignUpResult doInBackground(Void... params) {
            try {
                //Hex encode SHA-256 digest of password
                password = SHA.hash256(password);
                User user = new User(email, password);

                StorjService storjService = StorjServiceProvider.getInstance();
                Call<SignUpResult> signUpResultCall = storjService.registerUser(user);

                Response<SignUpResult> response = signUpResultCall.execute();
                if (response.isSuccessful()) {
                    final SignUpResult result = response.body();
                    Log.d(TAG, "SignUp request successful:\n" + result.toString());
                    return result;
                } else {
                    final ResponseBody responseBody = response.errorBody();
                    Log.e(TAG, "SignUp request failed:\n" + responseBody.string());
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                Log.e(TAG, "No algorithm found for \"SHA-256\"");
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(SignUpResult signUpResult) {
            super.onPostExecute(signUpResult);

            if (signUpResult == null) {
                //Show signup form again as call is failed
                signupView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else {

            }
        }
    }
}
