package com.example.serverapp;


import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.serverapp.pages.BrowsePhoneDirectories;
import com.example.serverapp.pages.GalleryPage;
import com.example.serverapp.pages.IndexPage;

import java.io.File;
import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class App extends NanoHTTPD {

    Utilities utilities;
    BrowsePhoneDirectories browsePhoneDirectories;

    public App() throws IOException {
        super(8080);
        utilities = new Utilities();
        browsePhoneDirectories = new BrowsePhoneDirectories();
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
    }

    public static void main(String[] args) {
        try {
            new App();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Response serve(IHTTPSession session) {
        String url = session.getUri();

        utilities.renameFileWithSpaces(url);


        if (url.equals("/")) {
            IndexPage indexPage = new IndexPage();
            return newFixedLengthResponse(indexPage.getText());
        }
        if (url.equals("/gallery")) {
            GalleryPage galleryPage = new GalleryPage();
            return newFixedLengthResponse(galleryPage.getText());
        }

        if (url.equals("/storage/emulated/0")) {
            return newFixedLengthResponse(browsePhoneDirectories.getText(url));
        } else {
            if (url.contains(".txt")) {
                File file = new File(url);
                return newFixedLengthResponse(browsePhoneDirectories.getTextForTextFiles(file.getAbsolutePath()));
            } else if (url.contains(".jpg") || url.contains(".png")) {
                File file = new File(url);
                return newFixedLengthResponse(browsePhoneDirectories.getTextForImages(file.getAbsolutePath()));
            }
            return newFixedLengthResponse(browsePhoneDirectories.getText(url));

        }
    }
}