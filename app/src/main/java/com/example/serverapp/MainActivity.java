package com.example.serverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.Formatter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    /**
     * TODO
     * 1. Make gallery slideshow
     * 2. Download documents
     * 3. Load more images... somehow :|
     * 4. tva e mai.
     * 123456
     * 32154654
     */

    TextView ipAddressTV;

    // commit test
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipAddressTV = findViewById(R.id.ip_addr);

        if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                Intent getpermission = new Intent();
                getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getpermission);
            }
        }
        try {
            new AndroidServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        @SuppressLint("SetTextI18n")
        Thread thread = new Thread(() -> {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
            ipAddressTV.setText(ipAddress + ":8080");
        });
        thread.start();

    }

    private static MainActivity instance;

    public MainActivity() {
        instance = this;
    }

    public static MainActivity getInstance() {
        return instance;
    }


}
