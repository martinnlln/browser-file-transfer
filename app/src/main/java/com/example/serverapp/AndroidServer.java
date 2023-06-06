package com.example.serverapp;

import android.util.Log;


import com.example.serverapp.pages.BrowsePhoneDirectories;
import com.example.serverapp.pages.DirectoriesRequestHandler;
import com.example.serverapp.pages.ImageRequestHandler;
import com.example.serverapp.pages.IndexPage;
import com.example.serverapp.pages.VideoRequestHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class AndroidServer extends NanoHTTPD {
    private static final int PORT = 8080;
    private static final String TAG = "ImageServer";


    public AndroidServer() throws IOException {
        super(PORT);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        Log.d(TAG, "Server started on port " + PORT);
    }


    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        VideoRequestHandler videoRequestHandler = new VideoRequestHandler(MainActivity.getInstance());
        ImageRequestHandler handler = new ImageRequestHandler(MainActivity.getInstance());
        DirectoriesRequestHandler directoriesRequestHandler = new DirectoriesRequestHandler(MainActivity.getInstance());

        if (uri.equals("/") || uri.equals("/index.html")) {
            // Serve the main HTML page or any other default page
            IndexPage indexPage = new IndexPage();
            return newFixedLengthResponse(Response.Status.OK, "text/html", indexPage.get());

        } else if (uri.equals("/gallery")) {
            // Serve the gallery page with images
            return handler.handleRequest(session);
        } else if (uri.startsWith("/images/")) {
            // Serve individual images
            return handler.handleRequest(session);
        } else if (uri.equals("/videos")) {
            return videoRequestHandler.handleRequest(session);
        } else if (uri.startsWith("/videos/")) {
            return videoRequestHandler.handleRequest(session);
        } else if (uri.equals("/directories")) {
            return directoriesRequestHandler.handleRequest(session);
        } else if (uri.startsWith("/storage/")) {
            return directoriesRequestHandler.handleRequest(session);
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Foundsdasdasdasd");
    }
}
