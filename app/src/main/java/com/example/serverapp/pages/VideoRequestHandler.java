package com.example.serverapp.pages;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;

import com.example.serverapp.MainActivity;
import com.example.serverapp.Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class VideoRequestHandler {

    private static final int VIDEOS_PER_PAGE = 30;
    private Context context;

    public VideoRequestHandler(Context context) {
        this.context = context;
    }

    public NanoHTTPD.Response handleRequest(NanoHTTPD.IHTTPSession session) {
        String uri = session.getUri();

        if (uri.equals("/videos")) {
            int pageNumber = getPageNumberFromUri(session.getParameters());
            Pair<List<String>, Integer> result = getVideoUrls(pageNumber, session);
            List<String> videoUrls = result.first;
            int totalVideos = result.second;
            String response = generateVideoGalleryResponse(videoUrls, totalVideos, pageNumber);

            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/html", response);
        } else if (uri.startsWith("/videos/")) {
            String videoPath = uri.substring(8); // Remove '/videos/' from the URI
            String videoUrl = "http://" + session.getHeaders().get("host") + "/videos/" + Uri.encode(videoPath);
            return generateVideoPlayerPage(videoUrl);
        }

        return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not Foundsdsadasasdad");
    }
    private Pair<List<String>, Integer> getVideoUrls(int pageNumber, NanoHTTPD.IHTTPSession session) {
        List<String> videoUrls = new ArrayList<>();
        int totalVideos = 0;

        String[] projection = { MediaStore.Video.Media.DATA };
        Uri contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = context.getContentResolver().query(contentUri, projection, null, null, null);

        if (cursor != null) {
            totalVideos = cursor.getCount();
            int startIndex = (pageNumber - 1) * VIDEOS_PER_PAGE;
            int endIndex = startIndex + VIDEOS_PER_PAGE;

            if (cursor.moveToPosition(startIndex)) {
                while (!cursor.isAfterLast() && videoUrls.size() < VIDEOS_PER_PAGE && videoUrls.size() < totalVideos) {
                    @SuppressLint("Range")
                    String videoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                    String videoUrl = "http://" + session.getHeaders().get("host") + "/images/" + Uri.encode(videoPath);

                    videoUrls.add(videoUrl);
                    cursor.moveToNext();
                }
            }

            cursor.close();
        }

        return new Pair<>(videoUrls, totalVideos);
    }

    private int getPageNumberFromUri(Map<String, List<String>> parameters) {
        int pageNumber = 1; // Default page number is 1

        if (parameters.containsKey("page")) {
            List<String> values = parameters.get("page");
            if (values != null && !values.isEmpty()) {
                try {
                    pageNumber = Integer.parseInt(values.get(0));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        return pageNumber;
    }

    private String generateVideoGalleryResponse(List<String> videoUrls, int totalVideos, int currentPage) {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append(Utilities.getHtmlFromAssets(MainActivity.getInstance(), "GalleryPage.html"));

        responseBuilder.append("<div class=\"gallery\">");
        for (String videoUrl : videoUrls) {
            responseBuilder.append("<div class=\"video\">");
            String videoTag = String.format("<video width=\"200\" height=\"200\" controls>" +
                    "<source src=\"%s\" type=\"video/mp4\"></video><br></div>", videoUrl);
            responseBuilder.append(videoTag);
        }

        responseBuilder.append("</div>");
        responseBuilder.append(getPaginationButtons(totalVideos, currentPage));
        responseBuilder.append("</body></html>");
        return responseBuilder.toString();
    }


    private String getPaginationButtons(int totalVideos, int currentPage) {
        StringBuilder buttonBuilder = new StringBuilder();

        int totalPages = (int) Math.ceil((double) totalVideos / VIDEOS_PER_PAGE);

        if (totalPages > 1) {
            buttonBuilder.append("<div class=\"pagination-container\">");

            // Previous button
            if (currentPage > 1) {
                buttonBuilder.append("<div class=\"pagination-number arrow\">");
                buttonBuilder.append("<a href=\"/gallery?page=").append(currentPage - 1).append("\">Previous Page</a>");
                buttonBuilder.append("</div>");
            }

            // Current page
            buttonBuilder.append("<div class=\"pagination-number current\">[");
            buttonBuilder.append(currentPage);
            buttonBuilder.append("</div>");

            // Total pages
            buttonBuilder.append("<div class=\"pagination-number total\">");
            buttonBuilder.append("of ");
            buttonBuilder.append(totalPages);
            buttonBuilder.append("]</div>");

            // Next button
            if (currentPage < totalPages) {
                buttonBuilder.append("<div class=\"pagination-number arrow\">");
                buttonBuilder.append("<a href=\"/gallery?page=").append(currentPage + 1).append("\">Next Page</a>");
                buttonBuilder.append("</div>");
            }

            buttonBuilder.append("</div>");
        }

        return buttonBuilder.toString();
    }

    private NanoHTTPD.Response generateVideoPlayerPage(String videoUrl) {
        FileInputStream fis = null;
        File file = new File(videoUrl);
        try {
            if (file.exists()) {
                fis = new FileInputStream(file);
            } else
                Log.d("FOF :", "File Not exists:");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "video/mp4", fis, file.length());
    }
}
