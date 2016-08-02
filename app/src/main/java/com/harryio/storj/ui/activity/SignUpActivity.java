package com.harryio.storj.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.harryio.storj.R;
import com.harryio.storj.model.UserStatus;
import com.harryio.storj.util.ConnectionDetector;
import com.harryio.storj.util.PrefUtils;
import com.harryio.storj.util.network.ApiExecutor;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = SignUpActivity.class.getSimpleName();

    @Bind(R.id.signup_email_edittext)
    EditText emailEdittext;
    @Bind(R.id.signup_password_edittext)
    EditText passwordEdittext;

    private ApiExecutor apiExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        apiExecutor = ApiExecutor.getInstance(this);
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

    private void hideKeyboard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void showActivateAccountDialog() {
        new AlertDialog.Builder(this, R.style.StorjDialog)
                .setTitle("Activate Account")
                .setMessage(R.string.activate_account_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private class RegisterUserTask extends AsyncTask<Void, Void, UserStatus> {
        private String email, password;
        private ProgressDialog progressDialog;

        RegisterUserTask(String email, String password) {
            this.email = email;
            this.password = password;
            progressDialog = ProgressDialog.show(SignUpActivity.this, "", "Signing Up", true);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideKeyboard();
            //Show progress bar
            progressDialog.show();
        }

        @Override
        protected UserStatus doInBackground(Void... params) {
            return apiExecutor.registerUser(email, password);
        }

        @Override
        protected void onPostExecute(UserStatus userStatus) {
            super.onPostExecute(userStatus);

            progressDialog.dismiss();
            if (userStatus != null) {
                //Sign up was successful
                final PrefUtils prefUtils = PrefUtils.instance(SignUpActivity.this);
                prefUtils.storeBoolean(PrefUtils.KEY_IS_USER_LOGGED_IN, true);
                prefUtils.storeString(PrefUtils.KEY_USERNAME, email);
                prefUtils.storeString(PrefUtils.KEY_PASSWORD, password);
                showActivateAccountDialog();
            } else {
                Toast.makeText(SignUpActivity.this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
