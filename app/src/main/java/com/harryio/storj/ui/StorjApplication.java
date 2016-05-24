package com.harryio.storj.ui;

import android.app.Application;

import com.harryio.storj.util.PRNGFixes;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

public class StorjApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PRNGFixes.apply();
        Security.addProvider(new BouncyCastleProvider());
    }
}
