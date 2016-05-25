package com.harryio.storj.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.harryio.storj.R;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Element butterknifeElement = new Element().setTitle("Butterknife by Jake Wharton");
        butterknifeElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebpage("https://github.com/JakeWharton/butterknife");
            }
        });

        Element retrofitElement = new Element().setTitle("Retrofit by Square");
        retrofitElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebpage("https://github.com/square/retrofit");
            }
        });

        Element picassoElement = new Element().setTitle("Picasso by Square");
        picassoElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebpage("https://github.com/square/picasso");
            }
        });

        Element spongyCastleElement = new Element().setTitle("Spongy Castle");
        spongyCastleElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebpage("https://github.com/rtyley/spongycastle");
            }
        });

        Element aboutPageElement = new Element().setTitle("About Page by Medyo");
        aboutPageElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebpage("https://github.com/medyo/android-about-page");
            }
        });

        Element githubElement = new Element().setTitle("View on Github").setIcon(R.drawable.ic_code);
        githubElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebpage("https://github.com/harryio/storj-android");
            }
        });

        Element developerElement = new Element().setTitle("Visit developer").setIcon(R.drawable.ic_face);
        developerElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebpage("http://harryio.com");
            }
        });

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher)
                .setDescription(getString(R.string.storj_desc))
                .addWebsite("https://storj.io/")
                .addItem(githubElement)
                .addItem(developerElement)
                .addGroup("Open Source Libraries Used")
                .addItem(butterknifeElement)
                .addItem(retrofitElement)
                .addItem(picassoElement)
                .addItem(spongyCastleElement)
                .addItem(aboutPageElement)
                .create();
        setContentView(aboutPage);
    }

    private void openWebpage(String webpageLink) {
        Intent webpageIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(webpageLink));
        startActivity(webpageIntent);
    }
}
