package com.example.serverapp.pages;

import static fi.iki.elonen.NanoHTTPD.MIME_HTML;
import static fi.iki.elonen.NanoHTTPD.MIME_PLAINTEXT;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.content.Context;
import android.os.Environment;

import com.example.serverapp.HTMLSCRIPTS;
import com.example.serverapp.MainActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

public class IndexPage extends RouterNanoHTTPD.DefaultHandler {

    @Override
    public String getText() {
        StringBuilder answer = new StringBuilder();
        answer.append(getHtmlFromAssets(MainActivity.getInstance(), "index.html"));

        return (answer.toString());
    }

    @Override
    public String getMimeType() {
        return MIME_HTML;
    }

    @Override
    public NanoHTTPD.Response.IStatus getStatus() {
        return NanoHTTPD.Response.Status.OK;
    }
    private String getHtmlFromAssets(Context context, String fileName) {
        InputStream is = null;
        String str = null;
        try {
            is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            str = new String(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return str;
    }


}
