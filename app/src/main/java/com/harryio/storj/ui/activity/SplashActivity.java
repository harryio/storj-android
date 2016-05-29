package com.harryio.storj.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.harryio.storj.R;
import com.harryio.storj.util.SharedPrefUtils;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean isUserLoggedIn = SharedPrefUtils
                        .instance(SplashActivity.this)
                        .getBoolean(SharedPrefUtils.KEY_IS_USER_LOGGED_IN, false);

                Intent intent = isUserLoggedIn ?
                        new Intent(SplashActivity.this, MainActivity.class) :
                        new Intent(SplashActivity.this, SignUpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }, 1500);
    }
}
