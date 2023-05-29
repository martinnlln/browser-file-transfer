package com.example.serverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    /**
     * TODO
     * 1. Make gallery slideshow
     * 2. Download documents
     * 3. Load more images... somehow :|
     * 4. tva e mai.
     * 123456
     *
     */


    // commit test

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                Intent getpermission = new Intent();
                getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getpermission);
            }
        }
        try {
            new App();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static MainActivity instance;

    public MainActivity() {
        instance = this;
    }

    public static MainActivity getInstance() {
        return instance;
    }
}
